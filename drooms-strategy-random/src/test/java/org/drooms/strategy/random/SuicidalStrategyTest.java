package org.drooms.strategy.random;

import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.ResultSeverity;
import org.junit.Assert;
import org.junit.Test;

public class SuicidalStrategyTest {

    @Test
    public void test() {
        final KnowledgeBuilder kb = new RandomSuicidalStrategy().getKnowledgeBuilder(this.getClass().getClassLoader());
        Assert.assertFalse(kb.getResults(ResultSeverity.ERROR).toString(), kb.hasErrors()); 
    }
    
    

}
