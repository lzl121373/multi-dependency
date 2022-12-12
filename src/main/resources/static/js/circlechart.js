(function() {
    var method;
    var noop = function () {};
    var methods = [
        'assert', 'clear', 'count', 'debug', 'dir', 'dirxml', 'error',
        'exception', 'group', 'groupCollapsed', 'groupEnd', 'info', 'log',
        'markTimeline', 'profile', 'profileEnd', 'table', 'time', 'timeEnd',
        'timeline', 'timelineEnd', 'timeStamp', 'trace', 'warn'
    ];
    var length = methods.length;
    var console = (window.console = window.console || {});

    while (length--) {
        method = methods[length];

        // Only stub undefined methods.
        if (!console[method]) {
            console[method] = noop;
        }
    }
}());

//设置数组
var clonedata = [
    {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/Interners.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/Interners.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/CompoundOrdering.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/CompoundOrdering.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/DescendingImmutableSortedSet.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/DescendingImmutableSortedSet.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/ForwardingListMultimap.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/ForwardingListMultimap.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/AbstractMultiset.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/AbstractMultiset.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/CompactHashSet.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/CompactHashSet.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/DescendingImmutableSortedMultiset.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/DescendingImmutableSortedMultiset.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/EnumMultiset.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/EnumMultiset.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/AbstractSetMultimap.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/AbstractSetMultimap.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/AllEqualOrdering.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/AllEqualOrdering.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/ForwardingMap.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/ForwardingMap.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/AbstractMapBasedMultimap.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/AbstractMapBasedMultimap.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/TreeRangeSet.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/TreeRangeSet.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/ForwardingSortedMultiset.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/ForwardingSortedMultiset.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/NaturalOrdering.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/NaturalOrdering.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/SingletonImmutableSet.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/SingletonImmutableSet.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/AbstractSortedSetMultimap.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/AbstractSortedSetMultimap.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/DescendingMultiset.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/DescendingMultiset.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/CompactLinkedHashMap.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/CompactLinkedHashMap.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/ForwardingMultimap.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/ForwardingMultimap.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/RegularImmutableSortedMultiset.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/RegularImmutableSortedMultiset.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/Table.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/Table.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/Multisets.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/Multisets.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/ForwardingList.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/ForwardingList.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/SortedSetMultimap.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/SortedSetMultimap.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/ImmutableAsList.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/ImmutableAsList.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/ImmutableSortedSet.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/ImmutableSortedSet.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/ComparatorOrdering.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/ComparatorOrdering.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/TreeBasedTable.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/TreeBasedTable.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/ImmutableMultimap.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/ImmutableMultimap.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/Count.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/Count.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/ForwardingListIterator.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/ForwardingListIterator.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/UsingToStringOrdering.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/UsingToStringOrdering.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/RegularImmutableTable.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/RegularImmutableTable.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/SortedMapDifference.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/SortedMapDifference.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/MapDifference.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/MapDifference.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/AbstractMultimap.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/AbstractMultimap.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/ImmutableListMultimap.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/ImmutableListMultimap.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/ForwardingMapEntry.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/ForwardingMapEntry.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/AbstractSortedKeySortedSetMultimap.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/AbstractSortedKeySortedSetMultimap.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/Multimap.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/Multimap.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/AbstractIndexedListIterator.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/AbstractIndexedListIterator.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/EnumHashBiMap.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/EnumHashBiMap.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/ForwardingSortedSetMultimap.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/ForwardingSortedSetMultimap.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/FilteredKeySetMultimap.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/FilteredKeySetMultimap.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/EmptyContiguousSet.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/EmptyContiguousSet.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/ForwardingSortedSet.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/ForwardingSortedSet.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/TreeMultiset.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/TreeMultiset.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/FilteredKeyMultimap.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/FilteredKeyMultimap.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/Hashing.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/Hashing.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/DiscreteDomain.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/DiscreteDomain.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/AbstractSortedMultiset.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/AbstractSortedMultiset.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/FilteredEntrySetMultimap.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/FilteredEntrySetMultimap.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/ForwardingNavigableSet.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/ForwardingNavigableSet.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/ForwardingSetMultimap.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/ForwardingSetMultimap.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/Comparators.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/Comparators.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/ReverseOrdering.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/ReverseOrdering.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/Iterables.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/Iterables.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/TreeMultimap.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/TreeMultimap.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/BiMap.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/BiMap.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/Serialization.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/Serialization.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/ImmutableSortedSetFauxverideShim.java", "/google__fdse__guava/android/guava/src/com/google/common/collect/ImmutableSortedMultisetFauxverideShim.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/ImmutableSortedSetFauxverideShim.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/ForwardingBlockingDeque.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/ForwardingBlockingDeque.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/AbstractListMultimap.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/AbstractListMultimap.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/CompactHashing.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/CompactHashing.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/ComparisonChain.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/ComparisonChain.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/TopKSelector.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/TopKSelector.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/ByFunctionOrdering.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/ByFunctionOrdering.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/ArrayListMultimap.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/ArrayListMultimap.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/BoundType.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/BoundType.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/ForwardingMultiset.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/ForwardingMultiset.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/NullsFirstOrdering.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/NullsFirstOrdering.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/Collections2.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/Collections2.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/ExplicitOrdering.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/ExplicitOrdering.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/LinkedHashMultimap.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/LinkedHashMultimap.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/HashBasedTable.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/HashBasedTable.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/ImmutableSortedSetFauxverideShim.java", "/google__fdse__guava/android/guava/src/com/google/common/collect/ImmutableSortedMultisetFauxverideShim.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/ImmutableSortedMultisetFauxverideShim.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/Iterators.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/Iterators.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/ObjectArrays.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/ObjectArrays.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/SortedIterables.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/SortedIterables.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/ConsumingQueueIterator.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/ConsumingQueueIterator.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/AbstractBiMap.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/AbstractBiMap.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/ListMultimap.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/ListMultimap.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/UnmodifiableSortedMultiset.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/UnmodifiableSortedMultiset.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/ConcurrentHashMultiset.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/ConcurrentHashMultiset.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/EmptyImmutableSetMultimap.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/EmptyImmutableSetMultimap.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/EvictingQueue.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/EvictingQueue.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/ImmutableSetMultimap.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/ImmutableSetMultimap.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/FilteredKeyListMultimap.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/FilteredKeyListMultimap.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/ForwardingConcurrentMap.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/ForwardingConcurrentMap.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/MapMakerInternalMap.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/MapMakerInternalMap.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/ForwardingIterator.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/ForwardingIterator.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/EnumBiMap.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/EnumBiMap.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/ForwardingSet.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/ForwardingSet.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/SingletonImmutableTable.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/SingletonImmutableTable.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/FilteredMultimapValues.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/FilteredMultimapValues.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/ContiguousSet.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/ContiguousSet.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/ForwardingSortedMap.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/ForwardingSortedMap.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/SetMultimap.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/SetMultimap.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/DenseImmutableTable.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/DenseImmutableTable.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/CartesianList.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/CartesianList.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/SortedMultiset.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/SortedMultiset.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/ImmutableList.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/ImmutableList.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/AbstractMapEntry.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/AbstractMapEntry.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/MinMaxPriorityQueue.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/MinMaxPriorityQueue.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/TransformedListIterator.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/TransformedListIterator.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/Sets.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/Sets.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/NullsLastOrdering.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/NullsLastOrdering.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/CollectPreconditions.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/CollectPreconditions.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/ImmutableClassToInstanceMap.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/ImmutableClassToInstanceMap.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/SparseImmutableTable.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/SparseImmutableTable.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/MapMaker.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/MapMaker.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/AbstractIterator.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/AbstractIterator.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/TransformedIterator.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/TransformedIterator.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/EmptyImmutableListMultimap.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/EmptyImmutableListMultimap.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/MutableClassToInstanceMap.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/MutableClassToInstanceMap.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/CompactLinkedHashSet.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/CompactLinkedHashSet.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/ForwardingTable.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/ForwardingTable.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/Cut.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/Cut.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/GeneralRange.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/GeneralRange.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/CompactHashMap.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/CompactHashMap.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/ReverseNaturalOrdering.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/ReverseNaturalOrdering.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/LexicographicalOrdering.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/LexicographicalOrdering.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/ForwardingQueue.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/ForwardingQueue.java"
}, {
    "imports": ["/google__fdse__guava/android/guava/src/com/google/common/collect/HashMultimap.java"],
    "name": "/google__fdse__guava/guava/src/com/google/common/collect/HashMultimap.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/BoundType.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/ForwardingBlockingDeque.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/SetMultimap.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/ComparisonChain.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/SingletonImmutableTable.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/AbstractMultiset.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/ForwardingSortedMap.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/ForwardingSortedMultiset.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/SortedSetMultimap.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/SortedIterables.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/ImmutableMultimap.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/ConcurrentHashMultiset.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/Count.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/MapMaker.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/EvictingQueue.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/CartesianList.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/CollectPreconditions.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/DescendingMultiset.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/TopKSelector.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/TreeBasedTable.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/ImmutableList.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/CompactLinkedHashSet.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/MapDifference.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/ForwardingListMultimap.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/AbstractBiMap.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/TreeMultimap.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/ByFunctionOrdering.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/HashBasedTable.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/ForwardingConcurrentMap.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/ForwardingMultiset.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/LinkedHashMultimap.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/ForwardingSortedSet.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/Multisets.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/AbstractMapBasedMultimap.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/ArrayListMultimap.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/AbstractSortedSetMultimap.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/TreeMultiset.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/FilteredKeySetMultimap.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/ImmutableSetMultimap.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/Interners.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/ImmutableSortedMultisetFauxverideShim.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/FilteredKeyListMultimap.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/LexicographicalOrdering.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/AbstractSetMultimap.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/MapMakerInternalMap.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/AbstractSortedKeySortedSetMultimap.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/ForwardingSetMultimap.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/ListMultimap.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/SingletonImmutableSet.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/CompactLinkedHashMap.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/RegularImmutableTable.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/NullsFirstOrdering.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/Cut.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/ForwardingMapEntry.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/Sets.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/ConsumingQueueIterator.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/Iterables.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/ReverseOrdering.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/CompactHashMap.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/SortedMapDifference.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/FilteredMultimapValues.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/GeneralRange.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/DescendingImmutableSortedSet.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/Collections2.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/CompactHashSet.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/DescendingImmutableSortedMultiset.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/ForwardingListIterator.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/AbstractMultimap.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/EmptyContiguousSet.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/NaturalOrdering.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/ImmutableListMultimap.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/Table.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/EnumHashBiMap.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/BiMap.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/DenseImmutableTable.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/ContiguousSet.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/ExplicitOrdering.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/AbstractListMultimap.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/SparseImmutableTable.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/Serialization.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/ForwardingSortedSetMultimap.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/ReverseNaturalOrdering.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/SortedMultiset.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/EmptyImmutableListMultimap.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/Comparators.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/CompoundOrdering.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/ObjectArrays.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/Hashing.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/AbstractIterator.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/AllEqualOrdering.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/HashMultimap.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/ForwardingList.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/ForwardingTable.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/ForwardingMap.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/AbstractSortedMultiset.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/CompactHashing.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/AbstractMapEntry.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/EmptyImmutableSetMultimap.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/RegularImmutableSortedMultiset.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/ImmutableClassToInstanceMap.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/TransformedListIterator.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/ForwardingMultimap.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/TreeRangeSet.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/ComparatorOrdering.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/MutableClassToInstanceMap.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/ImmutableSortedSet.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/ForwardingQueue.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/AbstractIndexedListIterator.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/MinMaxPriorityQueue.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/TransformedIterator.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/FilteredKeyMultimap.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/ForwardingNavigableSet.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/EnumBiMap.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/NullsLastOrdering.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/Iterators.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/ForwardingIterator.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/EnumMultiset.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/ImmutableSortedSetFauxverideShim.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/DiscreteDomain.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/Multimap.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/FilteredEntrySetMultimap.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/ForwardingSet.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/ImmutableAsList.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/UnmodifiableSortedMultiset.java"
}, {
    "imports": [],
    "name": "/google__fdse__guava/android/guava/src/com/google/common/collect/UsingToStringOrdering.java"
}]

