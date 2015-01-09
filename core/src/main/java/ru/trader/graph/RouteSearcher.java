package ru.trader.graph;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.Vendor;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class RouteSearcher {
    private final static Logger LOG = LoggerFactory.getLogger(RouteSearcher.class);
    private final static ForkJoinPool POOL = new ForkJoinPool();
    private final static int THRESHOLD = (int) Math.ceil(Runtime.getRuntime().availableProcessors()/2.0);

    private final double maxDistance;
    private final double stock;
    private final int segmentSize;

    public RouteSearcher(double maxDistance, double stock) {
        this(maxDistance, stock, 0);
    }
    public RouteSearcher(double maxDistance, double stock, int segmentSize) {
        this.maxDistance = maxDistance;
        this.stock = stock;
        this.segmentSize = segmentSize;
    }

    public List<PathRoute> getPaths(Vendor from, Vendor to, Collection<Vendor> vendors, int jumps, double balance, int cargo, int limit){
        return POOL.invoke(new SegmentSearcher(from, to, vendors, jumps, balance, cargo, limit));
    }

    public List<PathRoute> getPaths(Vendor from, Collection<Vendor> vendors, int jumps, double balance, int cargo, int limit){
        return POOL.invoke(new SegmentSearcher(from, null, vendors, jumps, balance, cargo, limit));
    }

    public class SegmentSearcher extends RecursiveTask<List<PathRoute>> {
        protected final Vendor source;
        protected final Vendor target;
        protected final Collection<Vendor> vendors;
        protected final int jumps;
        protected final double balance;
        protected final int cargo;
        protected int limit;

        public SegmentSearcher(Vendor source, Vendor target, Collection<Vendor> vendors, int jumps, double balance, int cargo, int limit) {
            this.source = source;
            this.target = target;
            this.vendors = vendors;
            this.jumps = jumps;
            this.balance = balance;
            this.cargo = cargo;
            this.limit = limit;
        }

        @Override
        protected List<PathRoute> compute() {
            LOG.trace("Start search route to {} from {}, jumps {}", source, target, jumps);
            RouteGraph sGraph = new RouteGraph(source, vendors, stock, maxDistance, true, jumps, true);
            int jumpsToAll = sGraph.getMinJumps();
            LOG.trace("Segment jumps {}", jumpsToAll);
            sGraph.setCargo(cargo);
            sGraph.setBalance(balance);
            TopList<PathRoute> res = new TopList<>(limit, RouteGraph.byProfitComparator);
            if (jumps <= jumpsToAll){
                LOG.trace("Is last segment");
                List<Path<Vendor>> paths;
                if (target == null){
                    paths = sGraph.getPaths(limit);
                } else {
                    paths = sGraph.getPathsTo(target, limit);
                }
                for (Path<Vendor> path : paths) {
                    res.add((PathRoute) path);
                }
            } else {
                LOG.trace("Split to segments");
                List<Path<Vendor>> paths = sGraph.getPaths(getPathsOnSegmentCount(sGraph), jumpsToAll).getList();
                int i = 0;
                ArrayList<SegmentSearcher> subTasks = new ArrayList<>(THRESHOLD);
                while (i < paths.size()) {
                    if (target != null){
                        PathRoute path = (PathRoute) paths.get(i);
                        if (path.getTarget().isEntry(target)){
                            LOG.trace("Is path to target, add to res");
                            res.add(path);
                        }
                    }
                    subTasks.clear();
                    for (int taskIndex = 0; taskIndex < THRESHOLD && i+taskIndex < paths.size(); taskIndex++) {
                        PathRoute path = (PathRoute) paths.get(i+taskIndex);
                        double newBalance = balance + path.getRoot().getProfit();
                        SegmentSearcher task = new SegmentSearcher(path.get(), target, vendors, jumps - path.getLength(), newBalance, cargo, 1);
                        task.fork();
                        subTasks.add(task);
                    }
                    for (int taskIndex = 0; taskIndex < subTasks.size(); taskIndex++) {
                        PathRoute path = (PathRoute) paths.get(i+taskIndex);
                        add(subTasks.get(taskIndex), path, res);
                    }
                    i+=subTasks.size();
                }
            }
            res.finish();
            return res.getList();
        }

        private int getPathsOnSegmentCount(RouteGraph graph){
            if (segmentSize ==0){
                return graph.vertexes.size()*graph.minJumps;
            } else {
                return segmentSize;
            }
        }


        private void add(SegmentSearcher task, PathRoute path, TopList<PathRoute> res){
            List<PathRoute> tail = task.join();
            if (tail.isEmpty()){
                LOG.trace("Not found route from {} to {}, jumps {}", task.source, task.target, task.jumps);
            } else {
                path.add(tail.get(0), false);
                path.sort(balance, cargo);
                res.add(path.getEnd());
            }
        }



    }


}
