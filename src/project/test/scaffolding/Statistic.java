package project.test.scaffolding;

public class Statistic {
    public Statistic(double outlierThreshold) {
        this.outlierThreshold = outlierThreshold;
    }

    private final double outlierThreshold;

    public StatisticSummary analyze(double[] measures) {
        var outliers = new boolean[measures.length];
        var sampleCount = measures.length;
        var mean = Double.NaN;
        var variance = Double.NaN;
        while (sampleCount > 0) {
            mean = calculateMean(measures, outliers, sampleCount);
            variance = 0.0;
            final var sqrtSampleCount = Math.sqrt(sampleCount);
            final var dividedMean = mean / sqrtSampleCount;
            var outlierCandidate = -1;
            var maxDeltaVariance = Double.NEGATIVE_INFINITY;
            for (var i = 0; i < measures.length; ++i) {
                if (outliers[i]) {
                    continue;
                }
                final var measure = measures[i];
                final var deltaVariance = Math.pow(measure / sqrtSampleCount - dividedMean, 2);
                if (deltaVariance > maxDeltaVariance) {
                    outlierCandidate = i;
                    maxDeltaVariance = deltaVariance;
                }
                variance += deltaVariance;
            }
            final var unbiasedSd = Math.sqrt(variance * (sampleCount / (sampleCount - 1.0)));
            final var isOutlier = checkOutlier(measures[outlierCandidate], unbiasedSd, mean);
            if (!isOutlier) {
                break;
            }

            outliers[outlierCandidate] = true;
            sampleCount--;
        }
        return new StatisticSummary(measures, outliers, variance, mean, sampleCount);
    }

    private static double calculateMean(double[] measures, boolean[] outliers, int sampleCount) {
        var mean = 0.0;
        for (var i = 0; i < measures.length; ++i) {
            if (outliers[i]) {
                continue;
            }
            final var measure = measures[i];
            mean += measure / sampleCount;
        }
        return mean;
    }

    private boolean checkOutlier(double measure, double unbiasedSd, double mean) {
        if (Double.isNaN(unbiasedSd)) {
            return false;
        }
        if (unbiasedSd < 1.0) {
            return (measure - mean) > this.outlierThreshold * unbiasedSd;
        }
        return (measure / unbiasedSd - mean / unbiasedSd) > this.outlierThreshold;
    }
}