var diameter = 1800,
    radius = diameter / 2,
    innerRadius = radius - 300;

var cluster = d3.layout.cluster()
    .size([360, innerRadius])
    .sort(null)
    .value(function(d) { return d.size; });

var bundle = d3.layout.bundle();

var line = d3.svg.line.radial()
    .interpolate("bundle")
    .tension(.85)
    .radius(function(d) { return d.y; })
    .angle(function(d) { return d.x / 180 * Math.PI; });


var svg = d3.select("#groupCytoscape2").append("svg")
    .attr("width", diameter)
    .attr("height", diameter)
    .attr("id", "svg1")
    .append("g")
    .attr("transform", "translate(" + radius + "," + radius + ")");


var link = svg.append("g").selectAll(".link"),
    node = svg.append("g").selectAll(".node");

//设置数组读取数据
var nodes = cluster.nodes(packageHierarchy(clonedata)),
    links = packageImports(nodes);
// var nodes = cluster.nodes(packageClone(classes)),
//     links = packageCloneImports(nodes);

console.log(nodes)

link = link
    .data(bundle(links))
    .enter().append("path")
    .each(function(d) { d.source = d[0], d.target = d[d.length - 1]; })
    .attr("class", "link")
    .attr("d", line);

node = node
    .data(nodes.filter(function(n) { return !n.children; }))
    .enter().append("text")
    // .style("fill", function (d) { if (checkChangeType(d.key, changes)== 3) { return '#b47500';}
    //                               if (checkChangeType(d.key, changes)== 4) { return '#00b40a';}})
    .attr("class", "node")
    .attr("dy", ".31em")
    .attr("transform", function(d) { return "rotate(" + (d.x - 90) + ")translate(" + (d.y + 8) + ",0)" + (d.x < 180 ? "" : "rotate(180)"); })
    .style("text-anchor", function(d) { return d.x < 180 ? "start" : "end"; })
    .text(function(d) { return d.key; })
    .on("mouseover", mouseovered)
    .on("mouseout", mouseouted)
    .call(text => text.append("title").text(function(d) { return d.key; }));
