package project.lib.scaffolding.streaming;

import java.util.ArrayList;

public class BufferWriterSubject<T> implements BufferWriterListener<T> {
    public BufferWriterSubject() {
        this.listeners = new ArrayList<>();
    }

    private ArrayList<BufferWriterListener<T>> listeners;

    public void listen(BufferWriterListener<T> listener) {
        this.listeners.add(listener);
    }

    @Override
    public void finished(T buffer, int offset, int length) {
        if (this.listeners == null) {
            return;
        }

        for (final var listener : this.listeners) {
            listener.finished(buffer, offset, length);
        }
    }
}
