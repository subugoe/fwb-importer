package sub.fwb.api;

import java.util.Map;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import sub.ent.api.ImporterStep;
import sub.fwb.SolrTester;

/**
 * Importer step that checks if the import succeeded.
 *
 */
public class ImporterStepRunTests extends ImporterStep {

	/**
	 * Gets a Solr endpoint and executes some tests on the Solr index.
	 */
	@Override
	public void execute(Map<String, String> params) throws Exception {
		String solrUrl = params.get("solrUrl");
		String solrImportCore = params.get("solrImportCore");
		String solrUser = params.get("solrUser");
		String solrPassword = params.get("solrPassword");
		if (!empty(solrUser, solrPassword)) {
			solrUrl = solrUrl.replace("://", "://" + solrUser + ":" + solrPassword + "@");
		}
		out.println();
		out.println("    Running test queries.");
		System.setProperty("SOLR_URL_FOR_TESTS", solrUrl);
		System.setProperty("SOLR_CORE_FOR_TESTS", solrImportCore);
		JUnitCore junit = new JUnitCore();
		Result testResult = junit.run(SolrTester.class);
		for (Failure fail : testResult.getFailures()) {
			out.println();
			out.println("WARNING in " + fail.getTestHeader() + ": " + fail.getMessage());
		}
	}

	private boolean empty(String... strings) {
		for (String s : strings) {
			if (s == null || "".equals(s))
				return true;
		}
		return false;
	}

	@Override
	public String getStepDescription() {
		return "Testabfragen in Solr";
	}

}
