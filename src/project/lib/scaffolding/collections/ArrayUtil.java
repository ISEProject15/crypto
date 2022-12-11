package project.lib.scaffolding.collections;

import java.lang.reflect.Array;
import java.util.Arrays;

public class ArrayUtil {
    public static <T> void clear(T array, int from, int to) {
        if (array == null) {
            return;
        }
        final var cls = array.getClass();

        if (cls == byte[].class) {
            Arrays.fill((byte[]) array, from, to, (byte) 0);
            return;
        }

        if (cls == Object[].class) {// object array(include any user defined type array)
            Arrays.fill((Object[]) array, from, to, null);
            return;
        }

        if (cls == char[].class) {
            Arrays.fill((char[]) array, from, to, '\0');
            return;
        }

        if (cls == short[].class) {
            Arrays.fill((short[]) array, from, to, (short) 0);
            return;
        }

        if (cls == int[].class) {
            Arrays.fill((int[]) array, from, to, 0);
            return;
        }

        if (cls == long[].class) {
            Arrays.fill((long[]) array, from, to, 0L);
            return;
        }

        if (cls == float[].class) {
            Arrays.fill((float[]) array, from, to, 0.0f);
            return;
        }
        if (cls == double[].class) {
            Arrays.fill((double[]) array, from, to, 0.0);
            return;
        }
        if (cls == boolean[].class) {
            Arrays.fill((boolean[]) array, from, to, false);
            return;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T resize(T array, int size) {
        if (array == null) {
            return null;
        }
        final var cls = array.getClass();
        final var newarr = (T) Array.newInstance(cls.componentType(), size);
        final var minLen = Math.min(size, Array.getLength(array));
        System.arraycopy(array, 0, newarr, 0, minLen);
        return newarr;
    }
}
