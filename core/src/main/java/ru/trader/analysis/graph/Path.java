package ru.trader.analysis.graph;

import ru.trader.graph.Connectable;

import java.util.ArrayList;
import java.util.List;

public class Path<T extends Connectable<T>> {
    private final List<PathEntry<T>> entries;

    public Path(List<ConnectibleEdge<T>> edges) {
        entries = new ArrayList<>(edges.size());
        for (int i = 0; i < edges.size(); i++) {
            ConnectibleEdge<T> edge = edges.get(i);
            if (i==0) entries.add(new PathEntry<>(edge.getSource().getEntry(), false));
            entries.add(new PathEntry<>(edge.getTarget().getEntry(), edge.isRefill()));
        }
    }

    public PathEntry<T> get(int index){
        return entries.get(index);
    }

}
