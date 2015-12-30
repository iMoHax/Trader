package ru.trader.analysis;

import ru.trader.analysis.graph.Edge;
import ru.trader.analysis.graph.Traversal;

import java.util.*;


public class RouteSpecificationAndMixer<T> implements RouteSpecificationMixer<T>, RouteSpecification<T>  {
    private final List<RouteSpecification<T>> specifications;

    public RouteSpecificationAndMixer() {
        this.specifications = new ArrayList<>();
    }

    @SafeVarargs
    private RouteSpecificationAndMixer(RouteSpecification<T> ... specification) {
        this();
        Collections.addAll(specifications, specification);
    }


    @Override
    public Collection<RouteSpecification<T>> getMixed() {
        return specifications;
    }

    @Override
    public boolean specified(Edge<T> edge, Traversal<T> entry) {
        for (RouteSpecification<T> specification : specifications) {
            if (!specification.specified(edge, entry)) return false;
        }
        return true;
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
        int res = 0;
        for (RouteSpecification<T> specification : specifications) {
            res += specification.lastFound(edge, entry);
        }
        return res;
    }

    @Override
    public int matchCount() {
        int res = 0;
        for (RouteSpecification<T> specification : specifications) {
            res += specification.matchCount();
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
    public RouteSpecification<T> and(RouteSpecification<T> other) {
        return this.mix(other);
    }

    @Override
    public RouteSpecification<T> mix(RouteSpecification<T> specification) {
        if (specification instanceof RouteSpecificationOrMixer){
            RouteSpecificationOrMixer<T> other = (RouteSpecificationOrMixer<T>)specification;
            for (RouteSpecification<T> s : specifications) {
                other.and(s);
            }
            return other;
        }
        if (specification instanceof RouteSpecificationAndMixer){
            Collection<RouteSpecification<T>> other = ((RouteSpecificationAndMixer<T>)specification).getMixed();
            other.forEach(this::collapse);
        } else {
            collapse(specification);
        }
        specifications.sort(RouteSpecificationOrMixer.ROUTE_SPECIFICATION_COMPARATOR);
        return this;
    }

    private int getMinHope(Collection<RouteSpecification<T>> specifications){
        if (specifications.isEmpty()) return -1;
        return specifications.stream().mapToInt(RouteSpecification::matchCount).sum();
    }

    private void collapse(RouteSpecification<T> specification){
        if (specifications.isEmpty()){
            specifications.add(specification);
            return;
        }
        Collection<RouteSpecification<T>> res = new ArrayList<>(specifications.size());
        RouteSpecification<T> minSpec = null;
        Collection<RouteSpecification<T>> minCollapsed = null;
        int min = -1;
        for (RouteSpecification<T> s : specifications) {
            Collection<RouteSpecification<T>> collapsed = andMix(s, specification);
            int hopes = getMinHope(collapsed);
            if (hopes != -1 && (minCollapsed == null || hopes < min || (hopes == min && minCollapsed.size()<collapsed.size()))){
                minCollapsed = collapsed;
                min = hopes;
                if (minSpec != null){
                    res.add(minSpec);
                }
                minSpec = s;
            } else {
                res.add(s);
            }
        }
        if (minCollapsed != null){
            res.addAll(minCollapsed);
        } else {
            res.add(specification);
        }
        specifications.clear();
        specifications.addAll(res);
    }

    @SafeVarargs
    public static <T> RouteSpecification<T> mix(RouteSpecification<T> ... specification){
        RouteSpecificationAndMixer<T> res = new RouteSpecificationAndMixer<>();
        for (RouteSpecification<T> s : specification) {
            res.mix(s);
        }
        return res;
    }

    // target fot time t1 A = At1
    // contains A and B and C fot time t1 = [A&B&C]t1
    // target A or B or C  fot time t1 = [A|B|C]t1
    // contains A or B or C  fot time t1 = [A,B,C]t1
    // pair A or B or C to D fot time t1 = [A,B,C - D]t1
    // contains A from t1 to t2 time = A[t1-t2]

    public static <T> Collection<RouteSpecification<T>> andMix(RouteSpecificationByTarget<T> spec, RouteSpecification<T> other) {
        Collection<RouteSpecification<T>> res = new ArrayList<>();
        T target = spec.getTarget();
        if (other instanceof RouteSpecificationByTarget){
            RouteSpecificationByTarget<T> os = (RouteSpecificationByTarget<T>)other;
            T otherTarget = os.getTarget();
            if (Objects.equals(target, otherTarget)){
                //At1 & At2 -> At1
                long time = Math.min(spec.getEnd(), os.getEnd());
                RouteSpecification<T> newSpec = new RouteSpecificationByTarget<>(target, time);
                res.add(newSpec);
            } else {
                //At1 & Bt2 -> empty
                res.add(new NullRouteSpecification<>());
            }
        } else
        if (other instanceof RouteSpecificationByTargets){
            RouteSpecificationByTargets<T> os = ((RouteSpecificationByTargets<T>)other);
            Collection<T> otherTargets = os.getTargets();
            if (otherTargets.contains(target)){
                if (os.isAll()){
                    //At1 & [A&B&C]t2 -> At1 & [B&C]t1
                    //[A&B&C]t1 & At2 -> At1 & [B&C]t1
                    Collection<T> remainTargets = new ArrayList<>(otherTargets);
                    long time = Math.min(spec.getEnd(), os.getEnd());
                    RouteSpecification<T> s1 = new RouteSpecificationByTarget<>(target, time);
                    res.add(s1);
                    if (!remainTargets.isEmpty()){
                        RouteSpecification<T> s2 = RouteSpecificationByTargets.all(remainTargets, time);
                        res.add(s2);
                    }
                } else
                if (os.isAny()){
                    //At1 & [A|B|C]t2 -> At1
                    //[A|B|C]t1 & At2 -> At1
                    long time = Math.min(spec.getEnd(), os.getEnd());
                    RouteSpecification<T> newSpec = new RouteSpecificationByTarget<>(target, time);
                    res.add(newSpec);
                } else {
                    Collection<T> remainTargets = new ArrayList<>(otherTargets);
                    remainTargets.remove(target);
                    if (spec.getEnd() <= os.getEnd()) {
                        //At1 & [A,B,C]t2 -> At1
                        RouteSpecification<T> newSpec =  new RouteSpecificationByTarget<>(target, spec.getEnd());
                        res.add(newSpec);
                    } else {
                        //[A,B,C]t1 & At2 -> At1 | (A[t1-t2] & [B,C]t1)
                        RouteSpecification<T> s1 = new RouteSpecificationByTarget<>(target, os.getEnd());
                        if (!remainTargets.isEmpty()){
                            RouteSpecification<T> s2 = RouteSpecificationByTargets.containAny(remainTargets, spec.getEnd(), os.getEnd());
                            RouteSpecification<T> s3 = new RouteSpecificationByTarget<>(target, spec.getEnd());
                            RouteSpecification<T> newSpec = RouteSpecificationOrMixer.mix(s1, new RouteSpecificationAndMixer<>(s2, s3));
                            res.add(newSpec);
                        } else {
                            res.add(s1);
                        }
                    }
                }
            } else {
                //A & [B,C] -> A & [B,C]
                return Collections.emptyList();
            }
        } else
        if (other instanceof RouteSpecificationByPair){
            RouteSpecificationByPair<T> os = (RouteSpecificationByPair<T>)other;
            Collection<T> otherTargets = os.getTargets();
            T otherTarget = os.getTarget();
            if (Objects.equals(target, otherTarget)){
                //At1 & [B,C - A]t2 -> At1 & [B,C]t1
                //[A,B,C - D]t1 & Dt2 -> [A,B,C]t1 & Dt1
                long time = Math.min(spec.getEnd(), os.getEnd());
                RouteSpecification<T> s1 = new RouteSpecificationByTarget<>(target, time);
                RouteSpecification<T> s2 = RouteSpecificationByTargets.containAny(otherTargets, time);
                res.add(s1);
                res.add(s2);
            } else
            if (otherTargets.contains(target) && spec.getEnd() < os.getEnd()){
                //At1 & [A,B,C - D]t2 -> At1 & [A,B,C - D]t1
                RouteSpecification<T> s2 = new RouteSpecificationByPair<>(otherTargets, otherTarget, spec.getEnd());
                res.add(spec);
                res.add(s2);
            } else {
                //[A,B,C - D]t1 & At2 -> [A,B,C - D]t1 & At2
                //At1 & [B,C - D]t2 -> At1 & [B,C - D]t2
                return Collections.emptyList();
            }
        }
        return res;
    }

    private static  <E> void split(Collection<E> source, Collection<E> collection,  Collection<E> intersect, Collection<E> retain, Collection<E> retainCollection){
        Collection<E> c = new ArrayList<>(collection);
        for (E s : source) {
            if (c.remove(s)){
                intersect.add(s);
            } else {
                retain.add(s);
            }
        }
        retainCollection.addAll(c);
    }

    public static <T> Collection<RouteSpecification<T>> andMix(RouteSpecificationByTargets<T> spec, RouteSpecification<T> other) {
        Collection<RouteSpecification<T>> res = new ArrayList<>();
        Collection<T> targets = spec.getTargets();
        if (other instanceof RouteSpecificationByTarget){
            // already implement in other methods
            return andMix((RouteSpecificationByTarget<T>)other, spec);
        } else
        if (other instanceof RouteSpecificationByTargets){
            RouteSpecificationByTargets<T> os = ((RouteSpecificationByTargets<T>)other);
            if (os.getEnd() < spec.getEnd()){
                //swap
                return andMix(os, spec);
            }
            Collection<T> otherTargets = os.getTargets();
            if (spec.isAll()){
                if (os.isAll()){
                    Collection<T> retainOtherTargets = new ArrayList<>(otherTargets);
                    if (retainOtherTargets.removeAll(targets)){
                        //[A&B&C]t1 & [B&C&D]t2 -> [A&B&C]t1 & [&D]t2
                        RouteSpecification<T> s1 = RouteSpecificationByTargets.all(targets, spec.getEnd());
                        res.add(s1);
                        if (!retainOtherTargets.isEmpty()){
                            RouteSpecification<T> s2 = RouteSpecificationByTargets.all(retainOtherTargets, os.getEnd());
                            res.add(s2);
                        }
                    } else {
                        //[A&B&C]t1 & [D&E&F]t2 -> [A&B&C]t1 & [D&E&F]t2
                        return Collections.emptyList();
                    }
                } else
                if (os.isAny()){
                    //[A&B&C]t1 & [B|C|D]t2 -> [&A]t1 & (([&C]t1 & Bt1) | ([&B]t1 & Ct1) | ([&B&C]t1 & [B|C|D][t1-t2]))
                    Collection<T> intersectTargets = new ArrayList<>();
                    Collection<T> retainTargets = new ArrayList<>();
                    Collection<T> retainOtherTargets = new ArrayList<>();
                    split(targets, otherTargets, intersectTargets, retainTargets, retainOtherTargets);
                    if (!intersectTargets.isEmpty()){
                        if (!retainTargets.isEmpty()){
                            RouteSpecification<T> s1 = RouteSpecificationByTargets.all(retainTargets, spec.getEnd());
                            res.add(s1);
                        }
                        RouteSpecificationOrMixer<T> orSpec = new RouteSpecificationOrMixer<>();
                        for (T intersectTarget : intersectTargets) {
                            Collection<T> its = new ArrayList<>(intersectTargets);
                            its.remove(intersectTarget);
                            RouteSpecification<T> s3 = new RouteSpecificationByTarget<>(intersectTarget, spec.getEnd());
                            if (!its.isEmpty()){
                                RouteSpecification<T> s2 = RouteSpecificationByTargets.all(its, spec.getEnd());
                                orSpec.mix(new RouteSpecificationAndMixer<>(s2,s3));
                            } else {
                                orSpec.mix(s3);
                            }
                        }
                        if (!retainOtherTargets.isEmpty()){
                            RouteSpecification<T> s4 = RouteSpecificationByTargets.all(intersectTargets, spec.getEnd());
                            RouteSpecification<T> s5 = RouteSpecificationByTargets.any(otherTargets, spec.getEnd(), os.getEnd());
                            orSpec.mix(new RouteSpecificationAndMixer<>(s4,s5));
                        }
                        res.add(orSpec);
                    } else {
                        //[A&B&C]t1 & [D|E|F]t2 -> [A&B&C]t1 & [D|E|F]t2
                        return Collections.emptyList();
                    }
                } else {
                    if (otherTargets.stream().filter(targets::contains).findAny().isPresent()){
                        //[A&B&C]t1 & [B,C,D]t2 -> [A&B&C]t1
                        res.add(spec);
                    } else {
                        //[A&B&C]t1 & [D,E,F]t2 -> [A&B&C]t1 & [D,E,F]t2
                        return Collections.emptyList();
                    }
                }
            } else
            if (spec.isAny()){
                if (os.isAll()){
                    //[A|B|C]t1 & [B&C&D]t2
                    Collection<T> intersectTargets = new ArrayList<>();     //B,C
                    Collection<T> retainTargets = new ArrayList<>();        //A
                    Collection<T> retainOtherTargets = new ArrayList<>();   //D
                    split(targets, otherTargets, intersectTargets, retainTargets, retainOtherTargets);
                    if (!intersectTargets.isEmpty()){
                        //[A|B|C]t1 & [B&C&D]t2 -> [&D]t1 & ((Bt1 & [&C]t1) | (Ct1 & [&B]t1) | [|A]t1 & [B&C]t1))
                        if (!retainOtherTargets.isEmpty()){
                            RouteSpecification<T> s1 = RouteSpecificationByTargets.all(retainOtherTargets, spec.getEnd());
                            res.add(s1);
                        }
                        RouteSpecificationOrMixer<T> orSpec = new RouteSpecificationOrMixer<>();
                        for (T intersectTarget : intersectTargets) {
                            Collection<T> its = new ArrayList<>(intersectTargets);
                            its.remove(intersectTarget);
                            RouteSpecification<T> s2 = new RouteSpecificationByTarget<>(intersectTarget, spec.getEnd());
                            if (!its.isEmpty()){
                                RouteSpecification<T> s3 = RouteSpecificationByTargets.all(its, spec.getEnd());
                                orSpec.mix(new RouteSpecificationAndMixer<>(s2,s3));
                            } else {
                                orSpec.mix(s2);
                            }
                        }
                        if (!retainTargets.isEmpty()){
                            RouteSpecification<T> s4 = RouteSpecificationByTargets.any(retainTargets, spec.getEnd());
                            RouteSpecification<T> s5 = RouteSpecificationByTargets.all(intersectTargets, spec.getEnd());
                            orSpec.mix(new RouteSpecificationAndMixer<>(s4,s5));
                        }

                        res.add(orSpec);
                    } else {
                        //[A|B|C]t1 & [D&E&F]t2 -> [A|B|C]t1 & [D&E&F]t2
                        return Collections.emptyList();
                    }

                } else
                if (os.isAny()){
                    Collection<T> intersectTarget = new ArrayList<>(otherTargets);
                    intersectTarget.retainAll(targets);
                    if (!intersectTarget.isEmpty()){
                        //[A|B|C]t1 & [B|C|D]t2 -> [B|C]t1
                        RouteSpecification<T> newSpec = RouteSpecificationByTargets.any(intersectTarget, spec.getEnd());
                        res.add(newSpec);
                    } else {
                        //[A|B|C]t1 & [D|E|F]t2 -> empty
                        res.add(new NullRouteSpecification<>());
                    }
                } else {
                    Collection<T> intersectTargets = new ArrayList<>();     //B,C
                    Collection<T> retainTargets = new ArrayList<>();        //A
                    Collection<T> retainOtherTargets = new ArrayList<>();   //D
                    split(targets, otherTargets, intersectTargets, retainTargets, retainOtherTargets);
                    if (!intersectTargets.isEmpty()){
                        //[A|B|C]t1 & [B,C,D]t2 -> ([B|C]t1) | ([,D]t1 & [|A]t1)
                        RouteSpecification<T> s1 = RouteSpecificationByTargets.any(intersectTargets, spec.getEnd());
                        if (!retainTargets.isEmpty() && !retainOtherTargets.isEmpty()){
                            RouteSpecification<T> s2 = RouteSpecificationByTargets.containAny(retainOtherTargets, spec.getEnd());
                            RouteSpecification<T> s3 = RouteSpecificationByTargets.any(retainTargets, spec.getEnd());
                            RouteSpecificationOrMixer<T> orSpec = new RouteSpecificationOrMixer<>();
                            orSpec.mix(new RouteSpecificationAndMixer<>(s2,s3));
                            orSpec.mix(s1);
                            res.add(orSpec);
                        } else {
                            res.add(s1);
                        }
                    } else {
                        //[A|B|C]t1 & [D,E,F]t2 -> [A|B|C]t1 & [D,E,F]t2
                        return Collections.emptyList();
                    }
                }
            } else {
                if (os.isAll()){
                    //[A,B,C]t1 & [B&C&D]t2
                    Collection<T> intersectTargets = new ArrayList<>();     //B,C
                    Collection<T> retainTargets = new ArrayList<>();        //A
                    Collection<T> retainOtherTargets = new ArrayList<>();   //D
                    split(targets, otherTargets, intersectTargets, retainTargets, retainOtherTargets);
                    if (!intersectTargets.isEmpty()){
                        //[A,B,C]t1 & [B&C&D]t2 -> [&D]t2 & (([B]t1 & [&C]t2) | ([C]t1 & [&B]t2) | ([A]t1 & [B&C][t1-t2]))
                        if (!retainOtherTargets.isEmpty()){
                            RouteSpecification<T> s1 = RouteSpecificationByTargets.all(retainOtherTargets, os.getEnd());
                            res.add(s1);
                        }
                        RouteSpecificationOrMixer<T> orSpec = new RouteSpecificationOrMixer<>();
                        for (T intersectTarget : intersectTargets) {
                            Collection<T> its = new ArrayList<>(intersectTargets);
                            its.remove(intersectTarget);
                            RouteSpecification<T> s2 = RouteSpecificationByTargets.containAny(Collections.singleton(intersectTarget), spec.getEnd());
                            if (!its.isEmpty()){
                                RouteSpecification<T> s3 = RouteSpecificationByTargets.all(its, os.getEnd());
                                orSpec.mix(new RouteSpecificationAndMixer<>(s2,s3));
                            } else {
                                orSpec.mix(s2);
                            }
                        }
                        if (!retainTargets.isEmpty()){
                            RouteSpecification<T> s4 = RouteSpecificationByTargets.containAny(retainTargets, spec.getEnd());
                            RouteSpecification<T> s5 = RouteSpecificationByTargets.all(intersectTargets, spec.getEnd(), os.getEnd());
                            orSpec.mix(new RouteSpecificationAndMixer<>(s4,s5));
                        }

                        res.add(orSpec);
                    } else {
                        //[A,B,C]t1 & [B&C&D]t2 -> [A,B,C]t1 & [D&E&F]t2
                        return Collections.emptyList();
                    }

                } else
                if (os.isAny()){
                    //[A,B,C]t1 & [B|C|D]t2
                    Collection<T> intersectTargets = new ArrayList<>();     //B,C
                    Collection<T> retainTargets = new ArrayList<>();        //A
                    Collection<T> retainOtherTargets = new ArrayList<>();   //D
                    split(targets, otherTargets, intersectTargets, retainTargets, retainOtherTargets);
                    if (!intersectTargets.isEmpty()){
                        //[A,B,C]t1 & [B|C|D]t2 -> [B|C]t1 | ([A]t1 & ([B|C][t1-t2] | [|D]t2))
                        RouteSpecification<T> s1 = RouteSpecificationByTargets.any(intersectTargets, spec.getEnd());
                        if (!retainTargets.isEmpty()){
                            RouteSpecificationOrMixer<T> orSpec = new RouteSpecificationOrMixer<>();
                            orSpec.mix(s1);
                            RouteSpecification<T> s2 = RouteSpecificationByTargets.containAny(retainTargets, spec.getEnd());
                            RouteSpecification<T> s3 = RouteSpecificationByTargets.any(intersectTargets, spec.getEnd(), os.getEnd());
                            RouteSpecificationAndMixer<T> andSpec;
                            if (!retainOtherTargets.isEmpty()){
                                RouteSpecification<T> s4 = RouteSpecificationByTargets.any(retainOtherTargets, os.getEnd());
                                andSpec = new RouteSpecificationAndMixer<>(s2, RouteSpecificationOrMixer.mix(s3, s4));
                            } else {
                                andSpec = new RouteSpecificationAndMixer<>(s2, s3);
                            }
                            orSpec.mix(andSpec);
                            res.add(orSpec);
                        } else {
                            res.add(s1);
                        }
                    } else {
                        //[A,B,C]t1 & [D|E|F]t2 -> [A,B,C]t1 & [D|E|F]t2
                        return Collections.emptyList();
                    }
                } else {
                    //[A,B,C]t1 & [B,C,D]t2
                    Collection<T> intersectTargets = new ArrayList<>();     //B,C
                    Collection<T> retainTargets = new ArrayList<>();        //A
                    Collection<T> retainOtherTargets = new ArrayList<>();   //D
                    split(targets, otherTargets, intersectTargets, retainTargets, retainOtherTargets);
                    if (!intersectTargets.isEmpty()){
                        //[A,B,C]t1 & [B,C,D]t2 -> [B,C]t1 | ([A]t1 & [D]t2)
                        RouteSpecification<T> s1 = RouteSpecificationByTargets.containAny(intersectTargets, spec.getEnd());
                        if (!retainTargets.isEmpty() && !retainOtherTargets.isEmpty()){
                            RouteSpecification<T> s2 = RouteSpecificationByTargets.containAny(retainTargets, spec.getEnd());
                            RouteSpecification<T> s3 = RouteSpecificationByTargets.containAny(retainOtherTargets, os.getEnd());
                            RouteSpecificationOrMixer<T> orSpec = new RouteSpecificationOrMixer<>();
                            orSpec.mix(s1);
                            orSpec.mix(new RouteSpecificationAndMixer<>(s2,s3));
                            res.add(orSpec);
                        } else {
                            res.add(s1);
                        }
                    } else {
                        //[A,B,C]t1 & [D,E,F]t2 -> [A,B,C]t1 & [D,E,F]t2
                        return Collections.emptyList();
                    }
                }
            }
        } else
        if (other instanceof RouteSpecificationByPair){
            // already implement in other methods
            return andMix((RouteSpecificationByPair<T>)other, spec);
        }
        return res;
    }

    public static <T> Collection<RouteSpecification<T>> andMix(RouteSpecificationByPair<T> spec, RouteSpecification<T> other) {
        Collection<RouteSpecification<T>> res = new ArrayList<>();
        Collection<T> targets = spec.getTargets();
        T target = spec.getTarget();
        if (other instanceof RouteSpecificationByTarget){
            // already implement in other methods
            return andMix((RouteSpecificationByTarget<T>)other, spec);
        } else
        if (other instanceof RouteSpecificationByTargets){
            RouteSpecificationByTargets<T> os = ((RouteSpecificationByTargets<T>)other);
            Collection<T> otherTargets = os.getTargets();

            if (os.isAll()){
                //[A,B,C - D]t1 & [B&C&D&E]t2
                Collection<T> intersectTargets = new ArrayList<>();     //B,C
                Collection<T> retainTargets = new ArrayList<>();        //A
                Collection<T> retainOtherTargets = new ArrayList<>();   //D
                split(targets, otherTargets, intersectTargets, retainTargets, retainOtherTargets);
                if (!intersectTargets.isEmpty() || otherTargets.contains(target)){
                    if (spec.getEnd() <= os.getEnd()){
                        //[A,B,C - D]t1 & [B&C&D&E]t2 -> [&E]t2 & (([B - D]t1 & [&C]t2) | ([C - D]t1 & [&B]t2) | ([A - D]t1 & [B&C]t2))
                        //[A,B,C - F]t1 & [B&C&D&E]t2 -> [D&E]t2 & (([B - F]t1 & [&C]t2) | ([C - F]t1 & [&B]t2) | ([A - F]t1 & [B&C]t2)
                        retainOtherTargets.remove(target);
                        if (!retainOtherTargets.isEmpty()){
                            RouteSpecification<T> s1 = RouteSpecificationByTargets.all(retainOtherTargets, os.getEnd());
                            res.add(s1);
                        }
                        RouteSpecificationOrMixer<T> orSpec = new RouteSpecificationOrMixer<>();
                        for (T intersectTarget : intersectTargets) {
                            Collection<T> its = new ArrayList<>(intersectTargets);
                            its.remove(intersectTarget);
                            RouteSpecification<T> s2 = new RouteSpecificationByPair<>(Collections.singleton(intersectTarget), target, spec.getEnd());
                            if (!its.isEmpty()){
                                RouteSpecification<T> s3 = RouteSpecificationByTargets.all(its, os.getEnd());
                                orSpec.mix(new RouteSpecificationAndMixer<>(s2,s3));
                            } else {
                                orSpec.mix(s2);
                            }
                        }
                        if (!retainTargets.isEmpty()){
                            RouteSpecification<T> s4 = new RouteSpecificationByPair<>(retainTargets, target, spec.getEnd());
                            if (!intersectTargets.isEmpty()){
                                RouteSpecification<T> s5 = RouteSpecificationByTargets.all(intersectTargets, os.getEnd());
                                orSpec.mix(new RouteSpecificationAndMixer<>(s4,s5));
                            } else {
                                orSpec.mix(s4);
                            }
                        }
                        res.add(orSpec);
                    } else {
                        //[B,C,D,E - A]t2 & [A&B&C]t1  -> ([&B]t1 & [C - A]t1) | ([&C]t1 & [B - A]t1) | ([B&C]t1 & [D,E - A]t1) | ([A&B&C]t1 & [A][t1-t2])
                        //[B,C,D - E]t2 & [A&B&C]t1  -> [&A]t1 & (([&B]t1 & [C - E]t1) | ([&C]t1 & [B - E]t1) | ([B&C]t1 & [D - E]t1) | ([B&C]t1 & [E][t1-t2]))
                        retainOtherTargets.remove(target);
                        if (!retainOtherTargets.isEmpty()){
                            RouteSpecification<T> s1 = RouteSpecificationByTargets.all(retainOtherTargets, os.getEnd());
                            res.add(s1);
                        }
                        RouteSpecificationOrMixer<T> orSpec = new RouteSpecificationOrMixer<>();
                        for (T intersectTarget : intersectTargets) {
                            Collection<T> its = new ArrayList<>(intersectTargets);
                            its.remove(intersectTarget);
                            RouteSpecification<T> s2 = new RouteSpecificationByPair<>(Collections.singleton(intersectTarget), target, os.getEnd());
                            if (!its.isEmpty()){
                                RouteSpecification<T> s3 = RouteSpecificationByTargets.all(its, os.getEnd());
                                orSpec.mix(new RouteSpecificationAndMixer<>(s2,s3));
                            } else {
                                orSpec.mix(s2);
                            }
                        }
                        if (!retainTargets.isEmpty()){
                            RouteSpecification<T> s4 = new RouteSpecificationByPair<>(retainTargets, target, os.getEnd());
                            RouteSpecification<T> s5 = RouteSpecificationByTargets.all(intersectTargets, os.getEnd());
                            orSpec.mix(new RouteSpecificationAndMixer<>(s4,s5));
                        }
                        if (!intersectTargets.isEmpty()){
                            Collection<T> targets6 = new ArrayList<>(intersectTargets);
                            if (otherTargets.contains(target)) targets6.add(target);
                            RouteSpecification<T> s6 = RouteSpecificationByTargets.all(targets6, os.getEnd());
                            RouteSpecification<T> s7 = RouteSpecificationByTargets.containAny(Collections.singleton(target), os.getEnd(), spec.getEnd());
                            orSpec.mix(new RouteSpecificationAndMixer<>(s6,s7));
                        }
                        res.add(orSpec);
                    }
                } else {
                    //[A,B,C - D]t1 & [E&F]t2 -> [A,B,C - D]t1 & [E&F]t2
                    return Collections.emptyList();
                }
            } else
            if (os.isAny()){
                long time = Math.min(spec.getEnd(), os.getEnd());
                if (otherTargets.contains(target)){
                    //[A,B,C,F - D]t1 & [B|C|D|E]t2 -> ([A,B,C,F]t1 & Dt1) | ([A,B,C,F - D]t1 & [B|C|E]t2)
                    //[A,B,C,F - D]t2 & [B|C|D|E]t1  -> ([A,B,C,F]t1 & Dt1) | ([A,B,C,F - D]t1 & [B|C|E]t1)
                    RouteSpecification<T> s1 = RouteSpecificationByTargets.containAny(targets, time);
                    RouteSpecification<T> s2 = new RouteSpecificationByTarget<>(target, time);
                    Collection<T> retainOtherTargets = new ArrayList<>(otherTargets); // B,C,E
                    retainOtherTargets.remove(target);
                    if (!retainOtherTargets.isEmpty()){
                        RouteSpecificationOrMixer<T> orSpec = new RouteSpecificationOrMixer<>();
                        orSpec.mix(new RouteSpecificationAndMixer<>(s1,s2));
                        RouteSpecification<T> s3 = new RouteSpecificationByPair<>(targets, target, time);
                        RouteSpecification<T> s4 = RouteSpecificationByTargets.any(retainOtherTargets, os.getEnd());
                        orSpec.mix(new RouteSpecificationAndMixer<>(s3,s4));
                    } else {
                        res.add(s1);
                        res.add(s2);
                    }
                } else {
                    //[A,B,C,D - F]t1 & [B|C|E]t2 -> [A,B,C,D - F]t1 & [B|C|E]t2
                    //[A,B,C,D - F]t2 & [B|C|E]t1 -> [A,B,C,D - F]t1 & [B|C|E]t1
                    //[A,B,C - D] & [E|F] -> [A,B,C - D] & [E|F]
                    RouteSpecification<T> s1 = new RouteSpecificationByPair<>(targets, target, time);
                    res.add(s1);
                    res.add(os);
                }
            } else {
                if (otherTargets.contains(target)){
                    //[A,B,C - D]t1 & [B,C,D,E]t2 -> [A,B,C - D]t1
                    if (spec.getEnd() <= os.getEnd()){
                        res.add(spec);
                    } else {
                        Collection<T> intersectTargets = new ArrayList<>(targets);     //B,C
                        intersectTargets.retainAll(otherTargets);
                        if (!intersectTargets.isEmpty()){
                            //[B,C,D - A]t2 & [A,B,C]t1  -> [B,C,D - A]t1 | ([B,C]t1 & [A][t1-t2])
                            RouteSpecification<T> s1 = new RouteSpecificationByPair<>(targets, target, os.getEnd());
                            RouteSpecificationOrMixer<T> orSpec = new RouteSpecificationOrMixer<>();
                            orSpec.mix(s1);
                            RouteSpecification<T> s2 = RouteSpecificationByTargets.containAny(intersectTargets, os.getEnd());
                            RouteSpecification<T> s3 = RouteSpecificationByTargets.containAny(Collections.singleton(target), os.getEnd(), spec.getEnd());
                            orSpec.mix(new RouteSpecificationAndMixer<>(s2,s3));
                            res.add(orSpec);
                        } else {
                            //[B,C,D - A]t2 & [A,E,F]t1  -> [B,C,D - A]t1 | ([E,F]t1 & [B,C,D - A][t1-t2])
                            RouteSpecification<T> s1 = new RouteSpecificationByPair<>(targets, target, os.getEnd());
                            Collection<T> retainOtherTargets = new ArrayList<>(otherTargets);
                            retainOtherTargets.remove(target);
                            if (!retainOtherTargets.isEmpty()){
                                RouteSpecificationOrMixer<T> orSpec = new RouteSpecificationOrMixer<>();
                                orSpec.mix(s1);
                                RouteSpecification<T> s2 = RouteSpecificationByTargets.containAny(otherTargets, os.getEnd());
                                RouteSpecification<T> s3 = new RouteSpecificationByPair<>(targets, target, os.getEnd(), spec.getEnd());
                                orSpec.mix(new RouteSpecificationAndMixer<>(s2,s3));
                                res.add(orSpec);
                            } else {
                                res.add(s1);
                            }
                        }
                    }
                } else {
                    Collection<T> intersectTargets = new ArrayList<>();     //B,C
                    Collection<T> retainTargets = new ArrayList<>();        //A
                    Collection<T> retainOtherTargets = new ArrayList<>();   //D,E
                    split(targets, otherTargets, intersectTargets, retainTargets, retainOtherTargets);
                    if (!intersectTargets.isEmpty()){
                        //[A,B,C - F]t1 & [B,C,D,E]t2 -> [B,C - F]t1 | ([A - F]t1 & [D,E]t2)
                        //[B,C,D - E]t2 & [A,B,C]t1  -> [B,C - E]t1 | ([D - E]t2 & [,A]t1 )
                        long time = Math.min(spec.getEnd(), os.getEnd());
                        RouteSpecification<T> s1 = new RouteSpecificationByPair<>(intersectTargets, target, time);
                        if (!retainTargets.isEmpty() && !retainOtherTargets.isEmpty()){
                            RouteSpecification<T> s2 = new RouteSpecificationByPair<>(retainTargets, target, spec.getEnd());
                            RouteSpecification<T> s3 = RouteSpecificationByTargets.containAny(retainOtherTargets, os.getEnd());
                            RouteSpecificationOrMixer<T> orSpec = new RouteSpecificationOrMixer<>();
                            orSpec.mix(s1);
                            orSpec.mix(new RouteSpecificationAndMixer<>(s2,s3));
                            res.add(orSpec);
                        } else {
                            res.add(s1);
                        }
                    } else {
                        //[A,B,C - F]t1 & [G,H,I]t2 -> [A,B,C - F]t1 & [G,H,I]t2
                        return Collections.emptyList();
                    }
                }
            }
        } else
        if (other instanceof RouteSpecificationByPair){
            RouteSpecificationByPair<T> os = ((RouteSpecificationByPair<T>)other);
            Collection<T> otherTargets = os.getTargets();
            T otherTarget = os.getTarget();
            if (os.getEnd() < spec.getEnd()){
                return andMix(os, spec);
            }
            if (target.equals(otherTarget)){
                //[A,B,C - F]t1 & [B,C,D - F]t2 -> [B,C - F]t1 | ([A - F]t1 & [D - F]t2)
                Collection<T> intersectTargets = new ArrayList<>();     //B,C
                Collection<T> retainTargets = new ArrayList<>();        //A
                Collection<T> retainOtherTargets = new ArrayList<>();   //D
                split(targets, otherTargets, intersectTargets, retainTargets, retainOtherTargets);
                if (!intersectTargets.isEmpty()){
                    RouteSpecification<T> s1 = new RouteSpecificationByPair<>(intersectTargets, target, spec.getEnd());
                    if (!retainTargets.isEmpty() && !retainOtherTargets.isEmpty()){
                        RouteSpecification<T> s2 = new RouteSpecificationByPair<>(retainTargets, target, spec.getEnd());
                        RouteSpecification<T> s3 = new RouteSpecificationByPair<>(retainOtherTargets, target, os.getEnd());
                        RouteSpecificationOrMixer<T> orSpec = new RouteSpecificationOrMixer<>();
                        orSpec.mix(s1);
                        orSpec.mix(new RouteSpecificationAndMixer<>(s2,s3));
                        res.add(orSpec);
                    } else {
                        res.add(s1);
                    }
                } else {
                    //[A,B,C - F]t1 & [D,E - F]t2 -> [A,B,C - F]t1 & [D,E - F]t2
                    return Collections.emptyList();
                }
            } else {
                //[A,B,C - D]t1 & [B,C,D - E]t2 -> [B,C - D]t1 & [- E]) | ([A - D] & [B,C - E])
                //[A,B,C - E]t1 & [B,C,D - A]t2 -> ([B,C - D - E]) | ([A - D] & [B,C - E])
                //[A,B,C - D]t1 & [B,C,E - G]t2 -> ([B,C - D - E]) | ([A - D] & [B,C - E])
                return Collections.emptyList();
            }
        } else {
            return Collections.emptyList();
        }
        return res;
    }

    public static <T> Collection<RouteSpecification<T>> andMix(RouteSpecification<T> spec, RouteSpecification<T> other) {
        if (spec instanceof NullRouteSpecification || other instanceof NullRouteSpecification){
            RouteSpecification<T> res = new NullRouteSpecification<>();
            return Collections.singleton(res);
        }
        Collection<RouteSpecification<T>> res = Collections.emptyList();
        RouteSpecification<T> first = spec;
        RouteSpecification<T> second = other;
        RouteSpecification<T> adding = null;
        if (spec.getStart() != other.getStart()){
            if (spec.getStart() > other.getStart()){
                first = other;
                second = spec;
            }
            if (first.getEnd() > second.getStart()){
                adding = clone(first, first.getStart(), second.getStart());
                if (adding != null){
                    first = clone(first, second.getStart(), first.getEnd());
                }
            }
            else {
                return Collections.emptyList();
            }
        }
        if (first instanceof RouteSpecificationByTarget){
            res = andMix((RouteSpecificationByTarget<T>)first, second);
        }
        if (first instanceof RouteSpecificationByTargets){
            res = andMix((RouteSpecificationByTargets<T>)first, second);
        }
        if (first instanceof RouteSpecificationByPair){
            res = andMix((RouteSpecificationByPair<T>)first, second);
        }
        if (!res.isEmpty() && adding != null){
            res.add(new RouteSpecificationAndMixer<>(adding, second));
        }
        return res;
    }

    private static <T> RouteSpecification<T> clone(RouteSpecification<T> specification, long startTime, long endTime){
        if (specification instanceof RouteSpecificationByTarget){
            RouteSpecificationByTarget<T> spec = (RouteSpecificationByTarget<T>) specification;
            return new RouteSpecificationByTarget<>(spec.getTarget(), startTime, endTime);
        }
        if (specification instanceof RouteSpecificationByTargets){
            RouteSpecificationByTargets<T> spec = (RouteSpecificationByTargets<T>) specification;
            if (spec.isAll()) return RouteSpecificationByTargets.all(spec.getTargets(), startTime, endTime);
            if (spec.isAny()) return RouteSpecificationByTargets.any(spec.getTargets(), startTime, endTime);
            return RouteSpecificationByTargets.containAny(spec.getTargets(), startTime, endTime);
        }
        if (specification instanceof RouteSpecificationByPair){
            RouteSpecificationByPair<T> spec = (RouteSpecificationByPair<T>) specification;
            return new RouteSpecificationByPair<>(spec.getTargets(), spec.getTarget(), startTime, endTime);
        }
        return null;
    }
}