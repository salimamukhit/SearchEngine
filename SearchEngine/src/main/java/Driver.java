import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

/**
 * Class responsible for running this project based on the provided command-line
 * arguments. See the README for details.
 */
public class Driver {
	/** A default path for writing out the inverted index */
	private static final Path defaultPath = Path.of("index.json");
	
	/** A default path for outputting total word counts of all paths */
	private static final Path defaultCountsPath = Path.of("counts.json");
	
	/** A default path for outputting the results of the search */
	private static final Path defaultResultsPath = Path.of("results.json");

	/**
	 * @brief Initializes the classes necessary based on the provided command-line
	 * arguments. This includes (but is not limited to) how to build or search an
	 * inverted index.
	 *
	 * @param args flag/value pairs used to start this program
	 */
	public static void main(String[] args) {
		// store initial start time
		Instant start = Instant.now();

		// My code goes here:
		// -------------------------------------------------------------

		// Object creation
		ArgumentMap argMap = new ArgumentMap(args);
		
		// Dependent on threads
		InvertedIndex index;
		InvertedIndexBuilder builder;
		QueryHandlerInterface handler;
		WebCrawler crawler = null;
		WorkQueue queue = null;
				
		if(argMap.hasFlag("-threads") || argMap.hasFlag("-url") || argMap.hasFlag("-server")) {
			try {
				int threads = Integer.parseInt(argMap.getString("-threads", "5"));
				queue = new WorkQueue(threads);
				ConcurrentInvertedIndex concurrentIndex = new ConcurrentInvertedIndex();
				index = concurrentIndex;
				builder = new ConcurrentIndexBuilder(concurrentIndex, queue);
				handler = new ConcurrentQueryHandler(concurrentIndex, queue);
				
				if(argMap.hasFlag("-url")) {
					int max = Integer.parseInt(argMap.getString("-max", "30"));
					crawler = new WebCrawler(concurrentIndex, queue, max);
				}
				
				// Start the web server
				if(argMap.hasFlag("-server")) {
					int port = argMap.getInteger("-server", 8080);
					try {
						System.out.println("Staring the crawl...");
						if(argMap.hasFlag("-url")) {
							crawler.crawl(argMap.getString("-url"));
							System.out.println("The crawl has completed!");
						}
					} catch (MalformedURLException e) {
						System.out.println("Malformed URL!");
					}
					
		
					
					SearchServer server = new SearchServer((ConcurrentQueryHandler) handler, port);
					server.start();
				}
				
			} catch(IllegalArgumentException e) {
				System.err.print("Illegal number of threads!");
				return;
			}
		} else {
			index = new InvertedIndex();
			builder = new InvertedIndexBuilder(index);
			handler = new QueryHandler(index);
		} 
		
		// Calling work
		if(argMap.hasFlag("-path")) {
			try {
				Path filePath = argMap.getPath("-path");
				builder.createIndex(filePath);
			} catch(Exception e) {
				System.out.println("Unable to build the inverted index");
			}
		}
		
		if(crawler != null) {
			try {
				crawler.crawl(argMap.getString("-url"));
			} catch (MalformedURLException e) {
				System.out.println("Malformed URL!");
			}
		}
		
		if(argMap.hasFlag("-queries")) {
			try {
				handler.performSearch(argMap.hasFlag("-exact"), argMap.getPath("-queries"));
			} catch (Exception e) {
				System.out.println("Unable to process queries");
			}
		}
		
		if(argMap.hasFlag("-index")) {
			Path indexPath = argMap.getPath("-index", defaultPath);
			try {
				builder.writeResults(indexPath);
			} catch (IOException e) {
				System.out.println("Couldn't write file " + indexPath.toString());
			}
		}
		
		if(argMap.hasFlag("-counts")) {
			Path countsPath = argMap.getPath("-counts", defaultCountsPath);
			try {
				builder.writeWordCounts(countsPath);
			} catch (IOException e) {
				System.out.println("Couldn't write counts in " + countsPath.toString());
			}
		}
		
		if(argMap.hasFlag("-results")) {
			Path resultsPath = argMap.getPath("-results", defaultResultsPath);
			try {
				handler.outputResults(resultsPath);
			} catch (IOException e) {
				System.out.println("Couldn't output results in " + resultsPath.toString());
			}
		}
		
		if(queue != null) queue.shutdown();
		
		// -------------------------------------------------------------

		// calculate time elapsed and output
		Duration elapsed = Duration.between(start, Instant.now());
		double seconds = (double) elapsed.toMillis() / Duration.ofSeconds(1).toMillis();
		System.out.printf("Elapsed: %f seconds%n", seconds);
	}
}
