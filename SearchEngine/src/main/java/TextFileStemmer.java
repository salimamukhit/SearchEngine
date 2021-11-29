import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Utility class for parsing and stemming text and text files into collections
 * of stemmed words.
 *
 * @author CS 212 Software Development
 * @author University of San Francisco
 * @version Fall 2020
 */
public class TextFileStemmer {
	/** 
	 * The default stemmer algorithm used by this class. 
	 **/
	public static final SnowballStemmer.ALGORITHM DEFAULT = SnowballStemmer.ALGORITHM.ENGLISH;

	/**
	 * Returns a list of cleaned and stemmed words parsed from the provided line.
	 *
	 * @param line the line of words to clean, split, and stem
	 * @return a list of cleaned and stemmed words
	 */
	public static ArrayList<String> listStems(String line) {
		Stemmer stemmer = new SnowballStemmer(DEFAULT);
		ArrayList<String> result = new ArrayList<>();
		stemLine(line, stemmer, result);
		return result;
	}

	/**
	 * @brief Returns a set of unique (no duplicates) cleaned and stemmed words parsed
	 * from the provided line.
	 *
	 * @param line the line of words to clean, split, and stem
	 * @param stemmer the stemmer to use
	 * @return a sorted set of unique cleaned and stemmed words
	 */
	public static TreeSet<String> uniqueStems(String line, Stemmer stemmer) {
		TreeSet<String> result = new TreeSet<>();
		stemLine(line, stemmer, result);
		return result;
	}
	
	/**
	 * @brief Reads a file line by line, parses each line into cleaned and stemmed words,
	 * and then adds those words to a set.
	 *
	 * @param inputFile the input file to parse
	 * @return a sorted set of stems from file
	 * @throws IOException if I/O error occurs
	 */
	public static TreeSet<String> uniqueStems(Path inputFile) throws IOException {
		Stemmer stemmer = new SnowballStemmer(DEFAULT);
		TreeSet<String> result = new TreeSet<>();
		try (
			BufferedReader br = Files.newBufferedReader(inputFile, StandardCharsets.UTF_8);
		) {
			String line;
			while((line = br.readLine()) != null) {
				stemLine(line, stemmer, result);
			}
		}
		return result;
	}
	
	/**
	 * @brief Reads a file line by line, parses each line into cleaned and stemmed words,
	 * and then adds those words to a set.
	 *
	 * @param inputFile the input file to parse
	 * @return a sorted set of stems from file
	 * @throws IOException if I/O error occurs
	 */
	public static ArrayList<String> listStems(Path inputFile) throws IOException {
		Stemmer stemmer = new SnowballStemmer(DEFAULT);
		ArrayList<String> result = new ArrayList<>();
		try (
				BufferedReader br = Files.newBufferedReader(inputFile, StandardCharsets.UTF_8);
				)
		{
			String line;
			while((line = br.readLine()) != null) {
				stemLine(line, stemmer, result);
			}
		}
		return result;
	}
	
	/**
	 * @brief A helper method for stemming a line
	 * @param line a line to stem
	 * @param stemmer a stemmer object
	 * @param result a container for the output
	 */
	private static void stemLine(String line, Stemmer stemmer, Collection<String> result) {
		for(String s : TextParser.parse(line)) {
			result.add(stemmer.stem(s).toString());
		}
	}
}

