public class App {
    public static void main(String[] args) throws Exception {
        final Task<Integer> first = () -> {
            System.out.println("first");
            return Integer.valueOf(0);
        };
        final Task<Integer> second = first.then((i) -> {
            System.out.println("second:" + i.toString());
            return Integer.valueOf(1);
        });

        Integer result = null;
        do {
            result = second.poll();
        } while (result == null);
        System.out.println("end:" + result.toString());
    }
}

class Runtime implements AsyncRuntime {
    static class Worker extends Thread {

    }

    java.util.concurrent.PriorityBlockingQueue<Task<?>> tasks = new java.util.concurrent.PriorityBlockingQueue<Task<?>>();

    @Override
    public void dispatch(Task<?> task) {
        this.tasks.add(task);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}