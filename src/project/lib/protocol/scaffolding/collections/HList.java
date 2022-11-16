package project.lib.protocol.scaffolding.collections;

public class HList<T, U> {
    public static <T, U> HList<T, U> of(T rest, U head) {
        return new HList<T, U>(rest, head);
    }

    public HList(T rest, U head) {
        this.head = head;
        this.rest = rest;
    }

    public final T rest;
    public final U head;

    public <S> HList<HList<T, U>, S> push(S item) {
        return new HList<HList<T, U>, S>(this, item);
    }
}