package org.drooms.api;

import org.drools.KnowledgeBase;

public interface Strategy {

    public KnowledgeBase getKnowledgeBase(ClassLoader cls);

    public String getName();

}
