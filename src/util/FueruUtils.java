package util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public final class FueruUtils {
    private FueruUtils() {
    }

    @NotNull
    public static Map<FizzBuzz, Long> countFizzBuzz(DoubleStream numbers) {
        return numbers
                .boxed()
                .mapToInt(Double::intValue)
                .mapToObj(FueruUtils::toFizzBuzz)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), TreeMap::new, Collectors.counting()));
    }

    public static String fizzBuzzCountsToString(Map<?, Long> fizzBuzzResultMap) {
        String fizzBuzzResultString = fizzBuzzResultMap.entrySet().stream().sequential()
                .map(entry -> "%9s: %7d".formatted(entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(System.lineSeparator()));
        return "# FizzBuzz レポート%n%s".formatted(fizzBuzzResultString);
    }

    @NotNull
    public static String sumToString(double sum) {
        return "# 合計レポート%n%13g".formatted(sum);
    }

    @Nullable
    public static FizzBuzz toFizzBuzz(int i) {
        boolean isFizz = i % 3 == 0;
        boolean isBuzz = i % 5 == 0;

        if (isFizz && isBuzz)
            return FizzBuzz.FizzBuzz;
        else if (isFizz)
            return FizzBuzz.Fizz;
        else if (isBuzz)
            return FizzBuzz.Buzz;
        else
            return null;
    }
}
