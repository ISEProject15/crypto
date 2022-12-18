package project.test.scaffolding.drawing;

public class GraphPath {
    GraphPath(String fill, String stroke, String width, boolean closed, double[] coords) {
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
}
