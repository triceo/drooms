package org.drooms.strategy.random;

import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drooms.api.Strategy;

/**
 * This is an example strategy. It makes its moves by random choice, but makes
 * sure it doesn't hit a wall.
 */
public class RandomStrategy implements Strategy {

    public RandomStrategy() {
        // do nothing
    }

    @Override
    public KnowledgeBuilder getKnowledgeBuilder(final ClassLoader cls) {
        final KnowledgeBuilder kb = KnowledgeBuilderFactory
                .newKnowledgeBuilder();
        kb.add(ResourceFactory.newClassPathResource("random.drl",
                cls), ResourceType.DRL);
        return kb;
    }

    @Override
    public String getName() {
        return "Random";
    }

    @Override
    public boolean enableAudit() {
        return false;
    }
}
