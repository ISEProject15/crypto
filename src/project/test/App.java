package project.test;

public class App {
    public static void main(String[] args) throws Exception {
        final var parser = new project.lib.protocol.MetaMsgParser();
        final var msg = parser.parse("id@key0:key01:key001=v001&key002=v002;&key02=v02;&key1=v1");
        System.out.print(msg.toString());

        final var senderloop = new SenderLoop();
        final var receiverLoop = new ReceiverLoop();
        final var wiretapperLoop = new WiretapperLoop();

        senderloop.start();
        receiverLoop.start();
        wiretapperLoop.start();
    }
}

class SenderLoop extends Thread {
    @Override
    public void run() {
        // wait receiver hello
        // send sender hello
        // wait cipher protocol suggestion
        // send cipher protocol prelude
        // receive data
    }
}

class ReceiverLoop extends Thread {
    @Override
    public void run() {
        // Send receiver hello(contains cipher spec)
        // wait sender hello
        // send cipher protocol suggestion
        // wait cipher protocol prelude
        // send data
    }
}

class WiretapperLoop extends Thread {
    @Override
    public void run() {

    }
}