package ru.trader.analysis.graph;

public interface CostTraversal<T> extends Traversal<T> {
    double getWeight();

}
