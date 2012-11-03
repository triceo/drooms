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
public class RandomSuicidalStrategy implements Strategy {
    
    public RandomSuicidalStrategy() {
        // do nothing
    }

    @Override
    public String getName() {
        return "Random Suicidal";
    }

    @Override
    public KnowledgeBase getKnowledgeBase(ClassLoader cls) {
        KnowledgeBuilder kb = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kb.add(ResourceFactory.newClassPathResource("random-suicidal.drl", cls), ResourceType.DRL);
        return kb.newKnowledgeBase();
    }
}
