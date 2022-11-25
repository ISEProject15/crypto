package project.lib.protocol;

// server, client のペアを定義する
public interface Protocol {
    public ClientSession establishClient(Ion arg);

    public ServerSession establishServer(Ion arg);
}
