package sub.fwb.api;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import sub.ent.api.ImporterStep;
import sub.fwb.SolrTest;

import java.util.Map;

/**
 * Importer step that checks if the import succeeded.
 */
public class ImporterStepRunTests extends ImporterStep {

    @Override
    public void execute(Map<String, String> params) throws Exception {
        String solrUrl = params.get("solrUrl");
        String solrImportCore = params.get("solrImportCore");
        out.println();
        out.println("    Running test queries.");
        System.setProperty("SOLR_URL_FOR_TESTS", solrUrl);
        System.setProperty("SOLR_CORE_FOR_TESTS", solrImportCore);
        JUnitCore junit = new JUnitCore();
        Result testResult = junit.run(SolrTest.class);
        for (Failure fail : testResult.getFailures()) {
            out.println();
            out.println("WARNING in " + fail.getTestHeader() + " - " + fail.getTrace() + ": " + fail.getMessage());
        }
    }

    @Override
    public String getStepDescription() {
        return "Testabfragen in Solr";
    }
}
