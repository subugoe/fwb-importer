package sub.fwb;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * JUnit test class.
 * Sends some queries to a running Solr server and checks if the expected results are returned.
 * 
 */ 
public class SolrTester {
	private static SolrWrapper solr;

	@BeforeClass
	public static void beforeAllTests() throws Exception {
		String solrUrl = System.getProperty("SOLR_URL_FOR_TESTS", "http://localhost:8983/solr");
		String core = System.getProperty("SOLR_CORE_FOR_TESTS", "fwb");
		SolrClient solrServerClient = new HttpSolrClient(solrUrl);
		solr = new SolrWrapper(solrServerClient, core);
	}

	@After
	public void afterEach() throws Exception {
		if (System.getProperty("SOLR_URL_FOR_TESTS") != null) {
			solr.printQueryString();
		} else {
			solr.printResults();
		}
	}

	@Test
	public void numberOfArticles() throws Exception {

		solr.list("lemma:*");

		assertThat(results(), greaterThan(44000));
	}

	@Test
	public void numberOfSources() throws Exception {

		solr.select("type:quelle");

		assertThat(results(), greaterThan(1500));
	}

	// @Test
	public void negatedQueryShouldCoverAllTerms() throws Exception {

		solr.select(
				"artikel:/.*[^\\|()\\[\\]\\-⁽⁾a-z0-9äöüßoͤúv́aͤñÿu͂Øůaͧuͥóoͮàïêŷǔıͤēëâôeͣîûwͦýãæáéòõœv̈èu̇ŭāōùēīíūėm̃Γͤŭẽũśŏǒǎǔẅẹìǹăṣẏẙẹσĕĩẃåg̮ńỹěçṅȳňṡćęъčẘịǧḥṁạṙľu֔b].*/");

		assertEquals(0, results());
	}

	// @Test
	public void dollarSignInKindeln() throws Exception {
		String[][] extraparams = { { "hl.q", "kindeln" } };
		solr.articleHl(extraparams, "internal_id:kindeln.s.3v");
		// This used to lead to an exception in Matcher class
		assertEquals(1, results());
	}

	@Test
	public void maxClauseCountOver1024() throws Exception {

		solr.search("artikel:*e*");
	}

	@Test
	public void imbisExact() throws Exception {

		solr.list("imbis EXAKT");

		assertThat(results(), greaterThan(0));
		assertEquals("imbis", lemma(1));
		assertBestResultsContainWordPart("imbis");
	}

	private String lemma(int resultNumber) {
		return solr.lemma(resultNumber);
	}

	private int results() {
		return (int) solr.results();
	}

	private void assertBestResultsContainWordPart(String wordPart) throws Exception {
		int numLemmas = solr.askForNumberOfLemmas(wordPart);
		for (int i = 1; i <= numLemmas; i++) {
			String currentLemma = solr.lemma(i).toLowerCase();
			assertThat(currentLemma, containsString(wordPart));
		}
	}

}
