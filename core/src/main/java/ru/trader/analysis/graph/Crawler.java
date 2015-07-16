package ru.trader.analysis.graph;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.analysis.LimitedQueue;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.function.Predicate;

public class Crawler<T> {
    private final static Logger LOG = LoggerFactory.getLogger(Crawler.class);
    private final static ForkJoinPool POOL = new ForkJoinPool();
    private final static int THRESHOLD = 12 * (Runtime.getRuntime().availableProcessors() > 1 ? Runtime.getRuntime().availableProcessors() - 1 : 1);
    private final static int SPLIT_SIZE = 3;

    protected final Graph<T> graph;
    private final Predicate<List<Edge<T>>> onFoundFunc;
    private final Predicate<Edge<T>> isFound;
    private T target;
    private int maxSize;

    public Crawler(Graph<T> graph, Predicate<List<Edge<T>>> onFoundFunc) {
        this.graph = graph;
        maxSize = graph.getRoot().getLevel();
        this.onFoundFunc = onFoundFunc;
        this.isFound = this::isTarget;
    }

    public Crawler(Graph<T> graph, Predicate<Edge<T>> isFoundFunc, Predicate<List<Edge<T>>> onFoundFunc) {
        this.graph = graph;
        maxSize = graph.getRoot().getLevel();
        this.onFoundFunc = onFoundFunc;
        this.isFound = isFoundFunc;
    }

    protected List<Edge<T>> getCopyList(Traversal<T> head, Edge<T> tail){
        List<Edge<T>> res = new ArrayList<>(head.size()+ 1);
        res.addAll(head.toEdges());
        res.add(tail);
        return res;
    }

    public T getTarget() {
        return target;
    }

    private void setTarget(T target) {
        if (this.target != null && target != null){
            throw new IllegalStateException("Crawler is busy");
        }
        this.target = target;
    }

    private boolean isTarget(Edge<T> edge){
        return edge.isConnect(this.target);
    }

