package project.lib.scaffolding.collections;

import project.lib.scaffolding.ArrayPool;

public class SegmentBufferStrategies {
    private SegmentBufferStrategies() {

    }

    public static <T> SegmentBufferStrategy<T> defaultPooledStrategy(ArrayPool<T> pool) {
        return new PooledStrategy<>(pool);
    }

    public static <T> SegmentBufferStrategy<T> defaultNonPooledStrategy(Class<T> cls) {
        return new NonPooledStrategy<>(cls);
    }

    private static class NonPooledStrategy<T> implements SegmentBufferStrategy<T> {
        NonPooledStrategy(Class<T> cls) {
            this.bufferClass = cls;
        }

        private final Class<T> bufferClass;

        @Override
        public T requireSegmentBuffer(int required) {
            return ArrayUtil.create(this.bufferClass, required);
        }

        @Override
        public T tryRequireSegmentBuffer(int required) {
            return null;
        }

        @Override
        public void backSegmentBuffer(T buffer) {

        }

        @Override
        public Class<T> bufferClass() {
            return this.bufferClass;
        }
    }

    private static class PooledStrategy<T> implements SegmentBufferStrategy<T> {
        PooledStrategy(ArrayPool<T> pool) {
            this.pool = pool;
        }

        private final ArrayPool<T> pool;

        @Override
        public T requireSegmentBuffer(int required) {
            return this.pool.rent(required);
        }

        @Override
        public T tryRequireSegmentBuffer(int required) {
            return this.pool.tryRent(required);
        }

        @Override
        public void backSegmentBuffer(T buffer) {
            this.pool.back(buffer);
        }

        @Override
        public Class<T> bufferClass() {
            return this.pool.arrayClass;
        }
    }
}
