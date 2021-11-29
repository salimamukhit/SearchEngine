import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * 
 * @author salimamukhit
 * 
 * A Web Crawler class that creates an Inverted Index from html
 */
public class WebCrawler {
	
	/** The default number of redirects when fetching HTML */
	private static final int REDIRECTS = 3;
	
	/** A Concurrent Inverted Index structure that supports multithreading */
	private final ConcurrentInvertedIndex index;
	
	/** A maximum amount of links to be parsed */
	private final int max;
	
	/** A Work Queue object*/
	private final WorkQueue queue;
	
	/** A Set object that stores links to parse */
	private final Set<URL> links;
	
	/**
	 * Constructor
	 * @param index a thread-safe Inverted Index structure
	 * @param queue a Work Queue object
	 * @param max a maximum amount of links to be parsed
	 */
	public WebCrawler(ConcurrentInvertedIndex index, WorkQueue queue, int max) {
		this.index = index;
		this.queue = queue;
		this.max = max;
		this.links = new HashSet<>();
	}
	
	/**
	 * 
	 * @param url a url to start crawling from
	 * @throws MalformedURLException if URL is Malformed
	 */
	public void crawl(String url) throws MalformedURLException {
			URL seedUrl = new URL(url);
			
			synchronized(this.links) {
				this.links.add(seedUrl);
			}
			
			this.queue.execute(new CrawlerTask(seedUrl));
			this.queue.finish();
	}
	
	/**
	 * 
	 * @author salimamukhit
	 * 
	 * A Runnable instance of WebCrawler that processes each link and populates InvertedIndex
	 */
	public class CrawlerTask implements Runnable {
		
		/** A URL to parse */
		private final URL seedUrl;
		
		/**
		 * Constructor
		 * @param seedUrl a seed url to start indexing
		 */
		public CrawlerTask(URL seedUrl) {
			this.seedUrl = seedUrl;
		}
		
		@Override
		public void run() {
			// Building a local Inverted Index from link and merging with concurrent one
			InvertedIndex local = new InvertedIndex();
			SnowballStemmer stemmer = new SnowballStemmer(TextFileStemmer.DEFAULT);
			String html = HtmlFetcher.fetch(seedUrl, REDIRECTS);
			
			if(html == null) {
				return;
			}
			
			List<URL> validLinks = LinkParser.getValidLinks(seedUrl, HtmlCleaner.stripBlockElements(html));
			
			synchronized(links) {
				for(URL link : validLinks) {
					if(links.size() >= max) {
						break;
					}
					
					if(!links.contains(link)) {
						links.add(link);
						queue.execute(new CrawlerTask(link));
					}
				}
			}
			
			int pos = 1;
			String location = seedUrl.toString();
			
			for(String item : TextParser.parse(HtmlCleaner.stripHtml(html))) {
				local.addItem(stemmer.stem(item).toString(), location, pos++);
			}
			
			index.addAll(local);
		}
	}
}
