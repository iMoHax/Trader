package ru.trader.analysis.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Mo on 20.05.2015.
 */
public class SimpleCollector<T> {
    private List<List<Edge<T>>> paths = new ArrayList<>();

    public boolean add(List<Edge<T>> path){
        paths.add(path);
        return true;
    }

    public List<List<Edge<T>>> get() {
        return paths;
    }

    public List<Edge<T>> get(int index) {
        if (index >= paths.size()) return Collections.emptyList();
        return paths.get(index);
    }

    public void clear(){
        paths.clear();
    }

    public double getWeight(int index){
        if (index >= paths.size()) return 0;
        return paths.get(index).stream().mapToDouble(Edge::getWeight).sum();
    }

    public Collection<Double> getWeights(){
        return paths.stream().map(p -> p.stream().mapToDouble(Edge::getWeight).sum()).collect(Collectors.toList());
    }
}
