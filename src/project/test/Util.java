package project.test;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

public class Util {
    public static Set<Entry<BigInteger, Integer>> factorize(BigInteger num) {
        if (num.equals(BigInteger.ZERO)) {
            return Collections.emptySet();
        }

        final var hash = new HashMap<BigInteger, Integer>();

        final var lsb = num.getLowestSetBit();
        if (lsb > 0) {
            hash.put(BigInteger.TWO, lsb);
            num = num.shiftRight(lsb);
        }
        var div = BigInteger.valueOf(3);

        while (!num.equals(BigInteger.ONE)) {
            var exp = 0;
            while (num.remainder(div).equals(BigInteger.ZERO)) {
                num = num.divide(div);
                exp++;
            }
            if (exp > 0) {
                hash.put(div, exp);
            }
            div = div.add(BigInteger.TWO);
        }

        return hash.entrySet();
    }
}
