import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Inverted Index structure that essentially preserves a structure of TreeMap
 * Where key is an item
 * And value is a TreeMap where key is path and value is the locations of this item in a path
 * 
 * @author salimamukhit
 */
public class InvertedIndex {
	/**
	 * Main Data Structure
	 * Template:
	 * 	item: {
	 * 		location: [positions]
	 * 	}
	 */
	private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> index;

	/**
	 * A Tree Map structure that stores all paths and their respective total word counts.
	 * Structure:
	 * { Path : totalWordCount }
	 */
	private final TreeMap<String, Integer> totalWordCounts;
	
	/**
	 * Constructor
	 */
	public InvertedIndex() {
		index = new TreeMap<>();
		totalWordCounts = new TreeMap<>();
	}

	/**
	 * @brief Adds an inverted index item with all necessary fields to the tree map
	 * @param item to add to the data structure
	 * @param location path to the word
	 * @param position of the word in a file
	 */
	public void addItem(String item, String location, int position) {
		index.putIfAbsent(item, new TreeMap<>());
		index.get(item).putIfAbsent(location, new TreeSet<>());
		boolean result = index.get(item).get(location).add(position);
		
		if(result) {
			totalWordCounts.putIfAbsent(location, 0);
			totalWordCounts.put(location, totalWordCounts.get(location) + 1);
		}
	}
	
	/**
	 * @brief Merges two InvertedIndex structures
	 * @param mergeIndex an InvertedIndex to merge
	 */
	public void addAll(InvertedIndex mergeIndex) {
		for(String item : mergeIndex.index.keySet()) {
			if(!this.index.containsKey(item)) {
				this.index.put(item, mergeIndex.index.get(item));
			} else {
				for(String path : mergeIndex.index.get(item).keySet()) {
					if(!this.index.get(item).containsKey(path)) {
						this.index.get(item).put(path, mergeIndex.index.get(item).get(path));
					} else {
						this.index.get(item).get(path).addAll(mergeIndex.index.get(item).get(path));
					}
				}
			}
		}
		
		for(String path : mergeIndex.totalWordCounts.keySet()) {
			if(!this.totalWordCounts.containsKey(path)) {
				this.totalWordCounts.put(path, mergeIndex.totalWordCounts.get(path));
			} else {
				this.totalWordCounts.put(path, mergeIndex.totalWordCounts.get(path) + this.totalWordCounts.get(path));
			}
		}
	}

	/**
	 * @brief Gets total word count of a path
	 * @param path a needed path
	 * @return word count in that path
	 */
	public int getWordCount(String path) {
		return totalWordCounts.get(path);
	}
	
	/**
	 * @brief Gets all items that are present in Inverted Index structure
	 * @return the set of all items
	 */
	public Collection<String> getAllItems() {
		return Collections.unmodifiableSet(index.keySet());
	}
	
	/**
	 * @brief Returns all paths where an item appears
	 * @param item and item which paths are needed to be accessed
	 * @return all paths where item appears in TreeSet data structure
	 */
	public Collection<String> getItemPaths(String item) {
		if(hasItem(item)) {
			return Collections.unmodifiableSet(index.get(item).keySet());
		}
		return Collections.emptySet();
	}
	
	/**
	 * @brief Returns all locations of the item in a path
	 * @param item an item to access
	 * @param path a path to access
	 * @return the tree set of all positions
	 */
	public Collection<Integer> getItemPositions(String item, String path) {
		if(hasPath(item, path)) {
			return Collections.unmodifiableSet(index.get(item).get(path));
		}
		return Collections.emptySet();
	}
	
	/**
	 * @brief Accesses the number of all appearances of the item in the path
	 * @param item an item which count need to be retrieved
	 * @param path a path where item is being present
	 * @return the number of item appearances in the path
	 */
	public Integer getItemCountsByPath(String item, String path) {
		return hasPath(item, path) ? index.get(item).get(path).size() : 0; 
	}
	
	/**
	 * @brief Checks if the item is present in the data structure
	 * @param item an item that is checked
	 * @return true or false, depending if item is found or not
	 */
	public boolean hasItem(String item) {
		return this.index.containsKey(item);
	}
	
	/**
	 * @brief Checks if an item appears in a certain path
	 * @param item an item to check
	 * @param path a path to check
	 * @return true if item is present false otherwise
	 */
	public boolean hasPath(String item, String path) { 
		return hasItem(item) && index.get(item).containsKey(path);
	}
	
	/**
	 * @brief Checks if the item is present in a certain file on a certain position
	 * @param item an item to check
	 * @param path a path to check
	 * @param pos a position to check
	 * @return true if position is present, false if absent
	 */
	public boolean hasPosition(String item, String path, int pos) {
		if(hasPath(item, path)) {
			return index.get(item).get(path).contains(pos);
		}
		
		return false;
	}
	
