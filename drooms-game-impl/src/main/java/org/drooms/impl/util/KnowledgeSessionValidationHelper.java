package org.drooms.impl.util;

import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.EntryPoint;

/**
 * A helper class to validate some of the properties of the Drools session.
 */
class KnowledgeSessionValidationHelper {

    private final KieSession session;

    /**
     * Instantiate a validator for a particular session.
     * 
     * @param ksession
     */
    public KnowledgeSessionValidationHelper(
            final KieSession ksession) {
        this.session = ksession;
    }

    /**
     * Whether or not the session has an entry point of a given name.
     * 
     * @param name
     *            Name for the entry point.
     * @return True if it has.
     */
    public boolean hasEntryPoint(final String name) {
        final EntryPoint wmep = this.session.getEntryPoint(name);
        return (wmep != null);
    }

    /**
     * Whether or not the session has a global of a given name.
     * 
     * @param name
     *            Name for the global.
     * @return True if it has.
     */
    public boolean hasGlobal(final String name) {
        return (this.session.getGlobal(name) != null);
    }

    /**
     * Whether or not the session has a global of a given name and type.
     * 
     * @param name
     *            Name for the global.
     * @param cls
     *            Type for the global.
     * @return True if it has.
     */
    public boolean hasGlobal(final String name, final Class<?> cls) {
        if (this.hasGlobal(name)) {
            final Class<?> global = this.session.getGlobal(name).getClass();
            return (global.equals(cls));
        } else {
            return false;
        }
    }

}
