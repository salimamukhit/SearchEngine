import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * The Search Engine Server class
 * 
 * @author salimamukhit
 *
 */
public class SearchServer {
	
	/** A thread safe Query Handler */
	private final ConcurrentQueryHandler queryHandler;
	
	/** A port to use */
	private final int port;

	/**
	 * Constructor
	 * @param handler a Query Handler object
	 * @param port a port to use
	 */
	public SearchServer(ConcurrentQueryHandler queryHandler, int port) {
		this.queryHandler = queryHandler;
		this.port = port;
	}
	
	/**
	 * @brief Starts the server
	 */
	public void start() {
		Server server = new Server(this.port);
		server.setHandler(setupHandlers());
		try {
			server.start();
			server.join();
		} catch (Exception e) {
			System.out.println("Couldn't start server");
		}
		System.out.println("Running server...");
	}
	
	/**
	 * @brief Sets up servlets
	 * 
	 * @return a list of servlet handlers
	 */
	public HandlerList setupHandlers() {
		ContextHandler defaultHandler = new ContextHandler("/favicon.ico");
		defaultHandler.setHandler(new DefaultHandler());
		
		// Main servlet handler
		ServletHandler servletHandler = new ServletHandler();
		servletHandler.addServletWithMapping(new ServletHolder(new SearchServlet(this.queryHandler)), "/");
		servletHandler.addServletWithMapping(new ServletHolder(new FavoritesServlet()), "/favorites");
		servletHandler.addServletWithMapping(new ServletHolder(new VisitedServlet()), "/visited");
		
		HandlerList handlers = new HandlerList();
		handlers.addHandler(defaultHandler);
		handlers.addHandler(servletHandler);

		return handlers;
	}
}
