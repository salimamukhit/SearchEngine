import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A utility class for finding all text files in a directory using lambda
 * functions and streams.
 *
 * @author CS 212 Software Development
 * @author University of San Francisco
 * @version Fall 2020
 */
public class TextFileFinder {

	/**
	 * @brief A lambda function that returns true if the path is a file that ends in a
	 * .txt or .text extension (case-insensitive). Useful for
	 * {@link Files#walk(Path, FileVisitOption...)}.
	 */
	public static final Predicate<Path> IS_TEXT = (path) -> {
		String filename = path.toString().toLowerCase();
		if(Files.isRegularFile(path)) {
			return (filename.endsWith(".txt") || filename.endsWith(".text") &&
				!filename.startsWith("."));
		}
		return false;
	};

	/**
	 * @brief Returns a stream of matching files, following any symbolic links
	 * encountered.
	 *
	 * @param start the initial path to start with
	 * @param keep function that determines whether to keep a file
	 * @return a stream of text files
	 * @throws IOException if an IO error occurs
	 */
	public static Stream<Path> find(Path start, Predicate<Path> keep) throws IOException {
		return Files.walk(start, FileVisitOption.FOLLOW_LINKS).filter(keep);
	};

	/**
	 * @brief Returns a stream of text files, following any symbolic links encountered.
	 *
	 * @param start the initial path to start with
	 * @return a stream of text files
	 * @throws IOException if an IO error occurs
	 */
	public static Stream<Path> find(Path start) throws IOException {
		return find(start, IS_TEXT);
	}

	/**
	 * @brief Returns a list of text files using streams.
	 *
	 * @param start the initial path to search
	 * @return list of text files
	 * @throws IOException if an IO error occurs
	 */
	public static List<Path> list(Path start) throws IOException {
		if(Files.isDirectory(start)) {
			return find(start).collect(Collectors.toList());
		}
		return List.of(start);
	}
}
