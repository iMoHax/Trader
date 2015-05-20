package ru.trader.analysis.graph;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;

public class Crawler<T> {
    private final static ForkJoinPool POOL = new ForkJoinPool();
    private final static int THRESHOLD = 4;
    private final static Logger LOG = LoggerFactory.getLogger(Crawler.class);

    protected final Graph<T> graph;
    private final Consumer<List<Edge<T>>> onFoundFunc;
    private int maxSize;

    public Crawler(Graph<T> graph, Consumer<List<Edge<T>>> onFoundFunc) {
        this.graph = graph;
        maxSize = graph.getRoot().getLevel();
        this.onFoundFunc = onFoundFunc;
    }

    protected List<Edge<T>> getCopyList(List<Edge<T>> head, Edge<T> tail){
        List<Edge<T>> res = new ArrayList<>(20);
        res.addAll(head);
        res.add(tail);
        return res;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public void findFast(T target){
        findFast(target, 1);
    }

    public void findFast(T target, int count){
        Vertex<T> t = graph.getVertex(target);
        int found = 0;
        if (t != null) {
            if (count > 1) {
                Vertex<T> s = graph.getRoot();
                s.sortEdges();
                found = bfs(start(s), target, 0, count);
            } else {
                found = dfs(start(graph.getRoot()), target, t.getLevel() + 1, count);
            }
        }
        LOG.debug("Found {} paths", found);
    }

    public void findMin(T target){
        findMin(target, 1);
    }

    public void findMin(T target, int count){
        Vertex<T> t = graph.getVertex(target);
        int found = 0;
        if (t != null) {
            found = ucs(costStart(graph.getRoot()), target, 0, count);
        }
        LOG.debug("Found {} paths", found);
    }

    protected TraversalEntry start(Vertex<T> vertex){
        return new TraversalEntry(new ArrayList<>(), vertex);
    }

    protected CostTraversalEntry costStart(Vertex<T> vertex){
        return new CostTraversalEntry(new ArrayList<>(), vertex);
    }

    protected TraversalEntry travers(TraversalEntry entry, Edge<T> edge){
        return new TraversalEntry(getCopyList(entry.head, edge), edge.getTarget());
    }

    private CostTraversalEntry costTravers(CostTraversalEntry entry, Edge<T> edge){
        return costTravers(entry, getCopyList(entry.head, edge), edge);
    }

    protected CostTraversalEntry costTravers(CostTraversalEntry entry, List<Edge<T>> head, Edge<T> edge){
        return new CostTraversalEntry(head, edge, entry.getWeight());
    }

    private int dfs(TraversalEntry entry, T target, int deep, int count) {
        int found = 0;
        List<Edge<T>> head = entry.head;
        Vertex<T> source = entry.vertex;
        LOG.trace("DFS from {} to {}, deep {}, count {}, head {}", source, target, deep, count, head);
        if (deep == source.getLevel()){
            for (Edge<T> next : entry.getEdges()) {
                if (next.isConnect(target)){
                    List<Edge<T>> res = getCopyList(head, next);
                    LOG.debug("Last edge find, path {}", res);
                    onFoundFunc.accept(res);
                    found++;
                    break;
                }
            }
        }
        if (found < count){
            if (deep < source.getLevel() && head.size() < maxSize-1) {
                LOG.trace("Search around");
                for (Edge<T> edge : entry.getEdges()) {
                    if (edge.getTarget().isSingle()) continue;
                    found += dfs(travers(entry, edge), target, deep, count-found);
                    if (found >= count) break;
                }
            }
        }
        return found;
    }

    private int bfs(TraversalEntry root, T target, int deep, int count) {
        LOG.trace("BFS from {} to {}, deep {}, count {}", root.vertex, target, deep, count);
        int found = 0;
        LinkedList<TraversalEntry> queue = new LinkedList<>();
        queue.add(root);
        while (!queue.isEmpty() && count > found){
            TraversalEntry entry = queue.poll();
            List<Edge<T>> head = entry.head;
            Vertex<T> source = entry.vertex;
            if (head.size() >= maxSize){
                LOG.trace("Is limit deep");
                continue;
            }
            LOG.trace("Search from {} to {}, head {}", source, target, head);
            Iterator<Edge<T>> iterator = entry.iterator();
            while (iterator.hasNext()){
                Edge<T> edge = iterator.next();
                if (edge.isConnect(target)){
                    List<Edge<T>> res = getCopyList(head, edge);
                    LOG.debug("Last edge find, path {}", res);
                    onFoundFunc.accept(res);
                    found++;
                }
                if (found >= count) break;
                if (edge.getTarget().isSingle()) continue;
                if (deep < source.getLevel()) {
                    edge.getTarget().sortEdges();
                    queue.add(travers(entry, edge));
                }
            }
        }
        return found;
    }

    private int ucs(CostTraversalEntry root, T target, int deep, int count) {
        LOG.trace("UCS from {} to {}, deep {}, count {}", root.vertex, target, deep, count);
        int found = 0;
        PriorityQueue<CostTraversalEntry> queue = new PriorityQueue<>();
        queue.add(root);
        while (!queue.isEmpty() && count > found){
            CostTraversalEntry entry = queue.poll();
            LOG.trace("Check path head {}, edge {}, weight {}", entry.head, entry.edge, entry.weight);
            List<Edge<T>> head = entry.head;
            Vertex<T> source = entry.vertex;
            Edge<T> edge = entry.edge;
            if (edge != null) {
                if (edge.isConnect(target)) {
                    List<Edge<T>> res = getCopyList(head, edge);
                    LOG.debug("Path found {}", res);
                    onFoundFunc.accept(res);
                    found++;
                    if (found >= count) break;
                }
                if (edge.getTarget().isSingle() || deep >= source.getLevel()){
                    continue;
                }
                head = getCopyList(entry.head, edge);
            }
            if (head.size() >= maxSize){
                LOG.trace("Is limit deep");
                continue;
            }
            Iterator<Edge<T>> iterator = entry.iterator();
            //put only 2 entry for iterate
            while (iterator.hasNext()){
                edge = iterator.next();
                if (deep < source.getLevel() && !edge.getTarget().isSingle() || edge.isConnect(target)) {
                    LOG.trace("Add edge {} to queue", edge);
                    queue.add(costTravers(entry, head, edge));
                }
            }
        }
        return found;
    }

    //last edge don't compare
    private int ucs2(CostTraversalEntry root, T target, int deep, int count) {
        LOG.trace("UCS2 from {} to {}, deep {}, count {}", root.vertex, target, deep, count);
        int found = 0;
        PriorityQueue<CostTraversalEntry> queue = new PriorityQueue<>();
        queue.add(root);
        while (!queue.isEmpty() && count > found){
            CostTraversalEntry entry = queue.peek();
            List<Edge<T>> head = entry.edge != null ? getCopyList(entry.head, entry.edge) : entry.head;
            Vertex<T> source = entry.vertex;
            Iterator<Edge<T>> iterator = entry.iterator();
            LOG.trace("Check path head {}, weight {}", head, entry.weight);
            int i = 0;
            //put only 2 entry for iterate
            while (iterator.hasNext() && i < 2){
                Edge<T> edge = iterator.next();
                LOG.trace("Check edge {}", edge);
                if (edge.isConnect(target)) {
                    List<Edge<T>> res = getCopyList(head, edge);
                    LOG.debug("Last edge find, path {}", res);
                    onFoundFunc.accept(res);
                    found++;
                }
                if (found >= count) break;
                if (edge.getTarget().isSingle()) continue;
                if (deep < source.getLevel() && head.size() < maxSize-1) {
                    edge.getTarget().sortEdges();
                    queue.add(costTravers(entry, head, edge));
                    i++;
                }
            }
            if (!iterator.hasNext()) queue.poll();
        }
        return found;
    }

    protected class TraversalEntry {
        protected final List<Edge<T>> head;
        protected final Vertex<T> vertex;
        private Iterator<Edge<T>> iterator;

        protected TraversalEntry(List<Edge<T>> head, Vertex<T> vertex) {
            this.head = head;
            this.vertex = vertex;
        }

        public Iterator<Edge<T>> iterator(){
            if (iterator == null){
                iterator = getIteratorInstance();
            }
            return iterator;
        }

        protected Iterator<Edge<T>> getIteratorInstance(){
            return vertex.getEdges().iterator();
        }

        private Iterable<Edge<T>> getEdges(){
            return this::iterator;
        }
    }

    protected class CostTraversalEntry extends TraversalEntry implements Comparable<CostTraversalEntry>{
        private final Edge<T> edge;
        private final double cost;
        private Double weight;

        protected CostTraversalEntry(List<Edge<T>> head, Vertex<T> vertex) {
            super(head, vertex);
            this.edge = null;
            this.cost = 0;
        }

        protected CostTraversalEntry(List<Edge<T>> head, Edge<T> edge, double cost) {
            super(head, edge.getTarget());
            this.edge = edge;
            this.cost = cost;
        }

        protected double getWeight(){
            if (weight == null){
                weight = cost + (edge !=null ? edge.getWeight() : 0);
            }
            return weight;
        }

        @Override
        public int compareTo(@NotNull CostTraversalEntry other) {
            int cmp = Double.compare(getWeight(), other.getWeight());
            if (cmp != 0) return cmp;
            return Integer.compare(head.size(), other.head.size());
        }
    }
/*
    private class PathFinder extends RecursiveAction {
        private final TopList<Path<T>> paths;
        private final Path<T> head;
        private final Vertex<T> target;

        private PathFinder(TopList<Path<T>> paths, Path<T> head, Vertex<T> target) {
            this.paths = paths;
            this.head = head;
            this.target = target;
        }

        @Override
        protected void compute() {
            if (target == null || isCancelled()) return;
            Vertex<T> source = head.getTarget();
            LOG.trace("Find path to deep from {} to {}, head {}", source, target, head);
            Edge<T> edge = source.getEdge(target);
            if (edge != null){
                Path<T> path = head.connectTo(edge.getTarget(), limit < edge.getLength());
                path.finish();
                LOG.trace("Last edge find, add path {}", path);
                synchronized (paths){
                    if (!paths.add(path)) complete(null);
                }
                callback.onFound();
            }
            if (!source.isSingle()){
                LOG.trace("Search around");
                ArrayList<PathFinder> subTasks = new ArrayList<>(source.getEdges().size());
                Iterator<Edge<T>> iterator = source.getEdges().iterator();
                while (iterator.hasNext()) {
                    Edge<T> next = iterator.next();
                    if (isDone() || callback.isCancel()) break;
                    // target already added if source consist edge
                    if (next.isConnect(target)) continue;
                    Path<T> path = head.connectTo(next.getTarget(), limit < next.getLength());
                    //Recursive search
                    PathFinder task = new PathFinder(paths, path, target);
                    task.fork();
                    subTasks.add(task);
                    if (subTasks.size() == THRESHOLD || !iterator.hasNext()){
                        for (PathFinder subTask : subTasks) {
                            if (isDone() || callback.isCancel()) {
                                subTask.cancel(callback.isCancel());
                            } else {
                                subTask.join();
                            }
                        }
                        subTasks.clear();
                    }
                }
                if (!subTasks.isEmpty()){
                    for (PathFinder subTask : subTasks) {
                        if (isDone() || callback.isCancel()) {
                            subTask.cancel(callback.isCancel());
                        } else {
                            subTask.join();
                        }
                    }
                    subTasks.clear();
                }
            }
        }
    }*/
}
