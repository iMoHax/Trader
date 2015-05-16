package ru.trader.analysis.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.analysis.AnalysisCallBack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public abstract class Graph<T> {
    private final static ForkJoinPool POOL = new ForkJoinPool();
    private final static int THRESHOLD = 4;

    private final static Logger LOG = LoggerFactory.getLogger(Graph.class);

    protected Vertex<T> root;
    protected final Map<T, Vertex<T>> vertexes;
    private final GraphCallBack callback;

    protected int minJumps;

    protected Graph() {
        this(new AnalysisCallBack());
    }

    protected Graph(AnalysisCallBack callback) {
        this.callback = new GraphCallBack(callback);
        vertexes = new ConcurrentHashMap<>(50, 0.9f, THRESHOLD);
    }

    protected abstract GraphBuilder createGraphBuilder(Vertex<T> vertex, Collection<T> set, int deep, double limit);

    public void build(T start, Collection<T> set, int maxDeep, double limit) {
        callback.startBuild(start);
        root = getInstance(start, maxDeep);
        POOL.invoke(createGraphBuilder(root, set, maxDeep - 1, limit));
        if (set.size() > vertexes.size()){
            minJumps = maxDeep;
        } else {
            minJumps = 1;
            for (Vertex<T> vertex : vertexes.values()) {
                int jumps = maxDeep - vertex.getLevel();
                if (jumps > minJumps) minJumps = jumps;
            }
        }
        callback.endBuild();
    }

    private Vertex<T> getInstance(T entry, int deep){
        Vertex<T> vertex = new Vertex<>(entry);
        vertex.setLevel(deep);
        Vertex<T> old = vertexes.get(entry);
        if (old == null || old.getLevel() < deep) {
            LOG.trace("Is top vertex");
            vertexes.put(entry, vertex);
        }
        return vertex;
    }

    public boolean isAccessible(T entry){
        return vertexes.containsKey(entry);
    }

    public Vertex<T> getVertex(T entry){
        return vertexes.get(entry);
    }

    public T getRoot() {
        return root.getEntry();
    }

    public int getMinJumps() {
        return minJumps;
    }

    protected abstract class GraphBuilder extends RecursiveAction {
        protected final Vertex<T> vertex;
        protected final Collection<T> set;
        protected final int deep;
        protected final double limit;

        protected GraphBuilder(Vertex<T> vertex, Collection<T> set, int deep, double limit) {
            this.vertex = vertex;
            this.set = set;
            this.deep = deep;
            this.limit = limit;
        }

        protected abstract double onConnect(T entry);
        protected abstract Edge<T> createEdge(Vertex<T> target);

        @Override
        protected void compute() {
            LOG.trace("Build graph from {}, limit {}, deep {}", vertex, limit, deep);
            ArrayList<GraphBuilder> subTasks = new ArrayList<>(THRESHOLD);
            Iterator<T> iterator = set.iterator();
            while (iterator.hasNext()) {
                if (callback.isCancel()) break;
                T entry = iterator.next();
                if (entry == vertex.getEntry()) continue;
                double nextLimit = onConnect(entry);
                if (nextLimit >= 0) {
                    LOG.trace("Connect {} to {}", vertex, entry);
                    Vertex<T> next = getInstance(entry, vertex.getLevel() - 1);
                    vertex.connect(createEdge(next));
                    if (deep > 0) {
                        //Recursive build
                        GraphBuilder task = createGraphBuilder(next, set, deep - 1, nextLimit);
                        task.fork();
                        subTasks.add(task);
                    }
                } else {
                    LOG.trace("Vertex {} is far away", entry);
                }
                if (subTasks.size() == THRESHOLD || !iterator.hasNext()){
                    for (GraphBuilder subTask : subTasks) {
                        if (callback.isCancel()){
                            subTask.cancel(true);
                        } else {
                            subTask.join();
                        }
                    }
                    subTasks.clear();
                }
            }
            if (!subTasks.isEmpty()){
                for (GraphBuilder subTask : subTasks) {
                    if (callback.isCancel()){
                        subTask.cancel(true);
                    } else {
                        subTask.join();
                    }
                }
                subTasks.clear();
            }
            LOG.trace("End build graph from {} on deep {}", vertex, deep);
        }
    }
}
