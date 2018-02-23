package pt.um.li.mas.blockchain;

import pt.um.li.mas.blockchain.stringutils.StringUtil;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

public class Block {
    private static int MAX_BLOCK_SIZE=500;
    private String hash;
    private final Transaction data[];
    private final AtomicInteger cur = new AtomicInteger(0);
    private String merkleRoot;
    private final String previousHash;
    private final String timeStamp;
    private long nonce;
    private final int difficulty;

    public Block(String previousHash, int difficulty ) {
        this.data = new Transaction[MAX_BLOCK_SIZE];
        this.nonce = 0;
        this.difficulty = difficulty;
        this.timeStamp = LocalDateTime.now().toString();
        this.previousHash = previousHash;
        this.merkleRoot = null;
        this.hash = null; //Making sure we do this after we set the other values.
    }

    public String calculateHash() {
        String calculatedhash = StringUtil.applySha256(
                previousHash +
                        Long.toHexString(nonce) +
                        timeStamp +
                        merkleRoot
        );
        return calculatedhash;
    }

    public void mineBlock() {
        String target = StringUtil.getDifficultyString(difficulty);
        merkleRoot = StringUtil.getMerkleRoot(data, cur.get());
        while(!hash.substring( 0, difficulty).equals(target)) {
            nonce ++;
            hash = calculateHash();
        }
        System.out.println("Block Mined!!! : " + hash);
    }

  public boolean addTransaction(Transaction transaction) {
    //process transaction and check if valid, unless block is genesis block then ignore.
    if(transaction == null) return false;
    if((previousHash != "0")) {
      if((transaction.processTransaction() != true)) {
        System.out.println("Transaction failed to process. Discarded.");
        return false;
      }
    }
    data[cur.getAndIncrement()] = transaction;
    System.out.println("Transaction Successfully added to Block");
    return true;
  }

    public String getHash() {
        return hash;
    }

    public Transaction[] getData() {
        return data;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public int getDifficulty() {
        return difficulty;
    }
}
