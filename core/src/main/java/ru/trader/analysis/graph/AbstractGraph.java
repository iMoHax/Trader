package ru.trader.analysis.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.analysis.AnalysisCallBack;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public abstract class AbstractGraph<T> implements Graph<T> {
    private final static ForkJoinPool POOL = new ForkJoinPool();
    //TODO: make it worked in multi thread
    private final static int THRESHOLD = 1;

    private final static Logger LOG = LoggerFactory.getLogger(AbstractGraph.class);

    protected Vertex<T> root;
    protected final List<Vertex<T>> vertexes;
    protected final GraphCallBack callback;
    protected int minJumps;

    protected AbstractGraph() {
        this(new AnalysisCallBack());
    }

    protected AbstractGraph(AnalysisCallBack callback) {
        this.callback = new GraphCallBack(callback);
        vertexes = new CopyOnWriteArrayList<>();
    }

    protected abstract RecursiveAction createGraphBuilder(Vertex<T> vertex, Collection<T> set, int deep, double limit);

    public void build(T start, Collection<T> set, int maxDeep, double limit) {
        callback.startBuild(start);
        minJumps = 1;
        root = getInstance(start, maxDeep, maxDeep);
        POOL.invoke(createGraphBuilder(root, set, maxDeep - 1, limit));
        onEnd();
        callback.endBuild();
    }

    protected void onEnd(){

    }

    protected Vertex<T> newInstance(T entry, int index){
        return new Vertex<>(entry, index);
    }

    protected Vertex<T> getInstance(T entry, int level, int deep){
        Vertex<T> vertex = getVertex(entry).orElse(null);
        if (vertex == null) {
            synchronized (vertexes){
                vertex = getVertex(entry).orElse(null);
                if (vertex == null){
                    LOG.trace("Is new vertex");
                    vertex = newInstance(entry, vertexes.size());
                    vertex.setLevel(level);
                    vertexes.add(vertex);
                    int jumps = root != null ? root.getLevel() - deep : 0;
                    if (jumps > minJumps)
                        minJumps = jumps;
                }
            }
        }
        return vertex;
    }

    @Override
    public boolean isAccessible(T entry){
        return getVertex(entry).isPresent();
    }

    @Override
    public Optional<Vertex<T>> getVertex(T entry){
        return vertexes.stream().filter(v -> v.isEntry(entry)).findFirst();
    }

    @Override
    public Vertex<T> getRoot() {
        return root;
    }

    @Override
    public Collection<Vertex<T>> vertexes() {
        return vertexes;
    }

    @Override
    public int getMinJumps() {
        return minJumps;
    }

    @Override
    public int getMinLevel() {
        return root.getLevel() - minJumps;
    }

    @Override
    public int getSize() {
        return vertexes.size();
    }

    protected abstract class GraphBuilder extends RecursiveAction {
        protected final List<RecursiveAction> subTasks = new ArrayList<>(THRESHOLD);
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

        protected abstract double checkConnect(T entry);
        protected abstract Edge<T> createEdge(Vertex<T> target);
        protected RecursiveAction createSubTask(Vertex<T> vertex, Collection<T> set, int deep, double limit){
            return createGraphBuilder(vertex, set, deep, limit);
        }

        @Override
        protected final void compute() {
            vertex.locker().lock();
            try {
                if (vertex.getLevel() <= deep){
                    vertex.setLevel(deep+1);
                }
            } finally {
                vertex.locker().unlock();
            }
            build();
        }

        protected void build(){
            LOG.trace("Build graph from {}, limit {}, deep {}", vertex, limit, deep);
            for (T entry : set) {
                if (callback.isCancel()) break;
                if (entry == vertex.getEntry()) continue;
                double nextLimit = checkConnect(entry);
                if (nextLimit >= 0) {
                    LOG.trace("Connect {} to {}", vertex, entry);
                    Vertex<T> next = getInstance(entry, 0, deep);
                    connect(next, nextLimit);
                } else {
                    LOG.trace("Vertex {} is far away", entry);
                }
                if (subTasks.size() >= THRESHOLD) {
                    joinSubTasks();
                }
            }
            if (!subTasks.isEmpty()){
                joinSubTasks();
            }
            LOG.trace("End build graph from {} on deep {}", vertex, deep);
        }

        protected void connect(Vertex<T> next, double nextLimit){
            vertex.connect(createEdge(next));
            if (next.getLevel() < deep) {
                if (deep > 0) {
                    //Recursive build
                    RecursiveAction task = createSubTask(next, set, deep - 1, nextLimit);
                    task.fork();
                    subTasks.add(task);
                }
            }
        }

        protected void joinSubTasks(){
            for (RecursiveAction subTask : subTasks) {
                if (callback.isCancel()){
                    subTask.cancel(true);
                } else {
                    subTask.join();
                }
            }
            subTasks.clear();
        }

    }
}
