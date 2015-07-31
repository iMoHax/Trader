package ru.trader.analysis.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.analysis.AnalysisCallBack;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public abstract class AbstractGraph<T> implements Graph<T> {
    private final static Logger LOG = LoggerFactory.getLogger(AbstractGraph.class);

    protected Vertex<T> root;
    protected final List<Vertex<T>> vertexes;
    protected final GraphCallBack callback;
    protected int minJumps;
    private final ReentrantLock lock = new ReentrantLock();

    protected AbstractGraph(AnalysisCallBack callback) {
        this.callback = new GraphCallBack(callback);
        vertexes = new ArrayList<>();
    }

    protected abstract GraphBuilder createGraphBuilder(Vertex<T> vertex, Collection<T> set, int deep, double limit);

    public void build(T start, Collection<T> set, int maxDeep, double limit) {
        callback.startBuild(start);
        minJumps = 1;
        root = getInstance(start, maxDeep, maxDeep);
        GraphBuilder builder = createGraphBuilder(root, set, maxDeep - 1, limit);
        builder.compute();
        onEnd();
        callback.endBuild();
    }

    protected void onEnd(){

    }

    protected Vertex<T> newInstance(T entry, int index){
        return new Vertex<>(entry, index);
    }

    private Vertex<T> getInstance(T entry, int level, int deep){
        Vertex<T> vertex = null;
        lock.lock();
        try {
            vertex = getVertex(entry).orElse(null);
            if (vertex == null){
                LOG.trace("Is new vertex");
                vertex = newInstance(entry, vertexes.size());
                vertexes.add(vertex);
                vertex.setLevel(level);
                int jumps = root != null ? root.getLevel() - deep : 0;
                if (jumps > minJumps)
                    minJumps = jumps;
            }
        } finally {
            lock.unlock();
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

    protected abstract class GraphBuilder {
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

        protected BuildHelper<T> createHelper(final T entry){
            return new BuildHelper<>(entry, limit);
        }
        protected abstract Edge<T> createEdge(BuildHelper<T> helper, Vertex<T> target);
        protected void connect(Edge<T> edge){
            vertex.connect(edge);
        }
        protected GraphBuilder createSubTask(Edge<T> edge, Collection<T> set, int deep, double limit){
            return createGraphBuilder(edge.getTarget(), set, deep, limit);
        }

        protected void compute() {
            List<BuildHelper<T>> helpers;
            if (vertex.getLevel() <= deep){
                vertex.setLevel(deep+1);
            } else {
                if (vertex.getLevel() > deep+1){
                    LOG.trace("Already build");
                    return;
                }
            }
            helpers = build();
            runSubTasks(helpers);
        }

        private List<BuildHelper<T>> build(){
            LOG.trace("Build graph from {}, limit {}, deep {}", vertex, limit, deep);
            List<BuildHelper<T>> helpers = set.parallelStream()
                    .filter(entry -> entry != vertex.getEntry())
                    .map(this::createHelper)
                    .filter(BuildHelper::isConnected)
                    .collect(Collectors.toList());
            helpers.parallelStream().forEach(this::connect);
            LOG.trace("End build graph from {} on deep {}", vertex, deep);
            return helpers;
        }

        private void connect(final BuildHelper<T> helper){
            LOG.trace("Connect {} to {}", vertex, helper.entry);
            Vertex<T> next = getInstance(helper.entry, 0, deep);
            Edge<T> edge = createEdge(helper, next);
            helper.setEdge(edge);
            connect(edge);
        }

        private void runSubTasks(final List<BuildHelper<T>> helpers){
            LOG.trace("Build sub graph from {}, limit {}, deep {}", vertex, limit, deep);
            for (BuildHelper<T> helper : helpers) {
                if (callback.isCancel()) break;
                addSubTask(helper.edge, helper.nextLimit);
            }
            LOG.trace("End build sub graph from {}, limit {}, deep {}", vertex, limit, deep);
        }

        protected void addSubTask(Edge<T> edge, double nextLimit){
            Vertex<T> next = edge.getTarget();
            if (next.getLevel() < deep) {
                if (deep > 0) {
                    //Recursive build
                    GraphBuilder task = createSubTask(edge, set, deep - 1, nextLimit);
                    task.compute();
                }
            }
        }
    }

    protected class BuildHelper<T> {
        private final T entry;
        private final double nextLimit;
        private Edge<T> edge;

        public BuildHelper(T entry, double nextLimit) {
            this.entry = entry;
            this.nextLimit = nextLimit;
            this.edge = null;
        }

        private void setEdge(Edge<T> edge) {
            this.edge = edge;
        }

        public boolean isConnected(){
            return nextLimit >= 0;
        }
    }
}
