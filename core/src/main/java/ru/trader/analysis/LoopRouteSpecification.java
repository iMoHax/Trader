package ru.trader.analysis;

import ru.trader.analysis.graph.Edge;
import ru.trader.analysis.graph.Traversal;

import java.util.Optional;

public class LoopRouteSpecification<T> implements MutableRouteSpecification<T> {
    private final boolean unique;

    public LoopRouteSpecification(boolean unique) {
        this.unique = unique;
    }

    private Traversal<T> getStart(Traversal<T> entry){
        Traversal<T> res = null;
        Traversal<T> last = entry;
        Optional<Traversal<T>> head = entry.getHead();
        while (head.isPresent()) {
            Traversal<T> e = head.get();
            res = last;
            last = e;
            head = e.getHead();
        }
        return res;
    }

    @Override
    public boolean specified(Edge<T> edge, Traversal<T> entry) {
        return check(edge, entry);
    }

    @Override
    public void update(Traversal<T> entry) {
        setSkip(entry);
    }

    private boolean check(Edge<T> edge, Traversal<T> entry) {
        Optional<Traversal<T>> head = entry.getHead();
        if (!head.isPresent() || head.get().getEdge() == null) return false;
        Traversal<T> start = getStart(head.get());
        boolean found = edge.isConnect(start.getTarget().getEntry());
        if (found && unique){
            found = !start.isSkipped();
        }
        return found;
    }

    private void setSkip(Traversal<T> entry) {
        if (entry.isSkipped()) return;
        Traversal<T> curr = entry;
        Optional<Traversal<T>> head = entry.getHead();
        while (head.isPresent()) {
            curr.setSkipped(true);
            curr = head.get();
            head = curr.getHead();
        }
    }
}
