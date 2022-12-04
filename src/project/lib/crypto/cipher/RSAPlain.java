package project.lib.crypto.cipher;

import project.lib.Transformer;
import project.lib.crypto.Cipher;
import project.lib.crypto.CipherArgs;
import project.lib.protocol.Ion;

public class RSAPlain implements Cipher {
    private static final String identity = "RSAPlain";

    @Override
    public String identity() {
        return identity;
    }

    @Override
    public CipherArgs create(Ion args) {
        final var view = args.dynamicView();
        final var lengthView = view.prop("keyLength");
        if (lengthView == null) {
            return null;
        }
        final var lengthStr = lengthView.as(String.class);
        if (lengthStr == null) {
            return null;
        }
        try {
            final var length = Integer.parseInt(lengthStr);

        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }

    @Override
    public Transformer encrypter(Ion encryptionKey) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Transformer decrypter(Ion decryptionKey) {
        // TODO Auto-generated method stub
        return null;
    }

}