// .call(text => text.append("title").text(d => `${node.data.name}
// ${d.outgoing.length} outgoing
// ${d.incoming.length} incoming`));


/*
*从json中读取数组
 */
// d3.json("../data/link.json", function(error, classes) {
// d3.json("../static/data/link.json", function(error,  classes) {
// d3.json("../static/data/2.json", function(error, classes) {
// d3.json("../static/data/flare.json", function(error, classes) {
// d3.json("../static/data/testpackages.json", function(error, classes) {
//     if (error) throw error;
//
//     var nodes = cluster.nodes(packageHierarchy(classes)),
//         links = packageImports(nodes);
//     // var nodes = cluster.nodes(packageClone(classes)),
//     //     links = packageCloneImports(nodes);
//
//     console.log(nodes)
//
//     link = link
//         .data(bundle(links))
//         .enter().append("path")
//         .each(function(d) { d.source = d[0], d.target = d[d.length - 1]; })
//         .attr("class", "link")
//         .attr("d", line);
//
//     node = node
//         .data(nodes.filter(function(n) { return !n.children; }))
//         .enter().append("text")
//         // .style("fill", function (d) { if (checkChangeType(d.key, changes)== 3) { return '#b47500';}
//         //                               if (checkChangeType(d.key, changes)== 4) { return '#00b40a';}})
//         .attr("class", "node")
//         .attr("dy", ".31em")
//         .attr("transform", function(d) { return "rotate(" + (d.x - 90) + ")translate(" + (d.y + 8) + ",0)" + (d.x < 180 ? "" : "rotate(180)"); })
//         .style("text-anchor", function(d) { return d.x < 180 ? "start" : "end"; })
//         .text(function(d) { return d.key; })
//         .on("mouseover", mouseovered)
//         .on("mouseout", mouseouted)
//         .call(text => text.append("title").text(function(d) { return d.key; }));
//         // .call(text => text.append("title").text(d => `${node.data.name}
//         // ${d.outgoing.length} outgoing
//         // ${d.incoming.length} incoming`));
// });

