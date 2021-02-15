package fueru5;

import util.Twin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.*;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import static fueru5.Fueru.fuyasu;
import static util.FueruUtils.sumToString;

public class Katamaru {
    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
        final Future<Boolean> found;
        final Future<Double> sum;

        try (DoubleStream numbers = new BufferedReader(new InputStreamReader(System.in)).lines()
                .map(Double::valueOf)
                .mapToDouble(Double::doubleValue)) {

            Twin<Stream<Double>> streams = fuyasu(numbers.boxed());
            ExecutorService executor = Executors.newFixedThreadPool(2);

            found = executor.submit(() -> streams.left().anyMatch(d -> d == 42.0));
            sum = executor.submit(() -> streams.right().mapToDouble(Double::doubleValue).sum());

            executor.shutdown();
            //noinspection ResultOfMethodCallIgnored
            executor.awaitTermination(20, TimeUnit.SECONDS);
        }

        System.out.println("################");
        System.out.println("#  結果レポート  #");
        System.out.println("################");
        System.out.println();
        System.out.print("生命、宇宙、そして万物についての究極の疑問の答え");
        if (found.get(1, TimeUnit.SECONDS))
            System.out.println("を見つけた");
        else
            System.out.println("は見つからなかった");
        System.out.println();
        System.out.println(sumToString(sum.get(1, TimeUnit.SECONDS)));
    }
}
