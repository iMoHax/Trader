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

    private final Graph<T> graph;
    private final Consumer<List<Edge<T>>> onFoundFunc;

    public Crawler(Graph<T> graph, Consumer<List<Edge<T>>> onFoundFunc) {
        this.graph = graph;
        this.onFoundFunc = onFoundFunc;
    }

    private List<Edge<T>> getCopyList(List<Edge<T>> head, Edge<T> tail){
        List<Edge<T>> res = new ArrayList<>(20);
        res.addAll(head);
        res.add(tail);
        return res;
    }

    public void findFast(T target){
        findFast(target, 1);
    }

    public void findFast(T target, int count){
        Vertex<T> t = graph.getVertex(target);
        int found = 0;
        if (t != null) {
            if (count > 1) {
                found = bfs(new ArrayList<>(), graph.root, target, 0, count);
            } else {
                found = dfs(new ArrayList<>(), graph.root, target, t.getLevel() + 1, count);
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
            found = ucs(new ArrayList<>(), graph.root, target, 0, count);
        }
        LOG.debug("Found {} paths", found);
    }

    private int dfs(List<Edge<T>> head, Vertex<T> source, T target, int deep, int count) {
        LOG.trace("DFS from {} to {}, deep {}, count {}, head {}", source, target, deep, count, head);
        int found = 0;
        if (deep == source.getLevel()){
            Optional<Edge<T>> last = source.getEdges().parallelStream()
                    .filter(next -> next.isConnect(target))
                    .findFirst();
            if (last.isPresent()){
                List<Edge<T>> res = getCopyList(head, last.get());
                LOG.debug("Last edge find, path {}", res);
                onFoundFunc.accept(res);
                found++;
            }
        }
        if (found < count){
            if (deep < source.getLevel()) {
                LOG.trace("Search around");
                for (Edge<T> edge : source.getEdges()) {
                    if (edge.getTarget().isSingle()) continue;
                    found += dfs(getCopyList(head, edge), edge.getTarget(), target, deep, count-found);
                    if (found >= count) break;
                }
            }
        }
        return found;
    }

    private int bfs(List<Edge<T>> head, Vertex<T> source, T target, int deep, int count) {
        LOG.trace("BFS from {} to {}, deep {}, count {}", source, target, deep, count);
        int found = 0;
        LinkedList<TraversalEntry> queue = new LinkedList<>();
        queue.add(new TraversalEntry(head, source));
        while (!queue.isEmpty() && count > found){
            TraversalEntry entry = queue.poll();
            head = entry.head;
            source = entry.vertex;
            LOG.trace("Search from {} to {}, head {}", source, target, head);
            source.sortEdges();
            for (Edge<T> edge : source.getEdges()) {
                if (edge.isConnect(target)){
                    List<Edge<T>> res = getCopyList(head, edge);
                    LOG.debug("Last edge find, path {}", res);
                    onFoundFunc.accept(res);
                    found++;
                }
                if (found >= count) break;
                if (edge.getTarget().isSingle()) continue;
                if (deep < source.getLevel()) {
                    queue.add(new TraversalEntry(getCopyList(head, edge), edge.getTarget()));
                }
            }
        }
        return found;
    }

    private int ucs(List<Edge<T>> head, Vertex<T> source, T target, int deep, int count) {
        LOG.trace("UCS from {} to {}, deep {}, count {}", source, target, deep, count);
        int found = 0;
        PriorityQueue<CostTraversalEntry> queue = new PriorityQueue<>();
        queue.add(new CostTraversalEntry(head, source));
        while (!queue.isEmpty() && count > found){
            CostTraversalEntry entry = queue.poll();
            LOG.trace("Check path head {}, edge {}, cost {}", entry.head, entry.edge, entry.cost);
            head = entry.head;
            Edge<T> edge = entry.edge;
            if (edge != null) {
                source = edge.getSource();
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
            Iterator<Edge<T>> iterator = entry.iterator;
            //put only 2 entry for iterate
            while (iterator.hasNext()){
                edge = iterator.next();
                if (deep < source.getLevel() && !edge.getTarget().isSingle() || edge.isConnect(target)) {
                    LOG.trace("Add edge {} to queue", edge);
                    queue.add(new CostTraversalEntry(head, edge, entry.cost));
                }
            }
        }
        return found;
    }

    //last edge don't compare
    private int ucs2(List<Edge<T>> head, Vertex<T> source, T target, int deep, int count) {
        LOG.trace("UCS2 from {} to {}, deep {}, count {}", source, target, deep, count);
        int found = 0;
        PriorityQueue<CostTraversalEntry> queue = new PriorityQueue<>();
        source.sortEdges();
        queue.add(new CostTraversalEntry(head, source));
        while (!queue.isEmpty() && count > found){
            CostTraversalEntry entry = queue.peek();
            head = entry.edge != null ? getCopyList(entry.head, entry.edge) : entry.head;
            Iterator<Edge<T>> iterator = entry.iterator;
            LOG.trace("Check path head {}, cost {}", head, entry.cost);
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
                if (deep < source.getLevel()) {
                    edge.getTarget().sortEdges();
                    queue.add(new CostTraversalEntry(head, edge, entry.cost));
                    i++;
                }
            }
            if (!iterator.hasNext()) queue.poll();
        }
        return found;
    }

    private class TraversalEntry {
        private final List<Edge<T>> head;
        private final Vertex<T> vertex;

        private TraversalEntry(List<Edge<T>> head, Vertex<T> vertex) {
            this.head = head;
            this.vertex = vertex;
        }
    }

    private class CostTraversalEntry implements Comparable<CostTraversalEntry>{
        private final List<Edge<T>> head;
        private final Edge<T> edge;
        private final Iterator<Edge<T>> iterator;
        private final double cost;

        private CostTraversalEntry(List<Edge<T>> head, Vertex<T> vertex) {
            this.head = head;
            this.iterator = vertex.getEdges().iterator();
            this.edge = null;
            this.cost = 0;
        }

        private CostTraversalEntry(List<Edge<T>> head, Edge<T> edge, double cost) {
            this.head = head;
            this.edge = edge;
            this.iterator = edge.getTarget().getEdges().iterator();
            this.cost = cost + edge.getWeight();
        }

        @Override
        public int compareTo(@NotNull CostTraversalEntry other) {
            int cmp = Double.compare(cost, other.cost);
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
