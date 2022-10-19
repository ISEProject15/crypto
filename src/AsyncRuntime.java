public interface AsyncRuntime {
    public void dispatch(Task<?> task);

    public void stop();

    public void start();
}