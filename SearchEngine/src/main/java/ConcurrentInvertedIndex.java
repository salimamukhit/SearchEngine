import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author salimamukhit
 * 
 * Thread-safe InvertedIndex object
 */
public class ConcurrentInvertedIndex extends InvertedIndex {
	
	/** Lock object used for thread safety of Inverted Index */
	private final SimpleReadWriteLock lock;
	
	/** Constructor */
	public ConcurrentInvertedIndex() {
		super();
		lock = new SimpleReadWriteLock();
	}
	
	@Override
	public void addItem(String item, String location, int position) {
		try {
			lock.writeLock().lock();
			super.addItem(item, location, position);
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	@Override
	public void addAll(InvertedIndex mergeIndex) {
		try {
			lock.writeLock().lock();
			super.addAll(mergeIndex);
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	@Override
	public int getWordCount(String path) {
		try {
			lock.readLock().lock();
			return super.getWordCount(path);
		} finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public Collection<String> getAllItems() {
		try {
			lock.readLock().lock();
			return super.getAllItems();
		} finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public Collection<String> getItemPaths(String item) {
		try {
			lock.readLock().lock();
			return super.getItemPaths(item);
		} finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public Collection<Integer> getItemPositions(String item, String path) {
		try {
			lock.readLock().lock();
			return super.getItemPositions(item, path);
		} finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public Integer getItemCountsByPath(String item, String path) {
		try {
			lock.readLock().lock();
			return super.getItemCountsByPath(item, path);
		} finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public boolean hasItem(String item) {
		try {
			lock.readLock().lock();
			return super.hasItem(item);
		} finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public boolean hasPath(String item, String path) { 
		try {
			lock.readLock().lock();
			return super.hasPath(item, path);
		} finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public boolean hasPosition(String item, String path, int pos) {
		try {
			lock.readLock().lock();
			return super.hasPosition(item, path, pos);
		} finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public List<QueryResult> exactSearch(Set<String> queries) {
		try {
			lock.readLock().lock();
			return super.exactSearch(queries);
		} finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public List<QueryResult> partialSearch(Set<String> queries) {
		try {
			lock.readLock().lock();
			return super.partialSearch(queries);
		} finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public String toJson() {
		try {
			lock.readLock().lock();
			return super.toJson();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public void toJson(Path outputPath) throws IOException {
		try {
			lock.readLock().lock();
			super.toJson(outputPath);
		} finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public void writeWordCounts(Path outputPath) throws IOException {
		try {
			lock.readLock().lock();
			super.writeWordCounts(outputPath);
		} finally {
			lock.readLock().unlock();
		}
	}

}