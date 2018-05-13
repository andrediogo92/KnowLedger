package pt.um.lei.masb.blockchain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.logging.Level;
import java.util.logging.Logger;

@NamedQueries({
                      @NamedQuery(name = "get_ident",
                                  query = "SELECT i from Ident i")
              })
@Entity
public class Ident {
    private final static Logger LOGGER = Logger.getLogger("Ident");

    //Ensure Bouncy Castle Crypto provider is present
    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        }
    }

    @Id
    @GeneratedValue
    private long id;

    @NotNull
    @Basic(optional = false)
    private PrivateKey privateKey;

    @NotNull
    @Basic(optional = false)
    private PublicKey publicKey;

    /**
     * @throws RuntimeException when key generation fails.
     */
    public Ident() {
        generateKeyPair();
    }

    /**
     * @throws RuntimeException when key generation fails.
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
            LOGGER.log(Level.SEVERE, e.getMessage());
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
