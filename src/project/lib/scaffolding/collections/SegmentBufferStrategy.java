package project.lib.scaffolding.collections;

import project.lib.scaffolding.ArrayPool;

public interface SegmentBufferStrategy<T> {
    public static <T> SegmentBufferStrategy<T> defaultPooledStrategy(ArrayPool<T> pool) {
        return SegmentBufferStrategies.defaultPooledStrategy(pool);
    }

    public static <T> SegmentBufferStrategy<T> defaultNonPooledStrategy(Class<T> cls) {
        return SegmentBufferStrategies.defaultNonPooledStrategy(cls);
    }

    public T requireSegmentBuffer(int required);

    public T tryRequireSegmentBuffer(int required);

    public void backSegmentBuffer(T buffer);

    public Class<T> bufferClass();
}
