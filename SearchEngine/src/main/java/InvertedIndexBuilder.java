import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Builds an Inverted Index structure and outputs it in JSON format is a path was specified.
 * Takes Inverted Index as an argument and populates it.
 * 
 * @author salimamukhit
 */
public class InvertedIndexBuilder {
	/** Inverted Index structure */
	private final InvertedIndex index;

	/**
	 * @brief Constructor takes the Inverted Index as a parameter
	 * @param index an InvertedIndex structure
	 */
	public InvertedIndexBuilder(InvertedIndex index) {
		this.index = index;
	}
	/**
	 * @brief Static method that parses a given file and then adds it's content to the index.
	 *
	 * @param file a file to parse
	 * @param index an inverted index to populate
	 * @throws IOException if an I/O error occurs
	 */
	public static void parseFile(Path file, InvertedIndex index) throws IOException {
		try (
				BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)
				)
		{
			String line;
			int pos = 0;
			String location = file.toString();
			Stemmer stemmer = new SnowballStemmer(TextFileStemmer.DEFAULT);
			while((line = br.readLine()) != null) {
				String[] items = TextParser.parse(line);
				int len = items.length;
				for(int i = 0; i < len; i++) {
					index.addItem(stemmer.stem(items[i]).toString(), location, pos + i + 1);
				}
				pos += len;
			}
		}
	}
	
	/**
	 * @brief Method that calls static parseFile for thread safety reason
	 * @param file a file to parse
	 * @throws IOException if an I/O error occurs
	 */
	public void parseFile(Path file) throws IOException {
		parseFile(file, this.index);
	}

	/**
	 * @brief Populates the index tree map by walking through every file in the path
	 * @param start a starting path
	 * @throws IOException if an I/O error occurs
	 */
	public void createIndex(Path start) throws IOException {
		List<Path> textPaths = TextFileFinder.list(start);

		for(Path file : textPaths) {
			parseFile(file);
		}
	}

	/**
	 * @brief Writes results in JSON format to the index file
	 * @param outputFile a file to write the output
	 * @throws IOException if an I/O error occurs
	 */
	public void writeResults(Path outputFile) throws IOException {
		this.index.toJson(outputFile);
	}
	
	/**
	 * @brief Writes total word counts in JSON format to the counts file
	 * @param outputFile a file to write the output
	 * @throws IOException if an I/O error occurs
	 */
	public void writeWordCounts(Path outputFile) throws IOException {
		this.index.writeWordCounts(outputFile);
	}
}
