package project.lib.scaffolding.dynamic;

public interface DynamicObjectView {
    public default DynamicObjectView prop(String identity) {
        return null;
    }

    public default DynamicObjectView index(int index) {
        return null;
    }

    public default <U> U as(Class<U> cls) {
        return null;
    }

    public default <U> boolean is(Class<U> cls) {
        return false;
    }
}
