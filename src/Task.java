@FunctionalInterface
public interface Task <T>  {
    public T poll();

    default <U> Task<U> then(java.util.function.Function<T, U> fn) {
        return this.continueWith((in) -> () -> fn.apply(in));
    }
    default <U> Task<U> continueWith(TaskSource<T, U> other) {
        final var self = this;
        return new Task <U>() {
            private Task<T> prev = self;
            private Task<U> current = null;
            private TaskSource<T, U> source = other;

            @Override
            public U poll() {
                if (this.prev != null) {
                    final var result = this.prev.poll();
                    if (result != null){
                        this.prev = null;
                        this.current = this.source.create(result);
                    }
                    return null;
                }
                return this.current.poll();
            }
        };
    } 
}

