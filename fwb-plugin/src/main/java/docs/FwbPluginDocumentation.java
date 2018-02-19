package docs;

import static docs.ConvenienceMethods.*;

import sub.ent.backend.BeanRetriever;
import sub.ent.backend.FileAccess;
import sub.fwb.SourcesParser;
import sub.fwb.WordTypesGenerator;
import sub.fwb.Xslt;
import sub.fwb.api.ImporterStepConvert;

public class FwbPluginDocumentation {

	void overview() {
		/*
		 
		 The code of this plug-in must be part of the bigger solr-importer project.
		 Consult the */ documentationIn(CoreModuleDocumentation.class); /* for more information.
		 
		 In general, the plug-in contains so-called importer steps and some helper classes.
		 The names and the order of the importer steps are defined in the context file of 
		 the Dependency Injection container. 
		 The file name is defined in the */ constant(BeanRetriever.DI_FILE_IN_PLUGIN); /*.
		 
		 Some importer steps listed in the file come from that other project.
		 The ones that are new here will be described in this documentation.
		 
		 */
	}

	void conversionStep() {
		/*
		 
		 The main purpose of this plug-in is it to convert some tens of thousands specifically
		 formatted TEI files to Solr XML index files. 
		 The entry point to this functionality is */ the(ImporterStepConvert.class); /*.
		 When it is executed, it gets two parameters, namely the input and the output directories.
		 The input directory must contain the TEI files and an Excel file.
		 That Excel file contains entries for book sources that are used in the TEI articles.
		 The output directory will be used to write the resulting Solr XML files.
		 
		 First, an */ objectOf(SourcesParser.class); /* converts the Excel file to a Solr XML file.
		 Here, some simple Solr string search fields are created, as well as some HTML snippets.
		 Those snippets are pre-generated here to be used as is in the front end.
		 
		 Next, an */ objectOf(Xslt.class); /* is prepared for the execution of an XSLT transformation.
		 It gets the XSLT script to use which contains the complete logic for the 
		 transformation of one TEI file to one Solr XML file.
		 The XSLT script itself also must get some parameters.
		 An */ objectOf(WordTypesGenerator.class); /* is used to generate them.
		 That object reads some text files containing coded mappings for word types (like 'Verb')
		 and prepares them for the XSLT script.
		 
		 Finally, all TEI files are read by an */ objectOf(FileAccess.class); /*.
		 They are fed one by one to the XSLT script.
		 The */ objectOf(Xslt.class); /* then writes each resulting file to the output directory.
		 
		 */
	}

	void xsltScript() {
		/*
		 
		 The XSLT script is probably the most important and also the most complex part of the FWB plug-in.
		 It navigates through a TEI file and generates XML elements for a Solr XML file.
		 The TEI files contain articles of the FWB dictionary.
		 Each article is composed of many different parts that together explain all the meanings of a word.
		 You can refer to XSLT test classes in 'src/test/java' that reference
		 such files and define unit tests.
		 
		 The general idea of the script is to produce Solr index files that contain all the necessary data
		 to be able to find and present the FWB dictionary articles.
		 
		 
		 HTML article.
		 
		 First of all, there is a huge field that contains the whole article inside of HTML elements.
		 It is not a complete HTML page, but rather a big <div> element that can be pasted into an HTML page.
		 For each specific TEI element, there is an XSLT template that produces the corresponding
		 HTML element, which in turn becomes part of the big <div> element.
		 Those part elements are mostly <div>'s with 'class' attributes.
		 All the formatting happens in the front end and is not generated here.
		 The HTML article can be used both for searching and for displaying.
		 
		 
		 Text article.
		 
		 One other Solr field also contains the whole article, but this time only the text, 
		 i. e. without any HTML tags.
		 The convention here is that the names of such fields end in '_text'.
		 This version of the article is used to generate text snippets which are shown as a preview
		 when the user sees the list of all the found results.
		 The field is only necessary, because Solr cannot generate text snippets of similar lengths
		 from the field that contains HTML tags.
		 
		 
		 HTML and text search fields.
		 
		 In addition to fields with the whole article, there are fields that contain
		 little parts of the article.
		 These fields hold individual words or sentences for which the user can search in the advanced search.
		 For example, in the TEI files there are parts marked as 'phrases'.
		 For each such phrase, the XSLT script creates two fields.
		 The first one is called 'phrase' and contains the phrase inside of HTML tags, analogously 
		 to the above HTML article.
		 In fact, the template for this field reuses the HTML template described above,
		 so that it is an exact copy of the corresponding part in the article.
		 This makes it possible to search only inside this one field and also to highlight only the 
		 corresponding part inside the whole article, i. e. without highlighting the rest of the article.
		 The trick is to highlight in the individual field and then replace the unhighlighted part 
		 in the article HTML field.
		 The second field is called 'phras_text' and again contains only the text for the generation
		 of preview snippets.
		 
		 */
	}
}
