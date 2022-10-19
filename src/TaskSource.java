public interface TaskSource<TIn, TOut> {
    Task<TOut> create(TIn arg);
}