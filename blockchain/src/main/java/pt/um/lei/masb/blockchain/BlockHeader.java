package pt.um.lei.masb.blockchain;

import pt.um.lei.masb.blockchain.data.MerkleTree;
import pt.um.lei.masb.blockchain.stringutils.Crypter;
import pt.um.lei.masb.blockchain.stringutils.StringUtil;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public final class BlockHeader implements Sizeable {
  private static BlockHeader origin;
  private static Crypter crypter = StringUtil.getDefaultCrypter();
  private String hash;
  private int byteSize;
  private MerkleTree merkleTree;
  private String previousHash;
  private String timeStamp;
  private int nonce;
  private final int difficulty;

  static {
    origin = new BlockHeader("0");
  }

  static BlockHeader getOrigin() {
    return origin;
  }

  /**
   * Origin block specialty constructor
   * @param origin special string "0"
   */
  private BlockHeader(String origin) {
    hash = origin;
    byteSize = 752;
    merkleTree = null;
    previousHash = "";
    timeStamp = ZonedDateTime.of(2018,3,13, 0,0,0,0,ZoneOffset.UTC)
                             .toString();
    nonce = 0;
    difficulty = 1;
  }

  BlockHeader(String previousHash, int difficulty) {
    this.nonce = 0;
    this.difficulty = difficulty;
    this.timeStamp = ZonedDateTime.now(ZoneOffset.UTC).toString();
    this.previousHash = previousHash;
    this.merkleTree = MerkleTree.buildMerkleTree(null, 0);
    this.hash = null; //Making sure we do this after we set the other values.
    this.byteSize = 752;
  }

  private BlockHeader() {
    this.difficulty = -1;
  }



    /**
   * Hash is a SHA-256 calculated from previous hash, nonce, timestamp,
   * {@link MerkleTree}'s root and each {@link Transaction}'s hash.
   * @return The hash string.
   */
  protected void updateHash() {
    hash = calculateHash();
  }

  protected String calculateHash() {
    StringBuilder sb = new StringBuilder();
    sb.append(previousHash)
      .append(Long.toHexString(nonce))
      .append(timeStamp)
      .append(merkleTree.getRoot())
      .append(byteSize);
    return crypter.applyHash(sb.toString());
  }

  protected String getHash() {
    return hash;
  }


  protected MerkleTree getMerkleTree() {
    return merkleTree;
  }

  protected String getPreviousHash() {
    return previousHash;
  }

  protected String getTimeStamp() {
    return timeStamp;
  }

  protected int getDifficulty() {
    return difficulty;
  }

  protected void setMerkleTree(MerkleTree merkleTree) {
    this.merkleTree = merkleTree;
  }

  protected void setPreviousHash(String previousHash) {
    this.previousHash = previousHash;
  }

  protected void setTimeStamp(String timeStamp) {
    this.timeStamp = timeStamp;
  }

  protected void updateByteSize(int delta) {
    byteSize += delta;
  }


  protected void zeroNonce() {
    nonce = 0;
  }

  protected void incNonce() {
    nonce++;
  }

  @Override
  public int getApproximateSize() {
    return byteSize;
  }
}
