package fueru5;

import org.jetbrains.annotations.NotNull;
import util.FizzBuzz;
import util.Twin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
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
    private static <T> Twin<Stream<T>> fuyasu(Stream<T> stream) {
        final TwinIterator<T> twinIterator = new TwinIterator<>(stream.iterator());
        Spliterator<T> leftSpliterator =
                Spliterators.spliteratorUnknownSize(twinIterator.getLeftIterator(), 0);
        Spliterator<T> rightSpliterator =
                Spliterators.spliteratorUnknownSize(twinIterator.getRightIterator(), 0);
        Stream<T> leftStream = StreamSupport.stream(leftSpliterator, false);
        Stream<T> rightStream = StreamSupport.stream(rightSpliterator, false);
        return new Twin<>(leftStream, rightStream);
    }
}


/**
 * 1つの Iterator から、2つのスレッドでそれぞれ使うための Iterator を作り出すクラス。
 * ディープ コピーどころか clone もしていないので、要素の値を書き換えたりしないこと！
 */
final class TwinIterator<T> {
    private final Iterator<T> origIterator;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notLeftAdvanced = lock.newCondition();
    private final Condition notRightAdvanced = lock.newCondition();
    private final Iterator<T> leftIterator = new ChildIterator(Source.LEFT);
    private final Iterator<T> rightIterator = new ChildIterator(Source.RIGHT);
    private volatile T next = null;
    private volatile State state = State.EQUALLY_ADVANCED;

    private enum Source {LEFT, RIGHT}

    private enum State {EQUALLY_ADVANCED, LEFT_ADVANCED, RIGHT_ADVANCED}

    TwinIterator(Iterator<T> iterator) {
        this.origIterator = iterator;
    }

    public Iterator<T> getLeftIterator() {
        return leftIterator;
    }

    public Iterator<T> getRightIterator() {
        return rightIterator;
    }

    private boolean hasNextFor(Source source) {
        return switch (state) {
            case EQUALLY_ADVANCED -> origIterator.hasNext();
            case LEFT_ADVANCED -> switch (source) {
                case LEFT -> origIterator.hasNext();
                case RIGHT -> next != null;
            };
            case RIGHT_ADVANCED -> switch (source) {
                case LEFT -> next != null;
                case RIGHT -> origIterator.hasNext();
            };
        };
    }

    private synchronized T nextFor(Source source) {
        state = switch (state) {
            case EQUALLY_ADVANCED -> {
                try {
                    next = origIterator.next();
                } catch (NoSuchElementException e) {
                    next = null;
                    throw e;
                }
                yield switch (source) {
                    case LEFT -> State.LEFT_ADVANCED;
                    case RIGHT -> State.RIGHT_ADVANCED;
                };
            }
            case LEFT_ADVANCED -> switch (source) {
                case LEFT -> throw new IllegalStateException("left advanced twice in a row!");
                case RIGHT -> State.EQUALLY_ADVANCED;
            };
            case RIGHT_ADVANCED -> switch (source) {
                case LEFT -> State.EQUALLY_ADVANCED;
                case RIGHT -> throw new IllegalStateException("right advanced twice in a row!");
            };
        };

        if (next != null)
            return next;
        else
            throw new NoSuchElementException();
    }

    @SuppressWarnings("NonStaticInnerClassInSecureContext")
    final class ChildIterator implements Iterator<T> {
        private final Source me;

        ChildIterator(Source sourceID) {
            me = sourceID;
        }

        @Override
        public boolean hasNext() {
            lock.lock();
            try {
                awaitForTheOther();
                return TwinIterator.this.hasNextFor(me);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
        }

        @Override
        public T next() throws NoSuchElementException {
            lock.lock();
            try {
                awaitForTheOther();
                switch (me) {
                    case LEFT -> notRightAdvanced.signal();
                    case RIGHT -> notLeftAdvanced.signal();
                }
                return TwinIterator.this.nextFor(me);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
        }

        private void awaitForTheOther() throws InterruptedException {
            switch (me) {
                case LEFT -> {
                    while (state == State.LEFT_ADVANCED)
                        notLeftAdvanced.await();
                }
                case RIGHT -> {
                    while (state == State.RIGHT_ADVANCED)
                        notRightAdvanced.await();
                }
            }
        }
    }
}
