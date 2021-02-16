package fueru6;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.FizzBuzz;
import util.Twin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static util.FueruUtils.*;

public final class Fueru {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        final Future<Map<FizzBuzz, Long>> fizzBuzzCounts;
        final Future<Double> sum;

        try (DoubleStream numbers = new BufferedReader(new InputStreamReader(System.in)).lines()
                .map(Double::valueOf)
                .mapToDouble(Double::doubleValue)) {

            Twin<Stream<Double>> streams = fuyasu(numbers.boxed());
            ExecutorService executor = Executors.newFixedThreadPool(2);

            fizzBuzzCounts = executor
                    .submit(() -> countFizzBuzz(streams.left().mapToDouble(Double::doubleValue)));
            sum = executor.submit(() -> streams.right().mapToDouble(Double::doubleValue).sum());

            executor.shutdown();
            //noinspection ResultOfMethodCallIgnored
            executor.awaitTermination(60, TimeUnit.SECONDS);
        }

        System.out.println("################");
        System.out.println("#  結果レポート  #");
        System.out.println("################");
        System.out.println();
        System.out.println(fizzBuzzCountsToString(fizzBuzzCounts.get()));
        System.out.println();
        System.out.println(sumToString(sum.get()));
    }

    /**
     * Stream をふやす
     */
    @NotNull
    static <T> Twin<Stream<T>> fuyasu(Stream<T> stream) {
        final NIterators<T> twoIterators = new NIterators<>(2, stream.iterator());
        Spliterator<T> leftSpliterator =
                Spliterators.spliteratorUnknownSize(twoIterators, 0);
        Spliterator<T> rightSpliterator =
                Spliterators.spliteratorUnknownSize(twoIterators, 0);
        Stream<T> leftStream = StreamSupport.stream(leftSpliterator, false);
        Stream<T> rightStream = StreamSupport.stream(rightSpliterator, false);
        return new Twin<>(leftStream, rightStream);
    }
}


/**
 * Iterator 継承の悪い見本。普通の Iterator だと思って使うとびっくりする。
 */
final class NIterators<E> implements Iterator<E> {
    private final Iterator<E> origIterator;
    private final CyclicBarrier barrier;
    private volatile NoSuchElementException exception;
    private volatile E next;

    NIterators(int parties, Iterator<E> iterator) {
        origIterator = iterator;
        exception = null;
        next = origNextOrNull();
        barrier = new CyclicBarrier(parties, () -> next = origNextOrNull());
    }

    private @Nullable E origNextOrNull() {
        try {
            return origIterator.next();
        } catch (NoSuchElementException e) {
            exception = e;
            return null;
        }
    }

    @Override
    public synchronized boolean hasNext() {
        return exception == null;
    }

    @SuppressWarnings("IteratorNextCanNotThrowNoSuchElementException") // why?
    public E next() throws NoSuchElementException {
        if (exception != null)
            throw exception;
        E ret = next;
        assert ret != null;
        try {
            barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            throw new RuntimeException(e);
        }
        return ret;
    }
}
