package org.drooms.strategy.hungry;

import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderConfiguration;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drooms.api.CustomPathBasedStrategy;
import org.drooms.api.Edge;
import org.drooms.api.Node;
import org.drooms.impl.util.shortestpath.astar.UnweightedAStarShortestPath;
import org.drooms.impl.util.shortestpath.astar.UnweightedAStarShortestPath.VertexDistanceHeuristics;

import edu.uci.ics.jung.algorithms.shortestpath.ShortestPath;
import edu.uci.ics.jung.graph.Graph;

public class SimpleHungryStrategy implements CustomPathBasedStrategy {

    public SimpleHungryStrategy() {
        // do nothing
    }

    @Override
    public KnowledgeBuilder getKnowledgeBuilder(final ClassLoader cls) {
        final KnowledgeBuilderConfiguration kbuilderConf = KnowledgeBuilderFactory
                .newKnowledgeBuilderConfiguration(null, cls);
        final KnowledgeBuilder kb = KnowledgeBuilderFactory
                .newKnowledgeBuilder(kbuilderConf);
        kb.add(ResourceFactory.newClassPathResource("hungry-simple.drl", cls),
                ResourceType.DRL);
        return kb;
    }

    @Override
    public String getName() {
        return "Simple Hungry";
    }

    @Override
    public ShortestPath<Node, Edge> getShortestPathAlgorithm(
            final Graph<Node, Edge> graph) {
        return new UnweightedAStarShortestPath<>(graph,
                VertexDistanceHeuristics.MANHATTAN);
    }

    @Override
    public boolean enableAudit() {
        return false;
    }
}
