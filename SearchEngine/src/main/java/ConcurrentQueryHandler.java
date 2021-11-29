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
 * A QueryHandler class that supports multi-threading
 */
public class ConcurrentQueryHandler implements QueryHandlerInterface {
	
	/** An inverted index data structure that supports multithreading */
	private final ConcurrentInvertedIndex index;
	
	/** A work queue object */
	private final WorkQueue queue;
	
	/** A main data structure that contains query results */
	private final TreeMap<String, List<InvertedIndex.QueryResult>> allResults;
	
	/**
	 * Constructor
	 * @param index a ConcurrentInvertedIndex structure used for searching
	 * @param queue the work queue
	 */
	public ConcurrentQueryHandler(ConcurrentInvertedIndex index, WorkQueue queue) {
		this.allResults = new TreeMap<>();
		this.index = index;
		this.queue = queue;
	}
	
	@Override
	public void parseQuery(String line, boolean exact) {
		queue.execute(new QueryTask(line, exact));
	}
	
	@Override
	public void performSearch(boolean exact, Path queryPath) throws IOException {
		QueryHandlerInterface.super.performSearch(exact, queryPath);
		queue.finish();
	}
	
	/**
	 * 
	 * @author salimamukhit
	 *
	 * A Runnable instance of QueryHandler object that populates results tree map
	 */
	public class QueryTask implements Runnable {
		
		/** The flag that indicates the type of search */
		private final boolean exact;
		
		/** The line to parse */
		private final String line;
		
		/**
		 * Constructor
		 * @param line a line to parse
		 * @param exact the flag that indicates the type of search
		 */
		public QueryTask(String line, boolean exact) {
			this.line = line;
			this.exact = exact;
		}
		
		@Override
		public void run() {
			Stemmer stemmer = new SnowballStemmer(TextFileStemmer.DEFAULT);
			TreeSet<String> query = TextFileStemmer.uniqueStems(line, stemmer);
			String joined = String.join(" ", query);
			synchronized(allResults) {
				if(query.isEmpty() || allResults.containsKey(joined)) {
					return;
				}
			}
			List<InvertedIndex.QueryResult> results = index.search(query, exact);
			synchronized(allResults) {
				allResults.put(joined, results);
			}
		}
	}

	@Override
	public void outputResults(Path outputFile) throws IOException {
		try (
				BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8);
				)
		{
			synchronized(allResults) {
				SimpleJsonWriter.asQueryResults(allResults, writer, 0);
			}
		}
	}
	
	public List<InvertedIndex.QueryResult> searchEngineSearch(String line, boolean exact) {
		// Don't redo the search if result already exists.
		synchronized(allResults) {
			if(this.allResults.containsKey(line)) {
				return allResults.get(line);
			}
		}
		
		// Perform the search.
		queue.execute(new QueryTask(line, exact));
		queue.finish();
		
		// Return the results.
		synchronized(allResults) {
			return allResults.get(line);
		}
		
	}
}