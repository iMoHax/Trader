package ru.trader.graph;

public class Path<T extends Connectable<T>> {
    private final Path<T> head;
    private final Vertex<T> target;
    private boolean refill;


    public Path(Vertex<T> source) {
        this.head = null;
        this.target = source;
        this.refill = false;
    }

    protected Path(Path<T> head, Vertex<T> vertex, boolean refill) {
        this.head = head;
        this.target = vertex;
        this.refill = refill;
    }

    public Path<T> connectTo(Edge<T> edge, boolean refill){
        return new Path<>(this, edge.getTarget(), refill);
    }

    public void finish(){
        finish(target);
    }

    protected void finish(Vertex<T> target){
        if (!isRoot()){
            head.finish(target);
            if (target != head.target) head.finish();
        }
    }

    public boolean isRoot(){
        return head == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Path)) return false;

        Path path = (Path) o;
        return (isRoot() ? path.isRoot() : head.equals(path.head)) && target.equals(path.target);

    }

    @Override
    public int hashCode() {
        int result = head != null ? head.hashCode() : 0;
        result = 31 * result + target.hashCode();
        return result;
    }

    @Override
    public String toString(){
        if (isRoot()) return target.getEntry().toString();
        final StringBuilder sb = new StringBuilder(head.toString());
        if (refill) sb.append("(R)");
        sb.append(" -> ").append(target.getEntry());
        return sb.toString();
    }

    public boolean contains(Edge<T> edge) {
        return target.equals(edge.getTarget()) || (!isRoot() && head.contains(edge));
    }

    @SafeVarargs
    public static <T extends Connectable<T>> Path<T> toPath(T... items){
        T s = items[0];
        Path<T> path = new Path<>(new Vertex<>(s));
        for (int i = 1; i < items.length; i++) {
            T t = items[i];
            path = new Path<>(path, new Vertex<>(t), false);
            s = t;
        }
        return path;
    }

    public Vertex<T> getTarget() {
        return target;
    }

    public boolean isRefill(){
        return refill;
    }

    protected Path<T> getHead(){
        return head;
    }


}
