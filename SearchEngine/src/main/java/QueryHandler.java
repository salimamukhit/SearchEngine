import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * 
 * @author salimamukhit
 *
 * The main class for handling queries. Parses file that contains queries, searches the inverted index and outputs
 * the search result. 
 */
public class QueryHandler implements QueryHandlerInterface {
	
	/** A main data structure that contains query results */
	private final TreeMap<String, List<InvertedIndex.QueryResult>> allResults;
	
	/** An InvertedIndex structure needed for search */
	private final InvertedIndex index;
	
	/**
	 * Constructor
	 * @param index an InvertedIndex where search is to be performed
	 */
	public QueryHandler(InvertedIndex index) {
		this.allResults = new TreeMap<>();
		this.index = index;
	}
	 
	@Override
	public void parseQuery(String line, boolean exact) {
		Stemmer stemmer = new SnowballStemmer(TextFileStemmer.DEFAULT);
		TreeSet<String> query = TextFileStemmer.uniqueStems(line, stemmer);
		String joined = String.join(" ", query);
		if(!query.isEmpty() && !allResults.containsKey(joined)) {
			allResults.put(joined, index.search(query, exact));
		}
	}
	
	@Override
	public void outputResults(Path outputFile) throws IOException {
		try (
				BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8);
				)
		{
			SimpleJsonWriter.asQueryResults(allResults, writer, 0);
		}
	}
}