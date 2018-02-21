package docs;

import static docs.ConvenienceMethods.*;

import sub.ent.api.ImporterStepUpload;
import sub.ent.backend.BeanRetriever;
import sub.ent.backend.FileAccess;
import sub.fwb.SolrTester;
import sub.fwb.SourcesParser;
import sub.fwb.TeiHtmlComparator;
import sub.fwb.WordTypesGenerator;
import sub.fwb.Xslt;
import sub.fwb.api.ImporterStepCompare;
import sub.fwb.api.ImporterStepConvert;
import sub.fwb.api.ImporterStepRunTests;

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
		 The output directory will be used to store the resulting Solr XML files.
		 
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

	void comparingStep() {
		/*
		 
		 This importer step is executed directly after the conversion.
		 Basically, it checks if the conversion had been successful.
		 Its implementation is in */ the(ImporterStepCompare.class); /*.
		 The idea is to assert that the generated Solr XML files contain the same text as the 
		 original TEI files.
		 If for example one word is missing in the output file, it means that the XSLT script
		 probably does not have a template for an element.
		 
		 At first, all the TEI file names are read using an */ objectOf(FileAccess.class); /*.
		 For each one of them, the corresponding Solr XML file is identified.
		 Both files are then given to an */ objectOf(TeiHtmlComparator.class); /*.
		 That object extracts the relevant text both from the TEI file and from the Solr XML file.
		 In the Solr XML file, the relevant part is inside the HTML article field.
		 Some adaptations are made to both text versions to make them comparable.
		 For example, it turned out to be hard to compare the whitespace characters,
		 so they are completely removed.
		 In the end, the two texts are compared using a JUnit method.
		 
		 */
	}

	void solrTestingStep() {
		/*
		 
		 After the actual import into Solr, which is implemented in */ the(ImporterStepUpload.class); /*,
		 it is useful to make sure that the import really worked.
		 For this purpose, there is */ the(ImporterStepRunTests.class); /*.
		 It gets the URL of a running Solr server and the core, and executes several Solr queries.
		 The queries are defined in */ the(SolrTester.class); /* which is actually a JUnit test class.
		 There, the responses from the Solr server are compared to the expected values.
		 
		 */
	}

}
