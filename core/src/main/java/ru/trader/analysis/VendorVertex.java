package ru.trader.analysis;

import ru.trader.analysis.graph.Edge;
import ru.trader.analysis.graph.Path;
import ru.trader.analysis.graph.Vertex;
import ru.trader.core.Vendor;

import java.util.Collection;
import java.util.Optional;

public class VendorVertex extends Vertex<Vendor> {

    public VendorVertex(Vendor entry, int index) {
        super(entry, index);
    }

    @Override
    public synchronized void connect(Edge<Vendor> edge) {
        VendorsGraph.VendorsBuildEdge vEdge = (VendorsGraph.VendorsBuildEdge) edge;
        Optional<Edge<Vendor>> old = getEdge(edge.getTarget());
        if (old.isPresent()){
            VendorsGraph.VendorsBuildEdge oEdge = (VendorsGraph.VendorsBuildEdge) old.get();
            if (oEdge.getPaths() == null) return;
            Collection<Path<Vendor>> paths = vEdge.getPaths();
            for (Path<Vendor> path : paths) {
                oEdge.add(path);
            }
        } else {
            super.connect(edge);
        }
    }
}