String.prototype.replaceAt=function(index, replacement) {
    return this.substr(0, index) + replacement+ this.substr(index + replacement.length);
}

String.prototype.replaceAll = function(search, replacement) {
    var target = this;
    return target.replace(new RegExp(search, 'g'), replacement);
};


var width = 360;
var height = 360;
var radius = Math.min(width, height) / 2;
var donutWidth = 75;
var legendRectSize = 18;                                  // NEW
var legendSpacing = 4;

var legend = d3.select('svg')
    .append("g")
    .selectAll("g")
    // .data(color.domain())
    //.enter()
    .append('g')
    .attr('class', 'legend')
    .attr('transform', function(d, i) {
        var height = legendRectSize;
        var x = 0;
        var y = (i+1) * height;
        return 'translate(' + x + ',' + y + ')';
    });

d3.select('svg')
    .select("g:nth-child(0)").append('text').text("Component Colors:");
//.attr('transform', 'translate(0,0)');


legend.append('rect')
    .attr('width', legendRectSize)
    .attr('height', legendRectSize)
// .style('fill', color)
// .style('stroke', color);

legend.append('text')
    .attr('x', legendRectSize + legendSpacing)
    .attr('y', legendRectSize - legendSpacing)
    .text(function(d) { return d; });

function mouseovered(d) {
    node
        .each(function(n) { n.target = n.source = false; });

    link
        .classed("link--target", function(l) { if (l.target === d) return l.source.source = true; })
        .classed("link--source", function(l) { if (l.source === d) return l.target.target = true; })
        .filter(function(l) { return l.target === d || l.source === d; })
        // .style("stroke", function (l) { if (checkOldLink(l, old_links)) { return '#b400ad';}})
        .style("stroke", "#e0230a")
        .each(function() { this.parentNode.appendChild(this); });

    node
        .classed("node--target", function(n) { return n.target; })
        .classed("node--source", function(n) { return n.source; });

}

