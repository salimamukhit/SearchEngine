import java.io.IOException;
import java.nio.file.Path;
/**
 *
 * @author salimamukhit
 *
 * An InvertedIndexBuilder class that supports multi-threading
 */
public class ConcurrentIndexBuilder extends InvertedIndexBuilder {
	
	/** A Work Queue object */
	private final WorkQueue queue;
	
	/** An Inverted Index data structure */
	private final ConcurrentInvertedIndex index; 

	/**
	 * Constructor
	 * @param index InvertedIndex structure
	 * @param queue the work queue
	 */
	public ConcurrentIndexBuilder(ConcurrentInvertedIndex index, WorkQueue queue) {
		super(index);
		this.index = index;
		this.queue = queue;
	}
	
	@Override
	public void createIndex(Path start) throws IOException {
		super.createIndex(start);
		
		queue.finish();
	}
	
	@Override
	public void parseFile(Path file) {
		queue.execute(new BuilderTask(file));
	}
	
	/**
	 * 
	 * @author salimamukhit
	 * 
	 * A Runnable instance of InvertedIndexBuilder that populates the inverted index
	 */
	public class BuilderTask implements Runnable {
		/** A path to build index from */
		private final Path file;
		
		/**
		 * Constructor
		 * @param file a path to process
		 */
		public BuilderTask(Path file) {
			this.file = file;
		}

		@Override
		public void run() {
			try {
				InvertedIndex local = new InvertedIndex();
				InvertedIndexBuilder.parseFile(file, local);
				index.addAll(local);
			} catch (IOException e) {
				System.out.println("I/O error occured!");
			}
		}
	}
}