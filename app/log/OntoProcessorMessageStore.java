package log;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class OntoProcessorMessageStore {
    private static volatile OntoProcessorMessageStore instance = null;

    protected ArrayList<String> warnings = new ArrayList<>();
    protected ArrayList<String> errors = new ArrayList<>();

    public void addWarning(String warning) {
        warnings.add(warning);
    }

    public void addError(String error) {
        errors.add(error);
    }

    @JsonProperty("warnings")
    public ArrayList<String> consumeWarnings() {
        ArrayList<String> warningsCopy = new ArrayList<>(warnings);
        warnings.clear();
        return warningsCopy;
    }

    @JsonProperty("errors")
    public ArrayList<String> consumeErrors() {
        ArrayList<String> errorsCopy = new ArrayList<>(errors);
        errors.clear();
        return errorsCopy;
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    private OntoProcessorMessageStore() {
        super();
    }

    /**
     * Returns the OntoProcessorMessageStore instance
     * @return The singleton instance.
     */
    public final static OntoProcessorMessageStore getInstance() {
        if (OntoProcessorMessageStore.instance == null) {
            synchronized(OntoProcessorMessageStore.class) {
                if (OntoProcessorMessageStore.instance == null) {
                    OntoProcessorMessageStore.instance = new OntoProcessorMessageStore();
                }
            }
        }
        return OntoProcessorMessageStore.instance;
    }
}
