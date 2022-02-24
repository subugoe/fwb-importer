package sub.fwb;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;


public class EmbeddedSolrTest {

    private static final SolrAccessForTesting solr = new SolrAccessForTesting();

    @BeforeClass
    public static void beforeAllTests() throws Exception {
        solr.initializeEmbedded("fwb");
    }

    @AfterClass
    public static void afterAllTests() throws Exception {
        solr.close();
    }

    @After
    public void afterEach() throws Exception {
        solr.cleanAndCommit();
        solr.printResults();
    }

    @Test
    public void shouldFindSigle() throws Exception {
        String[][] doc = {{"sigle", "123"}};
        solr.addDocumentFromArray(doc);

        solr.search("sigle:123");
        assertEquals(1, results());
    }

    @Test
    public void shouldProduceHlSnippetForSufo() throws Exception {
        String[][] doc = {{"sufo", "imbis"}, {"artikel_text", "article"}, {"sufo_text", "imbis##article"}};
        solr.addDocumentFromArray(doc);

        solr.search("imbis");
        String hlSnippet = solr.getHighlightings().get("1234").get("artikel_text").get(0);
        assertEquals("article", hlSnippet);
    }

    @Test
    public void shouldFindSufoUsingGeneralSearchExact() throws Exception {
        String[][] doc = {{"sufo", "Imbis"}};
        solr.addDocumentFromArray(doc);

        solr.search("Imbis EXAKT");
        assertEquals(1, results());

        solr.search("imbis EXAKT");
        assertEquals(0, results());
    }

    @Test
    public void shouldFindSufoUsingGeneralSearch() throws Exception {
        String[][] doc = {{"sufo", "imbis"}};
        solr.addDocumentFromArray(doc);

        solr.search("imbis");
        assertEquals(1, results());
    }

    @Test
    public void shouldFindSufo() throws Exception {
        String[][] doc = {{"sufo", "imbis"}};
        solr.addDocumentFromArray(doc);

        solr.search("sufo:imbis");
        assertEquals(1, results());
    }

    @Test
    public void shouldFindNotExactComplexPhrase() throws Exception {
        String[][] doc = {{"artikel", "imbis ward"}};
        solr.addDocumentFromArray(doc);

        solr.search("\"imbis War*\"");
        assertEquals(1, results());
    }

    @Test
    public void shouldFindExactComplexPhrase() throws Exception {
        String[][] doc = {{"artikel", "imbis Ward"}};
        solr.addDocumentFromArray(doc);

        solr.search("\"?mbis War*\" EXAKT");
        assertEquals(1, results());
    }

    @Test
    public void shouldSuggestSearchedTerm() throws Exception {
        String[][] doc = {{"id", "1"}, {"lemma", "test"}};
        solr.addDocumentFromArray(doc);

        solr.suggest("test");
        assertEquals("test", solr.suggestion(1));
    }

    @Test
    public void shouldSuggestWithParenthesis() throws Exception {
        String[][] doc = {{"id", "1"}, {"lemma", "ampt(s)kleid"}};
        solr.addDocumentFromArray(doc);

        solr.suggest("ampt(s");
        assertEquals("ampt(s)kleid", solr.suggestion(1));
    }

    @Test
    public void shouldSuggest() throws Exception {
        String[][] doc = {{"id", "1"}, {"lemma", "test1"}};
        solr.addDocumentFromArray(doc);
        String[][] doc2 = {{"id", "2"}, {"lemma", "test2"}};
        solr.addDocumentFromArray(doc2);

        solr.suggest("test");
        assertEquals("test1", solr.suggestion(1));
        assertEquals("test2", solr.suggestion(2));
    }

    @Test
    public void shouldRemoveSpecialCharsFromFrontAndBackInSnippet() throws Exception {
        String[][] doc = {{"artikel", ",)/‹.]- ;: test (,/[- ;:"},
                {"artikel_text", ",)/‹.]- ;: test (,/[- ;:"}};
        solr.addDocumentFromArray(doc);

        solr.search("test");
        assertEquals(1, results());
        String hlSnippet = assertHighlighted("artikel_text", "test");
        assertEquals("<span class=\"highlight\">test</span>", hlSnippet);
    }

    @Test
    public void shouldFindTermWithCaret() throws Exception {
        String[][] doc = {{"artikel", "imbisgast"},
                {"artikel_text", "imbisgast"}};
        solr.addDocumentFromArray(doc);

        solr.search("^imbis");
        assertEquals(1, results());
        assertHighlighted("artikel_text", "imbisgast");
    }

