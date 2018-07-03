package pt.um.lei.masb.blockchain.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class SHA256Encrypter implements Crypter {
    private final static Logger LOGGER = LoggerFactory.getLogger(SHA256Encrypter.class);

    //Applies Sha256 to a string and returns the result.
    public String applyHash(String input) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            //Applies sha256 to our input,
            var hash = digest.digest(input.getBytes("UTF-8"));
            var hexString = new StringBuilder(); // This will contain the hash as hexadecimal
            for (byte aHash : hash) {
                String hex = Integer.toHexString(0xff & aHash);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            LOGGER.error("", e.getMessage());
            throw new RuntimeException("Apply SHA256 problem", e);
        }
    }

    @Override
    public long hashSize() {
        return 32;
    }
}
