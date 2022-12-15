package project.test;

import java.util.ArrayList;

public class ProgressiveStatistic {
    public ProgressiveStatistic(double outlierThreshold, int capacity) {
        this.units = new ArrayList<>(capacity);
        this.outlierThreshold = outlierThreshold;
    }

    public ProgressiveStatistic(double outlierThreshold) {
        this.units = new ArrayList<>();
        this.outlierThreshold = outlierThreshold;
    }

    private final ArrayList<Unit> units;
    private final double outlierThreshold;

    public void add(double measure) {
        final var unit = calcurateUnit(measure);
        this.units.add(unit);
    }

    public int size() {
        return this.units.size();
    }

    public double measure(int index) {
        return this.units.get(index).measure;
    }

    public double mean(int index) {
        return this.units.get(index).mean;
    }

    public double mean() {
        return this.mean(this.size() - 1);
    }

    public double moment2(int index) {
        return this.units.get(index).moment2;
    }

    public double moment2() {
        return this.moment2(this.size() - 1);
    }

    public int sampleCount(int index) {
        return this.units.get(index).sampleCount;
    }

    public int sampleCount() {
        return this.sampleCount(this.size() - 1);
    }

    public Graph printGraph() {
        final var graph = new Graph();

        graph.addPath("black", "1", units, u -> u.measure);
        graph.addPath("blue", "1", units, u -> u.mean);
        graph.addPath("red", "1", units, u -> u.mean + u.sd());
        graph.addPath("red", "1", units, u -> u.mean - u.sd());

        return graph;
    }

    public void clear() {
        this.units.clear();
    }

    private Unit calcurateUnit(double measure) {
        final var units = this.units;
        final var outlierThreshold = this.outlierThreshold;
        final var newItemIndex = units.size();
        final var unit = new Unit(measure);
        if (newItemIndex == 0) {// new item is first item
            unit.mean = measure;
            unit.moment2 = measure * measure;
            unit.sampleCount = 1;
            return unit;
        }

        final var lastUnit = units.get(newItemIndex - 1);
        if (lastUnit.checkOutlier(measure, outlierThreshold)) {// new item is outlier
            unit.mean = lastUnit.mean;
            unit.moment2 = lastUnit.moment2;
            unit.sampleCount = lastUnit.sampleCount;
            return unit;
        }

        {
            unit.sampleCount = lastUnit.sampleCount + 1;
            final var c = 1.0 * lastUnit.sampleCount / unit.sampleCount;
            unit.mean = lastUnit.mean * c + 1.0 * measure / unit.sampleCount;
            unit.moment2 = lastUnit.moment2 * c + 1.0 * measure * measure / unit.sampleCount;
        }

        while (true) {
            var sampleCount = 0;
            var mean = 0.0;
            var moment2 = 0.0;
            for (final var u : units) {
                if (unit.checkOutlier(u.measure, outlierThreshold)) {
                    continue;
                }
                final var totalCount = sampleCount + 1.0;
                final var c = sampleCount / totalCount;
                mean = mean * c + u.measure / totalCount;
                moment2 = moment2 * c + u.measure * u.measure / totalCount;
                sampleCount += 1;
            }
            if (unit.sampleCount == sampleCount) {
                break;
            }
            unit.mean = mean;
            unit.moment2 = moment2;
            unit.sampleCount = sampleCount;
        }
        return unit;
    }

    private static class Unit {
        Unit(double measure) {
            this.measure = measure;
        }

        public final double measure;
        public double moment2;// does not conatin outlier
        public double mean;// does not conatin outlier
        public int sampleCount;

        public double variance() {
            return this.moment2 - this.mean * this.mean;
        }

        public double sd() {
            return Math.sqrt(this.variance());
        }

        public boolean checkOutlier(double num, double threshold) {
            final var sd = this.sd();
            return Math.abs(num / sd - this.mean / sd) > threshold;
        }

        public String toString() {
            return "{measure: " + this.measure + ", mean: " + this.mean + ", moment2: " + this.moment2
                    + ", sampleCount: " + this.sampleCount + "}";
        }
    }
}
