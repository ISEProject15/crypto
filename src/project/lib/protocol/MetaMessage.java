package project.lib.protocol;

public class MetaMessage {
    public static MetaMessage parse(String text) {

    }

    public final String identity;
    public final MessageBody body;

    private MetaMessage(String identity, MessageBody body) {
        this.identity = identity;
        this.body = body;
    }
}
