/*
 * @(#)UniquePathBuilder.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.graph.Arc;
import org.jhotdraw8.graph.path.backlink.ArcBackLink;
import org.jhotdraw8.util.TriFunction;

import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Searches a globally unique vertex path from a set of start vertices to a
 * set of goal vertices using a breadth-first search algorithm.
 * <p>
 * Uniqueness is global up to (inclusive) the specified maximal cost.
 *
 * @param <V> the vertex data type
 * @param <C> the cost number type
 */
public class GloballyUniqueArcPathSearchAlgo<V, A, C extends Number & Comparable<C>> implements ArcPathSearchAlgo<V, A, C> {


    @Override
    public @Nullable ArcBackLink<V, A, C> search(
            @NonNull Iterable<V> startVertices,
            @NonNull Predicate<V> goalPredicate,
            @NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction,
            @NonNull C zero, @NonNull C positiveInfinity,
            @NonNull C maxCost,
            @NonNull TriFunction<V, V, A, C> costFunction,
            @NonNull BiFunction<C, C, C> sumFunction) {

        Queue<ArcBackLink<V, A, C>> queue = new ArrayDeque<>(16);
        Map<V, Integer> visitedCount = new LinkedHashMap<>(16);
        for (V start : startVertices) {
            ArcBackLink<V, A, C> rootBackLink = new ArcBackLink<>(start, null, null, zero);
            if (visitedCount.put(start, 1) == null) {
                queue.add(rootBackLink);
            }
        }

        ArcBackLink<V, A, C> found = null;
        while (!queue.isEmpty()) {
            ArcBackLink<V, A, C> node = queue.remove();
            if (goalPredicate.test(node.getVertex())) {
                if (found != null) {
                    return null;// path is not unique!
                }
                found = node;
            }
            for (Arc<V, A> next : nextArcsFunction.apply(node.getVertex())) {
                if (visitedCount.merge(next.getEnd(), 1, Integer::sum) == 1) {
                    if (node.getCost().compareTo(maxCost) <= 0) {
                        C cost = sumFunction.apply(node.getCost(), costFunction.apply(node.getVertex(), next.getEnd(), next.getData()));
                        ArcBackLink<V, A, C> backLink = new ArcBackLink<V, A, C>(next.getEnd(), next.getData(), node, cost);
                        queue.add(backLink);
                    }
                }
            }
        }

        // Check if any of the preceding nodes has a non-unique path
        for (ArcBackLink<V, A, C> node = found; node != null; node = node.getParent()) {
            if (visitedCount.get(node.getVertex()) > 1) {
                return null;// path is not unique!
            }
        }
        return found;
    }
}

