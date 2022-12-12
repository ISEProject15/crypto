package project.lib.scaffolding.collections;

import project.lib.scaffolding.ArrayPool;

public interface SegmentBufferStrategy<T> {
    public static <T> SegmentBufferStrategy<T> defaultPooledStrategy(ArrayPool<T> pool) {
        return new SegmentBufferStrategy<T>() {
            {
                this._pool = pool;
            }
            private final ArrayPool<T> _pool;

            @Override
            public T requireSegmentBuffer(int required) {
                return this._pool.rent(required);
            }

            @Override
            public T tryRequireSegmentBuffer(int required) {
                return this._pool.tryRent(required);
            }

            @Override
            public void backSegmentBuffer(T buffer) {
                this._pool.back(buffer);
            }

        };
    }

    public T requireSegmentBuffer(int required);

    public T tryRequireSegmentBuffer(int required);

    public void backSegmentBuffer(T buffer);
}
