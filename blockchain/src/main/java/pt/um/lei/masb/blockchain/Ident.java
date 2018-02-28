package pt.um.lei.masb.blockchain;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Ident {
    private static Logger LOGGER = Logger.getLogger("Ident");
    private PrivateKey privateKey;
    private PublicKey publicKey;

    public Ident() {
        generateKeyPair();
    }

    private void generateKeyPair() {
        try {
            KeyPairGenerator keygen = KeyPairGenerator.getInstance("ECDSA","BC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
            // Initialize the key generator and generate a KeyPair
            keygen.initialize(ecSpec, random);   //256 bytes provides an acceptable security level
            KeyPair keyPair = keygen.generateKeyPair();
            // Set the public and private keys from the keyPair
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
        } catch (GeneralSecurityException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
            throw new RuntimeException("Keygen problem", e);
        }
    }


}
