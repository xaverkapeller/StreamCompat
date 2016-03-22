package com.github.wrdlbrnft.streamcompat.stream;

import android.support.v4.util.ArrayMap;
import android.support.v4.util.LongSparseArray;
import android.util.SparseArray;

import com.github.wrdlbrnft.streamcompat.function.BiConsumer;
import com.github.wrdlbrnft.streamcompat.function.BinaryOperator;
import com.github.wrdlbrnft.streamcompat.function.Function;
import com.github.wrdlbrnft.streamcompat.function.Supplier;
import com.github.wrdlbrnft.streamcompat.function.ToIntFunction;
import com.github.wrdlbrnft.streamcompat.function.ToLongFunction;
import com.github.wrdlbrnft.streamcompat.util.StringJoiner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by kapeller on 10/03/16.
 */
public class Collectors {

    public static <T> Collector<T, ?, List<T>> toList() {
        return new CollectorImpl<T, List<T>, List<T>>(
                ArrayList::new,
                List::add,
                (i) -> i
        );
    }

    public static <T, L extends List<T>> Collector<T, ?, L> toList(Supplier<L> listSupplier) {
        return new CollectorImpl<T, L, L>(
                listSupplier,
                List::add,
                (i) -> i
        );
    }

    public static <T> Collector<T, ?, List<T>> toOrdereredList(Comparator<T> comparator) {
        return new CollectorImpl<T, List<T>, List<T>>(
                ArrayList::new,
                List::add,
                list -> {
                    Collections.sort(list, comparator);
                    return list;
                }
        );
    }

    public static <T extends Comparable<T>> Collector<T, ?, List<T>> toOrdereredList() {
        return new CollectorImpl<T, List<T>, List<T>>(
                ArrayList::new,
                List::add,
                list -> {
                    Collections.sort(list);
                    return list;
                }
        );
    }

    public static <T> Collector<T, ?, Set<T>> toSet() {
        return new CollectorImpl<T, Set<T>, Set<T>>(
                HashSet::new,
                Set::add,
                i -> i
        );
    }

    public static <T, L extends Set<T>> Collector<T, ?, L> toSet(Supplier<L> setSupplier) {
        return new CollectorImpl<T, L, L>(
                setSupplier,
                Set::add,
                i -> i
        );
    }

    public static <T, K, U, M extends Map<K, U>> Collector<T, ?, M> toMap(Function<? super T, ? extends K> keyMapper,
                                                                          Function<? super T, ? extends U> valueMapper,
                                                                          BinaryOperator<U> mergeFunction,
                                                                          Supplier<M> mapSupplier) {
        final BiConsumer<M, T> accumulator = (map, element) -> {
            final K key = keyMapper.apply(element);
            final U value = map.containsKey(key)
                    ? mergeFunction.apply(map.get(key), valueMapper.apply(element))
                    : valueMapper.apply(element);
            map.put(key, value);
        };
        return new CollectorImpl<>(mapSupplier, accumulator, i -> i);
    }

    public static <T, K, U, M extends Map<K, U>> Collector<T, ?, M> toMap(Function<? super T, ? extends K> keyMapper,
                                                                          Function<? super T, ? extends U> valueMapper,
                                                                          Supplier<M> mapSupplier) {
        return toMap(keyMapper, valueMapper, throwingMerger(), mapSupplier);
    }

    public static <T, K, U> Collector<T, ?, Map<K, U>> toMap(Function<? super T, ? extends K> keyMapper,
                                                             Function<? super T, ? extends U> valueMapper,
                                                             BinaryOperator<U> mergeFunction) {
        return toMap(keyMapper, valueMapper, mergeFunction, ArrayMap::new);
    }

    public static <T, K, U> Collector<T, ?, Map<K, U>> toMap(Function<? super T, ? extends K> keyMapper,
                                                             Function<? super T, ? extends U> valueMapper) {
        return toMap(keyMapper, valueMapper, throwingMerger(), ArrayMap::new);
    }

    public static <T, U> Collector<T, ?, SparseArray<U>> toSparseArray(ToIntFunction<? super T> keyMapper,
                                                                       Function<? super T, ? extends U> valueMapper,
                                                                       BinaryOperator<U> mergeFunction) {
        final BiConsumer<SparseArray<U>, T> accumulator = (map, element) -> {
            final int key = keyMapper.apply(element);
            final U current = map.get(key);
            final U value = current != null
                    ? mergeFunction.apply(current,  valueMapper.apply(element))
                    : valueMapper.apply(element);
            map.put(key, value);
        };
        return new CollectorImpl<>(SparseArray<U>::new, accumulator, i -> i);
    }

    public static <T, U> Collector<T, ?, SparseArray<U>> toSparseArray(ToIntFunction<? super T> keyMapper,
                                                                       Function<? super T, ? extends U> valueMapper) {
        return toSparseArray(keyMapper, valueMapper, throwingMerger());
    }

    public static <T, U> Collector<T, ?, LongSparseArray<U>> toLongSparseArray(ToLongFunction<? super T> keyMapper,
                                                                               Function<? super T, ? extends U> valueMapper,
                                                                               BinaryOperator<U> mergeFunction) {
        final BiConsumer<LongSparseArray<U>, T> accumulator = (map, element) -> {
            final long key = keyMapper.apply(element);
            final U current = map.get(key);
            final U value = current != null
                    ? mergeFunction.apply(current,  valueMapper.apply(element))
                    : valueMapper.apply(element);
            map.put(key, value);
        };
        return new CollectorImpl<>(LongSparseArray<U>::new, accumulator, i -> i);
    }

    public static <T, U> Collector<T, ?, LongSparseArray<U>> toLongSparseArray(ToLongFunction<? super T> keyMapper,
                                                                       Function<? super T, ? extends U> valueMapper) {
        return toLongSparseArray(keyMapper, valueMapper, throwingMerger());
    }

    private static <U> BinaryOperator<U> throwingMerger() {
        return (t, u) -> {
            throw new IllegalStateException("Multiple values mapped to the same key");
        };
    }

    public static <T, A, R> Collector<T, A, R> create(Supplier<A> supplier, BiConsumer<A, T> accumulator, Function<A, R> finisher) {
        return new CollectorImpl<>(supplier, accumulator, finisher);
    }

    public static <T> Collector<T, ?, String> joining() {
        return new CollectorImpl<>(
                StringBuilder::new,
                StringBuilder::append,
                StringBuilder::toString
        );
    }

    public static <T> Collector<T, ?, String> joining(String delimiter) {
        return new CollectorImpl<>(
                () -> new StringJoiner(delimiter, "", ""),
                StringJoiner::add,
                StringJoiner::toString
        );
    }

    public static <T> Collector<T, ?, String> joining(String delimiter, String prefix, String suffix) {
        return new CollectorImpl<>(
                () -> new StringJoiner(delimiter, prefix, suffix),
                StringJoiner::add,
                StringJoiner::toString
        );
    }

    public static <T> Collector<T, ?, Collection<T>> appendTo(Collection<T> collection) {
        return new CollectorImpl<>(
                () -> collection,
                Collection::add,
                i -> i
        );
    }
}