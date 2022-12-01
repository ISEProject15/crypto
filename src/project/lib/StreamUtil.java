package project.lib;

public class StreamUtil {
    public static int lenof(int length) {
        return length ^ flagof(length);
    }

    public static int flagof(int length) {
        return length >> 31;
    }

    public static boolean isLast(int length) {
        return length < 0;
    }
}
