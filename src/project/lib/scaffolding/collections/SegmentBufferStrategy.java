package project.lib.scaffolding.collections;

public interface SegmentBufferStrategy {

    public static final SegmentBufferStrategy defaultStrategy = new SegmentBufferStrategy() {
        @Override
        public int nextSegmentSize(int required) {
            if (required <= 16)
                return 16;

            if (required <= 32)
                return 32;

            if (required <= 64)
                return 64;

            if (required <= 128)
                return 128;

            if (required <= 256)
                return 256;

            return required;
        }
    };

    // returns next segment size. MUST greater than required.
    public int nextSegmentSize(int required);
}
