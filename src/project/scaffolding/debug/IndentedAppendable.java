package project.scaffolding.debug;

import java.util.regex.Pattern;

public class IndentedAppendable implements Appendable {
    private static final Pattern newLinePattern = Pattern.compile("\\r?\\n");

    public static IndentedAppendable create(String indent) {
        return new IndentedAppendable(new StringBuilder(), indent);
    }

    public static IndentedAppendable create(Appendable base, String indent) {
        return new IndentedAppendable(base, indent);
    }

    private IndentedAppendable(Appendable base, String indent) {
        this.base = base;
        this.indent = indent;
        this.indentLevel = 0;
        this.indentPending = false;
    }

    final Appendable base;

    private int indentLevel;
    private boolean indentPending;
    private final String indent;

    public int indentLevel() {
        return this.indentLevel;
    }

    public void indentLevel(int level) {
        this.indentLevel = level;
    }

    public IndentedAppendable indent() {
        this.indentLevel += 1;
        return this;
    }

    public IndentedAppendable deindent() {
        this.indentLevel -= 1;
        return this;
    }

    public IndentedAppendable print(char c) {
        return this.append(c);
    }

    public IndentedAppendable println(char c) {
        return this.append(c).println();
    }

    public IndentedAppendable print(Object obj) {
        return this.print(String.valueOf(obj));
    }

    public IndentedAppendable println(Object obj) {
        return this.println(String.valueOf(obj));
    }

    public IndentedAppendable print(CharSequence csq) {
        return this.append(csq);
    }

    public IndentedAppendable println(CharSequence csq) {
        this.append(csq);
        try {
            this.flushIndent();
            this.base.append(System.lineSeparator());
            this.indentPending = true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public IndentedAppendable println() {
        try {
            this.flushIndent();
            this.base.append(System.lineSeparator());
            this.indentPending = true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public IndentedAppendable printf(String format, Object... args) {
        return this.print(String.format(format, args));
    }

    public IndentedAppendable printlnf(String format, Object... args) {
        return this.println(String.format(format, args));
    }

    @Override
    public IndentedAppendable append(CharSequence csq) {
        try {
            final var matcher = newLinePattern.matcher(csq);
            var last = 0;
            while (matcher.find()) {
                this.flushIndent();
                final var start = matcher.start();
                final var end = matcher.end();
                base.append(csq.subSequence(last, start)).append(System.lineSeparator());
                last = end;
                this.indentPending = true;
            }
            this.flushIndent();
            base.append(csq.subSequence(last, csq.length()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    @Override
    public IndentedAppendable append(CharSequence csq, int start, int end) {
        return this.append(csq.subSequence(start, end));
    }

    @Override
    public IndentedAppendable append(char c) {
        this.flushIndent();
        try {
            this.base.append(c);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (c == '\n') {
            this.indentPending = true;
        }
        return this;
    }

    @Override
    public String toString() {
        return this.base.toString();
    }

    private void flushIndent() {
        if (!this.indentPending) {
            return;
        }
        this.indentPending = false;

        try {
            final var base = this.base;
            for (var rest = this.indentLevel; rest >= 0; --rest) {
                base.append(indent);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
