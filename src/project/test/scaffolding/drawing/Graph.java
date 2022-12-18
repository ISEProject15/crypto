package project.test.scaffolding.drawing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.ToDoubleFunction;

public class Graph {
    public Graph() {
        this.pathes = new ArrayList<>();
        this.minX = Double.POSITIVE_INFINITY;
        this.minY = Double.POSITIVE_INFINITY;
        this.maxX = Double.NEGATIVE_INFINITY;
        this.maxY = Double.NEGATIVE_INFINITY;
    }

    private final ArrayList<GraphPath> pathes;
    private double minX, minY, maxX, maxY;

    public double minX() {
        return this.minX;
    }

    public double minY() {
        return this.minY;
    }

    public double maxX() {
        return this.maxX;
    }

    public double maxY() {
        return this.maxY;
    }

    public double width() {
        return Math.max(0, this.maxX - this.minX);
    }

    public double height() {
        return Math.max(0, this.maxY - this.minY);
    }

    public void addPath(String fill, String stroke, String width, boolean closed, double[] coords) {
        if ((coords.length & 1) != 0) {
            throw new IllegalArgumentException();
        }
        final var path = new GraphPath(fill, stroke, width, closed, coords);
        this.minX = Math.min(this.minX, path.minX);
        this.minY = Math.min(this.minY, path.minY);
        this.maxX = Math.max(this.maxX, path.maxX);
        this.maxY = Math.max(this.maxY, path.maxY);
        this.pathes.add(path);
    }

    public void addPath(String stroke, String width, double[] coords) {
        this.addPath(null, stroke, width, false, coords);
    }

    public <T> void addPath(String fill, String stroke, String width, boolean closed, Iterable<T> path,
            ToDoubleFunction<T> getterY) {
        final var count = countUp(path.iterator());
        final var coords = new double[count * 2];
        var index = 0;
        for (final var entry : path) {
            final var y = getterY.applyAsDouble(entry);

            coords[2 * index + 0] = index;
            coords[2 * index + 1] = y;

            ++index;
        }

        this.addPath(fill, stroke, width, closed, coords);
    }

    public <T> void addPath(String stroke, String width, Iterable<T> path, ToDoubleFunction<T> getterY) {
        this.addPath(null, stroke, width, false, path, getterY);
    }

    public <T> void addPath(String fill, String stroke, String width, boolean closed, Iterable<T> path,
            ToDoubleFunction<T> getterX,
            ToDoubleFunction<T> getterY) {
        final var count = countUp(path.iterator());
        final var coords = new double[count * 2];
        var index = 0;
        for (final var entry : path) {
            final var y = getterY.applyAsDouble(entry);
            final var x = getterX.applyAsDouble(entry);

            coords[2 * index + 0] = x;
            coords[2 * index + 1] = y;

            ++index;
        }

        this.addPath(fill, stroke, width, closed, coords);
    }

    public <T> void addPath(String stroke, String width, Iterable<T> path, ToDoubleFunction<T> getterX,
            ToDoubleFunction<T> getterY) {
        this.addPath(null, stroke, width, false, path, getterX, getterY);
    }

    public Iterable<GraphPath> paths() {
        return this.pathes;
    }

    private static <T> int countUp(Iterator<T> iter) {
        var count = 0;
        while (iter.hasNext()) {
            iter.next();
            count++;
        }
        return count;
    }
}