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
	public void amad() throws Exception {

		solr.list("amahd");

		mustBeFirstLemma("amad");
	}

	@Test
	public void kaiser() throws Exception {

		solr.list("kaiser");

		mustBeFirstLemma("keiser");
	}

	@Test
	public void gott() throws Exception {

		solr.list("gott");

		mustBeFirstLemma("gott");
	}

	@Test
	public void amt() throws Exception {

		solr.list("amt");

		mustBeFirstLemma("amt");
	}

	@Test
	public void anfangen() throws Exception {

		solr.list("anfangen");

		mustBeFirstLemma("anfängen");
	}

	@Test
	public void arzt() throws Exception {

		solr.list("arzt");

		mustBeFirstLemma("arzt");
	}

	@Test
	public void papst() throws Exception {

		solr.list("papst");

		mustBeFirstLemma("papst");
	}

	@Test
	public void backenzahn() throws Exception {

		solr.list("backenzahn");

		mustBeFirstLemma("backenzan");
	}

	@Test
	public void panier() throws Exception {

		solr.list("bannerherr");

		mustBeFirstLemma("bannerherre");
	}

	@Test
	public void jammer() throws Exception {

		solr.list("jammervoll");

		mustBeFirstLemma("jamerfol");
	}

	
	
	
}
