package project.lib.protocol.scaffolding;

public class Log {
    public static final Log global = new Log();

    private String indentString = "  ";
    private int indent = 0;

    public Log log(String msg) {
        this.writeIndent();
        System.out.println(msg);
        return this;
    }

    public Log indent(int delta) {
        this.indent += delta;
        return this;
    }

    private void writeIndent() {
        for (var i = 0; i < this.indent; ++i) {
            System.out.print(this.indentString);
        }
    }
}