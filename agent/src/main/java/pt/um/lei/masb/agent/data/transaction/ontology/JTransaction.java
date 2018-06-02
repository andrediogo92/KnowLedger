package pt.um.lei.masb.agent.data.transaction.ontology;


import jade.content.Concept;

import java.util.Arrays;
import java.util.Objects;

/**
 * JTransaction in ontology bean form.
 */
public class JTransaction implements Concept {
    private String transactionId;
    private String publicKey;
    private JSensorData sd;
    private byte[] signature;

    public JTransaction(String transactionId,
                        String publicKey,
                        JSensorData sd,
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

    public JSensorData getSd() {
        return sd;
    }

    public void setSd(JSensorData sd) {
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
        JTransaction that = (JTransaction) o;
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
        return "JTransaction{" +
                "transactionId='" + transactionId + '\'' +
                ", publicKey='" + publicKey + '\'' +
                ", sd=" + sd +
                ", signature=" + signature +
                '}';
    }
}
