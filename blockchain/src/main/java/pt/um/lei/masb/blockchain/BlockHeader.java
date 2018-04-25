package pt.um.lei.masb.blockchain;

import org.openjdk.jol.info.ClassLayout;
import pt.um.lei.masb.blockchain.data.MerkleTree;
import pt.um.lei.masb.blockchain.utils.Crypter;
import pt.um.lei.masb.blockchain.utils.StringUtil;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.math.BigInteger;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Entity
public final class BlockHeader implements Sizeable {
    private static BlockHeader origin;
    private static Crypter crypter = StringUtil.getDefaultCrypter();

    static {
        origin = new BlockHeader("0");
    }

    // Difficulty is fixed at block generation time.
    @Basic(optional = false)
    private final BigInteger difficulty;

    @Id
    private String hash;

    @Basic(optional = false)
    private String merkleRoot;

    @Basic(optional = false)
    private String previousHash;

    @Basic(optional = false)
    private String timeStamp;

    @Basic(optional = false)
    private int nonce;

    /**
     * Origin block specialty constructor
     *
     * @param origin special string "0"
     */
    private BlockHeader(String origin) {
        hash = origin;
        merkleRoot = null;
        previousHash = "";
        timeStamp = ZonedDateTime.of(2018, 3, 13, 0, 0, 0, 0, ZoneOffset.UTC)
                                 .toString();
        nonce = 0;
        difficulty = BigInteger.ZERO;
    }

    BlockHeader(String previousHash, BigInteger difficulty) {
        nonce = 0;
        this.difficulty = difficulty;
        this.timeStamp = ZonedDateTime.now(ZoneOffset.UTC).toString();
        this.previousHash = previousHash;
        merkleRoot = null;
        hash = null; //Making sure we do this after we set the other values.
    }

    protected BlockHeader() {
        this.difficulty = null;
    }

    static BlockHeader getOrigin() {
        return origin;
    }

    /**
     * Hash is a SHA-256 calculated from previous hash, nonce, timestamp,
     * {@link MerkleTree}'s root and each {@link Transaction}'s hash.
     *
     */
    protected void updateHash() {
        hash = calculateHash();
    }

    protected String calculateHash() {
        StringBuilder sb = new StringBuilder();
        sb.append(previousHash)
          .append(Long.toHexString(nonce))
          .append(timeStamp)
          .append(merkleRoot);
        return crypter.applyHash(sb.toString());
    }

    protected String getHash() {
        return hash;
    }


    protected String getMerkleRoot() {
        return merkleRoot;
    }

    protected void setMerkleRoot(String merkleRoot) {
        this.merkleRoot = merkleRoot;
    }

    protected String getPreviousHash() {
        return previousHash;
    }

    protected void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    protected String getTimeStamp() {
        return timeStamp;
    }

    protected void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    protected BigInteger getDifficulty() {
        return difficulty;
    }


    protected void zeroNonce() {
        nonce = 0;
    }

    protected void incNonce() {
        nonce++;
    }

    @Override
    public long getApproximateSize() {
        return ClassLayout.parseClass(this.getClass()).instanceSize();
    }

    public String toString() {
        var sb = new StringBuilder();
        sb.append("Header : [")
          .append(System.lineSeparator())
          .append("difficulty: ")
          .append(difficulty)
          .append(System.lineSeparator())
          .append("prevHash: ")
          .append(previousHash)
          .append(System.lineSeparator())
          .append("Hash: ")
          .append(hash)
          .append(System.lineSeparator())
          .append("Time: ")
          .append(timeStamp)
          .append(System.lineSeparator())
          .append(']')
          .append(System.lineSeparator());
        return sb.toString();
    }
}
