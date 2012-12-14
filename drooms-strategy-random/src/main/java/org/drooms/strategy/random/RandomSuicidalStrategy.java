package org.drooms.strategy.random;

import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drooms.api.Strategy;

/**
 * This is an example strategy. It makes its moves completely by random choice.
 */
public class RandomSuicidalStrategy implements Strategy {

    public RandomSuicidalStrategy() {
        // do nothing
    }

    @Override
    public KnowledgeBuilder getKnowledgeBuilder(final ClassLoader cls) {
        final KnowledgeBuilder kb = KnowledgeBuilderFactory
                .newKnowledgeBuilder();
        kb.add(ResourceFactory.newClassPathResource("random-suicidal.drl", cls),
                ResourceType.DRL);
        return kb;
    }

    @Override
    public String getName() {
        return "Random Suicidal";
    }
}
