package pt.um.lei.masb.blockchain;

import org.openjdk.jol.info.ClassLayout;
import pt.um.lei.masb.blockchain.data.MerkleTree;
import pt.um.lei.masb.blockchain.utils.Crypter;
import pt.um.lei.masb.blockchain.utils.StringUtil;

import javax.validation.constraints.*;
import java.math.BigInteger;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public final class BlockHeader implements Sizeable {
    private static BlockHeader origin;
    private static Crypter crypter = StringUtil.getDefaultCrypter();

    static {
        origin = new BlockHeader("0");
    }

    private final BigInteger difficulty;
    private String hash;
    private String merkleRoot;
    private String previousHash;
    private Instant timeStamp;
    private int nonce;

    /**
     * Origin block specialty constructor
     *
     * @param origin special string "0"
     */
    private BlockHeader(@Pattern(regexp = "0") String origin) {
        hash = origin;
        merkleRoot = null;
        previousHash = "";
        timeStamp = ZonedDateTime.of(2018, 3, 13, 0, 0, 0, 0, ZoneOffset.UTC)
                                 .toInstant();
        nonce = 0;
        difficulty = BigInteger.ZERO;
    }

    BlockHeader(@NotNull String previousHash,
                @NotNull BigInteger difficulty) {
        nonce = 0;
        this.difficulty = difficulty;
        this.timeStamp = ZonedDateTime.now(ZoneOffset.UTC).toInstant();
        this.previousHash = previousHash;
        merkleRoot = null;
        hash = null; //Making sure we do this after we set the other values.
    }

    private BlockHeader() {
        this.difficulty = null;
    }

    static BlockHeader getOrigin() {
        return origin;
    }

    /**
     * Hash is a SHA-256 calculated from previous hash, nonce, timestamp,
     * {@link MerkleTree}'s root and each {@link Transaction}'s hash.
     *
     * @return The hash string.
     */
    void updateHash() {
        hash = calculateHash();
    }

    @NotNull String calculateHash() {
        StringBuilder sb = new StringBuilder();
        sb.append(previousHash)
          .append(Long.toHexString(nonce))
          .append(timeStamp)
          .append(merkleRoot);
        return crypter.applyHash(sb.toString());
    }

    String getHash() {
        return hash;
    }


    protected String getMerkleRoot() {
        return merkleRoot;
    }

    void setMerkleRoot(@NotEmpty String merkleRoot) {
        this.merkleRoot = merkleRoot;
    }

    String getPreviousHash() {
        return previousHash;
    }

    protected void setPreviousHash(@NotEmpty String previousHash) {
        this.previousHash = previousHash;
    }

    Instant getTimeStamp() {
        return timeStamp;
    }

    void setTimeStamp(@NotNull Instant timeStamp) {
        this.timeStamp = timeStamp;
    }

    BigInteger getDifficulty() {
        return difficulty;
    }

    void zeroNonce() {
        nonce = 0;
    }

    void incNonce() {
        nonce++;
    }

    @Override
    public long getApproximateSize() {
        return ClassLayout.parseClass(this.getClass()).instanceSize();
    }

    public @NotEmpty String toString() {
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
