package sub.fwb;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


public class EmbeddedSolrTest {

	private static SolrWrapper solr;

	@BeforeClass
	public static void beforeAllTests() throws Exception {
		CoreContainer container = new CoreContainer("solr-embedded");
		container.load();
		EmbeddedSolrServer solrEmbedded = new EmbeddedSolrServer(container, "fwb");
		solr = new SolrWrapper(solrEmbedded, "fwb");
	}

	@AfterClass
	public static void afterAllTests() throws Exception {
		solr.close();
	}

	@After
	public void afterEach() throws Exception {
		solr.clean();
		solr.printResults();
	}

	@Test
	public void shouldFindNotExactComplexPhrase() throws Exception {
		String[][] doc = { { "artikel", "imbis ward" } };
		solr.addDocument(doc);

		solr.search("\"imbis War*\"");
		assertEquals(1, results());
	}

	@Test
	public void shouldFindExactComplexPhrase() throws Exception {
		String[][] doc = { { "artikel", "imbis Ward" } };
		solr.addDocument(doc);

		solr.search("\"?mbis War*\" EXAKT");
		assertEquals(1, results());
	}

	@Test
	public void shouldSuggestSearchedTerm() throws Exception {
		String[][] doc = { { "id", "1" }, { "lemma", "test" } };
		solr.addDocument(doc);

		solr.suggest("test");
		assertEquals("test", solr.suggestion(1));
	}

	@Test
	public void shouldSuggestWithParenthesis() throws Exception {
		String[][] doc = { { "id", "1" }, { "lemma", "ampt(s)kleid" } };
		solr.addDocument(doc);

		solr.suggest("ampt(s");
		assertEquals("ampt(s)kleid", solr.suggestion(1));
	}

	@Test
	public void shouldSuggest() throws Exception {
		String[][] doc = { { "id", "1" }, { "lemma", "test1" } };
		solr.addDocument(doc);
		String[][] doc2 = { { "id", "2" }, { "lemma", "test2" } };
		solr.addDocument(doc2);

		solr.suggest("test");
		assertEquals("test1", solr.suggestion(1));
		assertEquals("test2", solr.suggestion(2));
	}

	@Test
	public void shouldRemoveSpecialCharsFromFrontAndBackInSnippet() throws Exception {
		String[][] doc = { { "artikel", ",)/‹.]- ;: test (,/[- ;:" },
				{ "artikel_text", ",)/‹.]- ;: test (,/[- ;:" } };
		solr.addDocument(doc);

		solr.search("test");
		assertEquals(1, results());
		String hlSnippet = assertHighlighted("artikel_text", "test");
		assertEquals("<span class=\"highlight\">test</span>", hlSnippet);
	}

	@Test
	public void shouldFindTermWithCaret() throws Exception {
		String[][] doc = { { "artikel", "imbisgast" },
				{ "artikel_text", "imbisgast" } };
		solr.addDocument(doc);

		solr.search("^imbis");
		assertEquals(1, results());
		assertHighlighted("artikel_text", "imbisgast");
	}

	@Test
	public void shouldNotHighlightLonelyParenthesis() throws Exception {
		String[][] doc = { { "artikel", "imbis)" },
				{ "artikel_text", "imbis)" } };
		solr.addDocument(doc);

		solr.search("imbis");
		assertEquals(1, results());
		assertNotHighlighted("artikel_text", "imbis)");
		assertHighlighted("artikel_text", "imbis");
	}

	@Test
	public void shouldGenerateHlSnippetForLemmaWithOR() throws Exception {
		String[][] doc = { { "lemma", "imbis" }, { "artikel", "imbis" },
				{ "artikel_text", "imbis" } };
		String[][] doc2 = { { "id", "5678"}, { "lemma", "test" }, { "artikel", "test bla" },
				{ "artikel_text", "test bla" } };
		solr.addDocument(doc);
		solr.addDocument(doc2);

		solr.search("lemma:imbis OR bla");
		assertEquals(2, results());
		assertNotHighlighted("artikel_text", "imbis");
	}

