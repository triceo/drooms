package org.drooms.api;

import org.drools.KnowledgeBase;

public interface Strategy {

    public String getName();
    
    public KnowledgeBase getKnowledgeBase(ClassLoader cls);

}
