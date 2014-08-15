package ru.trader.graph;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VertexTest extends Assert {
    private final static Logger LOG = LoggerFactory.getLogger(VertexTest.class);

    private final static Point x1 = new Point("x1",1);
    private final static Point x2 = new Point("x2",4);
    private final static Point x3 = new Point("x3",-2);
    private final static Point x4 = new Point("x4",5);


    @Test
    public void testContains() throws Exception {
        LOG.info("Start vertex contains test");

        Vertex<Point> vertex = new Vertex<>(x1);
        vertex.addEdge(new Edge<>(vertex, new Vertex<>(x2)));
        vertex.addEdge(new Edge<>(vertex, x3));

        assertFalse("Vertex must contains entry",vertex.isConnected(x1));
        assertTrue("Vertex must contains entry",vertex.isConnected(new Vertex<>(x3)));
        assertTrue("Vertex must contains entry",vertex.isConnected(x2));
        assertFalse("Vertex not must contains entry", vertex.isConnected(new Vertex<>(x4)));
    }
}
