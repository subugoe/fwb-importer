package sub.fwb.api;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import sub.ent.api.ImporterStep;
import sub.ent.backend.FileAccess;
import sub.fwb.TeiHtmlComparator;

public class ImporterStepCompare extends ImporterStep {

	private FileAccess fileAccess = new FileAccess();

	@Override
	public void execute(Map<String, String> params) throws Exception {
		String teiInputDir = params.get("gitDir");
		String solrXmlDir = params.get("solrXmlDir");
		out.println();
		out.println("    Comparing text from TEIs to HTML text in index files:");
		TeiHtmlComparator comparator = new TeiHtmlComparator();
		List<File> allFiles = fileAccess.getAllXmlFilesFromDir(new File(teiInputDir));
		Collections.sort(allFiles);
		int i = 1;
		for (File tei : allFiles) {
			printCurrentStatus(i, allFiles.size());
			File solrXml = new File(new File(solrXmlDir), tei.getName());
			try {
				comparator.compareTexts(tei, solrXml);
			} catch(AssertionError e) {
				out.println();
				out.println("WARNING " + e.getMessage());
				out.println();
			}
			i++;
		}
	}

	private void printCurrentStatus(int currentNumber, int lastNumber) {
		if (currentNumber % 10000 == 0 || currentNumber == lastNumber) {
			out.println("    ... " + currentNumber);
		}
	}

	@Override
	public String getStepDescription() {
		return "Vergleich TEI <-> XML";
	}

}
