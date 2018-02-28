package pt.um.lei.masb.blockchain;

import pt.um.lei.masb.blockchain.data.SensorData;
import pt.um.lei.masb.blockchain.stringutils.StringUtil;
import pt.um.lei.masb.blockchain.stringutils.Crypter;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class Transaction {
  private static Crypter crypter = StringUtil.getDefaultCrypter();
  // this is also the hash of the transaction.
  private final String transactionId;
  // Agent's pub key.
  private final PublicKey publicKey;
  // this is to identify unequivocally an agent.
  private byte[] signature;
  private final List<SensorData> sd;

  private final List<TransactionInput> inputs;
  private final List<TransactionOutput> outputs = new ArrayList<>();

  // a rough count of how many transactions have been generated.
  private static AtomicLong sequence = new AtomicLong(0);

  // Constructor:
  public Transaction(PublicKey from, List<SensorData> sd,  List<TransactionInput> inputs) {
    this.publicKey = from;
    this.sd = sd;
    this.inputs = inputs;
    this.transactionId = calculateHash();
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

  public List<SensorData> getSd() {
    return sd;
  }

  public List<TransactionInput> getInputs() {
    return inputs;
  }

  public List<TransactionOutput> getOutputs() {
    return outputs;
  }

  // This Calculates the transaction hash (which will be used as its Id)
  private String calculateHash() {
    //Increase the sequence to avoid 2 identical transactions having the same hash
    return crypter.applyHash(
        StringUtil.getStringFromKey(publicKey) +
        sd.toString() + sequence.incrementAndGet()
    );
  }

  //Signs all the data we dont wish to be tampered with.
  public void generateSignature(PrivateKey privateKey) {
    String data = StringUtil.getStringFromKey(publicKey) + sd.toString();
    signature = StringUtil.applyECDSASig(privateKey,data);
  }

  //Verifies the data we signed hasn't been tampered with.
  public boolean verifySignature() {
    String data = StringUtil.getStringFromKey(publicKey) + sd.toString();
    return StringUtil.verifyECDSASig(publicKey, data, signature);
  }

  //Returns true if new transaction could be created.
  public boolean processTransaction() {
    return verifySignature();
  }

}
