package sub.fwb.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import sub.ent.api.ImporterStep;
import sub.fwb.FWBEnvironment;
import sub.fwb.SolrTest;

/**
 * Importer step that checks if the import succeeded.
 *
 */
public class ImporterStepRunTests extends ImporterStep {

	/**
	 * Gets a Solr endpoint and executes some tests on the Solr index.
	 */
	private static final FWBEnvironment fwbenv = new FWBEnvironment();

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
		clearCache();
	}

	@Override
	public String getStepDescription() {
		return "Testabfragen in Solr";
	}

	/**
	 * Clearing the FWB API cache after finish indexing
	 */
	private void clearCache() throws IOException {
		try {
			String webPage = fwbenv.cacheUrl();
			String name = fwbenv.fwbUser();
			String password = fwbenv.fwbPassword();

			String authString = name + ":" + password;
			byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
			String authStringEnc = new String(authEncBytes);

			URL url = new URL(webPage);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
			InputStream is = urlConnection.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			int responseCode = urlConnection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) { // success
				BufferedReader in = new BufferedReader(new InputStreamReader(
						urlConnection.getInputStream()));
				String inputLine;
				StringBuilder response = new StringBuilder();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();

				System.out.println(response);
			} else {
				System.out.println("GET request not worked, return server response is: '" + responseCode + "'");

			}
			out.println("    Clearing Cache");


		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

}
