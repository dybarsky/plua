package dmax.plua.settings;

/**
 * In which order cards are shown.
 */
public enum OrderMode {

    /** Cards are shuffled every time the links are loaded. */
    RANDOM,

    /** Cards are sorted by priority (forgotten words first), see {@code DataSource.PrioritySorter}. */
    SPACED;

    public static final OrderMode DEFAULT = RANDOM;

    /**
     * Parse a persisted value. Null, empty or unknown names (e.g. written by a newer app version) give {@link #DEFAULT}.
     */
    public static OrderMode fromName(String name) {
        if (name != null) {
            for (OrderMode mode : values()) {
                if (mode.name().equals(name)) return mode;
            }
        }
        return DEFAULT;
    }
}
