package project.lib.protocol;

import java.util.HashMap;

class MetaMsg implements MetaMessage {
    public static MetaMessage of(String id, MetaMessage.Body body) {
        return new MetaMsg(id, body);
    }

    public MetaMsg(String id, MetaMessage.Body body) {
        this.id = id;
        this.body = body;
    }

    private final String id;
    private final MetaMessage.Body body;

    @Override
    public String identity() {
        return this.id;
    }

    @Override
    public Body body() {
        return this.body;
    }

}

class AtomImpl extends MetaMessage.Body.Atom {
    public static MetaMessage.Body.Atom of(String text) {
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

class MappingImpl extends MetaMessage.Body.Mapping {
    public static class Builder {
        private final HashMap<String, MetaMessage.Body> map;

        Builder() {
            this.map = new HashMap<>();
        }

        public Builder add(String key, MetaMessage.Body value) {
            this.map.put(key, value);
            return this;
        }

        public MetaMessage.Body.Mapping build() {
            return new MappingImpl(map);
        }
    }

    private final HashMap<String, MetaMessage.Body> map;

    public MappingImpl(HashMap<String, MetaMessage.Body> map) {
        this.map = map;
    }

    @Override
    public MetaMessage.Body get(String key) {
        return map.get(key);
    }

    @Override
    public java.util.Set<java.util.Map.Entry<String, MetaMessage.Body>> entries() {
        return this.map.entrySet();
    }
}