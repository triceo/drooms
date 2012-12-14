package org.drooms.impl.util;

import java.util.Map;

import org.drools.impl.StatefulKnowledgeSessionImpl;
import org.drools.reteoo.ReteooRuleBase;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.WorkingMemoryEntryPoint;

/**
 * A helper class to validate some of the properties of the Drools session.
 */
public class KnowledgeSessionValidationHelper {

    /**
     * Uses internal Drools API to figure out the actual global declarations.
     * 
     * @param session
     *            The session for which to return the globals.
     * @return Contains names of the globals and their types.
     */
    private static Map<String, Class<?>> getInternalGlobalsRepresentation(
            final StatefulKnowledgeSession session) {
        final StatefulKnowledgeSessionImpl impl = (StatefulKnowledgeSessionImpl) session;
        final ReteooRuleBase rb = (ReteooRuleBase) impl.getRuleBase();
        return rb.getGlobals();
    }

    private final StatefulKnowledgeSession session;

    /**
     * Instantiate a validator for a particular session.
     * 
     * @param ksession
     */
    public KnowledgeSessionValidationHelper(
            final StatefulKnowledgeSession ksession) {
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
        final WorkingMemoryEntryPoint wmep = this.session
                .getWorkingMemoryEntryPoint(name);
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
        return (KnowledgeSessionValidationHelper
                .getInternalGlobalsRepresentation(this.session)
                .containsKey(name));
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
            final Class<?> global = KnowledgeSessionValidationHelper
                    .getInternalGlobalsRepresentation(this.session).get(name);
            return (global.equals(cls));
        } else {
            return false;
        }
    }

}
