package org.drooms.impl.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.kie.api.KieBase;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.rule.Global;

/**
 * A helper class to validate some of the properties of the Drools session.
 */
class KnowledgeSessionValidationHelper {

    private final Map<String, String> globals;
    private final Set<String> entryPoints;

    /**
     * Instantiate a validator for a particular session.
     * 
     * @param ksession
     */
    public KnowledgeSessionValidationHelper(final KieBase kbase) {
        entryPoints = kbase.getEntryPointIds();
        globals = new HashMap<>();
        for (KiePackage pkg : kbase.getKiePackages()) {
            for (Global global : pkg.getGlobalVariables()) {
                globals.put(global.getName(), global.getType());
            }
        }
    }

    /**
     * Whether or not the session has an entry point of a given name.
     * 
     * @param name
     *            Name for the entry point.
     * @return True if it has.
     */
    public boolean hasEntryPoint(final String name) {
        return entryPoints.contains(name);
    }

    /**
     * Whether or not the session has a global of a given name.
     * 
     * @param name
     *            Name for the global.
     * @return True if it has.
     */
    public boolean hasGlobal(final String name) {
        return globals.containsKey(name);
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
            return globals.get(name).equals(cls.getName());
        } else {
            return false;
        }
    }

}
