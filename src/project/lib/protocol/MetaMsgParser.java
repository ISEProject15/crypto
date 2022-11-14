package project.lib.protocol;

import project.lib.protocol.MetaMessage.Body;

public class MetaMsgParser implements MetaMessageParser {
    private static class Array implements MetaMessage.Body.Array {

        @Override
        public Body get(int index) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int length() {
            // TODO Auto-generated method stub
            return 0;
        }

    }

    @Override
    public MetaMessage parse(CharSequence sequence) {

        return null;
    }

}