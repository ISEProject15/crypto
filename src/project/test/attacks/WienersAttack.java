package project.test.attacks;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;

import project.scaffolding.BigFrac;
import project.scaffolding.IntMath;
import project.scaffolding.debug.IndentedAppendable;

public class WienersAttack {
    public static void demo() {
        final var writer = IndentedAppendable.create(System.out, "  ");

        final var modulo = BigInteger.valueOf(8927);
        final var exponent = BigInteger.valueOf(2621);
        final var f = BigFrac.of(exponent, modulo);
        // iterate guess of k/d
        final var iter = new ContinuedFractionApproximationIterator(f);
        final var fracE = BigFrac.of(exponent);

        while (iter.hasNext()) {
            writer.indentLevel(0);
            writer.println("guessing:").indent();

            final var guessOfkdg = iter.next();
            final var guessOfPhi = fracE.div(guessOfkdg).integerPart();
            writer.println("guess of phi(N): " + guessOfPhi);
            final var guessOfPplusQ = modulo.subtract(guessOfPhi).add(BigInteger.ONE);
            writer.println("guess of p + q: " + guessOfPplusQ);
            if (guessOfPplusQ.testBit(0)) {
                writer.println("discard guess: guess of p + q is not even");
                // p + q never be odd
                continue;
            }
            // (p + q) / 2
            final var guessOfPplusQ2 = guessOfPplusQ.shiftRight(1);
            final var pq2 = guessOfPplusQ2.pow(2).subtract(modulo);
            final var guessOfPminusQ2 = IntMath.trySqrt(pq2);
            writer.println("guess of ((p - q) / 2)**2: " + pq2);
            if (guessOfPminusQ2 == null) {
                writer.println("discard guess: guess of ((p - q) / 2)**2 is not perfect square");
                // pq2 is not perfect square
                continue;
            }
            // (p + q) / 2 + (p - q) / 2 == p
            final var guessOfP = guessOfPplusQ2.add(guessOfPminusQ2);
            // (p + q) / 2 - (p - q) / 2 == q
            final var guessOfQ = guessOfPplusQ2.subtract(guessOfPminusQ2);
            writer.println("guessing succeeded");
            writer.println("p: " + guessOfP + ", q: " + guessOfQ);
            writer.println("pq == modulo: " + (guessOfP.multiply(guessOfQ).equals(modulo)));
            break;
        }
    }
}

class AppendedIter<T> implements Iterator<T> {
    public static <T> AppendedIter<T> append(Iterator<T> iter, T item) {
        return new AppendedIter<>(iter, item);
    }

    private AppendedIter(Iterator<T> iter, T appended) {
        this.iter = iter;
        this.appended = appended;
        this.ended = false;
    }

    private final Iterator<T> iter;
    private final T appended;
    private boolean ended;

    @Override
    public boolean hasNext() {
        if (this.ended) {
            return false;
        }

        if (iter.hasNext()) {
            return true;
        }
        this.ended = true;
        return true;
    }

    @Override
    public T next() {
        if (this.ended) {
            return this.appended;
        }
        return iter.next();
    }

}

class ContinuedFractionApproximationIterator implements Iterator<BigFrac> {
    public ContinuedFractionApproximationIterator(BigFrac f) {
        this.f = f;
    }

    private final ArrayList<BigInteger> expanded = new ArrayList<BigInteger>();
    private BigFrac f;

    @Override
    public boolean hasNext() {
        return !f.equals(BigFrac.ZERO);
    }

    @Override
    public BigFrac next() {
        final var integer = this.f.integerPart();
        final var frac = this.f.fractionalPart();
        final var estimated = reconstruct(AppendedIter.append(
                expanded.iterator(),
                (expanded.size() & 1) == 1
                        ? integer
                        : integer.add(BigInteger.ONE)));
        expanded.add(integer);

        if (frac.equals(BigFrac.ZERO)) {
            this.f = frac;
        } else {
            this.f = frac.inv();
        }

        return estimated;
    }

    private static BigFrac reconstruct(Iterator<BigInteger> continuedFractional) {
        var pn = BigInteger.ZERO;
        var pd = BigInteger.ONE;
        var n = BigInteger.ONE;
        var d = BigInteger.ZERO;
        while (continuedFractional.hasNext()) {
            final var q = continuedFractional.next();
            final var nn = q.multiply(n).add(pn);
            final var nd = q.multiply(d).add(pd);
            pn = n;
            pd = d;
            n = nn;
            d = nd;
        }

        if (d.equals(BigInteger.ZERO)) {
            return null;
        }

        return BigFrac.of(n, d);
    }
}