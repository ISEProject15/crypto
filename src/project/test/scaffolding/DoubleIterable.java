package project.test.scaffolding;

import java.util.Iterator;
import java.util.PrimitiveIterator;

public interface DoubleIterable extends Iterable<Double> {

    public PrimitiveIterator.OfDouble doubleIterator();

    @Override
    default Iterator<Double> iterator() {
        return this.doubleIterator();
    }

}