	@Test
	public void shouldGenerateHlSnippetForLemmaWithNOT() throws Exception {
		String[][] doc = { { "lemma", "imbis" }, { "artikel", "imbis" },
				{ "artikel_text", "imbis" } };
		String[][] doc2 = { { "id", "5678"}, { "lemma", "test" }, { "artikel", "test bla" },
				{ "artikel_text", "test bla" } };
		solr.addDocument(doc);
		solr.addDocument(doc2);

		solr.search("lemma:imbis NOT (bla)");
		assertEquals(1, results());
		assertNotHighlighted("artikel_text", "imbis");
	}

	@Test
	public void shouldGenerateUnhighlightedSnippetForTwoLemmas() throws Exception {
		String[][] doc = { { "lemma", "imbis bla" }, { "artikel", "imbis bla imbisgast" },
				{ "artikel_text", "imbis bla imbisgast" } };
		solr.addDocument(doc);

		solr.search("lemma:imbis lemma:bla");
		assertEquals(1, results());
		assertNotHighlighted("artikel_text", "imbis", "bla", "imbisgast");
	}

	@Test
	public void shouldGenerateHlSnippetWithoutLemma() throws Exception {
		String[][] doc = { { "lemma", "imbis" }, { "artikel", "imbis imbisgast ward" },
				{ "artikel_text", "imbis imbisgast ward" } };
		solr.addDocument(doc);

		solr.search("lemma:imbis AND (ward)");
		assertEquals(1, results());
		assertHighlighted("artikel_text", "ward");
		assertNotHighlighted("artikel_text", "imbis");
		assertNotHighlighted("artikel_text", "imbisgast");
	}

	@Test
	public void shouldGenerateUnhighlightedSnippetForExactLemma() throws Exception {
		String[][] doc = { { "lemma", "imbis" }, { "artikel", "imbis" }, { "artikel_text", "imbis" } };
		solr.addDocument(doc);

		solr.search("EXAKT lemma:imbis");
		assertEquals(1, results());
		assertNotHighlighted("artikel_text", "imbis");
	}

	@Test
	public void shouldGenerateUnhighlightedSnippetForLemma() throws Exception {
		String[][] doc = { { "lemma", "imbis" }, { "artikel", "imbis, Nomen" }, { "artikel_text", "imbis, Nomen" } };
		solr.addDocument(doc);

		solr.search("lemma:imbis");
		assertEquals(1, results());
		String hlText = assertNotHighlighted("artikel_text", "Nomen");
		// the lemma itself must not be repeated in the snippet, it doesn't look good in the search results
		assertEquals("Nomen", hlText);
	}

	@Test
	public void shouldHighlightExactInArticle() throws Exception {
		String[][] doc = { { "artikel", "imbis IMBIS" } };
		solr.addDocument(doc);

		String[][] extraParams = { { "hl.q", "IMBIS EXAKT" } };
		solr.articleHl(extraParams, "id:1234");

		assertEquals(1, results());
		assertHighlighted("artikel", "IMBIS");
		assertNotHighlighted("artikel", "imbis");
	}

	@Test
	public void shouldHighlightOnlyExactTerm() throws Exception {
		String[][] doc = { { "zitat", "Imbis imbis" }, { "zitat_text", "Imbis imbis" }, { "artikel", "Imbis imbis" },
				{ "artikel_text", "Imbis imbis" } };
		solr.addDocument(doc);

		solr.search("EXAKT Imbis");
		assertEquals(1, results());
		assertHighlighted("artikel_text", "Imbis");
		assertNotHighlighted("artikel_text", "imbis");
	}

	@Test
	public void shouldListOnlyExactTerm() throws Exception {
		String[][] doc = { { "zitat", "Imbis" }, { "zitat_text", "Imbis" }, { "artikel", "Imbis" },
				{ "artikel_text", "Imbis" } };
		solr.addDocument(doc);

		solr.list("EXAKT Imbis");
		assertEquals(1, results());

		solr.search("EXAKT imbis");
		assertEquals(0, results());
	}

	@Test
	public void shouldFindOnlyExactTerm() throws Exception {
		String[][] doc = { { "zitat", "Imbis" }, { "zitat_text", "Imbis" }, { "artikel", "Imbis" },
				{ "artikel_text", "Imbis" } };
		solr.addDocument(doc);

		solr.search("EXAKT Imbis");
		assertEquals(1, results());

		solr.search("EXAKT imbis");
		assertEquals(0, results());
	}

