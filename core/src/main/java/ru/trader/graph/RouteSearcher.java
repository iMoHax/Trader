package ru.trader.graph;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.Place;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

    public List<PathRoute> getPaths(Place from, Place to, Collection<Place> places, int jumps, double balance, int cargo, int limit){
        return POOL.invoke(new SegmentSearcher(from, to, places, jumps, balance, cargo, limit));
    }

    public List<PathRoute> getPaths(Place from, Collection<Place> places, int jumps, double balance, int cargo, int limit){
        return POOL.invoke(new SegmentSearcher(from, null, places, jumps, balance, cargo, limit));
    }

    public class SegmentSearcher extends RecursiveTask<List<PathRoute>> {
        protected final Place source;
        protected final Place target;
        protected final Collection<Place> places;
        protected final int jumps;
        protected final double balance;
        protected final int cargo;
        protected int limit;

        public SegmentSearcher(Place source, Place target, Collection<Place> places, int jumps, double balance, int cargo, int limit) {
            this.source = source;
            this.target = target;
            this.places = places;
            this.jumps = jumps;
            this.balance = balance;
            this.cargo = cargo;
            this.limit = limit;
        }

        @Override
        protected List<PathRoute> compute() {
            LOG.trace("Start search route to {} from {}, jumps {}", source, target, jumps);
            RouteGraph sGraph = new RouteGraph(source, places, stock, maxDistance, true, jumps, true);
            int jumpsToAll = sGraph.getMinJumps();
            LOG.trace("Segment jumps {}", jumpsToAll);
            sGraph.setCargo(cargo);
            sGraph.setBalance(balance);
            List<PathRoute> res = new ArrayList<>(limit);
            if (jumps <= jumpsToAll){
                LOG.trace("Is last segment");
                List<Path<Place>> paths;
                if (target == null){
                    paths = sGraph.getPaths(limit);
                } else {
                    paths = sGraph.getPathsTo(target, limit);
                }
                for (Path<Place> path : paths) {
                    res.add((PathRoute) path);
                }
            } else {
                LOG.trace("Split to segments");
                List<Path<Place>> paths = sGraph.getPaths(getPathsOnSegmentCount(sGraph), jumpsToAll-1).getList();
                int i = 0;
                ArrayList<SegmentSearcher> subTasks = new ArrayList<>(THRESHOLD);
                while (i < paths.size()) {
                    subTasks.clear();
                    for (int taskIndex = 0; taskIndex < THRESHOLD && i+taskIndex < paths.size(); taskIndex++) {
                        PathRoute path = (PathRoute) paths.get(i+taskIndex);
                        double newBalance = balance + path.getRoot().getProfit();
                        SegmentSearcher task = new SegmentSearcher(path.get(), target, places, jumps - path.getLength(), newBalance, cargo, 1);
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
            finish(res);
            return res;
        }

        private int getPathsOnSegmentCount(RouteGraph graph){
            if (segmentSize ==0){
                return graph.vertexes.size()*graph.minJumps;
            } else {
                return segmentSize;
            }
        }


        private void add(SegmentSearcher task, PathRoute path, List<PathRoute> res){
            List<PathRoute> tail = task.join();
            if (tail.isEmpty()){
                LOG.trace("Not found route from {} to {}, jumps {}", task.source, task.target, task.jumps);
            } else {
                path.add(tail.get(0), false);
                path.sort(balance, cargo);
                TopList.addToTop(res, path.getEnd(), limit, RouteGraph.byProfitComparator);
            }
        }

        private void finish(List<PathRoute> res){
            if (res.size() < limit)
                res.sort(RouteGraph.byProfitComparator);
        }

    }


}
