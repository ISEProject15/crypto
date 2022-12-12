package project.lib.scaffolding;

import java.lang.reflect.Array;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;

import project.lib.scaffolding.collections.ArrayUtil;

public class ArrayPool<T> {
    public ArrayPool(Class<T> cls) {
        this.pool = new TreeMap<>();
        this.cls = cls;
        this.lock = new ReentrantLock(true);
    }

    private final SortedMap<Integer, T> pool;
    private final Class<T> cls;
    private final ReentrantLock lock;

    @SuppressWarnings("unchecked")
    public T rent(int minimumLength) {
        final var arr = this.tryRent(minimumLength);
        if (arr != null) {
            return arr;
        }
        return (T) Array.newInstance(cls.componentType(), minimumLength);
    }

    public T tryRent(int minimumLength) {
        try {
            this.lock.lock();

            final var tail = pool.tailMap(minimumLength);
            if (tail.isEmpty()) {
                return null;
            }
            final var first = tail.firstKey();

            return tail.remove(first);
        } finally {
            this.lock.unlock();
        }

    }

    public void back(T array) {
        try {
            this.lock.lock();
            ArrayUtil.clear(array, 0, Array.getLength(array));
            this.pool.put(Array.getLength(array), array);
        } finally {
            this.lock.unlock();
        }
    }
}
