import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Outputs several simple data structures in "pretty" JSON format where newlines
 * are used to separate elements and nested elements are indented using tabs.
 *
 * Warning: This class is not thread-safe. If multiple threads access this class
 * concurrently, access must be synchronized externally.
 *
 * @author CS 212 Software Development
 * @author University of San Francisco
 * @version Fall 2020
 */
public class SimpleJsonWriter {
	/**
	 * @brief Writes the elements as a pretty JSON array.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param level the initial indent level
	 * @throws IOException if an I/O error occurs
	 */
	public static void asArray(Collection<Integer> elements, Writer writer, int level) throws IOException {
		writer.append("[\n");
		Integer curr = null;
		Iterator<Integer> iter = elements.iterator();
		if(iter.hasNext()) {
			curr = iter.next();
			indent(curr, writer, level + 1);
		}
		while(iter.hasNext()) {
			writer.append(",\n");
			curr = iter.next();
			indent(curr, writer, level + 1);
		}
		if(curr != null) writer.append("\n");
		indent(writer, level);
		writer.append("]");
	}
	
	/**
	 * @brief Writes the elements as a pretty JSON object.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param level the initial indent level
	 * @throws IOException if an I/O error occurs
	 */
	public static void asObject(Map<String, Integer> elements, Writer writer, int level) throws IOException {
		writer.append("{\n");
		Iterator<Map.Entry<String, Integer>> iter = elements.entrySet().iterator();
		Map.Entry<String, Integer> curr = null;
		if(iter.hasNext()) {
			curr = iter.next();
			indent(curr.getKey(), writer, level + 1);
			writer.append(": " + curr.getValue().toString());
		}
		while(iter.hasNext()) {
			writer.append(",\n");
			curr = iter.next();
			indent(curr.getKey(), writer, level + 1);
			writer.append(": " + curr.getValue().toString());
		}
		if(curr != null) writer.append("\n");
		indent(writer, level);
		writer.append("}");
	}

	/**
	 * @brief Writes the elements as a pretty JSON object with a nested array. The
	 * generic notation used allows this method to be used for any type of map
	 * with any type of nested collection of integer objects. 
	 * This version of asNestedArray was designed specifically for asInvertedIndex so its indentation is adjusted
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param level the initial indent level
	 * @throws IOException if an I/O error occurs
	 */
	public static void asNestedArray(Map<String, ? extends Collection<Integer>> elements, Writer writer, int level)
			throws IOException {
		writer.append("{\n");
		Iterator<String> iter = elements.keySet().iterator();
		String curr = null;
		if(iter.hasNext()) {
			curr = iter.next();
			indent(curr, writer, level + 1);
			writer.append(": ");
			asArray(elements.get(curr), writer, level + 1);
		}
		while(iter.hasNext()) {
			writer.append(",\n");
			curr = iter.next();
			indent(curr, writer, level + 1);
			writer.append(": ");
			asArray(elements.get(curr), writer, level + 1);
		}
		indent(writer, level);
		if(curr != null) writer.append("\n");
		writer.append("}");
	}

/**
 * @brief Outputs Inverted Index structure in JSON format
 * @param elements an InvertedIndex structure
 * @param writer a writer to write
 * @param level indentation level
 * @throws IOException if an I/O error occurs
 */
	public static void asInvertedIndex(TreeMap<String, TreeMap<String, TreeSet<Integer>>> elements, Writer writer, int level) throws IOException {
		indent(writer, level);
		writer.append("{\n");
		Iterator<String> iter = elements.keySet().iterator();
		String curr = null;
		if(iter.hasNext()) {
			curr = iter.next();
			indent(curr, writer, level + 1);
			writer.append(": ");
			asNestedArray(elements.get(curr), writer, level + 1);
		}
		while(iter.hasNext()) {
			writer.append(",\n");
			curr = iter.next();
			indent(curr, writer, level + 1);
			writer.append(": ");
			asNestedArray(elements.get(curr), writer, level + 1);
		}
		if(curr != null) writer.append("\n");
		indent(writer, level);
		writer.append("}");
	}
	
