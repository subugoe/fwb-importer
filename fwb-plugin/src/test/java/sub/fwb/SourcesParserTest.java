package sub.fwb;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SourcesParserTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	// @Test
	public void test() throws Exception {
		SourcesParser parser = new SourcesParser();
		parser.convertExcelToXml(new
		File("/home/dennis/temp/quellen.xlsx"),
		new File(System.getProperty("java.io.tmpdir"), "sourcesList.xml"));
	}

}
