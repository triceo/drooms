package org.drooms.strategy.hungry;

import org.drooms.api.Strategy;
import org.drooms.impl.util.DroomsTestHelper;

public class SimpleHungryStrategyTest extends DroomsTestHelper {

    private final Strategy strategy;
    
    public SimpleHungryStrategyTest() {
        this.strategy = new SimpleHungryStrategy();
    }
    
    @Override
    public Strategy getStrategy() {
        return this.strategy;
    }
    
    

}
