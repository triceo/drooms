package org.drooms.api;

import org.drools.builder.KnowledgeBuilder;

public interface Strategy {

    public KnowledgeBuilder getKnowledgeBuilder(ClassLoader cls);

    public String getName();

}
