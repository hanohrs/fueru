package fueru3;

import util.FizzBuzz;
import util.FueruUtils;
import util.Pair;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static util.FueruUtils.fizzBuzzCountsToString;
import static util.FueruUtils.sumToString;

public final class Fueru {
    public static void main(String[] args) {
        Pair<Map<FizzBuzz, Long>, Double> resultPair = new BufferedReader(new InputStreamReader(System.in)).lines()
                .map(Double::valueOf)
                .collect(
                        Collectors.teeing(
                                Collectors.mapping(
                                        Double::intValue,
                                        Collectors.mapping(
                                                FueruUtils::toFizzBuzz,
                                                Collectors.filtering(
                                                        Objects::nonNull,
                                                        Collectors.groupingBy(
                                                                Function.identity(),
                                                                TreeMap::new,
                                                                Collectors.counting()
                                                        )
                                                )
                                        )
                                ),
                                Collectors.summingDouble(Double::doubleValue),
                                Pair::new
                        )
                );

        System.out.println("################");
        System.out.println("#  結果レポート  #");
        System.out.println("################");
        System.out.println();
        System.out.println(fizzBuzzCountsToString(resultPair.left()));
        System.out.println();
        System.out.println(sumToString(resultPair.right()));
    }
}
