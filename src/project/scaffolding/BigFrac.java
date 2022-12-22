package project.scaffolding;

import java.math.BigInteger;

public class BigFrac {
    public static final BigFrac ZERO = BigFrac.of(BigInteger.ZERO);
    public static final BigFrac ONE = BigFrac.of(BigInteger.ONE);

    public static BigFrac of(BigInteger numerator) {
        return new BigFrac(numerator, BigInteger.ONE);
    }

    public static BigFrac of(BigInteger numerator, BigInteger denominator) {
        return new BigFrac(numerator, denominator);
    }

    public static BigFrac of(long numerator) {
        return BigFrac.of(BigInteger.valueOf(numerator));
    }

    public static BigFrac of(long numerator, long denominator) {
        return new BigFrac(BigInteger.valueOf(numerator), BigInteger.valueOf(denominator));
    }

    private BigFrac(BigInteger numerator, BigInteger denominator) {
        if (denominator.signum() == 0) {
            throw new ArithmeticException();
        }
        if (denominator.signum() < 0) {
            numerator = numerator.negate();
            denominator = denominator.abs();
        }
        final var gcd = numerator.gcd(denominator);
        this.numerator = numerator.divide(gcd);
        this.denominator = denominator.divide(gcd);
    }

    public final BigInteger numerator, denominator;

    public int signum() {
        return this.numerator.signum();
    }

    public BigFrac mul(BigFrac frac) {
        final var g0 = this.numerator.gcd(frac.denominator);
        final var g1 = frac.numerator.gcd(this.denominator);
        final var num = this.numerator.divide(g0).multiply(frac.numerator.divide(g1));
        final var div = this.denominator.divide(g1).multiply(frac.denominator.divide(g0));
        return BigFrac.of(num, div);
    }

    public BigFrac div(BigFrac frac) {
        final var g0 = this.numerator.gcd(frac.numerator);
        final var g1 = this.denominator.gcd(frac.denominator);
        final var num = this.numerator.divide(g0).multiply(frac.denominator.divide(g1));
        final var div = this.denominator.divide(g1).multiply(frac.numerator.divide(g0));
        return BigFrac.of(num, div);
    }

    public BigFrac add(BigFrac frac) {
        final var g = this.denominator.gcd(frac.denominator);
        final var n0 = this.numerator.multiply(frac.denominator.divide(g));
        final var n1 = this.denominator.divide(g).multiply(frac.numerator);
        final var div = this.denominator.divide(g).multiply(frac.denominator);

        return BigFrac.of(n0.add(n1), div);
    }

    public BigFrac sub(BigFrac frac) {
        final var g = this.denominator.gcd(frac.denominator);
        final var n0 = this.numerator.multiply(frac.denominator.divide(g));
        final var n1 = this.denominator.divide(g).multiply(frac.numerator);
        final var div = this.denominator.divide(g).multiply(frac.denominator);

        return BigFrac.of(n0.subtract(n1), div);
    }

    public BigFrac negate() {
        return BigFrac.of(this.numerator.negate(), this.denominator);
    }

    public BigFrac inv() {
        return new BigFrac(this.denominator, this.numerator);
    }

    public BigInteger integerPart() {
        return this.numerator.divide(this.denominator);
    }

    public BigFrac fractionalPart() {
        final var mod = this.numerator.remainder(this.denominator);
        return BigFrac.of(mod, this.denominator);
    }

    public boolean isInteger() {
        return this.denominator.equals(BigInteger.ONE);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof BigFrac f) {
            return this.numerator.equals(f.numerator) && this.denominator.equals(f.denominator);
        }
        return false;
    }

    @Override
    public String toString() {
        return this.numerator.toString() + " / " + this.denominator.toString();
    }

    public String toString(int radix) {
        return this.numerator.toString(radix) + " / " + this.denominator.toString(radix);
    }
}