package sub.fwb;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import sub.ent.backend.Xslt;

public class XsltHtmlTest {

	private OutputStream outputBaos;
	private OutputStream errorBaos;
	private static Xslt xslt;

	@BeforeClass
	public static void beforeAllTests() throws Exception {
		xslt = new Xslt();
		xslt.setXsltScript("src/main/resources/fwb-indexer.xslt");
		xslt.setParameter("sourcesListFile", "src/test/resources/sourcesList.xml");
	}

	@Before
	public void beforeEachTest() throws Exception {
		outputBaos = new ByteArrayOutputStream();
		errorBaos = new ByteArrayOutputStream();
	}

	@After
	public void afterEachTest() {
		 System.out.println(outputBaos.toString());
		 System.out.println(errorBaos.toString());
	}

	@Test
	public void shouldInsertHeadingWithoutColon() throws Exception {
		xslt.transform("src/test/resources/html/headings_withColonInTei.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("Bedeutungsverwandte ", "//div[@class='bdv-begin']", html);
		// example: keiser
	}

	@Test
	public void shouldInsertHeadingWithColon() throws Exception {
		xslt.transform("src/test/resources/html/headings_withoutColonInTei.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("Bedeutungsverwandte: ", "//div[@class='bdv-begin']", html);
	}

	@Test
	public void shouldInsertStwFromArticleHead() throws Exception {
		xslt.transform("src/test/resources/usg-stw.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("in usg stw", "//div[@class='usg-stw']", html);
		// example: al 2 Konj.
	}

	@Test
	public void shouldInsertZursacheFromArticleHead() throws Exception {
		xslt.transform("src/test/resources/html/zursache-usg-ref.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("in usg", "//div[@class='usg-ref']", html);
		// example: abbrechen
	}

	@Test
	public void shouldNotInsertSpaceAfterNeblemWithComma() throws Exception {
		xslt.transform("src/test/resources/html/neblemWithComma.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("neblem, bla", "//div[@class='article-head']", html);
		// example: aberacht
	}

	@Test
	public void shouldNotInsertSpaceInDef() throws Exception {
		xslt.transform("src/test/resources/html/biblInsideDef.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("(Name)", "//div[@class='sense']", html);
		// example: katzenauge
	}

	@Test
	public void shouldNotInsertSpaceInBibl2() throws Exception {
		xslt.transform("src/test/resources/html/biblInsideEtym2.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("(), (Pfeifer)", "//div[@class='etymology']", html);
		// example: blahe
	}

	@Test
	public void shouldNotInsertSpaceInBibl() throws Exception {
		xslt.transform("src/test/resources/html/biblInsideEtym.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("aus (Lexer)", "//div[@class='etymology']", html);
		// example: bleuen
	}

	@Test
	public void shouldRecognizeInlineBdv() throws Exception {
		xslt.transform("src/test/resources/html/bdv.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("", "//span[@class='unknown-element']", html);
		// example: anfängen
	}

	@Test
	public void shouldPrintEtymology() throws Exception {
		xslt.transform("src/test/resources/html/etymWithDescAndLang.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("aus lat. ›desc-test‹.", "//div[@class='etymology']", html);
		// example: äs
	}

	@Test
	public void shouldPrintDescription() throws Exception {
		xslt.transform("src/test/resources/html/etymWithDescAndLang.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("›desc-test‹", "//div[@class='description']", html);
		// example: äs
	}

	@Test
	public void shouldPrintLanguage() throws Exception {
		xslt.transform("src/test/resources/html/etymWithDescAndLang.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("lat.", "//div[@class='language']", html);
		// example: äs
	}

	@Test
	public void shouldCauseOnlyOneWarning() throws Exception {
		beforeAllTests();
		PrintStream errorStream = new PrintStream(errorBaos);
		xslt.setErrorOut(errorStream);

		xslt.transform("src/test/resources/html/unknownElement.xml", outputBaos);
		xslt.transform("src/test/resources/html/unknownElement2.xml", outputBaos);

		String warningMessage = errorBaos.toString();
		assertThat(warningMessage, containsString("unknownElement"));
		assertThat(warningMessage, not(containsString("unknownElement2")));
	}

	@Test
	public void shouldCauseWarningWithUnknownElement() throws Exception {
		beforeAllTests();
		PrintStream errorStream = new PrintStream(errorBaos);
		xslt.setErrorOut(errorStream);

		xslt.transform("src/test/resources/html/unknownElement.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("article head, new text", "//*", html);

		String warningMessage = errorBaos.toString();
		assertThat(warningMessage, containsString("WARNING"));
		assertThat(warningMessage, containsString("Unknown element <newElement>"));
	}

	@Test
	public void shouldProduceItalicTypeOfWord() throws Exception {
		xslt.transform("src/test/resources/html/typeOfWordItalic.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("der", "//div[@class='type-of-word']/div[@class='italic']", html);
		// example: austag
	}

	@Test
	public void shouldNotAddSpaceBeforeLinkInsideCit() throws Exception {
		xslt.transform("src/test/resources/html/citationWithLink.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo(" [Luther 1545: ", "//div[@class='rect']", html);
		// example: bewegung
	}

	@Test
	public void shouldOutputWbvTwice() throws Exception {
		xslt.transform("src/test/resources/html/definitionWithTwoWbvs.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("2", "count(//div[@class='wbv'])", html);
		assertXpathEvaluatesTo("Wbv 2", "//div[@class='sense']/div[@class='wbv']", html);
		// example: bauchstränge femininum
	}

	@Test
	public void shouldOutputWbvOnlyOnce() throws Exception {
		xslt.transform("src/test/resources/html/complexDefWithWbvInside.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("1", "count(//div[@class='wbv'])", html);
		// example: ausrichtig
	}

	@Test
	public void shouldTransformInfoList() throws Exception {
		xslt.transform("src/test/resources/html/infoList.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("Beispiele: ", "//div[@class='meta']", html);
		assertXpathEvaluatesTo("wbg bla,", "//ul[@class='info-list']//li[1]", html);
		assertXpathEvaluatesTo("wbg2 bla2", "//ul[@class='info-list']//li[2]", html);
		// example: gut adjektiv
	}

	@Test
	public void shouldPreserveSpaceBetweenCitationLinks() throws Exception {
		xslt.transform("src/test/resources/html/spaceAfterCitationLink.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("1, 122; Dwb", "//div[@class='article-head']", html);
	}

	@Test
	public void shouldMakeParagraphOutOfBlsWithoutCit() throws Exception {
		xslt.transform("src/test/resources/html/blsWithoutCit.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("No cit here.", "//p", html);
	}

	@Test
	public void shouldPutAllCitationsAndSubvoceInSection() throws Exception {
		xslt.transform("src/test/resources/html/citationAndBlsAndBblock.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("1", "count(//section[@class='citations-block'])", html);
		assertXpathEvaluatesTo("2", "count(//div[@class='citations-subblock'])", html);
	}

	@Test
	public void shouldPrintNumberForFirstDefinitionOnly() throws Exception {
		xslt.transform("src/test/resources/html/twoDefinitions.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("1", "count(//div[@class='sense-number'])", html);
	}

	@Test
	public void shouldMakeHeaderForBlsInFront() throws Exception {
		xslt.transform("src/test/resources/html/citationAndBlsInFront.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("Belegblock: ", "//h1", html);
		assertXpathEvaluatesTo("1", "count(//h1)", html);
	}

	@Test
	public void shouldMakeHeaderForOneBls() throws Exception {
		xslt.transform("src/test/resources/html/bls.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("Belegblock: ", "//h1", html);
	}

	@Test
	public void shouldMakeHeaderForOneCitation() throws Exception {
		xslt.transform("src/test/resources/html/cite.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("Belegblock: ", "//h1", html);
	}

	@Test
	public void shouldPutTogetherCitationsAndBlsWithBblocks() throws Exception {
		xslt.transform("src/test/resources/html/citationAndBlsAndBblock.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("Belegblock: ", "//h1", html);
		assertXpathEvaluatesTo("1", "count(//h1)", html);
	}

	@Test
	public void shouldPutTogetherCitationsAndBls() throws Exception {
		xslt.transform("src/test/resources/html/citationAndBls.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("Belegblock: ", "//h1", html);
	}

	@Test
	public void shouldConvertListItems() throws Exception {
		xslt.transform("src/test/resources/html/wbgListItems.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("item1", "//ul/li[1]/div[@class='highlight-boundary']", html);
		assertXpathEvaluatesTo("item2", "//ul/li[2]/div[@class='highlight-boundary']", html);
	}

	@Test
	public void shouldConvertRomanNumber() throws Exception {
		xslt.transform("src/test/resources/html/romanNumber.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo(" I", "//div[@class='roman-number']", html);
	}

	@Test
	public void shouldProcessComplexDefinitions() throws Exception {
		xslt.transform("src/test/resources/html/complexDefinitions.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("Zu a): ", "//h2", html);
	}

	@Test
	public void shouldTakeCorrectSenseRanges() throws Exception {
		xslt.transform("src/test/resources/html/senseWithRange.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathExists("//div[@class='sense'][1]/span[@id='sense1']", html);
		assertXpathExists("//div[@class='sense'][1]/span[@id='sense2']", html);
		assertXpathExists("//div[@class='sense'][1]/span[@id='sense3']", html);
		assertXpathExists("//div[@class='sense'][1]/span[@id='sense4']", html);
		assertXpathExists("//div[@class='sense'][2]/span[@id='sense5']", html);
		assertXpathEvaluatesTo("1.; 2.; 3.; 4., ", "//div[span/@id='sense1']//div[@class='sense-number']", html);
		assertXpathEvaluatesTo("5. ", "//div[span/@id='sense5']//div[@class='sense-number']", html);
		// example: auszug
	}

	@Test
	public void shouldNotAddSpaceAfterParenthesis() throws Exception {
		xslt.transform("src/test/resources/html/spaces_articleHead.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("bla (Lexer)", "//div[@class='article-head']", html);
	}

	@Test
	public void shouldIgnoreEmptyRegion() throws Exception {
		xslt.transform("src/test/resources/html/emptyRegion.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("0", "count(//div[@class='region'])", html);
	}

	@Test
	public void shouldMakeHtmlField() throws Exception {
		xslt.transform("src/test/resources/html/articleField.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathExists("//field[@name='artikel']", result);
	}

	@Test
	public void shouldMakeEmptyArticle() throws Exception {
		xslt.transform("src/test/resources/html/articleField.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathExists("/div[@class='article']", html);
	}

	@Test
	public void shouldInsertLemma() throws Exception {
		xslt.transform("src/test/resources/html/lemma.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("testlemma", "//div[@class='lemma']", html);
	}

	@Test
	public void shouldInsertArticleHead() throws Exception {
		xslt.transform("src/test/resources/html/articleHead.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("article head", "//div[@class='article-head']", html);
	}

	@Test
	public void shouldInsertNeblem() throws Exception {
		xslt.transform("src/test/resources/html/neblem.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("2", "//div[@class='higher-and-smaller']", html);
		assertXpathEvaluatesTo("2neblem1, neblem2,", "//div[@class='neblem']", html);
	}

	@Test
	public void shouldInsertPhras() throws Exception {
		xslt.transform("src/test/resources/html/phras.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathExists("//div[@class='phras']", html);
		assertXpathExists("//div[@class='phras-begin']", html);
	}

	@Test
	public void shouldInsertGgs() throws Exception {
		xslt.transform("src/test/resources/html/ggs.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathExists("//div[@class='ggs']", html);
		assertXpathExists("//div[@class='ggs-begin']", html);
		assertXpathEvaluatesTo("", "//span[@class='unknown-element']", html);
		assertXpathEvaluatesTo("2", "count(//div[@class='highlight-boundary'])", html);
	}

	@Test
	public void shouldInsertRa() throws Exception {
		xslt.transform("src/test/resources/html/ra.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathExists("//div[@class='redensart']", html);
		assertXpathExists("//div[@class='redensart-begin']", html);
	}

	@Test
	public void shouldInsertHighlightings() throws Exception {
		xslt.transform("src/test/resources/html/highlightings.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("italic", "//div[@class='italic']", html);
		assertXpathEvaluatesTo("hoch", "//div[@class='higher-and-smaller']", html);
		assertXpathEvaluatesTo("tief", "//div[@class='deep']", html);
		assertXpathEvaluatesTo("rect", "//div[@class='rect']", html);
		assertXpathEvaluatesTo("sc", "//div[@class='small-capitals']", html);
		assertXpathEvaluatesTo("bold", "//strong", html);
		assertXpathEvaluatesTo("wide", "//div[@class='wide']", html);
	}

	@Test
	public void shouldTransformLinebreak() throws Exception {
		xslt.transform("src/test/resources/html/linebreak.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo(" | ", "//div[@class='article-head']", html);
	}

	@Test
	public void shouldTransformGrammar() throws Exception {
		xslt.transform("src/test/resources/html/grammar.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("Art", "//div[@class='type-of-word']", html);
		assertXpathEvaluatesTo("", "//div[@class='type-of-word']/div[@class='italic']", html);
		assertXpathEvaluatesTo("-Ø", "//div[@class='flex']", html);
	}

	@Test
	public void shouldTransformReference() throws Exception {
		xslt.transform("src/test/resources/html/reference.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("reference", "//a/@href", html);
		assertXpathEvaluatesTo("click here", "//a", html);
	}

	@Test
	public void shouldInsertSenseWithDefinition() throws Exception {
		xslt.transform("src/test/resources/html/sense.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathExists("//div[@class='sense']", html);
		assertXpathExists("//span[@id='sense1']", html);
		assertXpathEvaluatesTo("my definition", "//div[@class='definition']", html);
	}

	@Test
	public void shouldIgnoreSemanticsOfSenseWithBedzif() throws Exception {
		xslt.transform("src/test/resources/html/senseWithBedzif.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("", "//div[@class='sense']", html);
	}

	@Test
	public void shouldInsertNumbersIfSeveralSenses() throws Exception {
		xslt.transform("src/test/resources/html/twoSenses.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathExists("//div[@class='sense']", html);
		assertXpathExists("//div[@class='sense-number' and text()='1. ']", html);
		assertXpathExists("//div[@class='sense-number' and text()='2. ']", html);
	}

	@Test
	public void shouldInsertSenseWithWbv() throws Exception {
		xslt.transform("src/test/resources/html/senseWithWbv.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("Wortbildungsverweis", "//div[@class='definition']/div[@class='wbv']", html);
	}

	@Test
	public void shouldInsertStw() throws Exception {
		xslt.transform("src/test/resources/html/stw.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("Stw", "//div[@class='stw']", html);
	}

	@Test
	public void shouldInsertAcronyms() throws Exception {
		xslt.transform("src/test/resources/html/withBeginnings.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathExists("//div[@class='bdv']", html);
		assertXpathExists("//div[@class='bdv-begin']", html);
		assertXpathExists("//div[@class='synt']", html);
		assertXpathExists("//div[@class='synt-begin']", html);
		assertXpathExists("//div[@class='wbg']", html);
		assertXpathExists("//div[@class='wbg-begin']", html);
		assertXpathExists("//div[@class='dict-ref']", html);
		assertXpathExists("//div[@class='subvoce']", html);
		assertXpathExists("//div[@class='subvoce-begin']", html);
	}

	@Test
	public void shouldTransformCitation() throws Exception {
		xslt.transform("src/test/resources/html/cite.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathExists("//div[@class='citations']", html);
		assertXpathExists("//h1", html);
		assertXpathExists("//div[@class='citation']", html);
		assertXpathEvaluatesTo("Name", "//a[@class='name citation-source_link']", html);
		assertXpathEvaluatesTo("13, 20 ", "//div[@class='cited-range']", html);
		assertXpathEvaluatesTo("Region", "//div[@class='region']", html);
		assertXpathEvaluatesTo("1599", "//div[@class='date']", html);
		assertXpathExists("//div[@class='quote' and @id='quote1']", html);
		assertXpathEvaluatesTo("Miller", "//a[@href='/source/source_xyu']", html);
	}

	@Test
	public void shouldMakeHtmlQuote() throws Exception {
		xslt.transform("src/test/resources/html/cite.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathExists("//div[@class='quote' and @id='quote1']", html);
		assertXpathEvaluatesTo("Miller", "//a[@href='/source/source_xyu']", html);
	}

	@Test
	public void shouldMakeLinkInDefinition() throws Exception {
		xslt.transform("src/test/resources/html/definitionWithName.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("Meier", "//a[@class='name citation-source_link']", html);
	}

	@Test
	public void shouldTransformBls() throws Exception {
		xslt.transform("src/test/resources/html/bls.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathExists("//div[@class='bls']", html);
		assertXpathExists("//div[@class='citation']", html);
	}

	@Test
	public void shouldCreateAnchors() throws Exception {
		xslt.transform("src/test/resources/html/anchors.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("sense1", "//div[@class='sense']/span/@id", html);
		assertXpathEvaluatesTo("mylemma#sense2", "//a[1]/@href", html);
		assertXpathEvaluatesTo("#sense12", "//a[2]/@href", html);
	}

	@Test
	public void shouldDecideIfLinkIsItalic() throws Exception {
		xslt.transform("src/test/resources/html/refsItalicOrNot.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("lemma", "//div[@class='italic']/a", html);
		assertXpathEvaluatesTo("1", "//div[@class='article-head']/a", html);
	}

	@Test
	public void shouldCreateHomonymForLemma() throws Exception {
		xslt.transform("src/test/resources/homonym.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("2", "//div[@class='homonym']", html);
	}

	@Test
	public void shouldNotCreateHomonymForLemma() throws Exception {
		xslt.transform("src/test/resources/html/withoutHomonym.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("", "//div[@class='homonym']", html);
	}

	private String extractHtmlField(String s) {
		Pattern pattern = Pattern.compile("artikel\"><!\\[CDATA\\[(.*?)]]");
		Matcher matcher = pattern.matcher(s.replaceAll("\\n", " "));
		String html = "";
		if (matcher.find()) {
			html = matcher.group(1);
		}
		return html;
	}

}
