package project.test.scaffolding;

public class JoinedCharSequence implements CharSequence {
    public static JoinedCharSequence join(CharSequence first, CharSequence second) {
        return new JoinedCharSequence(first, second);
    }

    private JoinedCharSequence(CharSequence first, CharSequence second) {
        this.first = first;
        this.second = second;
    }

    private final CharSequence first;
    private final CharSequence second;

    @Override
    public int length() {
        return this.first.length() + this.second.length();
    }

    @Override
    public char charAt(int index) {
        final var firstLenth = this.first.length();
        if (index < firstLenth) {
            return this.first.charAt(index);
        }
        return this.second.charAt(index - firstLenth);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        final var firstLenth = this.first.length();
        if (end <= firstLenth) {
            return this.first.subSequence(start, end);
        }
        if (start >= firstLenth) {
            return this.second.subSequence(start - firstLenth, end - firstLenth);
        }

        return join(this.first.subSequence(start, this.first.length()), this.second.subSequence(0, end - firstLenth));
    }

}
