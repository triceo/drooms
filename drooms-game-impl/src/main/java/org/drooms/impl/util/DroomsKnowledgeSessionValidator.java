package org.drooms.impl.util;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.drools.KnowledgeBase;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drooms.impl.logic.PathTracker;
import org.slf4j.Logger;

/**
 * A class to validate a session's feasibility as a Drooms strategy.
 */
public class DroomsKnowledgeSessionValidator {

    private final KnowledgeSessionValidationHelper helper;
    private final List<String> errors = new LinkedList<String>();
    private final List<String> warnings = new LinkedList<String>();

    /**
     * Create validator for a new session obtained from a particular knowledge
     * base.
     * 
     * @param kbase
     */
    public DroomsKnowledgeSessionValidator(final KnowledgeBase kbase) {
        this(kbase.newStatefulKnowledgeSession());
    }

    /**
     * Create validator for the particular session.
     * 
     * @param session
     */
    public DroomsKnowledgeSessionValidator(
            final StatefulKnowledgeSession session) {
        this.helper = new KnowledgeSessionValidationHelper(session);
        this.validateGlobal("logger", Logger.class, false);
        this.validateGlobal("tracker", PathTracker.class, false);
        this.validateEntryPoint("rewardEvents", true);
        this.validateEntryPoint("playerEvents", true);
        this.validateEntryPoint("gameEvents", true);
    }

    /**
     * Retrieve problems that are critical to the strategy and will result in
     * the strategy not being accepted for the game.
     * 
     * @return
     */
    public List<String> getErrors() {
        return Collections.unmodifiableList(this.errors);
    }

    /**
     * Retrieve problems that aren't critical to the strategy. The strategy may
     * be sub-optimal, but it will be allowed into the game.
     * 
     * @return
     */
    public List<String> getWarnings() {
        return Collections.unmodifiableList(this.warnings);
    }

    /**
     * Whether or not the strategy is both valid and leverages all the available
     * options.
     * 
     * @return
     */
    public boolean isClean() {
        return (this.isValid() && this.warnings.size() == 0);
    }

    /**
     * Whether or not the strategy is valid. Invalid strategies may not be
     * accepted into the game.
     * 
     * @return
     */
    public boolean isValid() {
        return (this.errors.size() == 0);
    }

    private void report(final String report, final boolean isError) {
        if (isError) {
            this.errors.add(report);
        } else {
            this.warnings.add(report);
        }
    }

    private void validateEntryPoint(final String name, final boolean isError) {
        if (!this.helper.hasEntryPoint(name)) {
            this.report("Entry point '" + name + "' not declared.", isError);
        }
    }

    private void validateGlobal(final String name, final Class<?> cls,
            final boolean isError) {
        if (!this.helper.hasGlobal(name, cls)) {
            this.report(
                    "Global '" + name + "' of type '" + cls.getCanonicalName()
                            + "' not declared.", isError);
        }
    }

}
