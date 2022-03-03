package sub.fwb.api;

import org.apache.commons.codec.binary.Base64;
import sub.ent.api.ImporterStep;
import sub.fwb.FWBEnvironment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * Importer step that clears the cache after the import
 */
public class ImporterStepClearCache extends ImporterStep {

    /**
     * Gets a Solr endpoint and executes some tests on the Solr index.
     */
    private static final FWBEnvironment fwbenv = new FWBEnvironment();

    @Override
    public void execute(Map<String, String> params) throws Exception {
        clearCache();
    }

    @Override
    public String getStepDescription() {
        return "Clearing the FWB API cache after finish indexing";
    }

    /**
     * Clearing the FWB API cache after finish indexing
     */
    private void clearCache() {
        try {
            String webPage = fwbenv.cacheUrl();
            URL url = new URL(webPage);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            if (!fwbenv.fwbUser().equals(fwbenv.UNDEFINED_VALUE)) {
                String name = fwbenv.fwbUser();
                String password = fwbenv.fwbPassword();
                String authString = name + ":" + password;

                byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
                String authStringEnc = new String(authEncBytes);
                urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
            }

            InputStream is = urlConnection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
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
        } catch (IOException ioException) {
            out.println(ioException.getMessage());
        }
    }

}
