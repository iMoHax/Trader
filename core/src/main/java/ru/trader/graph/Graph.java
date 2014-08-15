package ru.trader.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class Graph<T extends Connectable<T>> {

    @FunctionalInterface
    public interface PathConstructor<E extends Connectable<E>> {
        Path<E> build(Vertex<E> source);
    }

    private final static Logger LOG = LoggerFactory.getLogger(Graph.class);

    private final Vertex<T> root;
    private final HashMap<T,Vertex<T>> vertexes;

    private final double stock;
    private final double maxDistance;
    private final boolean withRefill;
    private final PathConstructor<T> pathFabric;


    public Graph(T start, Collection<T> set, double stock, int maxDeep) {
        this(start, set, stock, stock, false, maxDeep, Path::new);
    }

    public Graph(T start, Collection<T> set, double stock, double maxDistance, int maxDeep) {
        this(start, set, stock, maxDistance, true, maxDeep, Path::new);
    }

    public Graph(T start, Collection<T> set, double stock, boolean withRefill, int maxDeep) {
        this(start, set, stock, stock, withRefill, maxDeep, Path::new);
    }

    public Graph(T start, Collection<T> set, double stock, boolean withRefill, int maxDeep, PathConstructor<T> pathFabric) {
        this(start, set, stock, stock, withRefill, maxDeep, pathFabric);
    }

    public Graph(T start, Collection<T> set, double stock, double maxDistance, boolean withRefill, int maxDeep, PathConstructor<T> pathFabric) {
        this.maxDistance = maxDistance;
        this.stock = stock;
        this.withRefill = withRefill;
        this.pathFabric = pathFabric;
        root = new Vertex<>(start);
        root.setLevel(maxDeep);
        vertexes = new HashMap<>();
        vertexes.put(root.getEntry(), root);
        buildGraph(root, set, maxDeep-1, stock);
    }

    private void buildGraph(Vertex<T> vertex, Collection<T> set, int deep, double limit) {
        LOG.trace("Build graph from {}, limit {}, deep {}", vertex, limit, deep);
        for (T entry : set) {
            if (entry == vertex.getEntry()) continue;
            double distance = vertex.getEntry().getDistance(entry);
            if (distance <= this.maxDistance){
                if (withRefill && distance > limit && !vertex.getEntry().canRefill()){
                    LOG.trace("Vertex {} is far away, {}", entry, distance);
                    continue;
                }
                Vertex<T> next = vertexes.get(entry);
                if (next == null){
                    LOG.trace("Is new vertex");
                    next = new Vertex<>(entry);
                    vertexes.put(entry, next);
                }
                LOG.trace("Add edge from {} to {}", vertex, next);
                Edge<T> edge = new Edge<>(vertex, next);
                double nextLimit = withRefill ? limit - edge.getLength(): stock;
                if (nextLimit < 0) {
                    LOG.trace("Refill");
                    nextLimit = stock - edge.getLength();
                }
                vertex.addEdge(edge);
                // If level >= deep when vertex already added on upper deep
                if (next.getLevel() < deep){
                    next.setLevel(vertex.getLevel()-1);
                    if (deep > 0){
                        buildGraph(next, set, deep-1, nextLimit);
                    }
                }
            }
        }
        LOG.trace("End build graph from {} on deep {}", vertex, deep);
    }

    public boolean isAccessible(T entry){
        return vertexes.containsKey(entry);
    }

    public Vertex<T> getVertex(T entry){
        return vertexes.get(entry);
    }

    public Collection<Path<T>> getPathsTo(T entry, boolean all){
        Vertex<T> target = getVertex(entry);
        return findPaths(pathFabric.build(root), target, root.getLevel()-1, stock, all);
    }


    private Collection<Path<T>> findPaths(Path<T> head, Vertex<T> target, int deep, double limit, boolean all){
        Collection<Path<T>> paths = new ArrayList<>();
        if (target == null) return paths;
        boolean found =false;
        Vertex<T> source = head.getTarget();
        LOG.trace("Find path to deep from {} to {}, deep {}, limit {}, head {}", source, target, deep, limit, head);
        Edge<T> edge = source.getEdge(target);
        if (edge != null ){
            if (!(withRefill && Math.min(limit, maxDistance) < edge.getLength() && !source.getEntry().canRefill())){
                found = true;
                Path<T> path = head.connectTo(edge, limit < edge.getLength());
                path.finish();
                LOG.trace("Last edge find, add path {}", path);
                paths.add(path);
            }
        }
        if ((all || !found) && deep > 0 ){
            if (source.getEdgesCount() > 0){
                LOG.trace("Search around");
                for (Edge<T> next : source.getEdges()) {
                    if (withRefill && Math.min(limit, maxDistance) < next.getLength() && !source.getEntry().canRefill()) continue;
                    if (head.contains(next)) continue;
                    // target already added if source consist edge
                    if (next.isConnect(target)) continue;
                    Path<T> path = head.connectTo(next, limit < next.getLength());
                    double nextLimit = withRefill ? limit - next.getLength(): stock;
                    // refill
                    if (nextLimit < 0 ) nextLimit = maxDistance - next.getLength();
                    paths.addAll(findPaths(path, target, deep - 1, nextLimit, all));
                    if (!all && !paths.isEmpty()) break;
                }
            }
        }
        return paths;
    }


    public Path<T> getFastPathTo(T entry){
        Vertex<T> target = getVertex(entry);
        return findFastPath(pathFabric.build(root), target, target.getLevel()+1, stock);
    }

    private Path<T> findFastPath(Path<T> head, Vertex<T> target, int deep, double limit) {
        Vertex<T> source = head.getTarget();
        LOG.trace("Find fast path from {} to {}, deep {}, limit {}, head {}", source, target, deep, limit, head);
        if (deep == source.getLevel()){
            for (Edge<T> next : source.getEdges()) {
                if (withRefill && Math.min(limit, maxDistance) < next.getLength() && !source.getEntry().canRefill()) continue;
                if (head.contains(next)) continue;
                if (next.isConnect(target)) {
                    Path<T> path = head.connectTo(next, limit < next.getLength());
                    path.finish();
                    LOG.trace("Last edge find, path {}", path);
                    return path;
                }
            }
        }
        if (deep < source.getLevel()){
            LOG.trace("Search around");
            for (Edge<T> next : source.getEdges()) {
                if (next.getTarget().getLevel() >= source.getLevel()) continue;
                if (withRefill && Math.min(limit, maxDistance) < next.getLength() && !source.getEntry().canRefill()) continue;
                Path<T> path = head.connectTo(next, limit < next.getLength());
                double nextLimit = withRefill ? limit - next.getLength(): stock;
                // refill
                if (nextLimit < 0 ) nextLimit = stock - next.getLength();
                Path<T> res = findFastPath(path, target, deep, nextLimit);
                if (res != null) return res;
            }
        }
        return null;
    }

    public T getRoot() {
        return root.getEntry();
    }
}
