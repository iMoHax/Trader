package ru.trader.analysis;

import java.util.Collection;

public class RouteSpecificationMixer {
    public static <T> void andMix(RouteSpecificationByTargets<T> spec, RouteSpecification<T> other) {
        if (other instanceof RouteSpecificationByTarget){
            RouteSpecificationByTarget<T> os = (RouteSpecificationByTarget<T>)other;
            T otherTarget = os.getTarget();
            if (os.time < spec.time){
                spec.targets.remove(otherTarget);
            } else {
                if (spec.targetOnly && spec.targets.contains(otherTarget)){
                    os.remove();
                }
            }
        } else
        if (other instanceof RouteSpecificationByTargets){
            RouteSpecificationByTargets<T> os = ((RouteSpecificationByTargets<T>)other);
            if (os.all){
                Collection<T> otherTargets = os.targets;
                if (os.time < spec.time){
                    spec.targets.removeAll(otherTargets);
                } else {
                    if (spec.all){
                        os.targets.removeAll(spec.targets);
                    }
                }
            }
        } else
        if (other instanceof RouteSpecificationByPair){
            RouteSpecificationByPair<T> os = (RouteSpecificationByPair<T>)other;
            T otherTarget = os.second;
            if (os.time < spec.time){
                spec.targets.remove(otherTarget);
            } else {
                if (spec.targetOnly && spec.targets.contains(otherTarget)){
                    os.remove();
                }
            }
        }
    }

    public static <T> void andMix(RouteSpecificationByTarget<T> spec, RouteSpecification<T> other) {
        if (other instanceof RouteSpecificationByTarget){
            RouteSpecificationByTarget<T> os = (RouteSpecificationByTarget<T>)other;
            T otherTarget = os.getTarget();
            if (spec.getTarget() == otherTarget){
                if (os.time < spec.time){
                    spec.remove();
                } else {
                    os.remove();
                }
            }
        } else
        if (other instanceof RouteSpecificationByTargets){
            RouteSpecificationByTargets<T> os = ((RouteSpecificationByTargets<T>)other);
            if (os.all){
                Collection<T> otherTargets = os.targets;
                if (otherTargets.contains(spec.getTarget())){
                    if (os.time < spec.time){
                        spec.remove();
                    } else {
                        os.targets.remove(spec.getTarget());
                    }
                }
            }
        } else
        if (other instanceof RouteSpecificationByPair){
            RouteSpecificationByPair<T> os = (RouteSpecificationByPair<T>)other;
            T otherTarget = os.second;
            if (spec.getTarget() == otherTarget){
                if (os.time < spec.time){
                    spec.remove();
                } else {
                    os.remove();
                }
            }
        }
    }

    public static <T> void andMix(RouteSpecificationByPair<T> spec, RouteSpecification<T> other) {
        if (other instanceof RouteSpecificationByTarget){
            RouteSpecificationByTarget<T> os = (RouteSpecificationByTarget<T>)other;
            T otherTarget = os.getTarget();
            if (spec.second == otherTarget){
                if (os.time >= spec.time) {
                    os.remove();
                }
            }
        } else
        if (other instanceof RouteSpecificationByTargets){
            RouteSpecificationByTargets<T> os = ((RouteSpecificationByTargets<T>)other);
            if (os.targetOnly){
                Collection<T> otherTargets = os.targets;
                if (otherTargets.contains(spec.second)){
                    if (os.time >= spec.time) {
                        os.targets.remove(spec.second);
                    }
                }
            }
        } else
        if (other instanceof RouteSpecificationByPair){
            RouteSpecificationByPair<T> os = (RouteSpecificationByPair<T>)other;
            boolean eqFirst = spec.first.containsAll(os.first);
            T otherTarget = os.second;
            if (eqFirst && spec.second == otherTarget){
                if (os.time < spec.time){
                    spec.remove();
                } else {
                    os.remove();
                }
            }
        }
    }

    public static <T> void andMix(RouteSpecification<T> spec, RouteSpecification<T> other) {
        if (spec instanceof RouteSpecificationByTarget){
            andMix((RouteSpecificationByTarget<T>)spec, other);
        } else
        if (spec instanceof RouteSpecificationByTargets){
            andMix((RouteSpecificationByTargets<T>)spec, other);
        } else
        if (spec instanceof RouteSpecificationByPair){
            andMix((RouteSpecificationByPair<T>)spec, other);
        }

    }


}