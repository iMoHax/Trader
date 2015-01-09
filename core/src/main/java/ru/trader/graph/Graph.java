package ru.trader.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.function.Predicate;

public class Graph<T extends Connectable<T>> {
    private final static ForkJoinPool POOL = new ForkJoinPool();
    private final static int THRESHOLD = 4;
    private final static int DEFAULT_COUNT = 200;

    @FunctionalInterface
    public interface PathConstructor<E extends Connectable<E>> {
        Path<E> build(Vertex<E> source);
    }

    private final static Logger LOG = LoggerFactory.getLogger(Graph.class);

    protected final Vertex<T> root;
    protected final Map<T,Vertex<T>> vertexes;

    protected final double stock;
    protected final double maxDistance;
    protected final boolean withRefill;
    private final PathConstructor<T> pathFabric;
    protected int minJumps;


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
        vertexes = new ConcurrentHashMap<>(50, 0.9f, THRESHOLD);
        vertexes.put(root.getEntry(), root);
        build(root, set, maxDeep, stock);
    }

    private void build(Vertex<T> root, Collection<T> set, int maxDeep, double stock) {
        POOL.invoke(new GraphBuilder(root, set, maxDeep - 1, stock));
        if (set.size() > vertexes.size()){
            minJumps = maxDeep;
        } else {
            minJumps = 1;
            for (Vertex<T> vertex : vertexes.values()) {
                int jumps = maxDeep - vertex.getLevel();
                if (jumps > minJumps) minJumps = jumps;
            }
        }
    }

    public boolean isAccessible(T entry){
        return vertexes.containsKey(entry);
    }

    public Vertex<T> getVertex(T entry){
        return vertexes.get(entry);
    }

    private void findPathsTo(Vertex<T> target, TopList<Path<T>> res, int deep){
        POOL.invoke(new PathFinder(res, pathFabric.build(root), target, deep-1, stock));
    }

    public List<Path<T>> getPathsTo(T entry){
        return getPathsTo(entry, DEFAULT_COUNT);
    }

    public List<Path<T>> getPathsTo(T entry, int max){
        return getPathsTo(entry, max, root.getLevel()).getList();
    }

    public TopList<Path<T>> getPathsTo(T entry, int max, int deep){
        Vertex<T> target = getVertex(entry);
        TopList<Path<T>> paths = newTopList(max);
        findPathsTo(target, paths, deep);
        paths.finish();
        return paths;
    }

    public List<Path<T>> getPaths(int count){
        return getPaths(count, root.getLevel()).getList();
    }

    public TopList<Path<T>> getPaths(int count, int deep){
        TopList<Path<T>> paths = newTopList(count);
        for (Vertex<T> target : vertexes.values()) {
            TopList<Path<T>> p = newTopList(minJumps);
            findPathsTo(target, p, deep);
            for (Path<T> path : p.getList()) {
                paths.add(path);
            }
        }
        paths.finish();
        return paths;
    }

    protected TopList<Path<T>> newTopList(int count){
        return new TopList<>(count);
    }

    public Path<T> getFastPathTo(T entry){
        Vertex<T> target = getVertex(entry);
        if (target == null) return null;
        return findFastPath(pathFabric.build(root), target, target.getLevel()+1, stock);
    }

    private Path<T> findFastPath(Path<T> head, Vertex<T> target, int deep, double limit) {
        Vertex<T> source = head.getTarget();
        LOG.trace("Find fast path from {} to {}, deep {}, limit {}, head {}", source, target, deep, limit, head);
        DistanceFilter distanceFilter = new DistanceFilter(limit, source.getEntry());
        if (deep == source.getLevel()){
            Optional<Edge<T>> last = source.getEdges().parallelStream()
                           .filter(next -> next.isConnect(target) && distanceFilter.test(next.getLength()) && !head.isConnect(next.getTarget()))
                           .findFirst();
            if (last.isPresent()){
                Path<T> path = head.connectTo(last.get().getTarget(), limit < last.get().getLength());
                path.finish();
                LOG.trace("Last edge find, path {}", path);
                return path;
            }
        }
        if (deep < source.getLevel()){
            LOG.trace("Search around");
            Optional<Path<T>> res = source.getEdges().parallelStream()
                    .filter(next -> next.getTarget().getLevel() < source.getLevel() && distanceFilter.test(next.getLength()))
                    .map((next) -> {
                        Path<T> path = head.connectTo(next.getTarget(), limit < next.getLength());
                        double nextLimit = withRefill ? limit - next.getLength(): stock;
                        // refill
                        if (nextLimit < 0 ) nextLimit = stock - next.getLength();
                        return findFastPath(path, target, deep, nextLimit);
                    })
                    .filter(path -> path != null)
                    .findFirst();
            if (res.isPresent()) return res.get();
        }
        return null;
    }

    public T getRoot() {
        return root.getEntry();
    }

    public int getMinJumps() {
        return minJumps;
    }

    private class DistanceFilter implements Predicate<Double> {
        private final double limit;
        private final T source;

        private DistanceFilter(double limit, T source) {
            this.limit = limit;
            this.source = source;
        }

        @Override
        public boolean test(Double distance) {
            return distance <= Math.min(limit, maxDistance) || (withRefill && distance <= maxDistance  &&  source.canRefill());
        }
    }

    private class GraphBuilder extends RecursiveAction {
        private final Vertex<T> vertex;
        private final Collection<T> set;
        private final int deep;
        private final double limit;
        private final DistanceFilter distanceFilter;

        private GraphBuilder(Vertex<T> vertex, Collection<T> set, int deep, double limit) {
            this.vertex = vertex;
            this.set = set;
            this.deep = deep;
            this.limit = limit;
            distanceFilter = new DistanceFilter(limit, vertex.getEntry());
        }

        @Override
        protected void compute() {
            LOG.trace("Build graph from {}, limit {}, deep {}", vertex, limit, deep);
            ArrayList<GraphBuilder> subTasks = new ArrayList<>(set.size());
            Iterator<T> iterator = set.iterator();
            while (iterator.hasNext()) {
                T entry = iterator.next();
                if (entry == vertex.getEntry()) continue;
                double distance = vertex.getEntry().getDistance(entry);
                if (distanceFilter.test(distance)) {
                    Vertex<T> next = vertexes.get(entry);
                    if (next == null) {
                        LOG.trace("Is new vertex");
                        next = new Vertex<>(entry);
                        vertexes.put(entry, next);
                    }
                    LOG.trace("Add edge from {} to {}", vertex, next);
                    vertex.addEdge(new Edge<>(vertex, next));
                    // If level >= deep when vertex already added on upper deep
                    if (next.getLevel() < deep) {
                        next.setLevel(vertex.getLevel() - 1);
                        if (deep > 0) {
                            double nextLimit = withRefill ? limit - distance : stock;
                            if (nextLimit < 0) {
                                LOG.trace("Refill");
                                nextLimit = stock - distance;
                            }
                            //Recursive build
                            GraphBuilder task = new GraphBuilder(next, set, deep - 1, nextLimit);
                            task.fork();
                            subTasks.add(task);
                        }
                    }
                } else {
                    LOG.trace("Vertex {} is far away, {}", entry, distance);
                }
                if (subTasks.size() == THRESHOLD || !iterator.hasNext()){
                    for (GraphBuilder subTask : subTasks) {
                        subTask.join();
                    }
                    subTasks.clear();
                }
            }
            if (!subTasks.isEmpty()){
                for (GraphBuilder subTask : subTasks) {
                    subTask.join();
                }
                subTasks.clear();
            }
            LOG.trace("End build graph from {} on deep {}", vertex, deep);
        }
    }

    private class PathFinder extends RecursiveAction {
        private final TopList<Path<T>> paths;
        private final Path<T> head;
        private final Vertex<T> target;
        private final int deep;
        private final double limit;
        private final DistanceFilter distanceFilter;

        private PathFinder(TopList<Path<T>> paths, Path<T> head, Vertex<T> target, int deep, double limit) {
            this.paths = paths;
            this.head = head;
            this.target = target;
            this.deep = deep;
            this.limit = limit;
            distanceFilter = new DistanceFilter(limit, head.getTarget().getEntry());
        }

        @Override
        protected void compute() {
            if (target == null || isCancelled()) return;
            Vertex<T> source = head.getTarget();
            LOG.trace("Find path to deep from {} to {}, deep {}, limit {}, head {}", source, target, deep, limit, head);
            Edge<T> edge = source.getEdge(target);
            if (edge != null){
                if (distanceFilter.test(edge.getLength())){
                    Path<T> path = head.connectTo(edge.getTarget(), limit < edge.getLength());
                    path.finish();
                    LOG.trace("Last edge find, add path {}", path);
                    synchronized (paths){
                        if (!paths.add(path)) complete(null);
                    }
                }
            }
            if (deep > 0 ){
                if (source.getEdgesCount() > 0){
                    LOG.trace("Search around");
                    ArrayList<PathFinder> subTasks = new ArrayList<>(source.getEdges().size());
                    Iterator<Edge<T>> iterator = source.getEdges().iterator();
                    while (iterator.hasNext()) {
                        Edge<T> next = iterator.next();
                        if (isDone()) break;
                        // target already added if source consist edge
                        if (next.isConnect(target)) continue;
                        if (!distanceFilter.test(next.getLength())) continue;
                        Path<T> path = head.connectTo(next.getTarget(), limit < next.getLength());
                        double nextLimit = withRefill ? limit - next.getLength() : stock;
                        // refill
                        if (nextLimit < 0) nextLimit = stock - next.getLength();
                        //Recursive search
                        PathFinder task = new PathFinder(paths, path, target, deep - 1, nextLimit);
                        task.fork();
                        subTasks.add(task);
                        if (subTasks.size() == THRESHOLD || !iterator.hasNext()){
                            for (PathFinder subTask : subTasks) {
                                if (isDone()) {
                                    subTask.cancel(false);
                                } else {
                                    subTask.join();
                                }
                            }
                            subTasks.clear();
                        }
                    }
                    if (!subTasks.isEmpty()){
                        for (PathFinder subTask : subTasks) {
                            if (isDone()) {
                                subTask.cancel(false);
                            } else {
                                subTask.join();
                            }
                        }
                        subTasks.clear();
                    }
                }
            }
        }
    }


}
