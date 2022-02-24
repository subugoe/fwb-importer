package sub.fwb;

/**
 * Access to environment variables.
 */
public class FWBEnvironment {

    public final String UNDEFINED_VALUE = "undefined";

    /**
     * Gets an environment variable by name.
     */
    private String getVariable(String name) {
        String variable = System.getenv(name);
        if (variable == null) {
            System.err.println("WARNING Environment variable not set: " + name + ". Setting to '" + UNDEFINED_VALUE + "'");
            return UNDEFINED_VALUE;
        }
        return variable;
    }

    public String cacheUrl() {
        return getVariable("FWB_CACHE_URL");
    }

    public String fwbUser() {
        return getVariable("FWB_USER");
    }

    public String fwbPassword() {
        return getVariable("FWB_PASSWORD");
    }
}
