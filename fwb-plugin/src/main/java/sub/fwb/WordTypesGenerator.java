package sub.fwb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Preprocessor for text files containing mappings
 * of word type codes to written word types.
 */
public class WordTypesGenerator {

	/**
	 * Sequentializes a text file into one line and adds special markings.
	 * This way, the data is easier to process in the XSLT script.
	 */
	public String prepareForXslt(InputStream wordTypes) throws IOException {
		BufferedReader lineReader = new BufferedReader(new InputStreamReader(wordTypes));
		String wordTypeLine = "";
		StringBuilder result = new StringBuilder();
		while ((wordTypeLine = lineReader.readLine()) != null) {
			result.append(wordTypeLine);
			result.append("###");
		}
		lineReader.close();
		return result.toString();
	}
}
