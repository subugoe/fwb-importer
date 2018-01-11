package sub.fwb;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import static org.junit.Assert.assertEquals;

public class TeiHtmlComparator {

	public void compareTexts(File tei, File solrXml) throws IOException {

		String teiString = FileUtils.readFileToString(tei);
		teiString = teiString.replaceAll("\n\\s*", "");
		teiString = extract("<body>(.*?)</body>", teiString);
		teiString = teiString.replace("<oRef/>", "-");
		teiString = teiString.replace("<quote>", ": ");
		teiString = teiString.replace("<lb/>", " | ");
		teiString = teiString.replace("> <", "");
		// need to replace the accidental occurrences
		teiString = teiString.replaceAll(
				"(Bedeutungsverwandte: |Syntagmen: |Belegblock: |Gegensätze: |Phraseme: |Wortbildungen: |Zur Sache: |Redensart: )",
				"");
		teiString = teiString.replaceAll("Zur Sache ", "");
		teiString = removeTags(teiString);
		teiString = teiString.replaceAll("\\s+", "").trim();

		String solrString = FileUtils.readFileToString(solrXml);
		solrString = solrString.replaceAll("\n\\s*", "");

		solrString = extract("<field name=\"artikel\"><!\\[CDATA\\[(.*?)\\]\\]>", solrString);
		solrString = solrString.replaceAll("<div class=\"sense-number\">.*?</div>", "");
		solrString = solrString.replaceAll("<div class=\"subvoce-begin\">.*?</div>", "");
		solrString = solrString.replaceAll("<div class=\"homonym\">.*?</div>", "");
		solrString = solrString.replace("> <", "");
		solrString = solrString.replaceAll(
				"(Bedeutungsverwandte: |Syntagmen: |Belegblock: |Gegensätze: |Phraseme: |Wortbildungen: |Zur Sache: |Redensart: )",
				"");
		solrString = solrString.replaceAll("Zur Sache ", "");
		solrString = removeTags(solrString);
		solrString = solrString.replaceAll("\\s+", "").trim();

		// System.out.println(teiString);
		// System.out.println(solrString);

		try {
			assertEquals("in file " + tei.getName() + "\n", teiString, solrString);
		} catch (AssertionError e) {
			throw e;
		}
	}

	private String extract(String regex, String s) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(s);
		while (matcher.find()) {
			return matcher.group(1);
		}
		return "";
	}

	private String removeTags(String html) {
		String textOnly = html.replaceAll("<.*?>", "");
		return textOnly.replaceAll("[\\p{Zs}\\s]+", " ").trim();
	}

}
