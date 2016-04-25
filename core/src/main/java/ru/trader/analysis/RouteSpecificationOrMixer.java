package ru.trader.analysis;

import ru.trader.analysis.graph.Edge;
import ru.trader.analysis.graph.Traversal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class RouteSpecificationOrMixer<T> implements RouteSpecificationMixer<T>, RouteSpecification<T> {

    private final List<RouteSpecification<T>> specifications;

    public RouteSpecificationOrMixer() {
        this.specifications = new ArrayList<>();
    }

    @Override
    public Collection<RouteSpecification<T>> getMixed() {
        return specifications;
    }

    @Override
    public boolean specified(Edge<T> edge, Traversal<T> entry) {
        for (RouteSpecification<T> specification : specifications) {
            if (specification.specified(edge, entry)) return true;
        }
        return false;
    }

    @Override
    public boolean content(Edge<T> edge, Traversal<T> entry) {
        for (RouteSpecification<T> specification : specifications) {
            if (specification.content(edge, entry)){
                return true;
            }
        }
        return false;
    }

    @Override
    public int lastFound(Edge<T> edge, Traversal<T> entry) {
        int res = Integer.MAX_VALUE;
        for (RouteSpecification<T> specification : specifications) {
            res = Math.min(res, specification.lastFound(edge, entry));
        }
        return res;
    }

    @Override
    public int maxMatches() {
        int res = 0;
        for (RouteSpecification<T> specification : specifications) {
            res = Math.max(res, specification.maxMatches());
        }
        return res;
    }

    @Override
    public int minMatches() {
        if (specifications.isEmpty()) return 0;
        int res = Integer.MAX_VALUE;
        for (RouteSpecification<T> specification : specifications) {
            res = Math.min(res, specification.minMatches());
        }
        return res;
    }

    @Override
    public boolean updateMutated() {
        for (RouteSpecification<T> specification : specifications) {
            if (specification.updateMutated()){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mutable() {
        for (RouteSpecification<T> specification : specifications) {
            if (specification.mutable()){
                return true;
            }
        }
        return false;
    }

    @Override
    public void update(Traversal<T> entry) {
        specifications.forEach(s -> s.update(entry));
    }

    @Override
    public RouteSpecification<T> and(RouteSpecification<T> specification) {
        Collection<RouteSpecification<T>> res = new ArrayList<>(specifications.size());
        for (RouteSpecification<T> s : specifications) {
            res.add(RouteSpecificationAndMixer.mix(s, specification));
        }
        specifications.clear();
        specifications.addAll(res);
        return this;
    }

    @Override
    public RouteSpecification<T> or(RouteSpecification<T> other) {
        return this.mix(other);
    }

    @Override
    public RouteSpecification<T> mix(RouteSpecification<T> specification) {
        if (specification instanceof NullRouteSpecification) return this;
        specifications.add(specification);
        specifications.sort(ROUTE_SPECIFICATION_COMPARATOR);
        return this;
    }

    @SafeVarargs
    public static <T> RouteSpecification<T> mix(RouteSpecification<T> ... specification){
        RouteSpecificationOrMixer<T> res = new RouteSpecificationOrMixer<>();
        for (RouteSpecification<T> s : specification) {
            res.mix(s);
        }
        return res;
    }


    public final static RouteSpecificationComparator ROUTE_SPECIFICATION_COMPARATOR = new RouteSpecificationComparator();

    private static class RouteSpecificationComparator implements Comparator<RouteSpecification> {

        private int getHard(RouteSpecification specification){
            if (specification instanceof RouteSpecificationByTarget){
                return 1;
            }
            if (specification instanceof RouteSpecificationByTargets){
                RouteSpecificationByTargets s = (RouteSpecificationByTargets) specification;
                if (s.isAny()){
                    return 2;
                }
                return s.isContainAny() ? 3 : 5;
            }
            if (specification instanceof RouteSpecificationByPair){
                return 4;
            }
            if (specification instanceof LoopRouteSpecification){
                return 4;
            }
            return 6;
        }

        @Override
        public int compare(RouteSpecification o1, RouteSpecification o2) {
            int hard1 = getHard(o1);
            int hard2 = getHard(o2);
            int cmp = Integer.compare(hard1, hard2);
            if (cmp != 0) return cmp;
            return Integer.compare(o1.minMatches(), o2.minMatches());
        }
    }
}