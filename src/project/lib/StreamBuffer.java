package project.lib;

public class StreamBuffer {
    Segment firstSegment;
    int firstBound;// inclusive
    Semgent lastSegment;
    int lastBound;// exclusive

    public void write(byte[] source) {

    }

    public void read(byte[] destination) {
        
    }

    private static class Segment {
        Segment(int capacity) {
            this.buffer = new byte[capacity];
        }

        private final byte[] buffer;
        private Segment next;
    }
}
