package org.jhotdraw8.graph;

class DirectedGraphBuilderTest extends AbstractMutableDirectedGraphTest {
    @Override
    protected MutableDirectedGraph<Integer, Character> newInstance() {
        return new DirectedGraphBuilder<>(0, 0);
    }

    @Override
    protected MutableDirectedGraph<Integer, Character> newInstance(DirectedGraph<Integer, Character> g) {
        return new DirectedGraphBuilder<>(g);
    }
}