    @Test
    public void shouldNotHighlightLonelyParenthesis() throws Exception {
        String[][] doc = {{"artikel", "imbis)"},
                {"artikel_text", "imbis)"}};
        solr.addDocumentFromArray(doc);

        solr.search("imbis");
        assertEquals(1, results());
        assertNotHighlighted("artikel_text", "imbis)");
        assertHighlighted("artikel_text", "imbis");
    }

    @Test
    public void shouldGenerateHlSnippetForLemmaWithOR() throws Exception {
        String[][] doc = {{"lemma", "imbis"}, {"artikel", "imbis"},
                {"artikel_text", "imbis"}};
        String[][] doc2 = {{"id", "5678"}, {"lemma", "test"}, {"artikel", "test bla"},
                {"artikel_text", "test bla"}};
        solr.addDocumentFromArray(doc);
        solr.addDocumentFromArray(doc2);

        solr.search("lemma:imbis OR bla");
        assertEquals(2, results());
        assertNotHighlighted("artikel_text", "imbis");
    }

    @Test
    public void shouldGenerateHlSnippetForLemmaWithNOT() throws Exception {
        String[][] doc = {{"lemma", "imbis"}, {"artikel", "imbis"},
                {"artikel_text", "imbis"}};
        String[][] doc2 = {{"id", "5678"}, {"lemma", "test"}, {"artikel", "test bla"},
                {"artikel_text", "test bla"}};
        solr.addDocumentFromArray(doc);
        solr.addDocumentFromArray(doc2);

        solr.search("lemma:imbis NOT (bla)");
        assertEquals(1, results());
        assertNotHighlighted("artikel_text", "imbis");
    }

    @Test
    public void shouldGenerateUnhighlightedSnippetForTwoLemmas() throws Exception {
        String[][] doc = {{"lemma", "imbis bla"}, {"artikel", "imbis bla imbisgast"},
                {"artikel_text", "imbis bla imbisgast"}};
        solr.addDocumentFromArray(doc);

        solr.search("lemma:imbis lemma:bla");
        assertEquals(1, results());
        assertNotHighlighted("artikel_text", "imbis", "bla", "imbisgast");
    }

    @Test
    public void shouldGenerateHlSnippetWithoutLemma() throws Exception {
        String[][] doc = {{"lemma", "imbis"}, {"artikel", "imbis imbisgast ward"},
                {"artikel_text", "imbis imbisgast ward"}};
        solr.addDocumentFromArray(doc);

        solr.search("lemma:imbis AND (ward)");
        assertEquals(1, results());
        assertHighlighted("artikel_text", "ward");
        assertNotHighlighted("artikel_text", "imbis");
        assertNotHighlighted("artikel_text", "imbisgast");
    }

    @Test
    public void shouldGenerateUnhighlightedSnippetForExactLemma() throws Exception {
        String[][] doc = {{"lemma", "imbis"}, {"artikel", "imbis"}, {"artikel_text", "imbis"}};
        solr.addDocumentFromArray(doc);

        solr.search("EXAKT lemma:imbis");
        assertEquals(1, results());
        assertNotHighlighted("artikel_text", "imbis");
    }

    @Test
    public void shouldGenerateUnhighlightedSnippetForLemma() throws Exception {
        String[][] doc = {{"lemma", "imbis"}, {"artikel", "imbis, Nomen"}, {"artikel_text", "imbis, Nomen"}};
        solr.addDocumentFromArray(doc);

        solr.search("lemma:imbis");
        assertEquals(1, results());
        String hlText = assertNotHighlighted("artikel_text", "Nomen");
        // the lemma itself must not be repeated in the snippet, it doesn't look good in the search results
        assertEquals("Nomen", hlText);
    }

    @Test
    public void shouldHighlightExactInArticle() throws Exception {
        String[][] doc = {{"artikel", "imbis IMBIS"}};
        solr.addDocumentFromArray(doc);

        String[][] extraParams = {{"hl.q", "IMBIS EXAKT"}};
        solr.articleHl(extraParams, "id:1234");

        assertEquals(1, results());
        assertHighlighted("artikel", "IMBIS");
        assertNotHighlighted("artikel", "imbis");
    }

