package org.drooms.impl.util;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.ReleaseId;
import org.kie.api.io.Resource;

import java.util.List;

public class DroomsStrategyValidatorTest {

    @Test
    public void testInvalidReleaseId() {
        final ReleaseId releaseId = KieServices.Factory.get().newReleaseId("this", "artifact", "is.invalid");
        final DroomsStrategyValidator validator = DroomsStrategyValidator.getInstance(releaseId);
        Assert.assertFalse(validator.isValid());
        final List<String> errors = validator.getErrors();
        Assertions.assertThat(errors).containsOnly("Cannot find KieModule: this:artifact:is.invalid");
    }

    @Test
    public void testMissingDefaultKieBase() {
        final ReleaseId releaseId = deployArtifact("test-strategy-1.0");
        final DroomsStrategyValidator validator = DroomsStrategyValidator.getInstance(releaseId);
        Assert.assertFalse(validator.isValid());
        final List<String> errors = validator.getErrors();
        /*
         * TODO fix typo once we upgrade to codebase with this commit:
         * https://github.com/droolsjbpm/drools/commit/fa1db09a5a6c0ac076ac3f0bcef39286716d8a65
         */
        Assertions.assertThat(errors).containsOnly("Cannot find a defualt KieBase");
    }

    @Test
    public void testMissingEntryPoints() {
        final ReleaseId releaseId = deployArtifact("test-strategy-2.0");
        final DroomsStrategyValidator validator = DroomsStrategyValidator.getInstance(releaseId);
        Assert.assertFalse(validator.isValid());
        final List<String> errors = validator.getErrors();
        Assertions.assertThat(errors).containsOnly("Entry point 'playerEvents' not declared.",
                "Entry point 'gameEvents' not declared.");
    }

    @Test
    public void testMissingLogger() {
        final ReleaseId releaseId = deployArtifact("test-strategy-3.0");
        final DroomsStrategyValidator validator = DroomsStrategyValidator.getInstance(releaseId);
        Assert.assertTrue(validator.isValid());
        Assert.assertFalse(validator.isClean());
        final List<String> warnings = validator.getWarnings();
        Assertions.assertThat(warnings).containsOnly("Global 'logger' of type 'org.slf4j.Logger' not declared.",
                "Global 'tracker' of type 'org.drooms.impl.logic.PathTracker' not declared.");
    }

    private ReleaseId deployArtifact(final String jarName) {
        final KieServices ks = KieServices.Factory.get();
        final Resource resource = ks.getResources().newClassPathResource(jarName, getClass());
        final KieRepository repository = ks.getRepository();
        return repository.addKieModule(resource).getReleaseId();
    }
}
