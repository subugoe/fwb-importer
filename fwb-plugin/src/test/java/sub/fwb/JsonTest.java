package sub.fwb;

import static org.junit.Assert.*;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import sub.ent.backend.FileAccess;
import sub.ent.backend.Xslt;

public class JsonTest {

	private FileAccess fileAccess = new FileAccess();
	private Xslt xslt = new Xslt();

	@Test
	public void test() throws Exception {
		File inputDir = new File("/home/dennis/mydocker/git/fwb-daten");
		File outputDir = new File("/tmp/json");
		File xsltForJson = new File("/home/dennis/fwb/analyzer.JSON.xsl");
		String sourcesXml = "file:///home/dennis/fwb/FWB-Quellen.xml";
		
		fileAccess.makeSureThatExists(outputDir);
		fileAccess.cleanDir(outputDir);
		
		InputStream xsltStream = FileUtils.openInputStream(xsltForJson);
		xslt.setXsltScript(xsltStream);
		xslt.setErrorOut(System.out);
		xslt.setParameter("quellenliste", sourcesXml);
		
		List<File> allFiles = fileAccess.getAllXmlFilesFromDir(inputDir);
		Collections.sort(allFiles);

		System.out.println("    Converting TEIs to index files:");
		int currentId = 1;
		for (File currentFile : allFiles) {
			printCurrentStatus(currentId, allFiles.size());
			xslt.setParameter("currentArticleId", currentId + "");
			OutputStream fileOs = fileAccess.createOutputStream(outputDir, currentFile.getName());
			xslt.transform(currentFile.getAbsolutePath(), fileOs);
			currentId++;
		}


	}

	private void printCurrentStatus(int currentNumber, int lastNumber) {
		if (currentNumber % 100 == 0 || currentNumber == lastNumber) {
			System.out.println("    ... " + currentNumber);
		}
	}

}
