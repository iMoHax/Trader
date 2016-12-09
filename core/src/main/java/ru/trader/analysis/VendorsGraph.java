package ru.trader.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.analysis.graph.ConnectibleGraph;
import ru.trader.analysis.graph.Edge;
import ru.trader.analysis.graph.Path;
import ru.trader.analysis.graph.Vertex;
import ru.trader.core.Order;
import ru.trader.core.Profile;
import ru.trader.core.Vendor;

import java.util.*;
import java.util.function.Consumer;


public class VendorsGraph extends ConnectibleGraph<Vendor> {
    private final static Logger LOG = LoggerFactory.getLogger(VendorsGraph.class);

    private final Scorer scorer;
    private final List<VendorsGraphBuilder> deferredTasks = new ArrayList<>();
    private final static int MAX_DEFERRED_TASKS = 5000;

    public VendorsGraph(Scorer scorer, AnalysisCallBack callback) {
        super(scorer.getProfile(), callback);
        this.scorer = scorer;
    }

    public Scorer getScorer() {
        return scorer;
    }

    public VendorsCrawler crawler(Consumer<List<Edge<Vendor>>> onFoundFunc, AnalysisCallBack callback){
        return new VendorsCrawler(this, new CrawlerSpecificationByProfit(onFoundFunc), callback);
    }

    public VendorsCrawler crawlerByTime(Consumer<List<Edge<Vendor>>> onFoundFunc, AnalysisCallBack callback){
        return new VendorsCrawler(this, new CrawlerSpecificationByTime(onFoundFunc), callback);
    }

    public VendorsCrawler crawler(VendorsCrawlerSpecification specification, AnalysisCallBack callback){
        return new VendorsCrawler(this, specification, callback);
    }

    @Override
    protected Vertex<Vendor> newInstance(Vendor entry, int index) {
        return new VendorVertex(entry, index);
    }

    @Override
    protected GraphBuilder createGraphBuilder(Vertex<Vendor> vertex, Collection<Vendor> set, int deep, double limit) {
        return new VendorsGraphBuilder(vertex, set, deep, limit);
    }

    @Override
    protected void onEnd() {
        super.onEnd();
        runDeferredTasks();
        updateVertexes();
    }

    protected void holdTask(VendorsGraphBuilder task){
        synchronized (deferredTasks){
            deferredTasks.add(task);
        }
    }

    protected boolean canHoldTask(){
        synchronized (deferredTasks){
            return deferredTasks.size() < MAX_DEFERRED_TASKS;
        }
    }

    private void runDeferredTasks(){
        deferredTasks.sort((b1,b2) -> Integer.compare(b2.getDeep(), b1.getDeep()));
        for (VendorsGraphBuilder task : deferredTasks) {
            if (callback.isCancel()) break;
            task.compute();
        }
        deferredTasks.clear();
    }

    private void updateVertexes(){
        vertexes.removeIf(v -> v.getEntry().isTransit());
        updateLevels(root);
    }

    private void updateLevels(Vertex<Vendor> vertex){
        vertex.getEdges().forEach(e -> {
            Vertex<Vendor> target = e.getTarget();
            if (target.getLevel()+1 <vertex.getLevel()){
                target.setLevel(vertex.getLevel()-1);
                updateLevels(target);
            }
        });
    }

    private class VendorsGraphBuilder extends ConnectibleGraphBuilder {
        private final VendorsGraphBuilder head;
        private final BuildEdge edge;
        private boolean isAdding;

        protected VendorsGraphBuilder(Vertex<Vendor> vertex, Collection<Vendor> set, int deep, double limit) {
            super(vertex, set, deep, limit);
            this.head = null;
            this.edge = null;
        }

        private VendorsGraphBuilder(VendorsGraphBuilder head, BuildEdge edge, Collection<Vendor> set, int deep, double limit) {
            super(edge.getTarget(), set, deep, limit);
            this.head = head;
            this.edge = edge;
        }

        public int getDeep(){
            return deep;
        }

        @Override
        protected void compute() {
            if (isAdding){
                addAlreadyCheckedEdges();
            } else {
                super.compute();
            }
        }

