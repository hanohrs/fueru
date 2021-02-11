package fueru1;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;

import static util.FueruUtils.*;

public final class Fueru {
    public static void main(String[] args) {
        double[] numbers = new BufferedReader(new InputStreamReader(System.in)).lines()
                .mapToDouble(Double::valueOf)
                .toArray();
        String fizzBuzzResult = fizzBuzzCountsToString(countFizzBuzz(Arrays.stream(numbers)));
        String sumResult = sumToString(Arrays.stream(numbers).sum());

        System.out.println("################");
        System.out.println("#  結果レポート  #");
        System.out.println("################");
        System.out.println();
        System.out.println(fizzBuzzResult);
        System.out.println();
        System.out.println(sumResult);
    }
}
