import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Parses and stores command-line arguments into simple key = value pairs.
 *
 * @author CS 212 Software Development
 * @author University of San Francisco
 * @version Fall 2020
 */
public class ArgumentMap {

	/**
	 * Stores command-line arguments in key = value pairs.
	 */
	private final Map<String, String> map;

	/**
	 * Initializes this argument map.
	 */
	public ArgumentMap() {
		this.map = new HashMap<>();
	}

	/**
	 * @brief Initializes this argument map and then parsers the arguments into
	 * flag/value pairs where possible. Some flags may not have associated values.
	 * If a flag is repeated, its value is overwritten.
	 *
	 * @param args the command line arguments to parse
	 */
	public ArgumentMap(String[] args) {
		this();
		parse(args);
	}

	/**
	 * @brief Parses the arguments into flag/value pairs where possible. Some flags may
	 * not have associated values. If a flag is repeated, its value is
	 * overwritten.
	 *
	 * @param args the command line arguments to parse
	 */
	public void parse(String[] args) {
		String value;
		for(int i = 0; i < args.length; i++) {
			if(isFlag(args[i])) {
				value = null;
				if( args.length != i + 1 && isValue(args[i + 1])) {
					value = args[i + 1];
				}
				this.map.put(args[i], value);
			}
		}
	}

	/**
	 * @brief Determines whether the argument is a flag. Flags start with a dash "-"
	 * character, followed by at least one other non-digit character.
	 *
	 * @param arg the argument to test if its a flag
	 * @return {@code true} if the argument is a flag
	 */
	public static boolean isFlag(String arg) {
		String regex = "[0-9]+";
		return ((arg != null) && 
				!(arg.length() < 2 || arg.charAt(0) != '-') && 
				!arg.substring(1).matches(regex));
	}

	/**
	 * @brief Determines whether the argument is a value. Anything that is not a flag is
	 * considered a value.
	 *
	 * @param arg the argument to test if its a value
	 * @return {@code true} if the argument is a value
	 */
	public static boolean isValue(String arg) {
		return !isFlag(arg);
	}

	/**
	 * @brief Returns the number of unique flags.
	 *
	 * @return number of unique flags
	 */
	public int numFlags() {
		return this.map.size();
	}

	/**
	 * @brief Determines whether the specified flag exists.
	 *
	 * @param flag the flag find
	 * @return {@code true} if the flag exists
	 */
	public boolean hasFlag(String flag) {
		return this.map.containsKey(flag);
	}

	/**
	 * @brief Determines whether the specified flag is mapped to a non-null value.
	 *
	 * @param flag the flag to find
	 * @return {@code true} if the flag is mapped to a non-null value
	 */
	public boolean hasValue(String flag) {
		return this.map.get(flag) != null;
	}

	/**
	 * @brief Returns the value to which the specified flag is mapped as a
	 * {@link String}, or null if there is no mapping.
	 *
	 * @param flag the flag whose associated value is to be returned
	 * @return the value to which the specified flag is mapped, or {@code null} if
	 *         there is no mapping
	 */
	public String getString(String flag) {
		return this.map.get(flag);
	}

	/**
	 * @brief Returns the value to which the specified flag is mapped as a
	 * {@link String}, or the default value if there is no mapping.
	 *
	 * @param flag the flag whose associated value is to be returned
	 * @param defaultValue the default value to return if there is no mapping
	 * @return the value to which the specified flag is mapped, or the default
	 *         value if there is no mapping
	 */
	public String getString(String flag, String defaultValue) {
		String result = this.map.get(flag);
		if(result == null) {
			result = defaultValue;
		}
		return result;
	}

	/**
	 * @brief Returns the value to which the specified flag is mapped as a {@link Path},
	 * or {@code null} if unable to retrieve this mapping (including being unable
	 * to convert the value to a {@link Path} or no value exists).
	 *
	 * This method should not throw any exceptions!
	 *
	 * @param flag the flag whose associated value is to be returned
	 * @return the value to which the specified flag is mapped, or {@code null} if
	 *         unable to retrieve this mapping
	 */
	public Path getPath(String flag) {
		if(this.map.get(flag) != null) {
			return Path.of(this.map.get(flag));
		}
		return null;
	}

	/**
	 * @brief Returns the value the specified flag is mapped as a {@link Path}, or the
	 * default value if unable to retrieve this mapping (including being unable to
	 * convert the value to a {@link Path} or if no value exists).
	 *
	 * @param flag the flag whose associated value will be returned
	 * @param defaultValue the default value to return if there is no valid
	 *        mapping
	 * @return the value the specified flag is mapped as a {@link Path}, or the
	 *         default value if there is no valid mapping
	 */
	public Path getPath(String flag, Path defaultValue) {
		if(this.map.get(flag) != null) {
			return Path.of(this.map.get(flag));
		}
		return defaultValue;
	}

	/**
	 * @brief Returns the value the specified flag is mapped as an int value, or the
	 * default value if unable to retrieve this mapping (including being unable to
	 * convert the value to an int or if no value exists).
	 *
	 * @param flag the flag whose associated value will be returned
	 * @param defaultValue the default value to return if there is no valid
	 *        mapping
	 * @return the value the specified flag is mapped as a int, or the default
	 *         value if there is no valid mapping
	 */
	public int getInteger(String flag, int defaultValue) {
		if(this.map.get(flag) != null) {
			String regex = "[0-9]+";
			if(this.map.get(flag).matches(regex)) {
				return Integer.parseInt(this.map.get(flag));
			}
		}
		return defaultValue;
	}

	@Override
	public String toString() {
		return this.map.toString();
	}
}