    @Test
    public void shouldHighlightOnlyExactTerm() throws Exception {
        String[][] doc = {{"zitat", "Imbis imbis"}, {"zitat_text", "Imbis imbis"}, {"artikel", "Imbis imbis"},
                {"artikel_text", "Imbis imbis"}};
        solr.addDocumentFromArray(doc);

        solr.search("EXAKT Imbis");
        assertEquals(1, results());
        assertHighlighted("artikel_text", "Imbis");
        assertNotHighlighted("artikel_text", "imbis");
    }

    @Test
    public void shouldListOnlyExactTerm() throws Exception {
        String[][] doc = {{"zitat", "Imbis"}, {"zitat_text", "Imbis"}, {"artikel", "Imbis"},
                {"artikel_text", "Imbis"}};
        solr.addDocumentFromArray(doc);

        solr.list("EXAKT Imbis");
        assertEquals(1, results());

        solr.search("EXAKT imbis");
        assertEquals(0, results());
    }

    @Test
    public void shouldFindOnlyExactTerm() throws Exception {
        String[][] doc = {{"zitat", "Imbis"}, {"zitat_text", "Imbis"}, {"artikel", "Imbis"},
                {"artikel_text", "Imbis"}};
        solr.addDocumentFromArray(doc);

        solr.search("EXAKT Imbis");
        assertEquals(1, results());

        solr.search("EXAKT imbis");
        assertEquals(0, results());
    }

    @Test
    public void shouldHighlightOnlyExactMatchInCitation() throws Exception {
        String[][] doc = {{"zitat", "Imbis imbis"}, {"zitat_text", "Imbis imbis"}};
        solr.addDocumentFromArray(doc);

        solr.search("zitat:Imbis EXAKT");
        assertEquals(1, results());

        assertHighlighted("artikel_text", "Imbis");
        assertNotHighlighted("artikel_text", "imbis");
    }

    @Test
    public void shouldFindOnlyExactInCitation() throws Exception {
        String[][] doc = {{"zitat", "Imbis"}, {"zitat_text", "Imbis"}};
        solr.addDocumentFromArray(doc);

        solr.search("EXAKT zitat:Imbis");
        assertEquals(1, results());

        solr.search("EXAKT zitat:imbis");
        assertEquals(0, results());
    }

    @Test
    public void shouldHighlightBdvOnly() throws Exception {
        String[][] doc = {{"artikel", "imbis <!--start bdv1--><div>imbisinbdv</div><!--end bdv1-->"},
                {"bdv", "<!--start bdv1--><div>imbisinbdv</div><!--end bdv1-->"}};
        solr.addDocumentFromArray(doc);

        String[][] extraParams = {{"hl.q", "bdv:imbis"}};
        solr.articleHl(extraParams, "id:1234");

        assertEquals(1, results());
        assertNotHighlighted("artikel", "imbis");
        assertHighlighted("artikel", "imbisinbdv");
    }

    @Test
    public void shouldHighlightInArticle() throws Exception {
        String[][] doc = {{"artikel", "imbis"}};
        solr.addDocumentFromArray(doc);

        String[][] extraParams = {{"hl.q", "imbis"}};
        solr.articleHl(extraParams, "id:1234");

        assertEquals(1, results());
        assertHighlighted("artikel", "imbis");
    }

    @Test
    public void shouldOverwriteArticleHighlighting() throws Exception {
        String[][] doc = {{"artikel", "bla"}, {"bdv", "bla"}, {"artikel_text", "different"},
                {"bdv_text", "bla"}};
        solr.addDocumentFromArray(doc);

        solr.search("bdv:bla");

        assertEquals(1, results());
        assertHighlighted("artikel_text", "bla");
    }

    @Test
    public void shouldHighlightInsideHtml() throws Exception {
        String[][] doc = {{"bdv", "<div>bla</div>"}};
        solr.addDocumentFromArray(doc);

        String[][] extraParams = {{"hl", "on"}};
        solr.select(extraParams, "bdv:bla");

        assertEquals(1, results());
        assertHighlighted("bdv", "bla");
    }

    @Test
    public void shouldRemoveDash() throws Exception {
        String[][] doc = {{"artikel", "legatar(-ius)"}};
        solr.addDocumentFromArray(doc);

        solr.search("legatarius");

        assertEquals(1, results());
    }

    @Test
    public void shouldNotHighlightTes() throws Exception {
        String[][] doc = {{"zitat", "das"}, {"zitat_text", "das"}, {"artikel_text", "tes das"},
                {"artikel", "tes das"}};
        solr.addDocumentFromArray(doc);

        solr.search("das");

        assertEquals(1, results());
        assertNotHighlighted("artikel_text", "tes");
    }

