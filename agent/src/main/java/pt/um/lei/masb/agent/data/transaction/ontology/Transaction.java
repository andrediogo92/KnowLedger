package pt.um.lei.masb.agent.data.transaction.ontology;


import jade.content.Concept;
import pt.um.lei.masb.blockchain.data.SensorData;

import java.util.Arrays;
import java.util.Objects;

/**
 * Transaction in ontology bean form.
 */
public class Transaction implements Concept {
    private String transactionId;
    private String publicKey;
    private SensorData sd;
    private byte[] signature;

    public Transaction(String transactionId,
                       String publicKey,
                       SensorData sd,
                       byte[] signature) {
        this.transactionId = transactionId;
        this.publicKey = publicKey;
        this.sd = sd;
        this.signature = signature;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public SensorData getSd() {
        return sd;
    }

    public void setSd(SensorData sd) {
        this.sd = sd;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Transaction that = (Transaction) o;
        return Objects.equals(transactionId, that.transactionId) &&
                Objects.equals(publicKey, that.publicKey) &&
                Objects.equals(sd, that.sd) &&
                Arrays.equals(signature, that.signature);
    }

    @Override
    public int hashCode() {

        int result = Objects.hash(transactionId, publicKey, sd);
        result = 31 * result + Arrays.hashCode(signature);
        return result;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId='" + transactionId + '\'' +
                ", publicKey='" + publicKey + '\'' +
                ", sd=" + sd +
                ", signature=" + signature +
                '}';
    }
}
