/*
 * Copyright 2014, by Benjamin Bertin and Contributors.
 *
 * This file is part of CarbonDB-UI project <http://www.carbondb.org>
 *
 * CarbonDB-UI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * CarbonDB-UI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CarbonDB-UI.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributor(s): -
 *
 */

package log;

import java.util.ArrayList;

/**
 * This singleton contains the errors and warnings that are sent to the logger
 * from the CarbonDB reasoner during the ontology processing.
 */
public class OntoProcessorMessageStore {
    private static volatile OntoProcessorMessageStore instance = null;

    protected ArrayList<String> warnings = new ArrayList<>();
    protected ArrayList<String> errors = new ArrayList<>();

    /**
     * Add a warning
     * @param warning a warning message
     */
    public void addWarning(String warning) {
        warnings.add(warning);
    }

    /**
     * Add an error
     * @param error an error message
     */
    public void addError(String error) {
        errors.add(error);
    }

    /**
     * Returns the warnings list
     * @return warnings list
     */
    public ArrayList<String> getWarnings() {
        return warnings;
    }

    /**
     * Returns the errors list
     * @return errors list
     */
    public ArrayList<String> getErrors() {
        return errors;
    }

    /**
     * Empty the warnings and errors
     */
    public void clear() {
        warnings.clear();
        errors.clear();
    }

    /**
     * @return true if the message store has warnings
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    /**
     * @return true if the message store has errors
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    private OntoProcessorMessageStore() {
        super();
    }

    /**
     * Returns the OntoProcessorMessageStore instance
     * @return the singleton instance
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
