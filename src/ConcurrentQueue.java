import java.util.ArrayList;

public class ConcurrentQueue<T> {
    private final ArrayList<T> list = new ArrayList<T>();

    public synchronized void push(T item) {
        list.add(item);
    }

    public synchronized T pop() {
        return list.remove(list.size() - 1);
    }
}
