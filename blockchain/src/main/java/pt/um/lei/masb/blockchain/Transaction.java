package pt.um.lei.masb.blockchain;

import org.openjdk.jol.info.ClassLayout;
import pt.um.lei.masb.blockchain.data.SensorData;
import pt.um.lei.masb.blockchain.utils.Crypter;
import pt.um.lei.masb.blockchain.utils.StringUtil;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.concurrent.atomic.AtomicLong;

@Entity
public class Transaction implements Sizeable {
    @NotNull
    private final static Crypter crypter = StringUtil.getDefaultCrypter();

    // A rough count of how many transactions have been generated.
    private final static AtomicLong sequence = new AtomicLong(0);

    // this is also the hash of the transaction.
    @Id
    private final String transactionId;

    // Agent's pub key.
    @Basic(optional = false)
    private final PublicKey publicKey;

    @OneToOne(cascade = CascadeType.ALL,
              optional = false)
    private final SensorData sd;

    // This is to identify unequivocally an agent.
    @Basic(optional = false)
    private byte[] signature;


    @Transient
    private transient long byteSize;

    protected Transaction() {
        transactionId = null;
        publicKey = null;
        sd = null;
    }

    public Transaction(@NotNull PublicKey from,
                       @NotNull SensorData sd) {
        this.publicKey = from;
        this.sd = sd;
        this.transactionId = calculateHash();
        byteSize = ClassLayout.parseClass(this.getClass()).instanceSize() +
                sd.getApproximateSize();
    }

    public String getTransactionId() {
        return transactionId;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public byte[] getSignature() {
        return signature;
    }

    public SensorData getSensorData() {
        return sd;
    }


    // This Calculates the transaction hash (which will be used as its Id)
    private @NotEmpty String calculateHash() {
        //Increase the sequence to avoid 2 identical transactions having the same hash
        return crypter.applyHash(StringUtil.getStringFromKey(publicKey) +
                                 sd.toString() +
                                 sequence.incrementAndGet());
    }

    /**
     * Signs the sensor data using the private key.
     * @return whether signing was successful.
     */
    public boolean generateSignature(@NotNull PrivateKey privateKey) {
        if (publicKey != null) {
            var data = StringUtil.getStringFromKey(publicKey) + sd.toString();
            signature = StringUtil.applyECDSASig(privateKey, data);
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Verifies the data we signed hasn't been tampered with.
     *
     * @return whether the data was signed with the corresponding private key.
     */
    public boolean verifySignature() {
        var data = StringUtil.getStringFromKey(publicKey) + sd.toString();
        return StringUtil.verifyECDSASig(publicKey, data, signature);
    }

    /**
     * TODO: Transaction verification.
     * @return whether the transaction is valid.
     */
    public boolean processTransaction() {
        return true;//verifySignature();
    }

    /**
     * Calculate the approximate size of the transaction.
     *
     * @return the size of the transaction in bytes.
     */
    @Override
    public long getApproximateSize() {
        return byteSize;
    }

    /**
     * Recalculates the Transaction size if it's necessary,
     *
     * The size of the transaction is lost when storing it in database or serialization.
     */
    public void resetSize() {
        byteSize = ClassLayout.parseClass(this.getClass()).instanceSize() +
                sd.getApproximateSize();
    }

    @Override
    public @NotEmpty String toString() {
        String sb = "Transaction {" +
                System.lineSeparator() +
                "Transaction id: " +
                transactionId +
                System.lineSeparator() +
                "Public Key: " +
                publicKey.toString() +
                System.lineSeparator() +
                "Data : {" + getSensorData().toString() +
                '}' +
                System.lineSeparator();
        return sb;
    }
}
