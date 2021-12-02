package sub.fwb;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.core.CoreContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sub.ent.api.ImporterStep;
import sub.ent.api.ImporterStepCoreSwap;
import sub.ent.api.ImporterStepUpload;
import sub.ent.backend.Importer;
import sub.ent.backend.SolrAccess;
import sub.ent.testing.EmbeddedSolr;
import sub.fwb.api.ImporterStepCompare;
import sub.fwb.api.ImporterStepConvert;

public class ImporterIntegrationTest {

	private final File coreProps1 = new File("solr-embedded/fwb/core.properties");
	private final File coreProps1Copy = new File("solr-embedded/fwb/core.properties.copy");
	private final File coreProps2 = new File("solr-embedded/fwboffline/core.properties");
	private final File coreProps2Copy = new File("solr-embedded/fwboffline/core.properties.copy");

	private String gitDir = "src/test/resources/import";
	private final String solrXmlDir = "target/solrxml";

	/* This is used as a signal to use the embedded Solr in this class: */ SolrAccess s;
	private String solrUrl = "embedded";

	private final String importCore = "fwboffline";
	private final String onlineCore = "fwb";

	@Before
	public void setUp() throws Exception {
		backupPropertyFiles();

		startEmbeddedSolr();
	}

	@After
	public void tearDown() throws Exception {
		restorePropertyFiles();
	}

	@Test
	public void test() throws Exception {

		Map<String, String> parametersForAllSteps = testingParams();
		Importer importer = initImporter();
		for (int i = 0; i < importer.getNumberOfSteps(); i++) {
			importer.executeStep(i, parametersForAllSteps);
		}

		SolrQuery solrQuery = new SolrQuery("lemma:test");
		solrQuery.setRequestHandler("/search");
		QueryResponse response = EmbeddedSolr.instance.query(onlineCore, solrQuery);
		EmbeddedSolr.instance.close();

		assertEquals("test", response.getResults().get(0).getFieldValue("lemma"));
	}

	private Importer initImporter() {
		Importer importer = new Importer();
		List<ImporterStep> steps = new ArrayList<>();
		steps.add(new ImporterStepConvert());
		steps.add(new ImporterStepCompare());
		steps.add(new ImporterStepUpload());
		steps.add(new ImporterStepCoreSwap());
		importer.setSteps(steps);
		return importer;
	}

	private Map<String, String> testingParams() {
		Map<String, String> allParams = new HashMap<>();
		allParams.put("gitDir", gitDir);
		allParams.put("solrXmlDir", solrXmlDir);
		allParams.put("solrUrl", solrUrl);
		allParams.put("solrImportCore", importCore);
		allParams.put("solrOnlineCore", onlineCore);
		return allParams;
	}

	private void backupPropertyFiles() throws IOException {
		FileUtils.copyFile(coreProps1, coreProps1Copy);
		FileUtils.copyFile(coreProps2, coreProps2Copy);
		if (new File(solrXmlDir).exists()) {
			FileUtils.forceDelete(new File(solrXmlDir));
		}
	}

	private void startEmbeddedSolr() throws SolrServerException, IOException {
		CoreContainer container = new CoreContainer("solr-embedded");
		container.load();
		EmbeddedSolr.instance = new EmbeddedSolrServer(container, importCore);
		EmbeddedSolr.instance.deleteByQuery(importCore, "*:*");
		EmbeddedSolr.instance.commit(importCore);
		EmbeddedSolr.instance.deleteByQuery(onlineCore, "*:*");
		EmbeddedSolr.instance.commit(onlineCore);
	}

	private void restorePropertyFiles() throws IOException {
		if (coreProps1Copy.exists() && coreProps2Copy.exists()) {
			FileUtils.forceDelete(coreProps1);
			FileUtils.forceDelete(coreProps2);
			FileUtils.moveFile(coreProps1Copy, coreProps1);
			FileUtils.moveFile(coreProps2Copy, coreProps2);
		}
	}

}
