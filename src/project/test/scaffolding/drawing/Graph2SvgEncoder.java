package project.test.scaffolding.drawing;

import project.scaffolding.IndentedAppendable;

public class Graph2SvgEncoder {
    public Graph2SvgEncoder(double width, double height) {
        this.width = width;
        this.height = height;
    }

    private static final String numberFormat = "%.12f";
    private final double width;
    private final double height;

    public void encode(Graph graph, IndentedAppendable builder) {
        final var cx = graph.width() == 0 ? 1 : width / graph.width();
        final var cy = graph.height() == 0 ? 1 : height / graph.height();
        final var ox = -graph.minX();
        final var oy = -graph.minY();

        builder.print("<svg viewBox=\"0 0 ")
                .printf(numberFormat, width).print(" ")
                .printf(numberFormat, height)
                .print("\" ");

        builder.print("preserveAspectRatio=\"none\" ");
        builder.print("xmlns=\"http://www.w3.org/2000/svg\" ");
        builder.println(">").indent();

        builder.print("<g transform=\"translate(0, ")
                .printf(numberFormat, height)
                .println(") scale(1, -1)\">")
                .indent();

        for (final var path : graph.paths()) {
            encodePath(builder, path, cx, ox, cy, oy);
        }

        builder.println("</g>").deindent();
        builder.deindent().print("</svg>");
    }

    private void encodePath(IndentedAppendable builder, GraphPath path, double cx, double ox, double cy, double oy) {
        if (path.coordinatesCount() <= 0) {
            return;
        }
        builder.print("<path ");
        builder.print("stroke=\"").print(path.stroke == null ? "none" : path.stroke).print("\" ");
        builder.print("fill=\"").print(path.fill == null ? "none" : path.fill).print("\" ");
        if (path.width != null) {
            builder.print("stroke-width=\"").print(path.width).print("\" ");
        }

        builder.print("d=\"M ")
                .printf(numberFormat, (path.coordinateX(0) + ox) * cx)
                .print(" ")
                .printf(numberFormat, (path.coordinateY(0) + oy) * cy);

        for (var i = 1; i < path.coordinatesCount(); ++i) {
            builder.print(" L ")
                    .printf(numberFormat, (path.coordinateX(i) + ox) * cx)
                    .print(" ")
                    .printf(numberFormat, (path.coordinateY(i) + oy) * cy);
        }
        if (path.closed) {
            builder.print(" Z");
        }
        builder.print("\"/>");
    }
}