        @Override
        protected BuildHelper<Vendor> createHelper(Vendor buyer) {
            BuildHelper<Vendor> helper = super.createHelper(buyer);
            if (helper.isConnected()){
                Vendor seller = vertex.getEntry();
                if (buyer.isTransit() && (deep == 0 || seller.getPlace().equals(buyer.getPlace()))){
                    LOG.trace("Buyer is transit of seller or is end, skipping");
                    return new BuildHelper<>(buyer, -1);
                }
                if (seller.isTransit() && seller.getPlace().equals(buyer.getPlace())){
                    LOG.trace("Seller is transit of buyer, skipping");
                    return new BuildHelper<>(buyer, -1);
                }

            }
            return helper;
        }

        @Override
        protected void connect(Edge<Vendor> edge) {
            if (edge instanceof VendorsBuildEdge){
                super.connect(edge);
            }
        }

        @Override
        protected BuildEdge createEdge(BuildHelper<Vendor> helper, Vertex<Vendor> next) {
            BuildEdge cEdge = super.createEdge(helper, next);
            if (next.getEntry().isTransit()){
                return cEdge;
            }
            if (vertex.getEntry().isTransit()){
                addEdgesToHead(cEdge);
            }
            return new VendorsBuildEdge(cEdge);
        }

        private void addEdgesToHead(BuildEdge lastEdge){
            Vertex<Vendor> target = lastEdge.getTarget();
            assert vertex.getEntry().isTransit() && !target.getEntry().isTransit();
            VendorsGraphBuilder h = this;
            Path<Vendor> path = new Path<>(lastEdge);
            while (h != null && h.edge != null){
                if (callback.isCancel()) break;
                BuildEdge cEdge = h.edge;
                Vertex<Vendor> source = cEdge.getSource();
                if (source.equals(vertex)){
                    LOG.trace("Found loop, break");
                    break;
                }
                path = path.addFirst(cEdge);
                if (path.getMinFuel() > path.getMaxFuel()){
                    LOG.trace("Path inaccessible");
                    break;
                }
                if (!source.equals(target)){
                    addEdge(source, target, path);
                }
                if (!source.getEntry().isTransit()){
                    break;
                }
                h = h.head;
            }
        }

        private void addEdge(Vertex<Vendor> source, Vertex<Vendor> target, Path<Vendor> path){
            LOG.trace("Is not transit, add edge to {}", source);
            VendorsBuildEdge vEdge = new VendorsBuildEdge(source, target, path);
            source.connect(vEdge);
        }

        private void addAlreadyCheckedEdges(){
            LOG.trace("Adding already checked vertex");
            vertex.getEdges().parallelStream().forEach(edge -> {
                VendorsBuildEdge e = (VendorsBuildEdge) edge;
                if (callback.isCancel()) return;
                Vendor entry = e.getTarget().getEntry();
                LOG.trace("Check {}", entry);
                if (limit >= e.getMinFuel() && limit <= e.getMaxFuel()) {
                    LOG.trace("Connect {} to {}", entry, vertex);
                    if (vertex.getEntry().isTransit() && !entry.isTransit()) {
                        addCheckedEdgesToHead(e);
                    }
                }
            });
        }

        private void addCheckedEdgesToHead(VendorsBuildEdge lastEdge){
            Vertex<Vendor> target = lastEdge.getTarget();
            assert vertex.getEntry().isTransit() && !target.getEntry().isTransit();
            List<Path<Vendor>> paths = lastEdge.paths;
            int i = 1;
            Path<Vendor> path = paths != null ? paths.get(0) : new Path<>(lastEdge);
            while (path != null){
                if (callback.isCancel()) break;
                VendorsGraphBuilder h = this;
                while (h != null && h.edge != null){
                    if (callback.isCancel()) break;
                    if (h.limit >= path.getMinFuel() && h.limit <= path.getMaxFuel()){
                        BuildEdge cEdge = h.edge;
                        Vertex<Vendor> source = cEdge.getSource();
                        if (source.equals(vertex)){
                            LOG.trace("Found loop, break");
                            break;
                        }
                        path = path.addFirst(cEdge);
                        if (path.getMinFuel() > path.getMaxFuel()){
                            LOG.trace("Path inaccessible");
                            break;
                        }
                        if (!source.equals(target)){
                            addEdge(source, target, path);
                        }
                        if (!source.getEntry().isTransit()){
                            break;
                        }
                        h = h.head;
                    } else {
                        break;
                    }
                }
                if (paths == null || i >= paths.size()){
                    path = null;
                } else {
                    path = paths.get(i++);
                }
            }
        }

