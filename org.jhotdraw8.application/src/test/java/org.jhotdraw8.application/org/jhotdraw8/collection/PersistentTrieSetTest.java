package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

class PersistentTrieSetTest extends AbstractPersistentSetTest {

    @Override
    protected PersistentSet<HashCollider> of() {
        return PersistentTrie5Set.of();
    }

    @Override
    protected PersistentSet<HashCollider> of(@NonNull HashCollider... keys) {
        return PersistentTrie5Set.of(keys);
    }

    @Override
    protected PersistentSet<HashCollider> copyOf(@NonNull Iterable<? extends HashCollider> set) {
        return PersistentTrie5Set.copyOf(set);
    }
}