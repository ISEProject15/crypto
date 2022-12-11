package project.lib.scaffolding;

public final class ByteArrayPool {
    private static final ArrayPool<byte[]> pool = new ArrayPool<>(byte[].class);

    public static byte[] rent(int minimumLength) {
        return pool.rent(minimumLength);
    }

    public static void back(byte[] array) {
        pool.back(array);
    }

    public static byte[] tryRent(int minimumLength) {
        return pool.tryRent(minimumLength);
    }

    private ByteArrayPool() {
    }
}