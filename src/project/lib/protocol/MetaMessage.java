package project.lib.protocol;

public class MetaMessage {
    public static MetaMessage of(String id, Ion body) {
        return new MetaMessage(id, body);
    }

    private MetaMessage(String id, Ion body) {
        this.identity = id;
        this.body = body;
    }

    public final String identity;

    public final Ion body;
}
