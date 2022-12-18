package project.test.scaffolding;

import java.util.PrimitiveIterator.OfDouble;
import java.util.function.ToIntFunction;

public interface DoubleRandomAccess extends DoubleIterable {
    public static DoubleRandomAccess from(double[] array) {
        return new DoubleRandomAccess() {
            {
                this.source = array;
            }
            final double[] source;

            @Override
            public double get(int index) {
                return this.source[index];
            }

            @Override
            public int length() {
                return this.source.length;
            }

        };
    }

    public static <T> DoubleRandomAccess from(T source, ToIntFunction<T> lengthGetter,
            ObjIntToDoubleFunction<T> getter) {
        return new DoubleRandomAccess() {
            {
                this.src = source;
                this.length = lengthGetter.applyAsInt(source);
                this.itemGetter = getter;
            }
            private final T src;
            private final int length;
            private final ObjIntToDoubleFunction<T> itemGetter;

            @Override
            public double get(int index) {
                return this.itemGetter.applyAsDouble(this.src, index);
            }

            @Override
            public int length() {
                return this.length;
            }
        };
    }

    public double get(int index);

    public int length();

    @Override
    default OfDouble doubleIterator() {
        return new OfDouble() {
            {
                this.length = DoubleRandomAccess.this.length();
            }
            private final int length;
            private int index = -1;

            @Override
            public boolean hasNext() {
                return this.index + 1 < this.length;
            }

            @Override
            public double nextDouble() {
                this.index++;
                return DoubleRandomAccess.this.get(this.index);
            }
        };
    }
}
