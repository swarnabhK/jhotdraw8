/*
 * @(#)StronglyConnectedComponentsAlgo.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.algo;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.Enumerator;
import org.jhotdraw8.collection.IntArrayDeque;
import org.jhotdraw8.collection.IteratorEnumerator;
import org.jhotdraw8.graph.DirectedGraph;

import java.util.*;
import java.util.function.Function;

import static java.lang.Math.min;

/**
 * Computes the sets of strongly connected components in a directed graph.
 * <p>
 * References:
 * <dl>
 *     <dt>Stackoverflow. Non-recursive implementation "Tarjan's strongly connected components".</dt>
 *     <dd><a href="https://stackoverflow.com/questions/46511682/non-recursive-version-of-tarjans-algorithm">stackoverflow.com</a></dd>
 * </dl>
 */
public class StronglyConnectedComponentsAlgo {
    /**
     * Holds bookkeeping data for a node v from the graph.
     */
    private static class NodeData {

        /**
         * Low represents the smallest index of any node known to be reachable from v through v's DFS subtree,
         * including v itself.
         * <p>
         * Therefore, v must be left on the stack if v.low < v.index, whereas v must be removed as the root of a
         * strongly connected component if v.low == v.index.
         * <p>
         * The value v.low is computed during the depth-first search from v, as this finds the nodes that are reachable from v.
         */
        private int low;

    }

    public StronglyConnectedComponentsAlgo() {

    }

    /**
     * Returns all strongly connected components in the specified graph.
     *
     * @param graph the graph
     * @param <V>   the vertex data type
     * @param <A>   the arrow data type
     * @return set of strongly connected components (sets of vertices).
     */
    public @NonNull <V, A> List<List<V>> findStronglyConnectedComponents(
            final @NonNull DirectedGraph<V, A> graph) {
        return findStronglyConnectedComponents(graph.getVertices(), graph::getNextVertices);
    }

    /**
     * Returns all strongly connected components in the specified graph.
     *
     * @param <V>              the vertex data type
     * @param vertices         the vertices of the graph
     * @param nextNodeFunction returns the next nodes of a given node
     * @return set of strongly connected components (sets of vertices).
     */
    public @NonNull <V> List<List<V>> findStronglyConnectedComponents(
            final @NonNull Collection<? extends V> vertices, final @NonNull Function<V, Iterable<? extends V>> nextNodeFunction
    ) {

        final List<List<V>> sccs = new ArrayList<>();
        final Map<V, NodeData> nodeMap = new HashMap<>();

        int pre = 0;
        Deque<V> stack = new ArrayDeque<>();

        IntArrayDeque minStack = new IntArrayDeque();
        Deque<Enumerator<V>> enumeratorStack = new ArrayDeque<>();
        Enumerator<V> enumerator = new IteratorEnumerator<>(vertices.iterator());

        STRONGCONNECT:
        while (true) {
            if (enumerator.moveNext()) {
                V v = enumerator.current();
                NodeData vdata = nodeMap.get(v);
                if (vdata == null) {
                    vdata = new NodeData();
                    nodeMap.put(v, vdata);
                    vdata.low = pre++;
                    stack.push(v);
                    // Level down:
                    minStack.push(vdata.low);
                    enumeratorStack.push(enumerator);
                    enumerator = new IteratorEnumerator<>(nextNodeFunction.apply(v).iterator());
                } else {
                    if (!minStack.isEmpty()) {
                        minStack.push(min(vdata.low, minStack.pop()));
                    }
                }
            } else {
                // Level up:
                if (enumeratorStack.isEmpty()) {
                    break STRONGCONNECT;
                }

                enumerator = enumeratorStack.pop();
                V v = enumerator.current();
                int min = minStack.pop();
                NodeData vdata = nodeMap.get(v);
                if (min < vdata.low) {
                    vdata.low = min;
                } else {
                    List<V> component = new ArrayList<>();
                    V w;
                    do {
                        w = stack.pop();
                        component.add(w);
                        NodeData wdata = nodeMap.get(w);
                        wdata.low = vertices.size();
                    } while (!w.equals(v));
                    sccs.add(component);
                }

                if (!minStack.isEmpty()) {
                    minStack.push(min(vdata.low, minStack.pop()));
                }
            }
        }
        return sccs;
    }
}