function mouseouted(d) {
    link
        .classed("link--target", false)
        .classed("link--source", false)
        .style("stroke", 'DarkGray');

    node
        .classed("node--target", false)
        .classed("node--source", false);

}

d3.select(self.frameElement).style("height", diameter + "px");

// Lazily construct the package hierarchy from class names.
function packageHierarchy(classes) {
    var map = {};

    function find(name, data) {
        var node = map[name], i;
        if (!node) {
            node = map[name] = data || {name: name, children: []};
            console.log(node)
            if (name.length) {
                node.parent = find(name.substring(0, i = name.lastIndexOf("/")));
                node.parent.children.push(node);
                node.key = name.substring(i + 1);
            }
        }
        return node;
    }

    // classes.result.forEach(function(d) {
    classes.forEach(function(d) {
        console.log(d)
        find(d.name, d);
    });

    return map[""];
}

// Return a list of imports for the given array of nodes.
function packageImports(nodes) {
    var map = {},
        imports = [];

    // Compute a map from name to node.
    nodes.forEach(function(d) {
        map[d.name] = d;
    });

    // For each import, construct a link from the source to target node.
    nodes.forEach(function(d) {
        if (d.imports) d.imports.forEach(function(i) {
            imports.push({source: map[d.name], target: map[i]});
        });
    });

    return imports;
}

//仿写packageHierarchy函数，用于处理clone关系json
function packageClone(classes) {
    var map = {};

    function find(name, data) {
        var node = map[name], i;
        if (!node) {
            node = map[name] = data || {data: {source: name}, children: [], parent: []};
            // console.log(node)
            if (name.length) {
                node.parent = find(name.substring(0, i = name.lastIndexOf(".")));
                node.parent.children.push(node);
                node.key = name.substring(i + 1);
            }
        }
        return node;
    }

    classes.value.edges.forEach(function(d) {
        // console.log(d)
        find(d.data.source, d);
    });

    return map[""];
}

// Return a list of imports for the given array of nodes.
function packageCloneImports(nodes) {
    var map = {},
        imports = [];

    // Compute a map from name to node.
    nodes.forEach(function(d) {
        // console.log(d.data.source)
        map[d.source] = d.data.source;
    });

    // For each import, construct a link from the source to target node.
    nodes.forEach(function(d) {
        if (d.data.target)
            imports.push({source: map[d.source], target: d.data.target});
    });
    // console.log(imports)
    return imports;
}
// Place any jQuery/helper plugins in here.
