package pt.um.lei.masb.blockchain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.security.*;
import java.security.spec.ECGenParameterSpec;

@Entity
public class Ident {
    private final static Logger LOGGER = LoggerFactory.getLogger(Ident.class);

    //Ensure Bouncy Castle Crypto provider is present
    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        }
    }

    @Id
    private long id = 0;

    @NotNull
    @Basic(optional = false)
    private PrivateKey privateKey;

    @NotNull
    @Basic(optional = false)
    private PublicKey publicKey;

    /**
     * @throws RuntimeException When key generation fails.
     */
    public Ident() {
        generateKeyPair();
    }

    /**
     * @throws RuntimeException When key generation fails.
     */
    private void generateKeyPair() {
        try {
            var keygen = KeyPairGenerator.getInstance("ECDSA", "BC");
            var random = SecureRandom.getInstance("SHA1PRNG");
            var ecSpec = new ECGenParameterSpec("prime192v1");
            // Initialize the key generator and generate a KeyPair
            keygen.initialize(ecSpec, random);   //256 bytes provides an acceptable security level
            var keyPair = keygen.generateKeyPair();
            // Set the public and private keys from the keyPair
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
        } catch (GeneralSecurityException e) {
            LOGGER.error("", e.getMessage());
            throw new RuntimeException("Keygen problem", e);
        }
    }

    public @NotNull PrivateKey getPrivateKey() {
        return privateKey;
    }

    public @NotNull PublicKey getPublicKey() {
        return publicKey;
    }
}
