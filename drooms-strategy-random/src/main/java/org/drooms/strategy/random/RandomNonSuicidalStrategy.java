package org.drooms.strategy.random;

import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drooms.api.Strategy;

/**
 * Hello world!
 * 
 */
public class RandomNonSuicidalStrategy implements Strategy {

    public RandomNonSuicidalStrategy() {
        // do nothing
    }

    @Override
    public KnowledgeBase getKnowledgeBase(final ClassLoader cls) {
        final KnowledgeBuilder kb = KnowledgeBuilderFactory
                .newKnowledgeBuilder();
        kb.add(ResourceFactory.newClassPathResource("random-nonsuicidal.drl",
                cls), ResourceType.DRL);
        return kb.newKnowledgeBase();
    }

    @Override
    public String getName() {
        return "Random Non-Suicidal";
    }
}
