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

}
