package org.drooms.api;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;

import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.definition.KnowledgePackage;
import org.junit.Assert;
import org.junit.Test;

public class PlayerTest {

    private static Collection<KnowledgePackage> obtainKnowledgePackages() {
        return KnowledgeBuilderFactory.newKnowledgeBuilder().getKnowledgePackages();
    }
    
    private static ClassLoader obtainClassLoader() {
        return new URLClassLoader(new URL[0]);
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullKBase() {
        new Player("test", null, PlayerTest.obtainClassLoader());
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullName() {
        new Player(null, PlayerTest.obtainKnowledgePackages(), PlayerTest.obtainClassLoader());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullClassLoader() {
        new Player("test", PlayerTest.obtainKnowledgePackages(), null);
    }

    public void testEquals() {
        final Collection<KnowledgePackage> packages = PlayerTest.obtainKnowledgePackages();
        final ClassLoader loader = PlayerTest.obtainClassLoader();
        final Player p1 = new Player("test", packages, loader);
        final Player p2 = new Player("test", packages, loader);
        final Player p3 = new Player("test", PlayerTest.obtainKnowledgePackages(), loader);
        Assert.assertEquals(p1, p1);
        Assert.assertEquals(p1, p2);
        Assert.assertEquals(p2, p3);
        Assert.assertEquals(p1, p3);
    }

    public void testNotEqualsName() {
        final Collection<KnowledgePackage> packages = PlayerTest.obtainKnowledgePackages();
        final ClassLoader loader = PlayerTest.obtainClassLoader();
        final Player p1 = new Player("test", packages, loader);
        final Player p2 = new Player("test2", packages, loader);
        final Player p3 = new Player("test2", PlayerTest.obtainKnowledgePackages(), loader);
        Assert.assertNotEquals(p1, p2);
        Assert.assertNotEquals(p2, p3);
        Assert.assertNotEquals(p1, p3);
    }
}
