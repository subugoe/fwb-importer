package sub.fwb;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Parser for the Excel file that is used in the FWB project to store book sources.
 *
 */
public class SourcesParser {

	private String[] headers = { "A 0 sort", "B 1 sigle", "C 2 kraftliste", "D 3 kurztitel", "E 4 ort und zeit", "F 5",
			"G 6 raum / ort", "H 7 raum (karte)", "I 8", "J 9 zeit", "K 10 zeit numerisch", "L 11", "M 12 pdf",
			"N 13 epdf", "O 14 digitalisat online", "P 15 eonline", "Q 16 permalink", "R 17 biblio", "S 18 ppn",
			"T 19 zitierweise", "U 20 textsorte", "V 21 name", "W 22 sinnwelt", "X 23 klassifikation",
			"Y 24 kommunikationsintention" };

	private final int SIGLE = 1;
	private final int KRAFTLISTE = 2;
	private final int DIGITALISAT_ONLINE = 14;
	private final int PERMALINK = 16;
	private final int BIBLIO = 17;
	private final int ZITIERWEISE = 19;
	private final int NAME = 21;

	/**
	 * Converts one Excel file to one Solr XML file. The Solr XML file will contain one 
	 * <doc> element for each row in the Excel file.
	 */
	public void convertExcelToXml(File excelFile, File xmlResult) throws IOException {
		FileInputStream file = new FileInputStream(excelFile);
		StringBuffer buffer = new StringBuffer();
		buffer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
		buffer.append("<add>\n");

		XSSFWorkbook workbook = new XSSFWorkbook(file);

		XSSFSheet sheet = workbook.getSheetAt(0);

		// for (int i = 1; i <= 49; i++) {
		for (int i = 1; i <= sheet.getLastRowNum(); i++) {
			Row row = sheet.getRow(i);
			String strongList = asString(row.getCell(KRAFTLISTE));
			if (extract(strongList).startsWith("x"))
				continue;

			buffer.append("<doc>\n");
			buffer.append("<field name=\"type\">quelle</field>\n");
			String sigle = asString(row.getCell(SIGLE));
			buffer.append("<field name=\"id\">source_" + sigle + "</field>\n");
			buffer.append("<field name=\"source_sigle\">" + sigle + "</field>\n");

			appendFromStronglist(row, buffer);

			buffer.append("<field name=\"source_html\"><![CDATA[");
			buffer.append("<div class=\"source-details\">\n");

			appendHeader(asString(row.getCell(KRAFTLISTE)), buffer);

			appendRowOfSpans("Bibliographie: ", asString(row.getCell(BIBLIO)), buffer);
			appendRowOfSpans("Zitierweise: ", asString(row.getCell(ZITIERWEISE)), buffer);

			Map<String, String> links = new HashMap<String, String>();
			String permalink = asString(row.getCell(PERMALINK));
			String[] splitPermalinks = permalink.trim().split("\\s+");
			if (!permalink.isEmpty()) {
				for (String p : splitPermalinks) {
					links.put("Permalink", p);
				}
			}
			String digi = asString(row.getCell(DIGITALISAT_ONLINE));
			String[] splitDigilinks = digi.trim().split("\\s+");
			if (!digi.isEmpty()) {
				for (String d : splitDigilinks) {
					links.put("Digitalisat online", d);
				}
			}
//			String pdf = asString(row.getCell(12));
//			if (!pdf.isEmpty()) {
//				links.put("PDF", pdf);
//			}

			if (!links.isEmpty()) {
				appendRowWithLinks(links, buffer);
			}

			buffer.append("</div>\n");
			buffer.append("]]></field>\n");
			
			if (!permalink.isEmpty()) {
				for (String p : splitPermalinks) {
					buffer.append("<field name=\"source_permalink\"><![CDATA[" + p + "]]></field>\n");
				}
			}
			if (!digi.isEmpty()) {
				for (String d : splitDigilinks) {
					buffer.append("<field name=\"source_digilink\"><![CDATA[" + d + "]]></field>\n");
				}
			}
			buffer.append("<field name=\"source_biblio\"><![CDATA[" + asString(row.getCell(BIBLIO)) + "]]></field>\n");
			buffer.append("<field name=\"source_header\"><![CDATA[" + extract(strongList) + "]]></field>\n");
			buffer.append("<field name=\"source_sort\"><![CDATA[" + getSortEntry(strongList) + "]]></field>\n");
			
			buffer.append("</doc>\n");

		}
		workbook.close();

		buffer.append("</add>");
		FileUtils.writeStringToFile(xmlResult, buffer.toString(), "UTF-8");
	}

