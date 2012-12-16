package org.drooms.impl.util;

import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.ResultSeverity;
import org.drooms.api.Strategy;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a base test for a {@link Strategy} that will, when passed, ensure
 * that the strategy will be accepted by Drooms.
 */
public abstract class DroomsTestHelper {

    private static Logger LOGGER = LoggerFactory
            .getLogger(DroomsTestHelper.class);

    private static KnowledgeBuilder getKnowledgeBuilder(final Strategy strategy) {
        return strategy.getKnowledgeBuilder(DroomsTestHelper.class
                .getClassLoader());
    }

    /**
     * Provide the strategy under test.
     * 
     * @return The strategy to test.
     */
    public abstract Strategy getStrategy();

    /**
     * Make sure the strategy compiles.
     */
    @Test
    public void testCompileTime() {
        final KnowledgeBuilder kbuilder = DroomsTestHelper
                .getKnowledgeBuilder(this.getStrategy());
        Assert.assertFalse(
                kbuilder.getResults(ResultSeverity.ERROR).toString(),
                kbuilder.hasErrors());

    }

    /**
     * Make sure the strategy meets all the hard requirements.
     */
    @Test
    public void testRunTime() {
        final KnowledgeBuilder kbuilder = DroomsTestHelper
                .getKnowledgeBuilder(this.getStrategy());
        Assume.assumeFalse(kbuilder.hasErrors());
        final KnowledgeBase kbase = kbuilder.newKnowledgeBase();
        final DroomsKnowledgeSessionValidator validator = new DroomsKnowledgeSessionValidator(
                kbase);
        Assert.assertTrue(validator.getErrors().isEmpty() ? "Cannot happen!"
                : validator.getErrors().get(0), validator.isValid());
    }

    /**
     * Warn when strategy doesn't fulfill some of the soft requirements.
     */
    @Test
    public void testRunTimeCleanliness() {
        final KnowledgeBuilder kbuilder = DroomsTestHelper
                .getKnowledgeBuilder(this.getStrategy());
        Assume.assumeFalse(kbuilder.hasErrors());
        final KnowledgeBase kbase = kbuilder.newKnowledgeBase();
        final DroomsKnowledgeSessionValidator validator = new DroomsKnowledgeSessionValidator(
                kbase);
        Assume.assumeTrue(validator.isValid());
        if (validator.isClean()) {
            return;
        }
        for (final String message : validator.getWarnings()) {
            DroomsTestHelper.LOGGER.info("A strategy is incomplete: {}",
                    message);
        }
    }

    /**
     * Make sure the strategy comes from its own package.
     */
    @Test
    public void testVisibility() {
        final String name = this.getStrategy().getClass().getPackage()
                .getName();
        Assert.assertFalse(
                "Strategy cannot belong to the game implementation package.",
                name.startsWith("org.drooms.impl"));
    }

}
