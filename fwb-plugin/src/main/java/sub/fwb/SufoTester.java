package sub.fwb;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import sub.ent.backend.Environment;

public class SufoTester {
	private static SolrAccessForTesting solr = new SolrAccessForTesting();
	private static Environment env = new Environment();
	
	@BeforeClass
	public static void beforeAllTests() throws Exception {
		String solrUrl = System.getProperty("SOLR_URL_FOR_TESTS", "http://localhost:8983/solr");
		String core = System.getProperty("SOLR_CORE_FOR_TESTS", "fwb");
		solr.initialize(solrUrl, core);
		solr.setCredentials(env.solrUser(), env.solrPassword());
	}

	@After
	public void afterEach() throws Exception {
		if (System.getProperty("SOLR_URL_FOR_TESTS") != null) {
			solr.printQueryString();
		} else {
			solr.printResults();
		}
	}

	private String lemma(int resultNumber) {
		return solr.lemma(resultNumber);
	}

	private int results() {
		return (int) solr.results();
	}

	private void mustBeFirstLemma(String result) {
		assertThat(results(), greaterThan(0));
		assertEquals(result, lemma(1));
	}

	
	
	@Test
	public void imbiss() throws Exception {

		solr.list("imbiss");

		mustBeFirstLemma("imbis");
	}

	
	
	
}
