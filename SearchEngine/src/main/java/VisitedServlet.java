import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.StringSubstitutor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A class that keeps track of and displays visited URLs
 * 
 * @author salimamukhit
 *
 */
@SuppressWarnings("serial")
public class VisitedServlet extends HttpServlet {
	
	/** Standard character set */
	private static final Charset UTF_8 = StandardCharsets.UTF_8;
	
	/** The prefix of the tracking URL */
	private static final String TRACKER_PREFIX = "/visited?visitedURL=";
	
	/** Stores the visited URLs */
	private final ArrayList<String> visitedResultsList;
	
	/**
	 * Constructor
	 */
	public VisitedServlet() {
		super();
		this.visitedResultsList = new ArrayList<>();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
		// Flags for the requests to process.
		boolean newVisitedURL = request.getParameter("visitedURL") != null;
		
		if(newVisitedURL) {
			String escapedURL = StringEscapeUtils.escapeHtml4(request.getParameter("visitedURL"));
			String decodedURL = URLDecoder.decode(escapedURL, UTF_8);
			this.visitedResultsList.add(decodedURL);
			response.setStatus(HttpServletResponse.SC_OK);
			response.sendRedirect(response.encodeRedirectURL(decodedURL));
			return;
		}
		
		// Read-in the HTML templates.
		String headerHTML = Files.readString(Path.of("html/common/header.html"));
		String footerHTML = Files.readString(Path.of("html/common/footer.html"));
		String visitedResultsListHTML = Files.readString(Path.of("html/visitedResultsPage/visitedResultsPage.html"));
		
		HashMap<String, String> visitedResultsListSubstitutionValues = new HashMap<>();
		visitedResultsListSubstitutionValues.put("header", headerHTML);
		visitedResultsListSubstitutionValues.put("visitedResultsList", generateVisitedURLsListHTML());
		visitedResultsListSubstitutionValues.put("footer", footerHTML);
		
		// Output the results
		out.print(StringSubstitutor.replace(visitedResultsListHTML, visitedResultsListSubstitutionValues));
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.flushBuffer();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html");
		
		// Flags for the requests to process.
		boolean newVisitedURL = request.getParameter("visitedURL") != null;
		boolean clearVisitedURLs = request.getParameter("clearVisitedURLs") != null;
		
		if(newVisitedURL) {
			String escapedURL = StringEscapeUtils.escapeHtml4(request.getParameter("visitedURL"));
			String decodedURL = URLDecoder.decode(escapedURL, UTF_8);
			this.visitedResultsList.add(decodedURL);
			response.setStatus(HttpServletResponse.SC_OK);
			response.sendRedirect(response.encodeRedirectURL(decodedURL));
		}
		
		if(clearVisitedURLs) {
			this.visitedResultsList.clear();
		}
		
		response.setStatus(HttpServletResponse.SC_OK);
	}
	
	/**
	 * @brief Generates the HTML for the visited results list.
	 * 
	 * @param cookie the favorites cooking containing the favorites list
	 * @return the HTML for the results list or empty string if cookie's value is null
	 * @throws IOException if I/O error occurs
	 */
	private String generateVisitedURLsListHTML() throws IOException {
		String listItemHTML = Files.readString(Path.of("html/visitedResultsPage/visitedResultElement.html"), UTF_8);
		String listHTML = "";
		
		HashMap<String, String> elementSubstitutionValues = new HashMap<>();
		elementSubstitutionValues.put("trackerUrl", "");
		elementSubstitutionValues.put("urlText", "None.");
		
		for(String url : visitedResultsList) {
			elementSubstitutionValues.put("trackerUrl", TRACKER_PREFIX + url);
			elementSubstitutionValues.put("urlText", url);
			listHTML += StringSubstitutor.replace(listItemHTML, elementSubstitutionValues);
		}
		return listHTML;
	}
}
