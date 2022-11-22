package project.lib.scaffolding.collections;

public interface SegmentBufferStrategy {
    public static final SegmentBufferStrategy doublingMin16Strategy = new SegmentBufferStrategy() {
        @Override
        public int nextSegmentSize(int totalLength, int lastLength) {
            return Math.max(16, lastLength * 2);
        }
    };

    public int nextSegmentSize(int totalLength, int lastLength);
}
