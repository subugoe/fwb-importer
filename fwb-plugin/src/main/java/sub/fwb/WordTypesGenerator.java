package sub.fwb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class WordTypesGenerator {

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
