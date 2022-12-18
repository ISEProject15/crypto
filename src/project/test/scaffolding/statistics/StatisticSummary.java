package project.test.scaffolding.statistics;

import project.test.scaffolding.DoubleRandomAccess;
import project.test.scaffolding.drawing.Graph;

public class StatisticSummary {
    StatisticSummary(DoubleRandomAccess measures, boolean[] outliers, double variance, double mean,
            int validSampleCount) {
        this.measures = measures;
        this.outliers = outliers;
        this.variance = variance;
        this.mean = mean;
        this.validSampleCount = validSampleCount;
    }

    private final DoubleRandomAccess measures;
    private final boolean[] outliers;
    private final double variance;
    private final double mean;
    private final int validSampleCount;

    public int sampleCount() {
        return this.measures.length();
    }

    public DoubleRandomAccess measures() {
        return this.measures;
    }

    public double measure(int index) {
        return this.measures.get(index);
    }

    public double variance() {
        return this.variance;
    }

    public double mean() {
        return this.mean;
    }

    public int validSampleCount() {
        return this.validSampleCount;
    }

    public double standardDeviation() {
        return Math.sqrt(this.variance());
    }

    public double unbiasedStandardDeviation() {
        return Math.sqrt(this.variance() * (this.validSampleCount / (this.validSampleCount - 1.0)));
    }

    public boolean isOutlier(int index) {
        return this.outliers[index];
    }

    public Graph print() {
        final var measures = this.measures;
        final var graph = new Graph();
        final var mean = this.mean;
        final var sd = this.standardDeviation();
        graph.addPath("black", "1px", measures, x -> x);
        graph.addPath("blue", "1px", new double[] { graph.minX(), mean, graph.maxX(), mean });
        graph.addPath("red", "1px", new double[] {
                graph.minX(), mean + sd,
                graph.maxX(), mean + sd });
        graph.addPath("red", "1px", new double[] {
                graph.minX(), mean - sd,
                graph.maxX(), mean - sd });

        return graph;
    }
}
