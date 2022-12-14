package project.lib.scaffolding.collections;

import project.lib.scaffolding.streaming.BufferWriterListener;

public class SequenceFunnel {
    private SequenceFunnel() {

    }

    public static <T> void into(Sequence<T> sequence, BufferWriterListener<T> listener) {
        final var iter = sequence.iterator();
        while (iter.hasNext()) {
            iter.move();
            listener.finished(iter.currentBuffer(), iter.currentOffset(), iter.currentLength());
        }
    }
}
