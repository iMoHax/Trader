package ru.trader.analysis.graph;

import ru.trader.analysis.AnalysisCallBack;
import ru.trader.core.Profile;

import java.util.Collections;

public class TestGraph<T extends Connectable<T>> extends ConnectibleGraph<T> {
    public TestGraph(Profile profile, AnalysisCallBack callback) {
        super(profile, callback);
    }

    private Vertex<T> createVertex(T entry){
        Vertex<T> vertex = getVertex(entry).orElse(null);
        if (vertex == null){
            vertex = newInstance(entry, vertexes.size());
            vertexes.add(vertex);
        }
        return vertex;
    }

    public BuildEdge createEdge(T x1, T x2){
        ConnectibleGraphBuilder builder = new ConnectibleGraphBuilder(createVertex(x1), Collections.emptySet(), 0,0);
        BuildHelper<T> helper = builder.createHelper(x2);
        return builder.createEdge(helper, createVertex(x2));
    }
}