    @Test
    public void shouldHighlightQuote() throws Exception {
        String[][] doc = {{"zitat", "und"}, {"zitat_text", "und"}, {"artikel_text", "und"},
                {"artikel", "und"}};
        solr.addDocumentFromArray(doc);

        solr.search("vnd");

        assertEquals(1, results());
        assertHighlighted("artikel_text", "und");
    }

    @Test
    public void shouldFilterNonletters() throws Exception {
        String[][] doc = {{"artikel", "bla"}};
        solr.addDocumentFromArray(doc);

        solr.search("artikel:#+bl,;.");

        assertEquals(1, results());
    }

    @Test
    public void shouldSearchInCitations() throws Exception {
        String[][] doc = {{"zitat", "únser"}};
        solr.addDocumentFromArray(doc);

        solr.search("unser");

        assertEquals(1, results());
    }

    @Test
    public void shouldReplaceAccentedLetter() throws Exception {
        String[][] doc = {{"zitat", "únser"}};
        solr.addDocumentFromArray(doc);

        solr.list("zitat:unser");

        assertEquals(1, results());
    }

    @Test
    public void shouldRemoveCombinedLetter() throws Exception {
        String[][] doc = {{"zitat", "svͤlen"}};
        solr.addDocumentFromArray(doc);

        solr.search("zitat:svlen");

        assertEquals(1, results());
    }

    @Test
    public void shouldHighlightChristDifferently() throws Exception {
        String[][] doc = {{"artikel", "christ krist"}, {"artikel_text", "christ krist"},
                {"zitat", "christ krist"}, {"zitat_text", "christ krist"}};
        solr.addDocumentFromArray(doc);

        solr.search("christ");

        assertEquals(1, results());
        assertHighlighted("artikel_text", "christ");
        assertNotHighlighted("artikel_text", "krist");
        assertHighlighted("zitat_text", "christ", "krist");
    }

    @Test
    public void shouldHighlightArticleAndCitationDifferently() throws Exception {
        String[][] doc = {{"artikel", "und vnd"}, {"zitat", "und vnd"}, {"artikel_text", "und vnd"},
                {"zitat_text", "und vnd"}};
        solr.addDocumentFromArray(doc);

        solr.search("und");

        assertEquals(1, results());
        assertHighlighted("artikel_text", "und");
        assertNotHighlighted("artikel_text", "vnd");
        assertHighlighted("zitat_text", "und", "vnd");
    }

    @Test
    public void shouldFindPartialAlternativeSpellings() throws Exception {
        String[][] doc = {{"zitat", "wvnde"}};
        solr.addDocumentFromArray(doc);

        solr.search("zitat:unt");

        assertEquals(1, results());
    }

    @Test
    public void shouldFindAlternativeSpellings() throws Exception {
        String[][] doc = {{"zitat", "vnd katze"}};
        solr.addDocumentFromArray(doc);

        solr.select("zitat:(+und +unt +vnt +vnd +katze +chatze +qatze +catze +gedza)");

        assertEquals(1, results());
    }

    @Test
    public void shouldFindUmlaut() throws Exception {
        String[][] doc = {{"artikel_text", "bär"}};
        solr.addDocumentFromArray(doc);

        solr.select("artikel_text:bar");

        assertEquals(1, results());
    }

    @Test
    public void shouldDeleteNonbreakingSpace() throws Exception {
        String[][] doc = {{"artikel_text", "test abc"}};
        solr.addDocumentFromArray(doc);

        solr.select("artikel_text:test");

        assertEquals(1, results());
    }

    @Test
    public void shouldFindPipe() throws Exception {
        String[][] doc = {{"artikel_text", "test |"}};
        solr.addDocumentFromArray(doc);

        solr.select("artikel_text:|");

        assertEquals(1, results());
    }

    @Test
    public void shouldIgnoreSpecialChars() throws Exception {
        String[][] doc = {{"artikel_text", "& test1, ›test2‹"}};
        solr.addDocumentFromArray(doc);

        solr.select("artikel_text:test1");
        assertEquals(1, results());
        solr.select("artikel_text:test2");
        assertEquals(1, results());
        solr.select("artikel_text:&");
        assertEquals(0, results());
    }

    @Test
    public void shouldFindWithoutPipe() throws Exception {
        String[][] doc = {{"lemma", "my|lemma"}};
        solr.addDocumentFromArray(doc);

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
