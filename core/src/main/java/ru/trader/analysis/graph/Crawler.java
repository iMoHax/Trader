package ru.trader.analysis.graph;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.analysis.*;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.function.Consumer;

public class Crawler<T> {
    private final static Logger LOG = LoggerFactory.getLogger(Crawler.class);
    private final static ForkJoinPool POOL = new ForkJoinPool();
    private final static int THRESHOLD = 12 * (Runtime.getRuntime().availableProcessors() > 1 ? Runtime.getRuntime().availableProcessors() - 1 : 1);
    private final static int SPLIT_SIZE = 3;

    protected final Graph<T> graph;
    protected final CrawlerCallBack callback;
    private final RouteSpecification<T> specification;
    private final CrawlerSpecification<T> crawlerSpecification;
    private T target;
    private int maxSize;

    public Crawler(Graph<T> graph, Consumer<List<Edge<T>>> onFoundFunc, AnalysisCallBack callback) {
        this(graph, new SimpleCrawlerSpecification<>(onFoundFunc), callback);
    }

    public Crawler(Graph<T> graph, CrawlerSpecification<T> specification, AnalysisCallBack callback) {
        this.graph = graph;
        this.callback = new CrawlerCallBack(callback);
        maxSize = graph.getRoot().getLevel();
        crawlerSpecification = specification;
        if (crawlerSpecification.routeSpecification() != null){
            this.specification = crawlerSpecification.routeSpecification();
        } else {
            this.specification = (edge, entry) -> isTarget(edge);
        }
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

    protected boolean isFound(Edge<T> edge, Traversal<T> head){
        return specification.specified(edge, head);
    }

    protected int lastFound(Edge<T> edge, Traversal<T> head){
        return specification.lastFound(edge, head);
    }

    private void updateState(Traversal<T> entry){
        if (specification.mutable()){
            specification.update(entry);
        }
    }

    private void found(List<Edge<T>> res){
        callback.found();
        crawlerSpecification.onFoundFunc().accept(res);
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
        callback.startSearch(s.getEntry(), target, count);
        Optional<Vertex<T>> t = graph.getVertex(target);
        int found = 0;
        if (t.isPresent()) {
            setTarget(target);
            if (count > 1 || s.isEntry(target)) {
                int maxDeep = maxSize - (s.isEntry(target) ? graph.getMinJumps() * 2 : graph.getMinJumps());
                if (maxDeep < 0) maxDeep = 0;
                found = bfs(start(s), maxDeep, count);
            } else {
                found = dfs(start(s), t.get().getLevel() + 1, count);
            }
        }
        callback.endSearch();
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
        callback.startSearch(s.getEntry(), target, count);
        Optional<Vertex<T>> t = graph.getVertex(target);
        int found = 0;
        if (t.isPresent()) {
            setTarget(target);
            int maxDeep = s.isEntry(target) ? maxSize - graph.getMinJumps() * 2 : graph.getMinLevel();
            if (maxDeep < 0) maxDeep = 0;
            found = ucs2(start(s), maxDeep, count);
        }
        LOG.debug("Found {} paths", found);
        callback.endSearch();
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
        if (deep == source.getLevel()){
            for (Edge<T> next : entry.getEdges()) {
                if (isFound(next, entry)){
                    updateState(entry);
                    List<Edge<T>> res = getCopyList(entry, next);
                    LOG.debug("Last edge found, path {}", res);
                    found++;
                    found(res);
                    break;
                }
                if (callback.isCancel()) break;
            }
        }
        if (found < count){
            if (deep < source.getLevel() && entry.size() < maxSize-1) {
                LOG.trace("Search around");
                for (Edge<T> edge : entry.getEdges()) {
                    if (callback.isCancel()) break;
                    if (entry.isSkipped()){
                        LOG.trace("Is skipped");
                        break;
                    }
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
            if (callback.isCancel()) break;
            CostTraversalEntry entry = queue.poll();
            if (entry.isSkipped()){
                LOG.trace("Is skipped");
                continue;
            }
            Vertex<T> source = entry.vertex;
            if (entry.size() >= maxSize){
                LOG.trace("Is limit deep");
                continue;
            }
            LOG.trace("Search from {} to {}, entry {}", source, target, entry);
            Iterator<Edge<T>> iterator = entry.iterator();
            while (iterator.hasNext()){
                if (callback.isCancel()) break;
                Edge<T> edge = iterator.next();
                if (isFound(edge, entry)){
                    updateState(entry);
                    List<Edge<T>> res = getCopyList(entry, edge);
                    LOG.debug("Last edge found, path {}", res);
                    found++;
                    found(res);
                }
                if (entry.isSkipped()){
                    LOG.trace("Is skipped");
                    break;
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
            if (callback.isCancel()) break;
            CostTraversalEntry entry = queue.poll();
            if (entry.isSkipped()){
                LOG.trace("Is skipped");
                continue;
            }
            LOG.trace("Check path entry {}, weight {}", entry, entry.weight);
            Edge<T> edge = entry.getEdge();
            if (edge != null) {
                if (isFound(edge, entry)) {
                    updateState(entry);
                    List<Edge<T>> res = entry.toEdges();
                    LOG.debug("Path found {}", res);
                    found++;
                    found(res);
                    if (found >= count) break;
                }
                if (entry.isSkipped()){
                    LOG.trace("Is skipped");
                    continue;
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
                if (callback.isCancel()) break;
                edge = iterator.next();
                boolean canDeep = !entry.getTarget().isSingle() && deep < entry.getTarget().getLevel();
                if (canDeep || isFound(edge, entry)){
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
        double limit = Double.NaN;
        Queue<CTEntrySupport> targetsQueue;
        Queue<CTEntrySupport> queue;
        if (crawlerSpecification.getGroupCount() > 0){
            int routesByGroup = 1 + count / crawlerSpecification.getGroupCount();
            targetsQueue = new GroupLimitedQueue<>(routesByGroup, Comparator.<CTEntrySupport>naturalOrder(), e -> crawlerSpecification.getGroupGetter(true).apply(e.entry));
            queue = new GroupLimitedQueue<>(routesByGroup * maxSize, Comparator.<CTEntrySupport>naturalOrder(), e -> crawlerSpecification.getGroupGetter(false).apply(e.entry));
        } else {
            targetsQueue = new LimitedQueue<>(count, Comparator.<CTEntrySupport>naturalOrder());
            queue = new LimitedQueue<>(count * maxSize, Comparator.<CTEntrySupport>naturalOrder());
        }
        root.sort();
        queue.add(new CTEntrySupport(root));
        while (!(queue.isEmpty() && targetsQueue.isEmpty()) && count > found){
            if (callback.isCancel()) break;
            CTEntrySupport curr = targetsQueue.peek();
            boolean isTarget = curr != null && (queue.isEmpty() || compareQueue(curr.entry, queue.peek().entry) <= 0);
            if (isTarget){
                targetsQueue.poll();
            } else {
                curr = queue.poll();
            }
            CostTraversalEntry entry = curr.entry;
            LOG.trace("Check path entry {}, weight {}", entry, entry.weight);
            if (specification.updateMutated() && entry.containsSkipped()){
                updateState(entry);
            }
            if (isTarget) {
                if (!entry.isSkipped()){
                    updateState(entry);
                    List<Edge<T>> res = entry.toEdges();
                    LOG.trace("Path found {}", res);
                    found++;
                    found(res);
                    if (found >= count) break;
                }
                CTEntrySupport next = targetsQueue.peek();
                limit = next != null ? next.entry.getWeight() :  Double.NaN;
                if (deep > entry.getTarget().getLevel() || entry.size() >= maxSize){
                    LOG.trace("Is limit deep");
                    continue;
                }
            }
            DFS task = new DFS(curr, deep, count - found, limit);
            POOL.invoke(task);
            targetsQueue.addAll(task.getTargets());
            queue.addAll(task.getQueue());
        }
        return found;
    }

    protected int compareQueue(CostTraversalEntry target, CostTraversalEntry queue) {
        return target.compareTo(queue);
    }

    private class CTEntrySupport implements Comparable<CTEntrySupport>, Iterator<Edge<T>>{
        private final CTEntrySupport parent;
        private Iterator<Edge<T>> iterator;
        private final CostTraversalEntry entry;
        private Edge<T> next;

        private CTEntrySupport(CostTraversalEntry entry) {
            this(null, entry);
        }

        private CTEntrySupport(CTEntrySupport parent, CostTraversalEntry entry) {
            this.parent = parent;
            this.entry = entry;
        }

        private void checkIterator(){
            if (iterator == null){
                this.iterator = entry.iterator();
                next = iterator.hasNext() ? next = iterator.next() : null;
            }
        }

        @Override
        public int compareTo(@NotNull CTEntrySupport o) {
            int cmp = entry.compareTo(o.entry);
            if (cmp != 0) return cmp;
            cmp = Integer.compare(entry.size(), o.entry.size());
            if (cmp != 0) return cmp;
            Iterator<Edge<T>> iter1 = entry.routeIterator();
            Iterator<Edge<T>> iter2 = o.entry.routeIterator();
            while (iter1.hasNext()){
                Edge<T> edge = iter1.next();
                Edge<T> oEdge = iter2.next();
                cmp = oEdge.compareTo(edge);
                if (cmp != 0) return cmp;
            }
            return 0;
        }

        @Override
        public boolean hasNext() {
            checkIterator();
            return next != null;
        }

        @Override
        public Edge<T> next(){
            checkIterator();
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
        private CTEntrySupport root;
        private CTEntrySupport curr;
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
            if (crawlerSpecification.getGroupCount() > 0){
                int routesByGroup = 1 + count / crawlerSpecification.getGroupCount();
                targets = new GroupLimitedQueue<>(routesByGroup, Comparator.<CTEntrySupport>naturalOrder(), e -> crawlerSpecification.getGroupGetter(true).apply(e.entry));
                queue = new GroupLimitedQueue<>(routesByGroup * maxSize, Comparator.<CTEntrySupport>naturalOrder(), e -> crawlerSpecification.getGroupGetter(false).apply(e.entry));
            } else {
                targets = new LimitedQueue<>(count, Comparator.<CTEntrySupport>naturalOrder());
                queue = new LimitedQueue<>(count * maxSize, Comparator.<CTEntrySupport>naturalOrder());
            }
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

        private boolean cancel(){
            if (isCancelled() || callback.isCancel()) return true;
            if (root.entry.isSkipped()){
                LOG.trace("Root skipped");
                if (isSubTask){
                    LOG.trace("Stop sub task");
                    return true;
                } else {
                    curr = root;
                }
            }
            return false;
        }

        private boolean skip(){
            if (curr.entry.isSkipped()){
                while (curr.entry.isSkipped()){
                    LOG.trace("Is skipped, return to prev level");
                    if (!levelUp()) break;
                }
                return true;
            }
            return false;
        }

        private boolean levelUp(){
            if (isRoot(curr)) return false;
            assert curr.parent != null;
            LOG.trace("Return to prev level");
            if (curr == root){
                root = root.parent;
            }
            curr = curr.parent;
            return true;
        }

        private void search(){
            curr = root;
            LOG.trace("Start {}", root);
            while (!curr.hasNext() && levelUp()){
                LOG.trace("Level complete");
            }
            while (curr.hasNext()){
                if (cancel()) break;
                Edge<T> edge = curr.next();
                CostTraversalEntry entry = curr.entry;
                if (skip()) continue;
                LOG.trace("Check edge {}, entry {}, weight {}, curr {}", edge, entry, entry.weight, curr);
                int lastFound = lastFound(edge, entry);
                boolean isTarget = lastFound == 0;
                boolean canDeep = !entry.getTarget().isSingle() && deep < entry.getTarget().getLevel() && entry.size()+lastFound < maxSize;
                if (canDeep || isTarget){
                    CostTraversalEntry nextEntry = travers(entry, edge);
                    if (canDeep){
                        nextEntry.sort();
                    }
                    curr = new CTEntrySupport(curr, nextEntry);
                    if (isTarget){
                        LOG.trace("Found, add entry {} to queue", nextEntry);
                        targets.add(curr);
                        limit = Double.isNaN(limit) ? nextEntry.getWeight() : Math.min(limit, nextEntry.getWeight());
                        levelUp();
                    } else {
                        if (skip()) continue;
                        if (!Double.isNaN(limit) && nextEntry.getWeight() >= limit){
                            LOG.trace("Not found, limit {}, add entry {} to queue", limit, nextEntry);
                            queue.add(curr);
                            levelUp();
                            if (!levelUp()){
                                break;
                            }
                        } else {
                            if (!isRoot(curr) && maxSize-nextEntry.size() < SPLIT_SIZE){
                                if (addSubTask(curr)){
                                    levelUp();
                                }
                            }
                        }
                    }
                } else {
                    LOG.trace("Is limit deep");
                }
                while (!curr.hasNext() && levelUp()){
                    LOG.trace("Level complete");
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
            limit = Double.isNaN(limit) ? subTask.limit : Math.min(limit, subTask.limit);
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
        private transient boolean skipped;

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
            this.skipped = head.isSkipped();
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

        @Override
        public void setSkipped(boolean skipped) {
            this.skipped = skipped;
        }

        @Override
        public boolean isSkipped() {
            return skipped;
        }

        @Override
        public boolean containsSkipped() {
            if (skipped) return true;
            Optional<Traversal<T>> head = getHead();
            while (head.isPresent()) {
                Traversal<T> curr = head.get();
                if (curr.isSkipped()) return true;
                head = curr.getHead();
            }
            return false;
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