	@Test
	public void shouldHighlightOnlyExactMatchInCitation() throws Exception {
		String[][] doc = { { "zitat", "Imbis imbis" }, { "zitat_text", "Imbis imbis" } };
		solr.addDocument(doc);

		solr.search("zitat:Imbis EXAKT");
		assertEquals(1, results());

		assertHighlighted("artikel_text", "Imbis");
		assertNotHighlighted("artikel_text", "imbis");
	}

	@Test
	public void shouldFindOnlyExactInCitation() throws Exception {
		String[][] doc = { { "zitat", "Imbis" }, { "zitat_text", "Imbis" } };
		solr.addDocument(doc);

		solr.search("EXAKT zitat:Imbis");
		assertEquals(1, results());

		solr.search("EXAKT zitat:imbis");
		assertEquals(0, results());
	}

	@Test
	public void shouldHighlightBdvOnly() throws Exception {
		String[][] doc = { { "artikel", "imbis <!--start bdv1--><div>imbisinbdv</div><!--end bdv1-->" },
				{ "bdv", "<!--start bdv1--><div>imbisinbdv</div><!--end bdv1-->" } };
		solr.addDocument(doc);

		String[][] extraParams = { { "hl.q", "bdv:imbis" } };
		solr.articleHl(extraParams, "id:1234");

		assertEquals(1, results());
		assertNotHighlighted("artikel", "imbis");
		assertHighlighted("artikel", "imbisinbdv");
	}

	@Test
	public void shouldHighlightInArticle() throws Exception {
		String[][] doc = { { "artikel", "imbis" } };
		solr.addDocument(doc);

		String[][] extraParams = { { "hl.q", "imbis" } };
		solr.articleHl(extraParams, "id:1234");

		assertEquals(1, results());
		assertHighlighted("artikel", "imbis");
	}

	@Test
	public void shouldOverwriteArticleHighlighting() throws Exception {
		String[][] doc = { { "artikel", "bla" }, { "bdv", "bla" }, { "artikel_text", "different" },
				{ "bdv_text", "bla" } };
		solr.addDocument(doc);

		solr.search("bdv:bla");

		assertEquals(1, results());
		assertHighlighted("artikel_text", "bla");
	}

	@Test
	public void shouldHighlightInsideHtml() throws Exception {
		String[][] doc = { { "bdv", "<div>bla</div>" } };
		solr.addDocument(doc);

		String[][] extraParams = { { "hl", "on" } };
		solr.select(extraParams, "bdv:bla");

		assertEquals(1, results());
		assertHighlighted("bdv", "bla");
	}

	@Test
	public void shouldRemoveDash() throws Exception {
		String[][] doc = { { "artikel", "legatar(-ius)" } };
		solr.addDocument(doc);

		solr.search("legatarius");

		assertEquals(1, results());
	}

	@Test
	public void shouldNotHighlightTes() throws Exception {
		String[][] doc = { { "zitat", "das" }, { "zitat_text", "das" }, { "artikel_text", "tes das" },
				{ "artikel", "tes das" } };
		solr.addDocument(doc);

		solr.search("das");

		assertEquals(1, results());
		assertNotHighlighted("artikel_text", "tes");
	}

	@Test
	public void shouldHighlightQuote() throws Exception {
		String[][] doc = { { "zitat", "und" }, { "zitat_text", "und" }, { "artikel_text", "und" },
				{ "artikel", "und" } };
		solr.addDocument(doc);

		solr.search("vnd");

		assertEquals(1, results());
		assertHighlighted("artikel_text", "und");
	}

	@Test
	public void shouldFilterNonletters() throws Exception {
		String[][] doc = { { "artikel", "bla" } };
		solr.addDocument(doc);

		solr.search("artikel:#+bl,;.");

		assertEquals(1, results());
	}

	@Test
	public void shouldSearchInCitations() throws Exception {
		String[][] doc = { { "zitat", "únser" } };
		solr.addDocument(doc);

		solr.search("unser");

		assertEquals(1, results());
	}

	@Test
	public void shouldReplaceAccentedLetter() throws Exception {
		String[][] doc = { { "zitat", "únser" } };
		solr.addDocument(doc);

		solr.list("zitat:unser");

		assertEquals(1, results());
	}