	/**
	 * @brief Outputs queryResults in a pretty JSON format
	 * @param allResults query results structure
	 * @param writer a writer
	 * @param level an indentation level
	 * @throws IOException if I/O error occurs
	 */
	public static void asQueryResults(TreeMap<String, List<InvertedIndex.QueryResult>> allResults, Writer writer, int level) 
			throws IOException {
		indent(writer, level);
		writer.append("{\n");
		Iterator<String> iter = allResults.keySet().iterator();
		String curr = null;
		if(iter.hasNext()) {
			curr = iter.next();
			indent(curr, writer, level + 1);
			writer.append(": ");
			asQueriesArray(allResults.get(curr), writer, level + 1);
		}
		while(iter.hasNext()) {
			writer.append(",\n");
			curr = iter.next();
			indent(curr, writer, level + 1);
			writer.append(": ");
			asQueriesArray(allResults.get(curr), writer, level + 1);
		}
		if(curr != null) writer.append("\n");
		indent(writer, level);
		writer.append("}");
	}
	
	/**
	 * @brief A helper function to output Array List of Query Result in a pretty JSON format
	 * @param list an Array List of Query Results
	 * @param writer a writer
	 * @param level an indentation level
	 * @throws IOException if I/O error occurs
	 */
	public static void asQueriesArray(List<InvertedIndex.QueryResult> list, Writer writer, int level) 
			throws IOException {
		writer.append("[\n");
		Iterator<InvertedIndex.QueryResult> iter = list.iterator();
		InvertedIndex.QueryResult curr = null;
		if(iter.hasNext()) {
			curr = iter.next();
			asQueryResult(curr, writer, level + 1);
		}
		while(iter.hasNext()) {
			writer.append(",\n");
			curr = iter.next();
			asQueryResult(curr, writer, level + 1);
		}
		if(curr != null) writer.append("\n");
		indent(writer, level);
		writer.append("]");
	}
	
	/**
	 * @brief A helper method which writes a query result in JSON format
	 * @param element an element of type Query Result to format
	 * @param writer a writer to output the result
	 * @param level an indent level
	 * @throws IOException if I/O error occurs
	 */
	public static void asQueryResult(InvertedIndex.QueryResult element, Writer writer, int level) throws IOException {
		indent(writer, level);
		writer.append("{\n");
		indent("where", writer, level + 1);
		writer.append(": ");
		indent(element.getWhere(), writer, 0);
		writer.append(",\n");
		indent("count", writer, level + 1);
		writer.append(": ");
		indent(element.getCount(), writer, 0);
		writer.append(",\n");
		indent("score", writer, level + 1);
		writer.append(": ");
		writer.append(String.format("%.8f", element.getScore()));
		writer.append("\n");
		indent(writer, level);
		writer.append("}");
	}

	/**
	 * @brief Indents using a tab character by the number of times specified.
	 *
	 * @param writer the writer to use
	 * @param times the number of times to write a tab symbol
	 * @throws IOException if an I/O error occurs
	 */
	public static void indent(Writer writer, int times) throws IOException {
		for (int i = 0; i < times; i++) {
			writer.write('\t');
		}
	}

	/**
	 * @brief Indents and then writes the integer element.
	 *
	 * @param element the element to write
	 * @param writer the writer to use
	 * @param times the number of times to indent
	 * @throws IOException if an I/O error occurs
	 */
	public static void indent(Integer element, Writer writer, int times) throws IOException {
		indent(writer, times);
		writer.write(element.toString());
	}

	/**
	 * @brief Indents and then writes the text element surrounded by {@code " "}
	 * quotation marks.
	 *
	 * @param element the element to write
	 * @param writer the writer to use
	 * @param times the number of times to indent
	 * @throws IOException if an I/O error occurs
	 */
	public static void indent(String element, Writer writer, int times) throws IOException {
		indent(writer, times);
		writer.write('"');
		writer.write(element);
		writer.write('"');
	}

	/**
	 * @brief Returns the elements as a pretty JSON array.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 */
	public static String asArray(Collection<Integer> elements) {
		try {
			StringWriter writer = new StringWriter();
			asArray(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}

	/**
	 * @brief Returns the elements as a nested pretty JSON object.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 */
	public static String asNestedArray(Map<String, ? extends Collection<Integer>> elements) {
		try {
			StringWriter writer = new StringWriter();
			asNestedArray(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}

	/**
	 * @brief Returns the elements as a nested JSON object
	 * @param elements an InvertedIndex structure
	 * @return a JSON string
	 */
	public static String asInvertedIndex(TreeMap<String, TreeMap<String, TreeSet<Integer>>> elements) {
		try {
			StringWriter writer = new StringWriter();
			asInvertedIndex(elements, writer, 0);
			return writer.toString();
		}
		catch(IOException e) {
			return null;
		}
	}
}
