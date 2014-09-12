package ru.trader.graph;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.Vendor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class RouteSearcher {
    private final static Logger LOG = LoggerFactory.getLogger(RouteSearcher.class);
    private final static ForkJoinPool POOL = new ForkJoinPool();
    private final static int THRESHOLD = (int) Math.ceil(Runtime.getRuntime().availableProcessors()/2.0);

    private double maxDistance;
    private double stock;
    private int segmentJump;

    public RouteSearcher(double maxDistance, double stock, double segment) {
        this.maxDistance = maxDistance;
        this.stock = stock;
        this.segmentJump = (int) Math.floor(segment/maxDistance);
    }

    public List<PathRoute> getPaths(Vendor from, Vendor to, Collection<Vendor> vendors, int jumps, double balance, int cargo, int limit){
        if (segmentJump == 0){
            RouteGraph sGraph = new RouteGraph(from, vendors, stock, maxDistance, true, jumps);
            segmentJump = sGraph.getMinJumps() > 1 ? sGraph.getMinJumps()-1 : sGraph.getMinJumps();
        }
        return POOL.invoke(new SegmentSearcher(from, to, vendors, jumps, balance, cargo, limit));
    }

    public List<PathRoute> getPaths(Vendor from, Collection<Vendor> vendors, int jumps, double balance, int cargo, int limit){
        if (segmentJump == 0){
            RouteGraph sGraph = new RouteGraph(from, vendors, stock, maxDistance, true, jumps);
            segmentJump = sGraph.getMinJumps() > 1 ? sGraph.getMinJumps()-1 : sGraph.getMinJumps();
        }
        return POOL.invoke(new SegmentSearcher(from, null, vendors, jumps, balance, cargo, limit));
    }

    public class SegmentSearcher extends RecursiveTask<List<PathRoute>> {
        private final Vendor source;
        private final Vendor target;
        private final Collection<Vendor> vendors;
        private final int jumps;
        private final double balance;
        private final int cargo;
        private int limit;

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
            RouteGraph sGraph = new RouteGraph(source, vendors, stock, maxDistance, true, Math.min(jumps, segmentJump));
            sGraph.setLimit(cargo);
            sGraph.setBalance(balance);
            List<PathRoute> res = new ArrayList<>(limit);
            if (jumps <= segmentJump){
                LOG.trace("Is last segment");
                List<Path<Vendor>> paths;
                if (target == null){
                    paths = sGraph.getPaths(10);
                } else {
                    paths = sGraph.getPathsTo(target, limit);
                }
                for (Path<Vendor> path : paths) {
                    res.add((PathRoute) path);
                }
            } else {
                LOG.trace("Split to segments");
                List<Path<Vendor>> paths = sGraph.getPaths(1);
                int i = 0;
                ArrayList<SegmentSearcher> subTasks = new ArrayList<>(THRESHOLD);
                while (i < paths.size()) {
                    subTasks.clear();
                    for (int taskIndex = 0; taskIndex < THRESHOLD && i+taskIndex < paths.size(); taskIndex++) {
                        PathRoute path = (PathRoute) paths.get(i+taskIndex);
                        double newBalance = balance + path.getRoot().getProfit();
                        SegmentSearcher task = new SegmentSearcher(path.get(), target, vendors, jumps - segmentJump, newBalance, cargo, (int) Math.ceil(limit / 2.0));
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
            return res;
        }


        private void add(SegmentSearcher task, PathRoute path, List<PathRoute> res){
            List<PathRoute> tail = task.join();
            if (tail.isEmpty()){
                LOG.trace("Not found route from {} to {}, jumps {}", task.source, task.target, task.jumps);
            } else {
                path.add(tail.get(0), false);
                path.sort(balance, cargo);
                RouteGraph.addToTop(res, path.getEnd(), limit, RouteGraph.comparator);
            }
        }

    }


}
