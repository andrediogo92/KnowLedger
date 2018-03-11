package pt.um.lei.masb.blockchain;

import pt.um.lei.masb.blockchain.data.MerkleTree;
import pt.um.lei.masb.blockchain.stringutils.Crypter;
import pt.um.lei.masb.blockchain.stringutils.StringUtil;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class Block {
  private static Crypter crypter = StringUtil.getDefaultCrypter();
    private static int MAX_BLOCK_SIZE=500;
    private String hash;
    private final Transaction data[];
    private final AtomicInteger cur = new AtomicInteger(0);
    private MerkleTree merkleTree;
    private final String previousHash;
    private final String timeStamp;
    private long nonce;
    private final int difficulty;
    private transient final String target;

    public Block(String previousHash, int difficulty ) {
        this.data = new Transaction[MAX_BLOCK_SIZE];
        this.nonce = 0;
        this.difficulty = difficulty;
        this.target = StringUtil.getDifficultyString(difficulty);
        this.timeStamp = LocalDateTime.now().toString();
        this.previousHash = previousHash;
        this.merkleTree = null;
        this.hash = null; //Making sure we do this after we set the other values.
    }

  /**
   * Hash is a SHA-256 calculated from previous hash, nonce, timestamp,
   * {@link MerkleTree}'s root and each {@link Transaction}'s hash.
   * @return The hash string.
   */
  public String calculateHash() {
    StringBuilder sb = new StringBuilder();
    sb.append(previousHash)
      .append(Long.toHexString(nonce))
      .append(timeStamp)
      .append(merkleTree.getRoot());
    for (Transaction d : data) {
      sb.append(d.getTransactionId());
    }
    return crypter.applyHash(sb.toString());
  }

  /**
   * Attempt one nonce calculation.
   * @param invalidate Whether to invalidate the nonce and MerkleTree in case block has changed.
   * @return Whether the block was successfully mined.
   */
  public boolean attemptMineBlock(boolean invalidate) {
      boolean res = false;
        if (invalidate) {
          merkleTree = MerkleTree.buildMerkleTree(data, cur.get());
          nonce = 0;
        }
        hash = calculateHash();
        if(hash.substring( 0, difficulty).equals(target)) {
          res = true;
        } else {
          nonce ++;
        }
        System.out.println("Block Mined!!! : " + hash);
        return res;
    }

  /**
   * Add a single new transaction.
   * Checks if the transaction is valid.
   * @param transaction to add.
   * @return whether transaction was valid.
   */
  public boolean addTransaction(Transaction transaction) {
    //process transaction and check if valid, unless block is genesis block then ignore.
    if(transaction == null) return false;
    if((!previousHash.equals("0"))) {
      if((!transaction.processTransaction())) {
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
