package sub.fwb.api;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import sub.ent.api.ImporterStep;
import sub.ent.backend.FileAccess;
import sub.ent.backend.Xslt;
import sub.fwb.SourcesParser;
import sub.fwb.WordTypesGenerator;

/**
 * Importer step that starts a transformation of FWB input files
 * (Excel and TEI) to Solr XML files.
 *
 */
public class ImporterStepConvert extends ImporterStep {

	private final SourcesParser sourcesParser = new SourcesParser();
	private final WordTypesGenerator wordTyper = new WordTypesGenerator();
	private final Xslt xslt = new Xslt();
	private final FileAccess fileAccess = new FileAccess();

	/**
	 * Reads an Excel file, a bunch of TEI files, and produces Solr XML files.
	 */
	@Override
	public void execute(Map<String, String> params) throws Exception {
		String gitDir = params.get("gitDir");
		String solrXmlDir = params.get("solrXmlDir");
		File outputDir = new File(solrXmlDir);
		File inputDir = new File(gitDir);
		File techDataInputDir = new File(inputDir, "TechData");
		File inputExcel = new File(techDataInputDir, "FWB-Quellenliste.xlsx");
		File teiInputDir = new File(inputDir, "V00");
		File inputSourcesXml = new File(techDataInputDir, "FWB-Quellen.xml");

		fileAccess.cleanDir(outputDir);
		out.println("    Converting Excel to index file.");

		File sourcesXml = new File(outputDir, "0-sources.xml");
		sourcesParser.convertExcelToXml(inputExcel, sourcesXml);

		InputStream xsltStream = ImporterStepConvert.class.getResourceAsStream("/fwb-indexer.xslt");
		xslt.setXsltScript(xsltStream);
		xslt.setErrorOut(out);

		InputStream wordTypes = ImporterStepConvert.class.getResourceAsStream("/wordtypes.txt");
		String wordTypesList = wordTyper.prepareForXslt(wordTypes);
		xslt.setParameter("wordTypes", wordTypesList);
		InputStream generalWordTypes = ImporterStepConvert.class.getResourceAsStream("/wordtypes_general.txt");
		String generalWordTypesList = wordTyper.prepareForXslt(generalWordTypes);
		xslt.setParameter("generalWordTypes", generalWordTypesList);

		InputStream subfacetWordTypes = ImporterStepConvert.class.getResourceAsStream("/wordtypes_subfacet.txt");
		String subfacetWordTypesList = wordTyper.prepareForXslt(subfacetWordTypes);
		xslt.setParameter("subfacetWordTypes", subfacetWordTypesList);

		xslt.setParameter("quellenliste", inputSourcesXml.getAbsolutePath());

		List<File> allFiles = fileAccess.getAllXmlFilesFromDir(teiInputDir);
		Collections.sort(allFiles);

		out.println("    Converting TEIs to index files:");
		int currentId = 1;
		for (File currentFile : allFiles) {
			printCurrentStatus(currentId, allFiles.size());
			xslt.setParameter("currentArticleId", currentId + "");
			OutputStream fileOs = fileAccess.createOutputStream(new File(solrXmlDir), currentFile.getName());
			xslt.transform(currentFile.getAbsolutePath(), fileOs);
			currentId++;
		}

	}

	private void printCurrentStatus(int currentNumber, int lastNumber) {
		if (currentNumber % 10000 == 0 || currentNumber == lastNumber) {
			out.println("    ... " + currentNumber);
		}
	}

	@Override
	public String getStepDescription() {
		return "Konvertierung Excel,TEI -> XML";
	}

}
