package sub.fwb;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.CoreContainer;
import org.noggit.JSONUtil;

import sub.ent.backend.SolrAccess;

/**
 * In-between class for all interactions with a Solr server while testing.
 *
 */
public class SolrAccessForTesting extends SolrAccess {

	private String solrQueryString = "";
	private SolrDocumentList docList;
	private Map<String, Map<String, List<String>>> highlightings;
	private List<String> suggestions;

	public void initializeEmbedded(String coreName) {
		CoreContainer container = new CoreContainer("solr-embedded");
		container.load();
		solr = new EmbeddedSolrServer(container, coreName);
		core = coreName;
	}
	
	public void list(String userInputs) {
		ask(new String[][] {}, userInputs, "/list");
	}

	public void select(String query) {
		ask(new String[][] {}, query, "/select");
	}

	public void select(String[][] extraParams, String query) {
		ask(extraParams, query, "/select");
	}

	public void search(String query) {
		ask(new String[][] {}, query, "/search");
	}

	public void articleHl(String[][] extraParams, String query) {
		ask(extraParams, query, "/article-hl");
	}

	public void suggest(String query) {
		ask(new String[][] {}, query, "/suggest");
	}

	private void ask(String[][] extraParams, String query, String requestHandler) {
		solrQueryString = query;
		SolrQuery solrQuery = new SolrQuery(query);
		solrQuery.setRequestHandler(requestHandler);
		solrQuery.set("rows", "500");
		for (String[] parameter : extraParams) {
			solrQuery.set(parameter[0], parameter[1]);
		}
		QueryResponse response;
		try {
			response = solr.query(core, solrQuery);
		} catch (Exception e) {
			throw new RuntimeException("Could not execute '" + query + "'", e);
		}

		docList = response.getResults();
		highlightings = response.getHighlighting();
		if (response.getSuggesterResponse() != null) {
			suggestions = response.getSuggesterResponse().getSuggestedTerms().get("suggestdictionary");
		}
	}

	public String lemma(int resultNumber) {
		return (String) docList.get(resultNumber - 1).getFieldValue("lemma");
	}

	public String id(int resultNumber) {
		return (String) docList.get(resultNumber - 1).getFieldValue("id");
	}

	public Map<String, Map<String, List<String>>> getHighlightings() {
		if (highlightings == null) {
			highlightings = new HashMap<>();
		}
		return highlightings;
	}

	public String jsonHighlights() {
		return JSONUtil.toJSON(highlightings);
	}

	public long results() {
		return docList.getNumFound();
	}

	public String suggestion(int number) {
		return suggestions.get(number - 1);
	}

	public int askForNumberOfLemmas(String wordPart) {
		SolrAccessForTesting tempSolr = new SolrAccessForTesting();
		tempSolr.initialize(url, core);
		tempSolr.setCredentials(solrUser, solrPassword);
		tempSolr.select("lemma:*" + wordPart + "*");

		return (int) tempSolr.results();
	}

	public void printResults() {
		System.out.println();
		System.out.println(solrQueryString);
		if (docList != null) {
			System.out.println(docList.getNumFound() + " results");
			for (int i = 0; i < 4; i++) {
				if (i < docList.size()) {
					SolrDocument doc = docList.get(i);
					System.out.println(doc.getFieldValue("lemma") + "\t" + doc.getFieldValue("score"));
				}
			}
		}
		if (highlightings != null && !highlightings.isEmpty()) {
			System.out.println("hl:");
			System.out.println(jsonHighlights());
		}
		if (suggestions != null) {
			System.out.println("suggest: " + suggestions);
		}
		// System.out.println(JSONUtil.toJSON(docList));
	}

	public void printQueryString() {
		System.out.println("    " + solrQueryString);
	}

	public void addDocumentFromArray(String[][] documentFields) throws Exception {
		SolrInputDocument newDoc = new SolrInputDocument();
		for (String[] docField : documentFields) {
			newDoc.addField(docField[0], docField[1]);
		}
		if (!newDoc.containsKey("id")) {
			newDoc.addField("id", "1234");
		}
		if (!newDoc.containsKey("type")) {
			newDoc.addField("type", "artikel");
		}
		if (!newDoc.containsKey("lemma")) {
			newDoc.addField("lemma", "mylemma");
		}
		solr.add(core, newDoc);
		solr.commit(core);
	}

	public void cleanAndCommit() throws Exception {
		solr.deleteByQuery(core, "*:*");
		solr.commit(core);
	}

	public void close() throws IOException {
		solr.close();
	}

}
