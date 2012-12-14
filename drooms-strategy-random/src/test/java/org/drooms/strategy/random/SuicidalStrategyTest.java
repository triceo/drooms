package org.drooms.strategy.random;

import org.drooms.api.Strategy;
import org.drooms.impl.util.DroomsTestHelper;

public class SuicidalStrategyTest extends DroomsTestHelper {

    private final Strategy strategy;
    
    public SuicidalStrategyTest() {
        this.strategy = new RandomSuicidalStrategy();
    }
    
    @Override
    public Strategy getStrategy() {
        return this.strategy;
    }
    
    

}