	private String getSortEntry(String strongList) {
		String extracted = extract(strongList);
		return extracted.replace("Ä", "A").replace("ä", "a").replace("Ö", "O").replace("ö", "o").replace("Ü", "U").replace("ü", "u").replace("ß", "s").trim();
	}

	private void appendFromStronglist(Row row, StringBuffer buffer) {
		String entryKind = asString(row.getCell(NAME));
		String fieldName = "";
		if ("1".equals(entryKind)) {
			fieldName = "source_author";
		} else if ("2".equals(entryKind)) {
			fieldName = "source_author_secondary";
		} else if ("3".equals(entryKind)) {
			fieldName = "source_herausgeber";
		} else if ("4".equals(entryKind)) {
			fieldName = "source_title";
		} else {
			return;
		}
		String stronglist = asString(row.getCell(KRAFTLISTE));
		String entryValue = extract(stronglist);
		buffer.append("<field name=\"" + fieldName + "\"><![CDATA[" + entryValue + "]]></field>\n");
	}

	private String asString(Cell cell) {
		String result = "";
		if (cell != null && !isEmpty(cell)) {
			if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
				result += cell.getStringCellValue();
			} else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
				result += new Double(cell.getNumericCellValue()).intValue();
			} else {
				throw new RuntimeException("Unknown cell type: " + cell.getCellType() + ".");
			}
		}
		return result.replace("<", "&lt;").replace(">", "&gt;");
	}

	private void appendHeader(String strongListField, StringBuffer buffer) {
		String entryValue = extract(strongListField);
		buffer.append("  <div class=\"source-details-header\">" + entryValue + "</div>\n");
	}

	private void appendRowOfSpans(String left, String right, StringBuffer buffer) {
		buffer.append("  <div class=\"source-details-row\">\n");
		buffer.append("  <span class=\"column-left\">" + left + "</span>\n");
		buffer.append("  <span class=\"column-right\">" + right + "</span>\n");
		buffer.append("  </div>\n");
	}

	private void appendRowWithLinks(Map<String, String> links, StringBuffer buffer) {
		buffer.append("  <div class=\"source-details-row\">\n");
		buffer.append("    <span class=\"column-left\">Links: </span>\n");
		buffer.append("    <span class=\"column-right\">");
		for (Map.Entry<String, String> entry : links.entrySet()) {
			buffer.append(asHref(entry.getKey(), entry.getValue()));
		}
		buffer.append("    </span>\n");
		buffer.append("  </div>\n");
	}

	private String asHref(String label, String reference) {
		if (reference == null || reference.isEmpty()) {
			return "";
		}
		return "<a class=\"source-details_link\" href=\"" + reference + "\">" + label + "</a>";
	}

	private boolean isEmpty(Cell cell) {
		if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
			return cell.getStringCellValue().isEmpty();
		} else if (cell.getCellType() == Cell.CELL_TYPE_BLANK) {
			return true;
		}
		return false;
	}
	
	private String extract(String strongList) {
		if (strongList == null)
			return "";
		
		return extractUsingRegex("\\$c(.*?)#", strongList).get(0);
	}

	private List<String> extractUsingRegex(String regex, String s) {
		List<String> results = new ArrayList<String>();
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(s);
		while (matcher.find()) {
			results.add(matcher.group(1));
		}

		if (results.isEmpty()) {
			results.add("");
		}
		return results;
	}

}
