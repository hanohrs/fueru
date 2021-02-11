package fueru0;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.DoubleStream;

import static util.FueruUtils.*;

public final class Fueru {
    public static void main(String[] args) {
        DoubleStream stream = new BufferedReader(new InputStreamReader(System.in)).lines()
                .mapToDouble(Double::valueOf);
        String fizzBuzzResult = fizzBuzzCountsToString(countFizzBuzz(stream));
        String sumResult = sumToString(stream.sum());

        System.out.println("################");
        System.out.println("#  結果レポート  #");
        System.out.println("################");
        System.out.println();
        System.out.println(fizzBuzzResult);
        System.out.println();
        System.out.println(sumResult);
    }
}
