package project.lib.protocol;

import java.util.HashMap;

class AtomImpl extends Ion.Atom {
    public static Ion.Atom of(String text) {
        return new AtomImpl(text);
    }

    private AtomImpl(String text) {
        this.text = text;
    }

    private final String text;

    @Override
    public String text() {
        return this.text;
    }
}

class MappingImpl extends Ion.Mapping {
    public static class Builder {
        private final HashMap<String, Ion> map;

        Builder() {
            this.map = new HashMap<>();
        }

        public Builder add(String key, Ion value) {
            this.map.put(key, value);
            return this;
        }

        public Ion.Mapping build() {
            return new MappingImpl(map);
        }
    }

    private final HashMap<String, Ion> map;

    public MappingImpl(HashMap<String, Ion> map) {
        this.map = map;
    }

    @Override
    public Ion get(String key) {
        return map.get(key);
    }

    @Override
    public java.util.Set<java.util.Map.Entry<String, Ion>> entries() {
        return this.map.entrySet();
    }
}