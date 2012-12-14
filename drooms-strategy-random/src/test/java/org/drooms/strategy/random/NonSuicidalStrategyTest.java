package org.drooms.strategy.random;

import org.drooms.api.Strategy;
import org.drooms.impl.util.DroomsTestHelper;

public class NonSuicidalStrategyTest extends DroomsTestHelper {

    private final Strategy strategy;
    
    public NonSuicidalStrategyTest() {
        this.strategy = new RandomNonSuicidalStrategy();
    }
    
    @Override
    public Strategy getStrategy() {
        return this.strategy;
    }
    
    

}
