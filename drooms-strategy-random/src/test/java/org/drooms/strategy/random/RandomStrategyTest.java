package org.drooms.strategy.random;

import org.drooms.api.Strategy;
import org.drooms.impl.util.DroomsTestHelper;

public class RandomStrategyTest extends DroomsTestHelper {

    private final Strategy strategy;
    
    public RandomStrategyTest() {
        this.strategy = new RandomStrategy();
    }
    
    @Override
    public Strategy getStrategy() {
        return this.strategy;
    }
    
    

}
