package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.graph.Arc;
import org.jhotdraw8.graph.path.backlink.ArcBackLink;
import org.jhotdraw8.util.TriFunction;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Searches an arbitrary shortest path from a set of start vertices to a set of goal
 * vertices using Dijkstra's algorithm.
 * <p>
 * The provided cost function must return values {@literal >= 0} for all arrows.
 * <p>
 * References:
 * <dl>
 * <dt>Esger W. Dijkstra (1959), A note on two problems in connexion with graphs,
 * Problem 2.
 * </dt>
 * <dd><a href="https://www-m3.ma.tum.de/twiki/pub/MN0506/WebHome/dijkstra.pdf">tum.de</a></dd>
 * </dl>
 * Performance characteristics:
 * <dl>
 *     <dt>When a path can be found</dt><dd>less or equal {@literal O( |A| + |V|*log|V| )} within max cost</dd>
 *     <dt>When no path can be found</dt><dd>exactly {@literal O( |A| + |V|*log|V| )} within max cost</dd>
 * </dl>
 *
 * @param <V> the vertex data type
 * @param <A> the arrow data type
 * @param <C> the cost number type
 */
public class ShortestArbitraryArcPathSearchAlgo<V, A, C extends Number & Comparable<C>> implements ArcPathSearchAlgo<V, A, C> {


    /**
     * {@inheritDoc}
     *
     * @param startVertices    the set of start vertices
     * @param goalPredicate    the goal predicate
     * @param nextArcsFunction the next arcs function
     * @param zero             the zero cost value
     * @param positiveInfinity the positive infinity value
     * @param searchLimit      the maximal cost (inclusive) of a path
     *                         Set this value as small as you can, to prevent
     *                         long search times if the goal can not be reached.
     * @param costFunction     the cost function
     * @param sumFunction      the sum function for adding two cost values
     * @return
     */
    @Override
    public @Nullable ArcBackLink<V, A, C> search(
            final @NonNull Iterable<V> startVertices,
            final @NonNull Predicate<V> goalPredicate,
            final @NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction,
            final @NonNull C zero,
            final @NonNull C positiveInfinity,
            final @NonNull C searchLimit,
            final @NonNull TriFunction<V, V, A, C> costFunction,
            final @NonNull BiFunction<C, C, C> sumFunction) {

        final C maxCost = searchLimit;

        // Priority queue: back-links by lower cost and shallower depth.
        //          Ordering by depth prevents that the algorithm
        //          unnecessarily follows zero-length arrows.
        PriorityQueue<ArcBackLink<V, A, C>> queue = new PriorityQueue<ArcBackLink<V, A, C>>(
                Comparator.<ArcBackLink<V, A, C>, C>comparing(ArcBackLink::getCost).thenComparing(ArcBackLink::getDepth)
        );

        // Map with best known costs from start to a specific vertex.
        // If an entry is missing, we assume infinity.
        Map<V, C> costMap = new HashMap<>();

        // Insert start itself in priority queue and initialize its cost to 0.
        for (V start : startVertices) {
            queue.add(new ArcBackLink<>(start, null, null, zero));
            costMap.put(start, zero);
        }

        // Loop until we have reached the goal, or queue is exhausted.
        while (!queue.isEmpty()) {
            ArcBackLink<V, A, C> node = queue.remove();
            final V u = node.getVertex();
            if (goalPredicate.test(u)) {
                return node;
            }

            for (Arc<V, A> arc : nextArcsFunction.apply(u)) {
                V v = arc.getEnd();
                C bestKnownCost = costMap.getOrDefault(v, positiveInfinity);
                C cost = sumFunction.apply(node.getCost(), costFunction.apply(u, v, arc.getData()));

                // If there is a cheaper path to v through u.
                if (cost.compareTo(bestKnownCost) < 0 && cost.compareTo(maxCost) <= 0) {
                    // Update cost to v and add v again to the queue.
                    costMap.put(v, cost);
                    queue.add(new ArcBackLink<>(v, arc.getData(), node, cost));
                }
            }
        }

        return null;
    }
}