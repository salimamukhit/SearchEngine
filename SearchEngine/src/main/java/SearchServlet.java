import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.StringSubstitutor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
/**
 * Servlet which holds search engine main interface
 * 
 * @author salimamukhit
 *
 */
public class SearchServlet extends HttpServlet {
	
	/** Standard character set */
	private static final Charset UTF_8 = StandardCharsets.UTF_8;
	
	/** A thread safe Query Handler */
	private final ConcurrentQueryHandler handler;
	
	/** Tracking prefix */
	private static final String TRACKER_PREFIX = "/visited?visitedURL=";

	/**
	 * Constructor
	 * 
	 * @param handler a Query Handler object
	 */
	public SearchServlet(ConcurrentQueryHandler handler) {
		super();
		this.handler = handler;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html");

		PrintWriter out = response.getWriter();
		
		// Read-in the HTML templates.
		String headerHTML = Files.readString(Path.of("html/common/header.html"));
		String footerHTML = Files.readString(Path.of("html/common/footer.html"));
		String searchPageHTML = Files.readString(Path.of("html/searchPage/searchPage.html"));
		
		HashMap<String, String> resultsListSubstitutionValues = new HashMap<>();
		resultsListSubstitutionValues.put("header", headerHTML);
		resultsListSubstitutionValues.put("searchResults", "");
		resultsListSubstitutionValues.put("footer", footerHTML);
		
		// Output the results
		out.print(StringSubstitutor.replace(searchPageHTML, resultsListSubstitutionValues));
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.flushBuffer();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html");
		
		// Look for the "isLucky" parameter for the I'm feeling lucky search.
		boolean isLucky = false;
		if(request.getParameter("isLucky") != null) {
			isLucky = true;
		}
		
		// Escape the query string for anything malicious.
		String query = StringEscapeUtils.escapeHtml4(request.getParameter("query"));

		// Do nothing if there is no query content
		if(query == null || query.isBlank()) {
			response.setStatus(HttpServletResponse.SC_OK);
			response.sendRedirect(request.getServletPath());
			return;
		}

		PrintWriter out = response.getWriter();
		
		// Generate the results HTML
		String resultsHTML = getResultsHTML(response, query, out, isLucky);
		
		// Read-in the HTML templates.
		String headerHTML = Files.readString(Path.of("html/common/header.html"));
		String footerHTML = Files.readString(Path.of("html/common/footer.html"));
		String searchPageHTML = Files.readString(Path.of("html/searchPage/searchPage.html"));
		
		HashMap<String, String> resultsListSubstitutionValues = new HashMap<>();
		resultsListSubstitutionValues.put("header", headerHTML);
		resultsListSubstitutionValues.put("searchResults", "");
		if(resultsHTML != null) {
			resultsListSubstitutionValues.replace("searchResults", resultsHTML);
		}
		resultsListSubstitutionValues.put("footer", footerHTML);
		
		out.print(StringSubstitutor.replace(searchPageHTML, resultsListSubstitutionValues));

		response.setStatus(HttpServletResponse.SC_OK);
	}
	
	/**
	 * @brief Prints out all the search results.
	 * 
	 * @param response the HTTP response
	 * @param query the escaped query string
	 * @param out the writer to use
	 * @param isLucky redirect if true
	 * @throws IOException if I/O error occurs
	 */
	protected String getResultsHTML(HttpServletResponse response, String query, PrintWriter out, boolean isLucky) 
			throws IOException {
		
		// Perform the search and track the time.
		Instant start  = Instant.now();
		List<InvertedIndex.QueryResult> resultsList = this.handler.searchEngineSearch(query, false);
		
		// Calculate the resulting difference in time.
		double searchTime = (double) Duration.between(start, Instant.now()).toMillis() / Duration.ofSeconds(1).toMillis();
		
		int numberOfResults = 0;
		if(resultsList != null) {
			numberOfResults = resultsList.size();
		}
		
		String resultsHTML = "No search results :(";
		
		if(numberOfResults != 0) {
			if(isLucky) {
				String encodedURL = URLEncoder.encode(resultsList.get(0).getWhere(), StandardCharsets.UTF_8);
				response.sendRedirect(TRACKER_PREFIX + encodedURL);
				return null;
			}
			resultsHTML = generateListHTML(resultsList);
		}
		// If there are no results and it was a lucky search, then display a special message.
		else if(isLucky) {
			resultsHTML = Files.readString(Path.of("html/searchPage/unlucky.html"));
		}
		
		// Generate the search time string.
		String timeString = String.format("%d Results for \"%s\" (%f seconds)", numberOfResults, query, searchTime);
		
		// Read-in the HTML template.
		String resultListHTML = Files.readString(Path.of("html/searchPage/resultList.html"));
		
		HashMap<String, String> resultsListSubstitutionValues = new HashMap<>();
		resultsListSubstitutionValues.put("searchTime", timeString);
		resultsListSubstitutionValues.put("resultsList", resultsHTML);
		
		return StringSubstitutor.replace(resultListHTML, resultsListSubstitutionValues);
	}
	
	/**
	 * @brief Generates the HTML for all the results.
	 * 
	 * @param resultsList the list of results from the query
	 * @return the HTML for the results list
	 * @throws IOException if I/O error occurs
	 */
	public static String generateListHTML(List<InvertedIndex.QueryResult> resultsList) throws IOException {
		String listItemHTML = Files.readString(Path.of("html/searchPage/resultElement.html"), UTF_8);
		String listHTML = "";
		
		HashMap<String, String> elementSubstitutionValues = new HashMap<>();
		elementSubstitutionValues.put("trackerUrl", "");
		elementSubstitutionValues.put("urlText", "None.");
		elementSubstitutionValues.put("score", "0");
		elementSubstitutionValues.put("matches", "0");
		
		for(InvertedIndex.QueryResult result : resultsList) {
			String encodedURL = URLEncoder.encode(result.getWhere(), StandardCharsets.UTF_8);
			elementSubstitutionValues.put("trackerUrl", TRACKER_PREFIX + encodedURL);
			elementSubstitutionValues.put("urlText", result.getWhere());
			elementSubstitutionValues.put("score", String.format("%.6f", result.getScore()));
			elementSubstitutionValues.put("matches", String.valueOf(result.getCount()));
			listHTML += StringSubstitutor.replace(listItemHTML, elementSubstitutionValues);
		}
		
		return listHTML;
	}

}
