package pt.um.lei.masb.blockchain;

import pt.um.lei.masb.blockchain.data.SensorData;
import pt.um.lei.masb.blockchain.stringutils.StringUtil;
import pt.um.lei.masb.blockchain.stringutils.Crypter;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class Transaction implements Sizeable {
  private static Crypter crypter = StringUtil.getDefaultCrypter();
  // this is also the hash of the transaction.
  private final String transactionId;
  // Agent's pub key.
  private final PublicKey publicKey;
  // this is to identify unequivocally an agent.
  private byte[] signature;
  private final SensorData sd;

  private final List<TransactionInput> inputs;
  private final List<TransactionOutput> outputs = new ArrayList<>();

  private transient int byteSize;

  // a rough count of how many transactions have been generated.
  private static AtomicLong sequence = new AtomicLong(0);


  public Transaction(PublicKey from, SensorData sd,  List<TransactionInput> inputs) {
    this.publicKey = from;
    this.sd = sd;
    this.inputs = inputs;
    this.transactionId = calculateHash();
    byteSize = 512 + sd.getApproximateSize()
                   + (inputs.size() * new TransactionInput().getApproximateSize())
                   + (outputs.size() * new TransactionOutput().getApproximateSize());
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

  /**
   * Signs the sensor data using the public key.
   */
  public void generateSignature(PrivateKey privateKey) {
    String data = StringUtil.getStringFromKey(publicKey) + sd.toString();
    signature = StringUtil.applyECDSASig(privateKey,data);
  }

  /**
   * Verifies the data we signed hasn't been tampered with.
   * @return whether the data was signed with the corresponding private key.
   */
  public boolean verifySignature() {
    String data = StringUtil.getStringFromKey(publicKey) + sd.toString();
    return StringUtil.verifyECDSASig(publicKey, data, signature);
  }

  /**
   * @return whether the transaction is valid.
   */
  public boolean processTransaction() {
    return verifySignature();
  }

  /**
   * Calculate the approximate size of the transaction.
   * @return the size of the transaction in bytes.
   */
  @Override
  public int getApproximateSize() {
    return byteSize;
  }
}
