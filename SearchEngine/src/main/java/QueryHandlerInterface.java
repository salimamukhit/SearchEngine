import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 
 * @author salimamukhit
 *
 * A common interface for handling query search
 */
public interface QueryHandlerInterface {
	
	/**
	 * @brief The main default method that performs search
	 * @param exact the flag that indicates the type of search
	 * @param queryPath a path that contains all queries
	 * @throws IOException if I/O error occurs
	 */
	public default void performSearch(boolean exact, Path queryPath) throws IOException {
		try (
				BufferedReader br = Files.newBufferedReader(queryPath, StandardCharsets.UTF_8);
				) {
			String line;
			while((line = br.readLine()) != null) {
				parseQuery(line, exact);
			}
		}
	}
	
	/**
	 * @brief A helper method that adds result to the results map
	 * @param line a line with queries
	 * @param exact a flag that indicates the type of search
	 */
	public void parseQuery(String line, boolean exact);
	
	/**
	 * @brief Outputs the search results into an output file
	 * @param outputFile a file to output results
	 * @throws IOException if I/O error occurs
	 */
	public void outputResults(Path outputFile) throws IOException;
}