	@Test
	public void shouldRemoveCombinedLetter() throws Exception {
		String[][] doc = { { "zitat", "svͤlen" } };
		solr.addDocument(doc);

		solr.search("zitat:svlen");

		assertEquals(1, results());
	}

	@Test
	public void shouldHighlightChristDifferently() throws Exception {
		String[][] doc = { { "artikel", "christ krist" }, { "artikel_text", "christ krist" },
				{ "zitat", "christ krist" }, { "zitat_text", "christ krist" } };
		solr.addDocument(doc);

		solr.search("christ");

		assertEquals(1, results());
		assertHighlighted("artikel_text", "christ");
		assertNotHighlighted("artikel_text", "krist");
		assertHighlighted("zitat_text", "christ", "krist");
	}

	@Test
	public void shouldHighlightArticleAndCitationDifferently() throws Exception {
		String[][] doc = { { "artikel", "und vnd" }, { "zitat", "und vnd" }, { "artikel_text", "und vnd" },
				{ "zitat_text", "und vnd" } };
		solr.addDocument(doc);

		solr.search("und");

		assertEquals(1, results());
		assertHighlighted("artikel_text", "und");
		assertNotHighlighted("artikel_text", "vnd");
		assertHighlighted("zitat_text", "und", "vnd");
	}

	@Test
	public void shouldFindPartialAlternativeSpellings() throws Exception {
		String[][] doc = { { "zitat", "wvnde" } };
		solr.addDocument(doc);

		solr.search("zitat:unt");

		assertEquals(1, results());
	}

	@Test
	public void shouldFindAlternativeSpellings() throws Exception {
		String[][] doc = { { "zitat", "vnd katze" } };
		solr.addDocument(doc);

		solr.select("zitat:(+und +unt +vnt +vnd +katze +chatze +qatze +catze +gedza)");

		assertEquals(1, results());
	}

	@Test
	public void shouldFindUmlaut() throws Exception {
		String[][] doc = { { "artikel_text", "bär" } };
		solr.addDocument(doc);

		solr.select("artikel_text:bar");

		assertEquals(1, results());
	}

	@Test
	public void shouldDeleteNonbreakingSpace() throws Exception {
		String[][] doc = { { "artikel_text", "test abc" } };
		solr.addDocument(doc);

		solr.select("artikel_text:test");

		assertEquals(1, results());
	}

	@Test
	public void shouldFindPipe() throws Exception {
		String[][] doc = { { "artikel_text", "test |" } };
		solr.addDocument(doc);

		solr.select("artikel_text:|");

		assertEquals(1, results());
	}

	@Test
	public void shouldIgnoreSpecialChars() throws Exception {
		String[][] doc = { { "artikel_text", "& test1, ›test2‹" } };
		solr.addDocument(doc);

		solr.select("artikel_text:test1");
		assertEquals(1, results());
		solr.select("artikel_text:test2");
		assertEquals(1, results());
		solr.select("artikel_text:&");
		assertEquals(0, results());
	}

	@Test
	public void shouldFindWithoutPipe() throws Exception {
		String[][] doc = { { "lemma", "my|lemma" } };
		solr.addDocument(doc);

		solr.search("lemma:mylemma");

		assertEquals(1, results());
		assertEquals("my|lemma", lemma(1));
	}

	private String lemma(int resultNumber) {
		return solr.lemma(resultNumber);
	}

	private long results() {
		return solr.results();
	}

	private String assertHighlighted(String fieldName, String... words) {
		return assertHighlighted(true, fieldName, words);
	}

	private String assertNotHighlighted(String fieldName, String... words) {
		return assertHighlighted(false, fieldName, words);
	}

	private String assertHighlighted(boolean forReal, String fieldName, String... words) {
		String hlText = solr.getHighlightings().get("1234").get(fieldName).get(0);
		// System.out.println(hlText);
		for (String word : words) {
			String hlWord = "<span class=\"highlight\">" + word + "</span>";
			if (forReal) {
				assertThat(hlText, containsString(hlWord));
			} else {
				assertThat(hlText, not(containsString(hlWord)));
			}
		}
		return hlText;
	}

}