    protected boolean isFound(Edge<T> edge){
        return isFound.test(edge);
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public void findFast(T target){
        findFast(target, 1);
    }

    public void findFast(T target, int count){
        findFast(graph.getRoot(), target, count);
    }

    public void findFast(T source, T target){
        findFast(source, target, 1);
    }

    public void findFast(T source, T target, int count){
        Optional<Vertex<T>> s = graph.getVertex(source);
        if (s.isPresent()){
            findFast(s.get(), target, count);
        }
    }

    private void findFast(Vertex<T> s, T target, int count){
        Optional<Vertex<T>> t = graph.getVertex(target);
        int found = 0;
        if (t.isPresent()) {
            setTarget(target);
            if (count > 1 || s.isEntry(target)) {
                int maxDeep = maxSize - (s.isEntry(target) ? graph.getMinJumps() * 2 : graph.getMinJumps());
                if (maxDeep < 0) maxDeep = 0;
                found = bfs(start(s), maxDeep, count);
            } else {
                found = dfs(start(s), Math.min(t.get().getLevel() + 1, s.getLevel()), count);
            }
        }
        LOG.debug("Found {} paths", found);
        setTarget(null);
    }

    public void findMin(T target){
        findMin(target, 1);
    }

    public void findMin(T target, int count){
        findMin(graph.getRoot(), target, count);
    }

    public void findMin(T source, T target){
        findMin(source, target, 1);
    }

    public void findMin(T source, T target, int count){
        Optional<Vertex<T>> s = graph.getVertex(source);
        if (s.isPresent()){
            findMin(s.get(), target, count);
        }
    }

    public void findMin(Vertex<T> s, T target, int count){
        Optional<Vertex<T>> t = graph.getVertex(target);
        int found = 0;
        if (t.isPresent()) {
            setTarget(target);
            int maxDeep = s.isEntry(target) ? maxSize - graph.getMinJumps() * 2 : graph.getMinLevel();
            if (maxDeep < 0) maxDeep = 0;
            if (maxSize - maxDeep <= SPLIT_SIZE){
                found = ucs(start(s), maxDeep, count);
            } else {
                found = ucs2(start(s), maxDeep, count);
            }
        }
        LOG.debug("Found {} paths", found);
        setTarget(null);
    }

    protected CostTraversalEntry start(Vertex<T> vertex){
        return new CostTraversalEntry(vertex);
    }

    protected CostTraversalEntry travers(CostTraversalEntry entry, Edge<T> edge){
        return new CostTraversalEntry(entry, edge);
    }

    private int dfs(CostTraversalEntry entry, int deep, int count) {
        int found = 0;
        Vertex<T> source = entry.vertex;
        LOG.trace("DFS from {} to {}, deep {}, count {}, entry {}", source, target, deep, count, entry);
        boolean stop = false;
        if (deep == source.getLevel()){
            for (Edge<T> next : entry.getEdges()) {
                if (isFound(next)){
                    List<Edge<T>> res = getCopyList(entry, next);
                    LOG.debug("Last edge found, path {}", res);
                    found++;
                    if (!onFoundFunc.test(res)){
                        stop = true;
                    }
                    break;
                }
            }
        }
        if (!stop && found < count){
            if (deep <= source.getLevel() && entry.size() < maxSize-1) {
                LOG.trace("Search around");
                for (Edge<T> edge : entry.getEdges()) {
                    if (edge.getTarget().isSingle()) continue;
                    found += dfs(travers(entry, edge), deep, count-found);
                    if (found >= count) break;
                }
            }
        }
        return found;
    }

    private int bfs(CostTraversalEntry root, int deep, int count) {
        LOG.trace("BFS from {} to {}, deep {}, count {}", root.vertex, target, deep, count);
        int found = 0;
        LinkedList<CostTraversalEntry> queue = new LinkedList<>();
        root.sort();
        queue.add(root);
        while (!queue.isEmpty() && count > found){
            CostTraversalEntry entry = queue.poll();
            Vertex<T> source = entry.vertex;
            if (entry.size() >= maxSize){
                LOG.trace("Is limit deep");
                continue;
            }
            LOG.trace("Search from {} to {}, entry {}", source, target, entry);
            Iterator<Edge<T>> iterator = entry.iterator();
            while (iterator.hasNext()){
                Edge<T> edge = iterator.next();
                if (isFound(edge)){
                    List<Edge<T>> res = getCopyList(entry, edge);
                    LOG.debug("Last edge found, path {}", res);
                    found++;
                    if (!onFoundFunc.test(res)){
                        break;
                    }
                }
                if (found >= count) break;
                if (edge.getTarget().isSingle()) continue;
                if (deep < source.getLevel()) {
                    CostTraversalEntry nextEntry = travers(entry, edge);
                    nextEntry.sort();
                    queue.add(nextEntry);
                }
            }
        }
        return found;
    }

    private int ucs(CostTraversalEntry root, int deep, int count) {
        LOG.trace("UCS from {} to {}, deep {}, count {}", root.vertex, target, deep, count);
        int found = 0;
        PriorityQueue<CostTraversalEntry> queue = new PriorityQueue<>();
        queue.add(root);
        while (!queue.isEmpty() && count > found){
            CostTraversalEntry entry = queue.poll();
            LOG.trace("Check path entry {}, weight {}", entry, entry.weight);
            Edge<T> edge = entry.getEdge();
            if (edge != null) {
                if (isFound(edge)) {
                    List<Edge<T>> res = entry.toEdges();
                    LOG.debug("Path found {}", res);
                    found++;
                    if (!onFoundFunc.test(res)){
                        break;
                    }
                    if (found >= count) break;
                }
                if (edge.getTarget().isSingle()){
                    continue;
                }
            }
            if (entry.size() >= maxSize){
                LOG.trace("Is limit deep");
                continue;
            }
            Iterator<Edge<T>> iterator = entry.iterator();
            while (iterator.hasNext()){
                edge = iterator.next();
                boolean canDeep = !entry.getTarget().isSingle() && deep < entry.getTarget().getLevel();
                if (canDeep || isFound(edge)){
                    LOG.trace("Add edge {} to queue", edge);
                    queue.add(travers(entry, edge));
                }
            }
        }
        return found;
    }

    private int ucs2(CostTraversalEntry root, int deep, int count) {
        LOG.trace("UCS2 from {} to {}, deep {}, count {}", root.vertex, target, deep, count);
        int found = 0;
        double limit = Double.MAX_VALUE;
        LimitedQueue<CTEntrySupport> targetsQueue = new LimitedQueue<>(count, Comparator.<CTEntrySupport>naturalOrder());
        LimitedQueue<CTEntrySupport> queue = new LimitedQueue<>(count, Comparator.<CTEntrySupport>naturalOrder());
        root.sort();
        queue.add(new CTEntrySupport(root));
        while (!(queue.isEmpty() && targetsQueue.isEmpty()) && count > found){
            int alreadyFound = targetsQueue.size();
            CTEntrySupport curr = targetsQueue.peek();
            boolean isTarget = curr != null && (queue.isEmpty() || alreadyFound + found >= count || Comparator.<CTEntrySupport>naturalOrder().compare(curr, queue.peek()) <= 0);
            if (isTarget){
                targetsQueue.poll();
            } else {
                curr = queue.poll();
            }
            CostTraversalEntry entry = curr.entry;
            LOG.trace("Check path entry {}, weight {}", entry, entry.weight);
            if (isTarget) {
                List<Edge<T>> res = entry.toEdges();
                LOG.trace("Path found {}", res);
                found++;
                if (!onFoundFunc.test(res)){
                    break;
                }
                if (found >= count) break;
                CTEntrySupport next = targetsQueue.peek();
                limit = next != null ? next.entry.getWeight() :  Double.MAX_VALUE;
            }
            if (alreadyFound + found < count){
                LOG.trace("Continue search, limit {}", limit);
            } else {
                LOG.trace("Already {} found, extracting", alreadyFound);
                continue;
            }
            if (deep >= entry.getTarget().getLevel() || entry.size() >= maxSize){
                LOG.trace("Is limit deep");
                continue;
            }
            DFS task = new DFS(curr, deep, count - found, limit);
            POOL.invoke(task);
            targetsQueue.addAll(task.getTargets());
            queue.addAll(task.getQueue());
        }
        return found;
    }

    private class CTEntrySupport implements Comparable<CTEntrySupport>, Iterator<Edge<T>>{
        private final CTEntrySupport parent;
        private final Iterator<Edge<T>> iterator;
        private final CostTraversalEntry entry;
        private Edge<T> next;

        private CTEntrySupport(CostTraversalEntry entry) {
            this(null, entry);
        }

        private CTEntrySupport(CTEntrySupport parent, CostTraversalEntry entry) {
            this.parent = parent;
            this.iterator = entry.iterator();
            this.entry = entry;
            next = iterator.hasNext() ? next = iterator.next() : null;
        }

        @Override
        public int compareTo(@NotNull CTEntrySupport o) {
            int cmp = entry.compareTo(o.entry);
            if (cmp != 0) return cmp;
            cmp = Integer.compare(entry.size(), o.entry.size());
            if (cmp != 0) return cmp;
            CostTraversal<T> cur = entry;
            CostTraversal<T> oCur = o.entry;
            while (!cur.isRoot()){
                Edge<T> edge = cur.getEdge();
                Edge<T> oEdge = oCur.getEdge();
                cmp = oEdge.compareTo(edge);
                if (cmp != 0) return cmp;
                cur = (CostTraversal<T>) cur.getHead().get();
                oCur = (CostTraversal<T>) oCur.getHead().get();
            }
            return 0;
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public Edge<T> next(){
            Edge<T> res = next;
            next = iterator.hasNext() ? next = iterator.next() : null;
            return res;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("{");
            sb.append("entry=").append(entry);
            sb.append(", next=").append(next);
            sb.append('}');
            return sb.toString();
        }
    }

    private class DFS extends RecursiveAction {
        private final CTEntrySupport root;
        private final int count;
        private final int deep;
        private final Collection<CTEntrySupport> queue;
        private final Collection<CTEntrySupport> targets;
        private final ArrayList<DFS> subTasks;
        private final boolean isSubTask;
        private double limit;

        private DFS(CTEntrySupport root, int deep, int count, double limit) {
            this(root, deep, count, limit, false);
        }

        private DFS(CTEntrySupport root, int deep, int count, double limit, boolean subtask) {
            this.root = root;
            this.deep = deep;
            this.count = count;
            this.limit = limit;
            queue = new LimitedQueue<>(count, Comparator.<CTEntrySupport>naturalOrder());
            targets = new LimitedQueue<>(count, Comparator.<CTEntrySupport>naturalOrder());
            subTasks = new ArrayList<>(THRESHOLD);
            isSubTask = subtask;
        }

        private boolean isRoot(CTEntrySupport entry){
            return entry.parent == null || isSubTask && entry == root;
        }

        private Collection<CTEntrySupport> getTargets() {
            if (!isDone())
                throw new IllegalStateException();
            return targets;
        }

        private Collection<CTEntrySupport> getQueue() {
            if (!isDone())
                throw new IllegalStateException();
            return queue;
        }

        private void search(){
            CTEntrySupport curr = root;
            LOG.trace("Start {}", root);
            while (curr.hasNext()){
                Edge<T> edge = curr.next();
                CostTraversalEntry entry = curr.entry;
                LOG.trace("Check edge {}, entry {}, weight {}", edge, entry, entry.weight);
                boolean isTarget = isFound(edge);
                boolean canDeep = !entry.getTarget().isSingle() && deep < entry.getTarget().getLevel() && entry.size() < maxSize-1;
                if (canDeep || isTarget){
                    CostTraversalEntry nextEntry = travers(entry, edge);
                    nextEntry.sort();
                    curr = new CTEntrySupport(curr, nextEntry);
                    if (isTarget){
                        LOG.trace("Found, add entry {} to queue", nextEntry);
                        targets.add(curr);
                        limit = nextEntry.getWeight();
                        curr = curr.parent;
                    } else {
                        if (nextEntry.getWeight() >= limit && targets.size() > 0){
                            if (targets.size() < count){
                                LOG.trace("Not found, limit {}, add entry {} to queue", limit, nextEntry);
                                queue.add(curr);
                            } else {
                                LOG.trace("Not found, limit {}, don't add entry {} to queue", limit, nextEntry);
                            }
                            if (!isRoot(curr.parent)){
                                curr = curr.parent.parent;
                            } else {
                                break;
                            }
                        } else {
                            if (!isRoot(curr) && maxSize-nextEntry.size() < SPLIT_SIZE){
                                if (addSubTask(curr))
                                    curr = curr.parent;
                            }
                        }
                    }
                } else {
                    LOG.trace("Is limit deep");
                }
                while (!curr.hasNext() && !isRoot(curr)){
                    LOG.trace("Level complete, return to prev level");
                    curr = curr.parent;
                }
            }
            LOG.trace("Done {}", root);
            joinSubTasks();
        }

        private boolean addSubTask(CTEntrySupport entry){
            if (subTasks.size() < THRESHOLD){
                DFS subTask = new DFS(entry, deep, count, limit, true);
                subTask.fork();
                subTasks.add(subTask);
                return true;
            } else {
                joinSubTasks();
            }
            return false;
        }

        private void joinSubTasks(){
            for (DFS subTask : subTasks) {
                subTask.join();
                fill(subTask);
            }
            subTasks.clear();
        }

        private void fill(DFS subTask){
            LOG.trace("Sub task is done");
            targets.addAll(subTask.getTargets());
            queue.addAll(subTask.getQueue());
            limit = Math.min(limit, subTask.limit);
        }

        @Override
        protected void compute() {
            search();
        }
    }


    protected class TraversalEntry implements Traversal<T> {
        protected final Traversal<T> head;
        protected final Vertex<T> vertex;
        private final Edge<T> edge;
        private List<Edge<T>> edges;
        private Integer size;

        protected TraversalEntry(Vertex<T> vertex) {
            this.vertex = vertex;
            this.head = null;
            this.edge = null;
            edges = null;
        }

        protected TraversalEntry(Traversal<T> head, Edge<T> edge) {
            this.head = head;
            this.vertex = edge.getTarget();
            this.edge = edge;
            edges = null;
        }

        @Override
        public Vertex<T> getTarget() {
            return vertex;
        }

        @Override
        public Optional<Traversal<T>> getHead() {
            return Optional.ofNullable(head);
        }

        public Edge<T> getEdge() {
            return edge;
        }

        @Override
        public final List<Edge<T>> getEdges(){
            if (edges == null){
                edges = collect(vertex.getEdges());
            }
            return edges;
        }

        @Override
        public final Iterator<Edge<T>> iterator(){
            return getEdges().iterator();
        }

        @Override
        public void sort(){
            getEdges().sort(Comparator.<Edge<T>>naturalOrder());
        }

        protected List<Edge<T>> collect(Collection<Edge<T>> src){
            return new ArrayList<>(src);
        }

        @Override
        public int size() {
            if (size == null){
                size = Traversal.super.size();
            }
            return size;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("{");
            sb.append("head=");
            if (!isRoot()){
                sb.append(getHead().get().toEdges());
            }
            sb.append(", edge=").append(edge);
            sb.append("}");
            return sb.toString();
        }
    }

    protected class CostTraversalEntry extends TraversalEntry implements CostTraversal<T>, Comparable<CostTraversalEntry>{
        protected Double weight;

        protected CostTraversalEntry(Vertex<T> vertex) {
            super(vertex);
        }

        protected CostTraversalEntry(CostTraversalEntry head, Edge<T> edge) {
            super(head, edge);
        }

        @Override
        public double getWeight(){
            if (weight == null){
                Edge<T> edge = getEdge();
                Optional<Traversal<T>> head = getHead();
                weight = (head.isPresent() ? ((CostTraversalEntry)head.get()).getWeight() : 0)  + (edge != null ? edge.getWeight() : 0);
            }
            return weight;
        }

        @Override
        public int compareTo(@NotNull CostTraversalEntry other) {
            int cmp = Double.compare(getWeight(), other.getWeight());
            if (cmp != 0) return cmp;
            return Integer.compare(size(), other.size());
        }
    }
}
