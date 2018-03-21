package pt.um.lei.masb.blockchain.stringutils;

import java.security.*;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class StringUtil {
    private static Logger LOGGER = Logger.getLogger("StringUtil");
    private static Crypter DEFAULTCRYPTER = new SHA256Encrypter();

    //Ensure Bouncy Castle Crypto provider is present
    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        }
    }

    public static Crypter getDefaultCrypter() {
        return DEFAULTCRYPTER;
    }

    //Applies ECDSA Signature and returns the result ( as bytes ).
    public static byte[] applyECDSASig(PrivateKey privateKey, String input) {
        Signature dsa;
        byte[] output;
        try {
            dsa = Signature.getInstance("ECDSA", "BC");
            dsa.initSign(privateKey);
            byte[] strByte = input.getBytes();
            dsa.update(strByte);
            output = dsa.sign();
        } catch (GeneralSecurityException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
            throw new RuntimeException("ECDSA Signature problem", e);
        }
        return output;
    }

    //Verifies a String signature
    public static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature) {
        try {
            Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
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


    public static String getDifficultyString(int difficulty) {
        return new String(new char[difficulty]).replace('\0', '0'); //Create a string with difficulty * "0"
    }
}
