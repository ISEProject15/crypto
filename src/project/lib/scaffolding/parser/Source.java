package project.lib.scaffolding.parser;

public interface Source extends CharSequence {
    public static Source from(CharSequence sequence) {
        return Source.from(0, sequence);
    }

    public static Source from(int offset, CharSequence sequence) {
        return new Source() {
            {
                this._offsetTotal = offset;
                this._sequence = sequence;
            }

            final int _offsetTotal;
            final CharSequence _sequence;

            @Override
            public int length() {
                return this._sequence.length();
            }

            @Override
            public char charAt(int index) {
                return this._sequence.charAt(index);
            }

            @Override
            public Source subSequence(int start, int end) {
                return Source.from(start + this._offsetTotal, this._sequence.subSequence(start, end));
            }

            @Override
            public int offsetTotal() {
                return this._offsetTotal;
            }

            @Override
            public Source subSequence(int start) {
                final var seq = this._sequence;
                return Source.from(start + this._offsetTotal, seq.subSequence(start, seq.length()));
            }

            @Override
            public String toString() {
                return this._sequence.toString();
            }

        };
    }

    public int offsetTotal();

    public Source subSequence(int start);
}
