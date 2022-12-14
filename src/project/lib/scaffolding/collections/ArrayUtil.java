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

    public static <T> int compare(T arrayL, int fromL, int toL, T arrayR, int fromR, int toR) {
        if (arrayL == null || arrayR == null) {
            throw new IllegalArgumentException();
        }
        final var cls = arrayL.getClass();

        if (arrayR.getClass() != cls || arrayR.getClass().componentType() != cls.componentType()) {
            throw new IllegalArgumentException();
        }

        if (cls == byte[].class) {
            final var l = (byte[]) arrayL;
            final var r = (byte[]) arrayR;
            final var lenL = toL - fromL;
            final var lenR = toR - fromR;
            final var minLen = Math.min(lenL, lenR);
            for (var i = 0; i < minLen; ++i) {
                final var c = Byte.compare(l[i + fromL], r[i + fromR]);
                if (c != 0) {
                    return c;
                }
            }
            return Integer.compare(lenL, lenR);
        }

        if (cls == char[].class) {
            final var l = (char[]) arrayL;
            final var r = (char[]) arrayR;
            final var lenL = toL - fromL;
            final var lenR = toR - fromR;
            final var minLen = Math.min(lenL, lenR);
            for (var i = 0; i < minLen; ++i) {
                final var c = Character.compare(l[i + fromL], r[i + fromR]);
                if (c != 0) {
                    return c;
                }
            }
            return Integer.compare(lenL, lenR);
        }

        if (cls == short[].class) {
            final var l = (short[]) arrayL;
            final var r = (short[]) arrayR;
            final var lenL = toL - fromL;
            final var lenR = toR - fromR;
            final var minLen = Math.min(lenL, lenR);
            for (var i = 0; i < minLen; ++i) {
                final var c = Short.compare(l[i + fromL], r[i + fromR]);
                if (c != 0) {
                    return c;
                }
            }
            return Integer.compare(lenL, lenR);
        }

        if (cls == int[].class) {
            final var l = (int[]) arrayL;
            final var r = (int[]) arrayR;
            final var lenL = toL - fromL;
            final var lenR = toR - fromR;
            final var minLen = Math.min(lenL, lenR);
            for (var i = 0; i < minLen; ++i) {
                final var c = Integer.compare(l[i + fromL], r[i + fromR]);
                if (c != 0) {
                    return c;
                }
            }
            return Integer.compare(lenL, lenR);
        }

        if (cls == long[].class) {
            final var l = (long[]) arrayL;
            final var r = (long[]) arrayR;
            final var lenL = toL - fromL;
            final var lenR = toR - fromR;
            final var minLen = Math.min(lenL, lenR);
            for (var i = 0; i < minLen; ++i) {
                final var c = Long.compare(l[i + fromL], r[i + fromR]);
                if (c != 0) {
                    return c;
                }
            }
            return Integer.compare(lenL, lenR);
        }

        if (cls == float[].class) {
            final var l = (float[]) arrayL;
            final var r = (float[]) arrayR;
            final var lenL = toL - fromL;
            final var lenR = toR - fromR;
            final var minLen = Math.min(lenL, lenR);
            for (var i = 0; i < minLen; ++i) {
                final var c = Float.compare(l[i + fromL], r[i + fromR]);
                if (c != 0) {
                    return c;
                }
            }
            return Integer.compare(lenL, lenR);
        }
        if (cls == double[].class) {
            final var l = (double[]) arrayL;
            final var r = (double[]) arrayR;
            final var lenL = toL - fromL;
            final var lenR = toR - fromR;
            final var minLen = Math.min(lenL, lenR);
            for (var i = 0; i < minLen; ++i) {
                final var c = Double.compare(l[i + fromL], r[i + fromR]);
                if (c != 0) {
                    return c;
                }
            }
            return Integer.compare(lenL, lenR);
        }

        if (cls == boolean[].class) {
            final var l = (boolean[]) arrayL;
            final var r = (boolean[]) arrayR;
            final var lenL = toL - fromL;
            final var lenR = toR - fromR;
            final var minLen = Math.min(lenL, lenR);
            for (var i = 0; i < minLen; ++i) {
                final var c = Boolean.compare(l[i + fromL], r[i + fromR]);
                if (c != 0) {
                    return c;
                }
            }
            return Integer.compare(lenL, lenR);
        }

        if (!Comparable.class.isAssignableFrom(cls.componentType())) {
            throw new IllegalArgumentException();
        }
        final var l = (Comparable[]) arrayL;
        final var r = (Comparable[]) arrayR;
        final var lenL = toL - fromL;
        final var lenR = toR - fromR;
        final var minLen = Math.min(lenL, lenR);
        for (var i = 0; i < minLen; ++i) {
            @SuppressWarnings("unchecked")
            final var c = l[i + fromL].compareTo(r[i + fromR]);
            if (c != 0) {
                return c;
            }
        }
        return Integer.compare(lenL, lenR);
    }

    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> cls, int length) {
        return (T) Array.newInstance(cls.componentType(), length);
    }

    @SuppressWarnings("unchecked")
    public static <T> T resize(T array, int size) {
        if (array == null) {
            return null;
        }
        final var cls = (Class<T>) array.getClass();
        final var newarr = create(cls, size);
        final var minLen = Math.min(size, Array.getLength(array));
        System.arraycopy(array, 0, newarr, 0, minLen);
        return newarr;
    }

    public static <T> void copyFromBack2Back(T src, T dst, int dstOffsetFromFirst, int length) {
        final var srcLen = Array.getLength(src);
        final var dstLen = Array.getLength(dst);

        if (length < 0) {
            throw new IllegalArgumentException();
        }
        if (dstOffsetFromFirst + length > dstLen) {
            throw new IllegalArgumentException();
        }
        if (length == 0) {
            return;
        }
        final var minLen = Math.min(srcLen, length);
        final var srcOff = srcLen - minLen;
        final var dstOff = dstOffsetFromFirst + (length - minLen);
        System.arraycopy(src, srcOff, dst, dstOff, minLen);
    }
}
