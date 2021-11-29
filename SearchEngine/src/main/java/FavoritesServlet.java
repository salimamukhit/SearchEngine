import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.StringSubstitutor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;

/**
 * Class that keeps track of and displays favorites
 * 
 * @author salimamukhit
 *
 */
@SuppressWarnings("serial")
public class FavoritesServlet extends HttpServlet {
	
	/** Standard character set */
	private static final Charset UTF_8 = StandardCharsets.UTF_8;
	
	/** The prefix of the tracking URL */
	private static final String TRACKER_PREFIX = "/visited?visitedURL=";
	
	/**
	 * Constructor
	 */
	public FavoritesServlet() {
		super();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html");
		
		// Load all the cookies into a map for easy access.
		HashMap<String, Cookie> cookieMap = new HashMap<>();
		if(request.getCookies() == null) {
			cookieMap.put("favorites", new Cookie("favorites", "|"));
		} else {
			for(Cookie cookie : request.getCookies()) {
				cookieMap.put(cookie.getName(), cookie);
			}
		}
		
		Cookie favoritesCookie = cookieMap.get("favorites");

		PrintWriter out = response.getWriter();
		
		// Read-in the HTML templates.
		String headerHTML = Files.readString(Path.of("html/common/header.html"));
		String footerHTML = Files.readString(Path.of("html/common/footer.html"));
		String favoritesPageHTML = Files.readString(Path.of("html/favoritesPage/favoritesPage.html"));
		
		HashMap<String, String> favoritesListSubstitutionValues = new HashMap<>();
		favoritesListSubstitutionValues.put("header", headerHTML);
		favoritesListSubstitutionValues.put("favoritesList", generateFavoritesListHTML(favoritesCookie));
		favoritesListSubstitutionValues.put("footer", footerHTML);
		
		// Output the results
		out.print(StringSubstitutor.replace(favoritesPageHTML, favoritesListSubstitutionValues));
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.flushBuffer();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html");
		
		// Load all the cookies into a map for easy access.
		HashMap<String, Cookie> cookieMap = new HashMap<>();
		if(request.getCookies() == null) {
			cookieMap.put("favorites", new Cookie("favorites", "|"));
		} else {
			for(Cookie cookie : request.getCookies()) {
				cookieMap.put(cookie.getName(), cookie);
			}
		}
		
		// Flags for the requests to process.
		boolean newFavorite = request.getParameter("newFavorite") != null;
		boolean removeFavorite = request.getParameter("removeFavorite") != null;
		boolean clearFavorites = request.getParameter("clearFavorites") != null;
		
		Cookie favoritesCookie = cookieMap.get("favorites");
		
		if(favoritesCookie == null) {
			cookieMap.put("favorites", new Cookie("favorites", ""));
			favoritesCookie = cookieMap.get("favorites");
		}
		
		if(newFavorite) {
			favoritesCookie = appendToCookie(favoritesCookie, request.getParameter("newFavorite"));
		}
		
		if(removeFavorite) {
			favoritesCookie = removeFromCookie(favoritesCookie, request.getParameter("removeFavorite"));
		}
		
		if(clearFavorites) {
			favoritesCookie.setValue(null);
			favoritesCookie.setMaxAge(0);
			response.addCookie(favoritesCookie);
		}
		
		response.addCookie(favoritesCookie);
		response.setStatus(HttpServletResponse.SC_OK);
	}
	
	/**
	 * @brief Adds a target from a Cookie. It assumes the cookie is a CSV like format.
	 * 
	 * @param cookie the cookie.
	 * @param value the string to add.
	 * @return the modified cookie.
	 * @return
	 */
	public static Cookie appendToCookie(Cookie cookie, String value) {
		String escapedValue = StringEscapeUtils.escapeHtml4(value);
		
		String decoded = URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);
		String escaped = StringEscapeUtils.escapeHtml4(decoded);
		
		ArrayList<String> list = new ArrayList<>(Arrays.asList(StringUtils.split(escaped, "|")));
		list.add(escapedValue);
		
		String joinedString = StringUtils.join(list, "|");
		
		cookie.setValue(URLEncoder.encode(joinedString, StandardCharsets.UTF_8));
		return cookie;
	}
	
	/**
	 * @brief Removes a target from a Cookie. It assumes the cookie is a CSV like format.
	 * 
	 * @param cookie the cookie.
	 * @param target the string to remove.
	 * @return the modified cookie.
	 */
	public static Cookie removeFromCookie(Cookie cookie, String target) {
		if(cookie.getValue() == null) {
			return cookie;
		}
		
		String escapedTarget = StringEscapeUtils.escapeHtml4(target);
		
		String decoded = URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);
		String escaped = StringEscapeUtils.escapeHtml4(decoded);
		
		ArrayList<String> list = new ArrayList<>(Arrays.asList(StringUtils.split(escaped, "|")));
		ArrayList<String> newList = new ArrayList<>();
		
		for(String url : list) {
			if(!url.equals(escapedTarget)) {
				newList.add(url);
			}
		}
		
		String joinedString = StringUtils.join(newList, "|");
		
		cookie.setValue(URLEncoder.encode(joinedString, StandardCharsets.UTF_8));
		return cookie;
	}
	
	/**
	 * @brief Generates the HTML for the favorites list.
	 * 
	 * @param cookie the favorites cooking containing the favorites list.
	 * @return the HTML for the results list or empty string if cookie's value is null.
	 * @throws IOException
	 */
	public static String generateFavoritesListHTML(Cookie cookie) throws IOException {
		if(cookie == null || cookie.getValue() == null) {
			return "";
		}
		
		String decoded = URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);
		String escaped = StringEscapeUtils.escapeHtml4(decoded);
		
		HashSet<String> favoritesList = new HashSet<>(Arrays.asList(StringUtils.split(escaped, "|")));
		
		String listItemHTML = Files.readString(Path.of("html/favoritesPage/favoritesElement.html"), UTF_8);
		String listHTML = "";
		
		HashMap<String, String> elementSubstitutionValues = new HashMap<>();
		elementSubstitutionValues.put("trackerUrl", "");
		elementSubstitutionValues.put("urlText", "None.");
		
		for(String url : favoritesList) {
			elementSubstitutionValues.put("trackerUrl", TRACKER_PREFIX + url);
			elementSubstitutionValues.put("urlText", url);
			listHTML += StringSubstitutor.replace(listItemHTML, elementSubstitutionValues);
		}
		
		return listHTML;
	}
}
