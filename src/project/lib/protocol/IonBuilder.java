package project.lib.protocol;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public final class IonBuilder {
    public static IonBuilder.Mapping mapping() {
        return new Mapping();
    }

    public static IonBuilder.Mapping map(String key, Ion value) {
        return mapping().map(key, value);
    }

    public static IonBuilder.Mapping map(String key, String value) {
        return mapping().map(key, atom(value));
    }

    public static IonBuilder.Array array() {
        return new IonBuilder.Array();
    }

    public static Ion.Array array(Ion... items) {
        return new IonBuilder.ConstantArray(items);
    }

    public static Ion.Array array(List<Ion> items) {
        return new IonBuilder.Array(items);
    }

    public static IonBuilder.Atom atom(String text) {
        return new IonBuilder.Atom(text);
    }

    public final static class Mapping extends Ion.Mapping {
        private Mapping() {
            this.map = new HashMap<>();
        }

        private final HashMap<String, Ion> map;

        @Override
        public Ion get(String key) {
            return map.get(key);
        }

        @Override
        public Set<Entry<String, Ion>> entries() {
            return map.entrySet();
        }

        public IonBuilder.Mapping map(String key, Ion value) {
            this.map.put(key, value);
            return this;
        }

        public IonBuilder.Mapping map(String key, String value) {
            return this.map(key, atom(value));
        }
    }

    public final static class Atom extends Ion.Atom {
        private Atom(String text) {
            this.text = text;
        }

        private final String text;

        @Override
        public String text() {
            return this.text;
        }
    }

    public final static class Array extends Ion.Array {
        private Array() {
            this(new ArrayList<>());
        }

        private Array(List<Ion> list) {
            this.list = list;
        }

        private final List<Ion> list;

        @Override
        public Ion get(int index) {
            return this.list.get(index);
        }

        @Override
        public int length() {
            return this.list.size();
        }

        @Override
        public Iterator<Ion> iterator() {
            return this.list.iterator();
        }

        public IonBuilder.Array add(Ion value) {
            this.list.add(value);
            return this;
        }

        public IonBuilder.Array add(String value) {
            return this.add(atom(value));
        }
    }

    private final static class ConstantArray extends Ion.Array {
        private ConstantArray(Ion[] array) {
            this.array = array;
        }

        private final Ion[] array;

        @Override
        public Ion get(int index) {
            return this.array[index];
        }

        @Override
        public int length() {
            return this.array.length;
        }

    }
}