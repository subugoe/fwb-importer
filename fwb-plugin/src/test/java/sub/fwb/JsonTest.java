package sub.fwb;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import sub.ent.backend.Xslt;

public class JsonTest {

	private OutputStream outputBaos;
	private static Xslt xslt;

	@BeforeClass
	public static void beforeAllTests() throws Exception {
		xslt = new Xslt();
		xslt.setXsltScript("src/main/resources/fwb-indexer.xslt");
		xslt.setParameter("quellenliste", "src/test/resources/forJson_fwbQuellen.xml");
	}

	@Before
	public void beforeEachTest() throws Exception {
		outputBaos = new ByteArrayOutputStream();
	}

	@After
	public void afterEachTest() {
		 System.out.println(outputBaos.toString());
	}

	@Test
	public void createsJson() throws Exception {
		xslt.transform("src/test/resources/forJson_tei.xml", outputBaos);
		String result = outputBaos.toString();

		String expectedJson =
				"{\"Felder\":"
				+ "[\"Sigle\",\"Textsorte\",\"Sinnwelt\",\"Klassifikation\",\"Kommunikationsintention\",\"Raum\",\"Zeit\"],"
				+ "\"Bedeutungen\":{"
				+ "\"1\":"
				+ "{"
				+ "\"Definition\":\"def1\","
				+ "\"Siglen\":"
				+ "[{"
				+ "\"Sigle\":\"89\","
				+ "\"Textsorte\":[\"Did.\",\"Theol.\"],"
				+ "\"Sinnwelt\":[\"Alltag\",\"Dichtung\"],"
				+ "\"Klassifikation\":[\"Literatur\"],"
				+ "\"Kommunikationsintention\":[\"unterhaltend\",\"agitierend\"],"
				+ "\"Raum\":[\"alem.\"],"
				+ "\"Zeit\":[{\"confidence\":\"1\",\"from\":\"1520\",\"to\":\"1520\",\"from-custom\":\"\",\"to-custom\":\"\""
				+ "}]},"
				+ "{"
				+ "\"Sigle\":\"777\","
				+ "\"Textsorte\":[\"Rewi.\"],"
				+ "\"Sinnwelt\":[\"Institutionen\",\"Religion\"],"
				+ "\"Klassifikation\":[\"Recht\",\"Religion\"],"
				+ "\"Kommunikationsintention\":[\"dokumentierend\"],"
				+ "\"Raum\":[\"rhfrk.\"]"
				+ ",\"Zeit\":[{\"confidence\":\"1\",\"from\":\"1513\",\"to\":\"1513\",\"from-custom\":\"\",\"to-custom\":\"\""
				+ "}]},"
				+ "{"
				+ "\"Sigle\":\"363\","
				+ "\"Textsorte\":[\"Rewi.\"],"
				+ "\"Sinnwelt\":[\"Institutionen\"],"
				+ "\"Klassifikation\":[\"Recht\",\"Religion\"],"
				+ "\"Kommunikationsintention\":[\"dokumentierend\"],"
				+ "\"Raum\":[\"mosfrk.\"],"
				+ "\"Zeit\":[{\"confidence\":\"0.00265\",\"from\":\"1350\",\"to\":\"1500\",\"from-custom\":\"\",\"to-custom\":\"\""
				+ "}]}]},"
				+ "\"2\":"
				+ "{"
				+ "\"Definition\":\"def2\","
				+ "\"Siglen\":"
				+ "[{"
				+ "\"Sigle\":\"89\","
				+ "\"Textsorte\":[\"Did.\",\"Theol.\"],"
				+ "\"Sinnwelt\":[\"Alltag\",\"Dichtung\"],"
				+ "\"Klassifikation\":[\"Literatur\"],"
				+ "\"Kommunikationsintention\":[\"unterhaltend\",\"agitierend\"],"
				+ "\"Raum\":[\"alem.\"],"
				+ "\"Zeit\":[{\"confidence\":\"1\",\"from\":\"1520\",\"to\":\"1520\",\"from-custom\":\"\",\"to-custom\":\"\""
				+ "}]}]}},"
				+ "\"Übersichten\":{"
				+ "\"Textsorte\":[\"Did.\",\"Rewi.\",\"Theol.\"],"
				+ "\"Sinnwelt\":[\"Alltag\",\"Dichtung\",\"Institutionen\",\"Religion\"],"
				+ "\"Klassifikation\":[\"Literatur\",\"Recht\",\"Religion\"],"
				+ "\"Kommunikationsintention\":[\"agitierend\",\"dokumentierend\",\"unterhaltend\"]}}";

		assertXpathEvaluatesTo(expectedJson, "//field[@name='sources_json']", result);

	}

}
