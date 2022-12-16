package project.test.scaffolding;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class StatisticSummary {
    StatisticSummary(double[] measures, boolean[] outliers, double variance, double mean, int validSampleCount) {
        this.measures = measures;
        this.outliers = outliers;
        this.variance = variance;
        this.mean = mean;
        this.validSampleCount = validSampleCount;
    }

    private final double[] measures;
    private final boolean[] outliers;
    private final double variance;
    private final double mean;
    private final int validSampleCount;

    public int sampleCount() {
        return this.measures.length;
    }

    public List<Double> measures() {
        return DoubleStream.of(this.measures).boxed().collect(Collectors.toUnmodifiableList());
    }

    public double measure(int index) {
        return this.measures[index];
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
}