	/**
	 * @brief Converts the Inverted Index to JSON format
	 * @return converted Inverted Index
	 */
	public String toJson() {
		return SimpleJsonWriter.asInvertedIndex(this.index);
	}

	/**
	 * @brief outputs JSON formatted InvertedIndex to the output file
	 * @param outputPath the path to output file
	 * @throws IOException if IO error occurs
	 */
	public void toJson(Path outputPath) throws IOException {
		try (
				BufferedWriter bw = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8);
				)
		{
			SimpleJsonWriter.asInvertedIndex(this.index, bw, 0);
		}
	}
	
	/**
	 * @brief Outputs JSON formatted total word counts to the output file
	 * @param outputPath the path to write output
	 * @throws IOException if I/O error occurs
	 */
	public void writeWordCounts(Path outputPath) throws IOException {
		try(
				BufferedWriter bw = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8);
				)
		{
			SimpleJsonWriter.asObject(totalWordCounts, bw, 0);
		}
	}

	@Override
	public String toString() {
		return this.toJson();
	}
	
	/**
	 * @brief Handles search operation, calls exact or partial search methods
	 * @param queries all queries to process
	 * @param exact the flag that indicates the type of search
	 * @return a list of query results
	 */
	public List<QueryResult> search(Set<String> queries, boolean exact) {
		return exact ? exactSearch(queries) : partialSearch(queries);
	}
	
	/**
	 * @brief A helper method that adds results
	 * @param query an item to add
	 * @param results a list of results
	 * @param lookup a map that contains found results
	 */
	private void addResults(String query, List<QueryResult> results, HashMap<String, QueryResult> lookup) {
		for(String path : this.getItemPaths(query)) {
			if(!lookup.containsKey(path)) {
				QueryResult queryResult = new QueryResult(path);
				results.add(queryResult);
				lookup.put(path, queryResult);
			} 
			lookup.get(path).update(query);
		}
	}
	
	/**
	 * @brief Exact search method
	 * @param queries queries to process
	 * @return the list of query results
	 */
	public List<QueryResult> exactSearch(Set<String> queries) {
		List<QueryResult> results = new ArrayList<>();
		HashMap<String, QueryResult> lookup = new HashMap<>();
		
		for(String query : queries) {
			if(this.hasItem(query)) {
				addResults(query, results, lookup);
			}
		}

		Collections.sort(results);
		return results;
	}
	
	/**
	 * @brief Partial search method
	 * @param queries queries to process
	 * @return the list of query results
	 */
	public List<QueryResult> partialSearch(Set<String> queries) {
		List<QueryResult> results = new ArrayList<>();
		HashMap<String, QueryResult> lookup = new HashMap<>();
		
		for(String query : queries) {
			for(String match : index.tailMap(query).keySet()) {
				if(!match.startsWith(query)) break;
				addResults(match, results, lookup);
			}
		}

		Collections.sort(results);
		return results;
	}
	
	/**
	 * 
	 * @author salimamukhit
	 *
	 * A nested object of Query Result that implements Comparable interface.
	 * Mainly consists of getters and setters.
	 * Object structure:
	 * where: location path
	 * count: int word count in the path
	 * score: count / total word count in a path
	 */
	public class QueryResult implements Comparable<QueryResult> {
		/**
		 * Location
		 */
		private final String where;
		/**
		 * Item count
		 */
		private int count;
		/**
		 * Item count / Total word count
		 */
		private double score;
		
		/**
		 * Constructor
		 * @param where the location of the result
		 */
		public QueryResult(String where) {
			this.where = where;
			this.count = 0;
			this.score = 0;
		}
		
		/**
		 * @brief Getter for where
		 * @return location
		 */
		public String getWhere() {
			return this.where;
		}
		
		/**
		 * @brief Getter for count
		 * @return item count
		 */
		public int getCount() {
			return this.count;
		}
		
		/**
		 * @brief Getter for score
		 * @return item core
		 */
		public double getScore() {
			return this.score;
		}
		
		/**
		 * @brief Updates score and count of the query result
		 * @param item an item from inverted index
		 */
		private void update(String item) {
			this.count += index.get(item).get(where).size();
			this.score = (double) this.count / totalWordCounts.get(where);
		}

		/**
		 * Note: for efficiency reasons this compareTo is made that way that if applied 
		 * to a sorting algorithm it will sort a list in descending order
		 */
		@Override
		public int compareTo(QueryResult o) {
			int compare = Double.compare(o.score, this.score);
			
			if(compare == 0) {
				compare = Integer.compare(o.count, this.count);
				
				if(compare == 0) {
					compare = this.where.compareTo(o.where);
				}
			}
			return compare;
		}
	}
}
