package dmax.plua.importer;

import dmax.plua.domain.Language;

/**
 * Pure (no Android dependencies) parsing helpers for the lines of the 'data' asset file.
 * All methods are tolerant: they never throw on malformed input, they return a sentinel instead.
 * <br/>
 * Format: items are separated with '|' ({@link #SEPARATOR_REGEX}). See {@link Importer} for the file layout.
 */
public final class DataFileParser {

    /** Regex for {@link String#split(String)} - the literal '|' character. */
    public static final String SEPARATOR_REGEX = "\\|";

    /** Name of the first header item: 'version|int'. */
    public static final String VERSION_KEY = "version";

    /** Returned by {@link #parseVersion(String)} when the header is missing or invalid. */
    public static final int NO_VERSION = -1;

    private DataFileParser() {
    }

    /**
     * Parse the first line of the file: 'version|int'.
     * @return the version, or {@link #NO_VERSION} if the line is null, has not exactly 2 fields,
     * its first field is not 'version' or the second one is not an int. Never throws.
     */
    public static int parseVersion(String headerLine) {
        if (headerLine == null) return NO_VERSION;
        String[] data = headerLine.split(SEPARATOR_REGEX);
        if (data.length != 2 || !VERSION_KEY.equals(data[0])) return NO_VERSION;
        try {
            return Integer.parseInt(data[1]);
        } catch (NumberFormatException e) {
            return NO_VERSION;
        }
    }

    /**
     * Parse the second line of the file: 'LANGUAGE1|LANGUAGE2' (names of {@link Language} constants, case sensitive).
     * @return array of 2 languages, or null if the line is null, has not exactly 2 fields or contains an unknown name.
     * Never throws.
     */
    public static Language[] parseLanguages(String line) {
        String[] data = parsePair(line);
        if (data == null) return null;
        try {
            return new Language[] { Language.valueOf(data[0]), Language.valueOf(data[1]) };
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Split a words line into exactly 2 items on '|'.
     * <br/>
     * Items are not trimmed. Note the behavior of {@link String#split(String)}: trailing empty strings are removed,
     * so 'a|' gives 1 item (rejected, null), while '|b' gives 2 items ("" and "b") and is accepted.
     * @return the 2 items, or null if the line is null or does not contain exactly 2 items.
     */
    public static String[] parsePair(String line) {
        if (line == null) return null;
        String[] data = line.split(SEPARATOR_REGEX);
        return data.length == 2 ? data : null;
    }
}
