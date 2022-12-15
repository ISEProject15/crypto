package project.test.scaffolding;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.ToDoubleFunction;

import project.scaffolding.debug.IndentedAppendable;

public class Graph {
    private static final String numberFormat = "%.12f";

    public Graph() {
        this.pathes = new ArrayList<>();
        this.minX = Double.POSITIVE_INFINITY;
        this.minY = Double.POSITIVE_INFINITY;
        this.maxX = Double.NEGATIVE_INFINITY;
        this.maxY = Double.NEGATIVE_INFINITY;
    }

    private final ArrayList<Path> pathes;
    private double minX, minY, maxX, maxY;

    public double minX() {
        return this.minX;
    }

    public double minY() {
        return this.minY;
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
        final var path = new Path(fill, stroke, width, closed, coords);
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

    public void encode(IndentedAppendable builder) {
        final var size = Math.min(this.width(), this.height());
        final var cx = size / this.width();
        final var cy = size / this.height();

        builder.print("<svg viewBox=\"")
                .printf(numberFormat, this.minX * cx).print(" ")
                .printf(numberFormat, this.minY * cy).print(" ")
                .printf(numberFormat, size).print(" ")
                .printf(numberFormat, size)
                .print("\" ");

        builder.print("preserveAspectRatio=\"none\" ");
        builder.print("xmlns=\"http://www.w3.org/2000/svg\" ");
        builder.println(">").indent();

        builder.print("<g transform=\"translate(")
                .printf(numberFormat, this.minX * cx)
                .print(", ")
                .printf(numberFormat, this.maxY * cy)
                .println(") scale(1, -1)\">")
                .indent();

        for (final var path : this.pathes) {
            path.encode(builder, cx, cy);
            builder.println();
        }

        builder.println("</g>").deindent();
        builder.deindent().print("</svg>");
    }

    private static <T> int countUp(Iterator<T> iter) {
        var count = 0;
        while (iter.hasNext()) {
            iter.next();
            count++;
        }
        return count;
    }

    private static class Path {
        Path(String fill, String stroke, String width, boolean closed, double[] coords) {
            if ((coords.length & 1) != 0) {
                throw new IllegalArgumentException();
            }

            this.fill = fill;
            this.stroke = stroke;
            this.width = width;
            this.closed = closed;
            this.coordinates = coords;
            var minX = Double.POSITIVE_INFINITY;
            var minY = Double.POSITIVE_INFINITY;
            var maxX = Double.NEGATIVE_INFINITY;
            var maxY = Double.NEGATIVE_INFINITY;
            for (var i = 0; i < coords.length; i += 2) {
                final var x = coords[i + 0];
                final var y = coords[i + 1];
                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
                minX = Math.min(minX, x);
                maxX = Math.max(maxX, x);
            }
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
        }

        final String fill;
        final String stroke;
        final String width;
        final boolean closed;
        final double[] coordinates;
        final double minX, minY, maxX, maxY;

        void encode(IndentedAppendable builder, double cx, double cy) {
            if (coordinates.length <= 0) {
                return;
            }
            builder.print("<path ");
            builder.print("stroke=\"").print(this.stroke == null ? "none" : this.stroke).print("\" ");
            builder.print("fill=\"").print(this.fill == null ? "none" : this.fill).print("\" ");
            if (this.width != null) {
                builder.print("stroke-width=\"").print(this.width).print("\" ");
            }
            final var coords = this.coordinates;
            builder.print("d=\"M ")
                    .printf(numberFormat, coords[0] * cx)
                    .print(" ")
                    .printf(numberFormat, coords[1] * cy);

            for (var i = 0; i < coords.length; i += 2) {
                builder.print(" L ")
                        .printf(numberFormat, coords[i + 0] * cx)
                        .print(" ")
                        .printf(numberFormat, coords[i + 1] * cy);
            }
            if (this.closed) {
                builder.print(" Z");
            }
            builder.print("\"/>");
        }
    }
}