        @Override
        protected GraphBuilder createSubTask(Edge<Vendor> edge, Collection<Vendor> set, int deep, double limit) {
            return new VendorsGraphBuilder(this, (BuildEdge) edge, set, deep, limit);
        }

        @Override
        protected void addSubTask(Edge<Vendor> edge, double nextLimit) {
            Vertex<Vendor> next = edge.getTarget();
            if (next.getLevel() >= deep && next.getEntry().isTransit()) {
                if (deep > 0 && canHoldTask()){
                    VendorsGraphBuilder task = new VendorsGraphBuilder(this, (BuildEdge) edge, set, deep - 1, nextLimit);
                    task.isAdding = true;
                    holdTask(task);
                }
            }
            super.addSubTask(edge, nextLimit);
        }
    }

    public class VendorsBuildEdge extends BuildEdge {
        private List<Path<Vendor>> paths = new ArrayList<>();
        private List<Order> orders;

        protected VendorsBuildEdge(Vertex<Vendor> source, Vertex<Vendor> target, Path<Vendor> path) {
            super(source, target);
            if (path == null) throw new IllegalArgumentException("Path must be no-null");
            paths.add(path);
            setFuel(path.getMinFuel(), path.getMaxFuel());
        }

        protected VendorsBuildEdge(BuildEdge edge) {
            super(edge.getSource(), edge.getTarget());
            Path<Vendor> path = new Path<>(edge);
            paths.add(path);
            setFuel(path.getMinFuel(), path.getMaxFuel());
        }

        private void update(Path<Vendor> path){
            setFuel(Math.min(getMinFuel(), path.getMinFuel()), Math.max(getMaxFuel(), path.getMaxFuel()));
        }

        protected void setOrders(List<Order> orders){
            this.orders = orders;
        }

        public List<Order> getOrders(){
            if (orders == null){
                Vendor seller = source.getEntry();
                Vendor buyer = target.getEntry();
                orders = getScorer().getOrders(seller, buyer);
            }
            return orders;
        }

        public double getProfit(){
            return getOrders().stream().mapToDouble(Order::getProfit).sum();
        }

        public Collection<Path<Vendor>> getPaths() {
            return paths;
        }

        public Path<Vendor> getPath(double fuel){
            Path<Vendor> res = null;
            for (Path<Vendor> p : paths) {
                if ((fuel > p.getMinFuel() || getSource().getEntry().canRefill()) && fuel <= p.getMaxFuel()) {
                    if (getProfile().getPathPriority().equals(Profile.PATH_PRIORITY.FAST)) {
                        if (res == null || (p.getSize() < res.getSize() || p.getSize() == res.getSize() && p.getFuelCost() < res.getFuelCost()) && p.getRefillCount(fuel) <= res.getRefillCount(fuel)) {
                            res = p;
                        }
                    } else {
                        if (res == null || p.getFuelCost() < res.getFuelCost()) {
                            res = p;
                        }
                    }

                }
            }
            return res;
        }

        private boolean isRemove(Path<Vendor> path, Path<Vendor> best){
            if (path.getMinFuel() > path.getMaxFuel()) return true;
            if (profile.getPathPriority() == Profile.PATH_PRIORITY.FAST){
                return (path.getSize() > best.getSize() || (path.getSize() == best.getSize() && path.getFuelCost() >= best.getFuelCost()))
                        && (path.getSource().canRefill() || path.getMinFuel() >= best.getMinFuel() && path.getRefillCount() >= best.getRefillCount())
                        && path.getMaxFuel() <= best.getMaxFuel();
            }
            return (path.getSource().canRefill() || path.getMinFuel() >= best.getMinFuel() && path.getRefillCount() >= best.getRefillCount())
                    && path.getMaxFuel() <= best.getMaxFuel() && path.getFuelCost() >= best.getFuelCost();
        }

        public void add(Path<Vendor> path) {
            for (Iterator<Path<Vendor>> iterator = paths.iterator(); iterator.hasNext(); ) {
                Path<Vendor> p = iterator.next();
                if (isRemove(path, p)) {
                    return;
                } else {
                    if (isRemove(p, path)) {
                        iterator.remove();
                    }
                }
            }
            paths.add(path);
            update(path);
        }
    }

}
