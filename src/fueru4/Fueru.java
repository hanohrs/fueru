package fueru4;

import org.jetbrains.annotations.NotNull;
import util.FizzBuzz;
import util.Pair;
import util.Twin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import static util.FueruUtils.*;

public final class Fueru {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        final Future<Map<FizzBuzz, Long>> fizzBuzzCounts;
        final Future<Double> sum;
        Twin<Stream<Double>> streams;
        try (DoubleStream numbers = new BufferedReader(new InputStreamReader(System.in)).lines()
                .map(Double::valueOf)
                .mapToDouble(Double::doubleValue)) {
            streams = fuyasu(numbers.boxed());

            ExecutorService executor = Executors.newFixedThreadPool(2);
            fizzBuzzCounts = executor
                    .submit(() -> countFizzBuzz(streams.left().mapToDouble(Double::doubleValue)));
            sum = executor
                    .submit(() -> streams.right().mapToDouble(Double::doubleValue).sum());

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
        Supplier<Pair<List<T>, Twin<Stream<T>>>> supplier = () ->
                new Pair<>(new ArrayList<>(), new Twin<>(Stream.empty(), Stream.empty()));

        BiConsumer<Pair<List<T>, Twin<Stream<T>>>, T> biConsumer = (p, t) -> p.left().add(t);

        BinaryOperator<Pair<List<T>, Twin<Stream<T>>>> combiner = (
                Pair<List<T>, Twin<Stream<T>>> p1,
                Pair<List<T>, Twin<Stream<T>>> p2) -> {
            Stream<T> leftStream = Stream
                    .of(p1.right().left(), p1.left().stream(), p2.right().left(), p2.left().stream())
                    .reduce(Stream::concat).orElseThrow();
            Stream<T> rightStream = Stream
                    .of(p1.right().right(), p1.left().stream(), p2.right().right(), p2.left().stream())
                    .reduce(Stream::concat).orElseThrow();
            Twin<Stream<T>> streams = new Twin<>(leftStream, rightStream);
            return new Pair<>(new ArrayList<>(), streams);
        };

        Function<Pair<List<T>, Twin<Stream<T>>>, Twin<Stream<T>>> finisher = pair -> new Twin<>(
                Stream.concat(pair.right().left(), pair.left().stream()),
                Stream.concat(pair.right().right(), pair.left().stream())
        );

        // 名ばかり Collector
        var fuyasuNabakariCollector =
                Collector.of(supplier, biConsumer, combiner, finisher);
        return stream.collect(fuyasuNabakariCollector);
    }

}
