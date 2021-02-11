package fueru2;

import org.jetbrains.annotations.NotNull;
import util.FizzBuzz;
import util.Twin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

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
        return stream.collect(
                Collectors.teeing(
                        Collectors.<T, Stream<T>>reducing(Stream.empty(), Stream::of, Stream::concat),
                        Collectors.<T, Stream<T>>reducing(Stream.empty(), Stream::of, Stream::concat),
                        Twin::new
                )
        );
    }
}
