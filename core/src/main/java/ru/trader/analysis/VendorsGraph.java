package ru.trader.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.analysis.graph.*;
import ru.trader.core.*;

import java.util.*;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.function.Function;
import java.util.stream.Collectors;


public class VendorsGraph extends ConnectibleGraph<Vendor> {
    private final static Logger LOG = LoggerFactory.getLogger(VendorsGraph.class);
    private final static int THRESHOLD = 8;

    private final Scorer scorer;
    private final List<VendorsGraphBuilder> deferredTasks = new ArrayList<>();

    public VendorsGraph(Scorer scorer) {
        super(scorer.getProfile());
        this.scorer = scorer;
    }

    public VendorsGraph(Scorer scorer, AnalysisCallBack callback) {
        super(scorer.getProfile(), callback);
        this.scorer = scorer;
    }

    public VendorsCrawler crawler(Function<List<Edge<Vendor>>, Boolean> onFoundFunc){
        return new VendorsCrawler(onFoundFunc);
    }

    public VendorsCrawler crawler(Function<Edge<Vendor>, Boolean> isFoundFunc,Function<List<Edge<Vendor>>, Boolean> onFoundFunc){
        return new VendorsCrawler(isFoundFunc, onFoundFunc);
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

    private void runDeferredTasks(){
        deferredTasks.sort((b1,b2) -> Integer.compare(b2.getDeep(), b1.getDeep()));
        for (VendorsGraphBuilder task : deferredTasks) {
            ForkJoinTask.invokeAll(task);
        }
        deferredTasks.clear();
    }

    private void updateVertexes(){
        vertexes.removeIf(v -> v.getEntry() instanceof TransitVendor);
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
        private final ArrayList<RecursiveAction> subTasks = new ArrayList<>(THRESHOLD);
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
            LOG.trace("Build graph from {}, limit {}, deep {}", vertex, limit, deep);
            if (isAdding){
                addAlreadyCheckedEdges();
            } else {
                checkVertex();
            }
            if (!subTasks.isEmpty()){
                joinSubTasks();
            }
            LOG.trace("End build graph from {} on deep {}", vertex, deep);
        }

        private void checkVertex(){
            Iterator<Vendor> iterator = set.iterator();
            while (iterator.hasNext()) {
                if (callback.isCancel()) break;
                Vendor entry = iterator.next();
                LOG.trace("Check {}", entry);
                if (entry == vertex.getEntry()) continue;
                double nextLimit = onConnect(entry);
                if (nextLimit >= 0) {
                    LOG.trace("Connect {} to {}", entry, vertex);
                    Vertex<Vendor> next = getInstance(entry, 0, deep);
                    BuildEdge e;
                    if (entry instanceof TransitVendor){
                        e = super.createEdge(next);
                    } else {
                        e = createEdge(next);
                        vertex.connect(e);
                    }
                    addSubTask(e, nextLimit);
                } else {
                    LOG.trace("Vertex {} is far away", entry);
                }
                if (subTasks.size() == THRESHOLD || !iterator.hasNext()){
                    joinSubTasks();
                }
            }
        }

        @Override
        protected double onConnect(Vendor buyer) {
            double nextlimit = super.onConnect(buyer);
            Vendor seller = vertex.getEntry();
            if (nextlimit > 0){
                if (buyer instanceof TransitVendor && (deep == 0 || seller.getPlace().equals(buyer.getPlace()))){
                    LOG.trace("Buyer is transit of seller or is end, skipping");
                    nextlimit = -1;
                }
                if (seller instanceof TransitVendor && seller.getPlace().equals(buyer.getPlace())){
                    LOG.trace("Seller is transit of buyer, skipping");
                    nextlimit = -1;
                }
            }
            return nextlimit;
        }

        @Override
        protected VendorsBuildEdge createEdge(Vertex<Vendor> target) {
            BuildEdge cEdge = super.createEdge(target);
            if (vertex.getEntry() instanceof TransitVendor){
                addEdgesToHead(cEdge);
            }
            return new VendorsBuildEdge(cEdge);
        }

        private void addEdgesToHead(BuildEdge lastEdge){
            Vertex<Vendor> target = lastEdge.getTarget();
            assert vertex.getEntry() instanceof TransitVendor && !(target.getEntry() instanceof TransitVendor);
            VendorsGraphBuilder h = this;
            Path<Vendor> path = new Path<>(Collections.singleton(lastEdge));
            while (h != null){
                BuildEdge cEdge = h.edge;
                Vertex<Vendor> source = cEdge.getSource();
                if (source.equals(vertex)){
                    LOG.trace("Found loop, break");
                    break;
                }
                path = path.add(cEdge);
                if (!source.equals(target)){
                    addEdge(source, target, path);
                }
                if (!(source.getEntry() instanceof TransitVendor)){
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
            vertex.getEdges().parallelStream().forEach(aEdge -> {
                VendorsBuildEdge e = (VendorsBuildEdge) aEdge;
                if (callback.isCancel()) return;
                Vendor entry = e.getTarget().getEntry();
                LOG.trace("Check {}", entry);
                if (limit >= e.getMinFuel() && limit <= e.getMaxFuel()) {
                    LOG.trace("Connect {} to {}", entry, vertex);
                    if (vertex.getEntry() instanceof TransitVendor && !(entry instanceof TransitVendor)) {
                        addCheckedEdgesToHead(e);
                    }
                }
            });
        }

        private void addCheckedEdgesToHead(VendorsBuildEdge lastEdge){
            Vertex<Vendor> target = lastEdge.getTarget();
            assert vertex.getEntry() instanceof TransitVendor && !(target.getEntry() instanceof TransitVendor);
            List<Path<Vendor>> paths = lastEdge.paths;
            int i = 1;
            Path<Vendor> path = paths != null ? paths.get(0) : new Path<>(Collections.singleton((BuildEdge)lastEdge));
            while (path != null){
                VendorsGraphBuilder h = this;
                while (h != null){
                    if (h.limit >= path.getMinFuel() && h.limit <= path.getMaxFuel()){
                        BuildEdge cEdge = h.edge;
                        Vertex<Vendor> source = cEdge.getSource();
                        if (source.equals(vertex)){
                            LOG.trace("Found loop, break");
                            break;
                        }
                        path = path.add(cEdge);
                        if (!source.equals(target)){
                            addEdge(source, target, path);
                        }
                        if (!(source.getEntry() instanceof TransitVendor)){
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

        private void addSubTask(BuildEdge e, double nextLimit){
            Vertex<Vendor> next = e.getTarget();
            // If level > deep when vertex already added on upper deep
            if (next.getLevel() < deep || next.getEntry() instanceof TransitVendor) {
                boolean adding = next.getLevel() >= deep;
                if (!adding){
                    next.setLevel(vertex.getLevel() - 1);
                }
                if (deep > 0 || adding) {
                    //Recursive build
                    VendorsGraphBuilder task = new VendorsGraphBuilder(this, e, set, deep - 1, nextLimit);
                    task.isAdding = adding;
                    if (adding){
                        holdTask(task);
                    } else {
                        task.fork();
                        subTasks.add(task);
                    }
                }
            } else {
                LOG.trace("Vertex {} already check", next);
            }
        }

        private void joinSubTasks(){
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

    public class VendorsBuildEdge extends BuildEdge {
        private List<Path<Vendor>> paths = new ArrayList<>();
        private List<Order> orders;

        protected VendorsBuildEdge(Vertex<Vendor> source, Vertex<Vendor> target, Path<Vendor> path) {
            super(source, target);
            if (path == null) throw new IllegalArgumentException("Path must be no-null");
            paths.add(path);
            update(path);
        }

        protected VendorsBuildEdge(BuildEdge edge) {
            super(edge.getSource(), edge.getTarget());
            Path<Vendor> path = new Path<>(Collections.singleton(edge));
            paths.add(path);
            update(path);
        }

        private void update(Path<Vendor> path){
            setFuel(path.getMinFuel(), path.getMaxFuel());
        }

        protected void setOrders(List<Order> orders){
            this.orders = orders;
        }

        public List<Order> getOrders(){
            if (orders == null){
                Vendor seller = source.getEntry();
                Vendor buyer = target.getEntry();
                orders = MarketUtils.getOrders(seller, buyer);
            }
            return orders;
        }

        public double getProfit(){
            return getOrders().stream().mapToDouble(Order::getProfit).sum();
        }

        public Collection<Path<Vendor>> getPaths() {
            return paths;
        }

        private Path<Vendor> getPath(double fuel){
            Path<Vendor> res = null;
            for (Path<Vendor> p : paths) {
                if (fuel >= p.getMinFuel() && fuel <= p.getMaxFuel() || getSource().getEntry().canRefill()) {
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
            if (profile.getPathPriority() == Profile.PATH_PRIORITY.FAST){
                return path.getSize() > best.getSize() || (path.getSize() == best.getSize() && path.getFuelCost() >= best.getFuelCost()) && path.getMinFuel() >= best.getMinFuel() && path.getRefillCount() >= best.getRefillCount();
            }
            return path.getMinFuel() >= best.getMinFuel() && path.getFuelCost() >= best.getFuelCost() && path.getRefillCount() >= best.getRefillCount();
        }

        public void add(Path<Vendor> path) {
            for (Iterator<Path<Vendor>> iterator = paths.iterator(); iterator.hasNext(); ) {
                Path<Vendor> p = iterator.next();
                if (isRemove(p, path)) {
                    iterator.remove();
                } else {
                    if (isRemove(path, p)) {
                        return;
                    }
                }
            }
            paths.add(path);
            if (getMinFuel() > path.getMinFuel()) {
                update(path);
            }
        }

        public VendorsEdge getInstance(double fuel, double balance){
            Path<Vendor> path = getPath(fuel);
            if (path == null) return null;
            VendorsEdge res = new VendorsEdge(source, target, new TransitPath(path,fuel));
            res.setOrders(MarketUtils.getStack(getOrders(), balance, getShip().getCargo()));
            return res;
        }
    }

    public class VendorsEdge extends ConnectibleEdge<Vendor> {
        private TransitPath  path;
        private List<Order> orders;

        protected VendorsEdge(Vertex<Vendor> source, Vertex<Vendor> target, TransitPath path) {
            super(source, target);
            if (path == null) throw new IllegalArgumentException("Path must be no-null");
            this.path = path;
        }

        protected void setOrders(List<Order> orders){
            this.orders = orders;
        }

        public double getProfit(){
            return getOrders().stream().mapToDouble(Order::getProfit).sum();
        }

        public List<Order> getOrders(){
            if (orders == null){
                Vendor seller = source.getEntry();
                Vendor buyer = target.getEntry();
                orders = MarketUtils.getOrders(seller, buyer);
            }
            return orders;
        }

        public double getRemain() {
            return path.getRemain();
        }

        public boolean isRefill() {
            return path.isRefill();
        }

        public TransitPath getPath() {
            return path;
        }

        @Override
        protected double computeWeight() {
            int jumps = source.getEntry().getPlace().equals(target.getEntry().getPlace())? 0 : 1;
            int lands = 1; double fuel = fuelCost;
            if (path != null){
                jumps = path.size(); fuel = getFuelCost();
                lands += path.getRefillCount();
            }
            double profit = getProfit();
            double score = scorer.getScore(target.getEntry(), profit, jumps, lands, fuel);
            score = scorer.getMaxScore() - score;
            if (score < 0) score = 0;
            return score;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof VendorsEdge)) return false;
            if (!super.equals(o)) return false;
            VendorsEdge edge = (VendorsEdge) o;
            return !(path != null ? !path.equals(edge.path) : edge.path != null);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + (path != null ? path.hashCode() : 0);
            return result;
        }

    }

    public class VendorsCrawler extends Crawler<Vendor> {
        private double startFuel;
        private double startBalance;

        protected VendorsCrawler(Function<List<Edge<Vendor>>, Boolean> onFoundFunc) {
            super(VendorsGraph.this, onFoundFunc);
            startFuel = getShip().getTank();
            startBalance = getProfile().getBalance();
        }

        protected VendorsCrawler(Function<Edge<Vendor>, Boolean> isFoundFunc, Function<List<Edge<Vendor>>, Boolean> onFoundFunc) {
            super(VendorsGraph.this, isFoundFunc, onFoundFunc);
            startFuel = getShip().getTank();
            startBalance = getProfile().getBalance();
        }

        public void setStartFuel(double startFuel) {
            this.startFuel = startFuel;
        }

        public void setStartBalance(double startBalance) {
            this.startBalance = startBalance;
        }

        @Override
        protected VendorsTraversalEntry start(Vertex<Vendor> vertex) {
            return new VendorsTraversalEntry(super.start(vertex), startFuel, startBalance);
        }

        @Override
        protected VendorsTraversalEntry travers(final CostTraversalEntry entry, final Edge<Vendor> edge) {
            VendorsTraversalEntry vEntry = (VendorsTraversalEntry)entry;
            VendorsEdge vEdge = (VendorsEdge) edge;
            return new VendorsTraversalEntry(vEntry, vEdge);
        }

        protected class VendorsTraversalEntry extends CostTraversalEntry {
            private final double fuel;
            private final double balance;

            protected VendorsTraversalEntry(CostTraversalEntry entry, double fuel, double balance) {
                super(entry.getTarget());
                this.fuel = fuel;
                this.balance = balance;
            }

            protected VendorsTraversalEntry(VendorsTraversalEntry head, VendorsEdge edge) {
                super(head, edge);
                this.balance = head.balance + edge.getProfit();
                this.fuel = edge.getRemain();
            }

            @Override
            public List<Edge<Vendor>> collect(Collection<Edge<Vendor>> src) {
                return src.stream().filter(this::check).map(this::wrap).filter(e -> e != null).collect(Collectors.toList());
            }

            protected boolean check(Edge<Vendor> e){
                VendorsBuildEdge edge = (VendorsBuildEdge) e;
                return fuel <= edge.getMaxFuel() && (fuel >= edge.getMinFuel() || edge.getSource().getEntry().canRefill()) && (edge.getProfit() > 0 || isFound(edge));
            }

            protected VendorsEdge wrap(Edge<Vendor> e) {
                VendorsBuildEdge edge = (VendorsBuildEdge) e;
                return edge.getInstance(fuel, balance);
            }

        }


    }
}
