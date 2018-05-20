package pt.um.lei.masb.blockchain.utils;

import java.math.BigInteger;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class StringUtil {
    private final static Logger LOGGER = Logger.getLogger("StringUtil");
    private final static Crypter DEFAULTCRYPTER = new SHA256Encrypter();

    //Ensure Bouncy Castle Crypto provider is present
    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        }
    }

    public static Crypter getDefaultCrypter() {
        return DEFAULTCRYPTER;
    }

    /**
     * Applies ECDSA Signature and returns the result (as bytes).
     */
    public static byte[] applyECDSASig(PrivateKey privateKey, String input) {
        Signature dsa;
        byte[] output;
        try {
            dsa = Signature.getInstance("ECDSA", "BC");
            dsa.initSign(privateKey);
            var strByte = input.getBytes();
            dsa.update(strByte);
            output = dsa.sign();
        } catch (GeneralSecurityException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
            throw new RuntimeException("ECDSA Signature problem", e);
        }
        return output;
    }

    /**
     * Verifies a String signature.
     */
    public static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature) {
        try {
            var ecdsaVerify = Signature.getInstance("ECDSA", "BC");
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(data.getBytes());
            return ecdsaVerify.verify(signature);
        } catch (GeneralSecurityException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
            throw new RuntimeException("ECDSA Verification problem", e);
        }
    }


    public static String getStringFromKey(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static PublicKey stringToPublicKey(String s) {
        var decoder = Base64.getDecoder();

        PublicKey returnKey = null;

        try {
            var c = decoder.decode(s);
            var keyFact = KeyFactory.getInstance("ECDSA", "BC");
            var x509KeySpec = new X509EncodedKeySpec(c);
            returnKey = keyFact.generatePublic(x509KeySpec);
        } catch (GeneralSecurityException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }

        return returnKey;
    }


    public static BigInteger getInitialDifficulty() {
        var targetbuilder = new byte[256];
        targetbuilder[0] = (byte) 0xE0;
        for(int i=1; i<256;i++) {
            targetbuilder[i] = 0x0;
        }
        return new BigInteger(targetbuilder);
    }
}
