package project.test.attacks;

import java.util.Random;

import project.lib.crypto.algorithm.RSA;

public class WeakPrimeAttack {
    public static void demo() throws Exception {
        final var random = new Random();
        var keybundle = RSA.generateKey(10, random);

    }
}
