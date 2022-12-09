package project.scaffolding.debug;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.regex.Pattern;

public class IndentedPrintStream extends PrintStream {
    private static final Pattern newLinePattern = Pattern.compile("\\r?\\n");

    public IndentedPrintStream(String indent, OutputStream out, boolean autoFlush) {
        super(out, autoFlush);
        this.indent = indent;
        this.indentLevel = 0;
        this.indentPending = false;
    }

    public IndentedPrintStream(String indent, OutputStream out, boolean autoFlush, Charset charset) {
        super(out, autoFlush, charset);
        this.indent = indent;
        this.indentLevel = 0;
        this.indentPending = false;
    }

    private int indentLevel;
    private boolean indentPending;
    private final String indent;

    public int indentLevel() {
        return this.indentLevel;
    }

    public void indentLevel(int level) {
        this.indentLevel = level;
    }

    public IndentedPrintStream indent() {
        this.indentLevel += 1;
        return this;
    }

    public IndentedPrintStream deindent() {
        this.indentLevel -= 1;
        return this;
    }

    @Override
    public IndentedPrintStream append(CharSequence csq) {
        this.print(csq);
        return this;
    }

    @Override
    public IndentedPrintStream append(CharSequence csq, int start, int end) {
        return this.append(csq.subSequence(start, end));
    }

    @Override
    public IndentedPrintStream append(char c) {
        this.print(c);
        return this;
    }

    @Override
    public IndentedPrintStream format(String format, Object... args) {
        this.print(String.format(format, args));
        return this;
    }

    @Override
    public IndentedPrintStream format(Locale l, String format, Object... args) {
        this.print(String.format(l, format, args));
        return this;
    }

    public void print(CharSequence csq) {
        final var matcher = newLinePattern.matcher(csq);
        var last = 0;
        while (matcher.find()) {
            this.flushIndent();
            final var start = matcher.start();
            final var end = matcher.end();
            super.print(csq.subSequence(last, start));
            last = end;
            this.indentPending = true;
        }
        this.flushIndent();
        super.print(csq.subSequence(last, csq.length()));
    }

    @Override
    public void print(String s) {
        this.print((CharSequence) s);
    }

    @Override
    public void print(Object obj) {
        this.print(String.valueOf(obj));
    }

    @Override
    public void print(boolean b) {
        this.flushIndent();
        super.print(b);
    }

    @Override
    public void print(char c) {
        this.flushIndent();
        super.print(c);
        if (c == '\n') {
            this.indentPending = true;
        }
    }

    @Override
    public void print(int i) {
        this.flushIndent();
        super.print(i);
    }

    @Override
    public void print(long l) {
        this.flushIndent();
        super.print(l);
    }

    @Override
    public void print(float f) {
        this.flushIndent();
        super.print(f);
    }

    @Override
    public void print(double d) {
        this.flushIndent();
        super.print(d);
    }

    @Override
    public void print(char[] s) {
        this.print(CharBuffer.wrap(s));
    }

    @Override
    public IndentedPrintStream printf(String format, Object... args) {
        return this.format(format, args);
    }

    @Override
    public IndentedPrintStream printf(Locale l, String format, Object... args) {
        return this.format(l, format, args);
    }

    public void println(CharSequence csq) {
        this.print(csq);
        this.flushIndent();
        this.println();
    }

    @Override
    public void println() {
        this.flushIndent();
        super.println();
        this.indentPending = true;
    }

    @Override
    public void println(boolean x) {
        this.print(x);
        this.println();
    }

    @Override
    public void println(char x) {
        this.print(x);
        this.println();
    }

    @Override
    public void println(int x) {
        this.print(x);
        this.println();
    }

    @Override
    public void println(long x) {
        this.print(x);
        this.println();
    }

    @Override
    public void println(float x) {
        this.print(x);
        this.println();
    }

    @Override
    public void println(double x) {
        this.print(x);
        this.println();
    }

    @Override
    public void println(char[] x) {
        this.print(x);
        this.println();
    }

    @Override
    public void println(String x) {
        this.print(x);
        this.println();
    }

    @Override
    public void println(Object x) {
        this.print(x);
        this.println();
    }

    private void flushIndent() {
        if (this.indentPending) {
            return;
        }
        this.indentPending = false;

        try {
            for (var rest = this.indentLevel; rest >= 0; --rest) {
                super.append(indent);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
