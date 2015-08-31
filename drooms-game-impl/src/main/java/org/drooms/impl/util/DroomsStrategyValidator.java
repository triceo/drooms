package org.drooms.impl.util;

import org.drooms.impl.logic.PathTracker;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.runtime.KieContainer;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.*;

/**
 * A class to validate strategy's feasibility.
 * 
 */
public class DroomsStrategyValidator implements Callable<DroomsStrategyValidator> {

    private final ReleaseId releaseId;
    private final List<String> errors = new LinkedList<>();
    private final List<String> warnings = new LinkedList<>();

    private static final Map<String, Future<DroomsStrategyValidator>> validators = new HashMap<>();
    private static final ExecutorService E = Executors.newCachedThreadPool();

    private static String getInternalId(final ReleaseId strategyReleaseId) {
        return strategyReleaseId.getGroupId().trim() + ":" + strategyReleaseId.getArtifactId().trim() + ":" +
                strategyReleaseId.getVersion().trim();
    }

    public static DroomsStrategyValidator getInstance(final ReleaseId strategyReleaseId) {
        final String internalId = DroomsStrategyValidator.getInternalId(strategyReleaseId);
        synchronized (DroomsStrategyValidator.class) {
            if (!DroomsStrategyValidator.validators.containsKey(internalId)) {
                DroomsStrategyValidator.validators.put(internalId, DroomsStrategyValidator.E.submit(new
                        DroomsStrategyValidator(strategyReleaseId)));
            }
        }
        try {
            System.out.println(validators);
            return DroomsStrategyValidator.validators.get(internalId).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException("This should not have happened.", e);
        }
    }

    private DroomsStrategyValidator(ReleaseId releaseId) {
        this.releaseId = releaseId;
    }

    /**
     * Retrieve problems that are critical to the strategy and will result in
     * the strategy not being accepted for the game.
     * 
     * @return An unmodifiable collection of error messages.
     */
    public List<String> getErrors() {
        return Collections.unmodifiableList(this.errors);
    }

    /**
     * Retrieve problems that aren't critical to the strategy. The strategy may
     * be sub-optimal, but it will be allowed into the game.
     * 
     * @return An unmodifiable collection of warnings.
     */
    public List<String> getWarnings() {
        return Collections.unmodifiableList(this.warnings);
    }

    /**
     * Whether or not the strategy is both valid and leverages all the available
     * options.
     * 
     * @return True if clean.
     */
    public boolean isClean() {
        return (this.isValid() && this.warnings.size() == 0);
    }

    /**
     * Whether or not the strategy is valid. Invalid strategies may not be
     * accepted into the game.
     * 
     * @return True if valid.
     */
    public boolean isValid() {
        return (this.errors.size() == 0);
    }

    public DroomsStrategyValidator call() {
        final KieServices ks = KieServices.Factory.get();
        try {
            final KieContainer container = ks.newKieContainer(this.releaseId);
            final KieBaseConfiguration config = ks.newKieBaseConfiguration();
            config.setOption(EventProcessingOption.STREAM);
            final KieBase kbase = container.newKieBase(config);
            final KnowledgeSessionValidationHelper helper = new KnowledgeSessionValidationHelper(kbase);
            this.validateGlobal(helper, "logger", Logger.class, false);
            this.validateGlobal(helper, "tracker", PathTracker.class, false);
            this.validateEntryPoint(helper, "rewardEvents", true);
            this.validateEntryPoint(helper, "playerEvents", true);
            this.validateEntryPoint(helper, "gameEvents", true);
        } catch (RuntimeException ex) {
            // KieServices throw RuntimeException when KieModule or default KieBase is not found
            report(ex.getMessage(), true);
        }
        return this;
    }

    private void report(final String report, final boolean isError) {
        if (isError) {
            this.errors.add(report);
        } else {
            this.warnings.add(report);
        }
    }

    private void validateEntryPoint(final KnowledgeSessionValidationHelper helper, final String name, final boolean isError) {
        if (!helper.hasEntryPoint(name)) {
            this.report("Entry point '" + name + "' not declared.", isError);
        }
    }

    private void validateGlobal(final KnowledgeSessionValidationHelper helper, final String name, final Class<?> cls,
            final boolean isError) {
        if (!helper.hasGlobal(name, cls)) {
            this.report("Global '" + name + "' of type '" + cls.getCanonicalName() + "' not declared.", isError);
        }
    }

    @Override
    public String toString() {
        return "DroomsStrategyValidator [releaseId=" + releaseId + ", errors=" + errors.size() + ", warnings=" +
                warnings.size() + ']';
    }
}
