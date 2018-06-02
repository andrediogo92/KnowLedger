package pt.um.lei.masb.blockchain;

import org.openjdk.jol.info.ClassLayout;
import pt.um.lei.masb.blockchain.data.MerkleTree;
import pt.um.lei.masb.blockchain.utils.Crypter;
import pt.um.lei.masb.blockchain.utils.StringUtil;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.math.BigInteger;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;

@NamedQueries({
                      @NamedQuery(
                              name = "blockheader_by_height",
                              query = "SELECT b from BlockHeader b where blockheight = :height"
                      )
              })
@Entity
public final class BlockHeader implements Sizeable {
    private final static BlockHeader origin = new BlockHeader(null);
    private final static Crypter crypter = StringUtil.getDefaultCrypter();

    // Difficulty is fixed at block generation time.
    @Basic(optional = false)
    private final BigInteger difficulty;

    @OneToOne(cascade = CascadeType.ALL,
              fetch = FetchType.LAZY,
              optional = false,
              mappedBy = "blockid",
              orphanRemoval = true)
    private Block block;

    @Column(unique = true,
            nullable = false)
    private long blockheight;

    @Id
    private String hash;

    @Basic(optional = false)
    private String merkleRoot;

    @Basic(optional = false)
    private String previousHash;

    @Basic(optional = false)
    private Instant timeStamp;

    @Basic(optional = false)
    private long nonce;

    /**
     * Origin block specialty constructor
     *
     * @param v Null parameter for special constructor.
     */
    private BlockHeader(@Null Void v) {
        hash = "0";
        merkleRoot = "0";
        previousHash = "";
        blockheight = 0;
        timeStamp = ZonedDateTime.of(2018, 3, 13, 0, 0, 0, 0, ZoneOffset.UTC)
                                 .toInstant();
        nonce = 0;
        difficulty = BigInteger.ZERO;
    }

    BlockHeader(@NotNull String previousHash,
                @NotNull BigInteger difficulty,
                long blockheight) {
        nonce = 0;
        this.difficulty = difficulty;
        this.timeStamp = Instant.now();
        this.previousHash = previousHash;
        this.blockheight = blockheight;
        merkleRoot = null;
        hash = null; //Making sure we do this after we set the other values.
    }

    public BlockHeader(@NotNull BigInteger difficulty,
                       long blockheight,
                       @NotEmpty String hash,
                       @NotNull String merkleRoot,
                       @NotNull String previousHash,
                       @NotNull Instant timeStamp,
                       long nonce) {
        this.difficulty = difficulty;
        this.block = block;
        this.blockheight = blockheight;
        this.hash = hash;
        this.merkleRoot = merkleRoot;
        this.previousHash = previousHash;
        this.timeStamp = timeStamp;
        this.nonce = nonce;
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
    void updateHash() {
        hash = calculateHash();
    }

    @NotNull String calculateHash() {
        String sb = previousHash +
                Long.toHexString(nonce) +
                timeStamp +
                merkleRoot;
        return crypter.applyHash(sb);
    }

    public boolean setBlockReferenceOnce(Block b) {
        if (this.block == null) {
            //We're dealing with the exact same pointer.
            if (b.getHeader() == this) {
                this.block = b;
                return true;
            }
        }
        return false;
    }

    public String getHash() {
        return hash;
    }

    public long getBlockheight() {
        return blockheight;
    }

    public String getMerkleRoot() {
        return merkleRoot;
    }

    void setMerkleRoot(@NotEmpty String merkleRoot) {
        this.merkleRoot = merkleRoot;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public Instant getTimeStamp() {
        return timeStamp;
    }

    void setTimeStamp(@NotNull Instant timeStamp) {
        this.timeStamp = timeStamp;
    }

    public BigInteger getDifficulty() {
        return difficulty;
    }

    public long getNonce() {
        return nonce;
    }

    void zeroNonce() {
        nonce = 0;
    }

    void incNonce() {
        nonce++;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BlockHeader that = (BlockHeader) o;
        return blockheight == that.blockheight &&
                nonce == that.nonce &&
                Objects.equals(difficulty, that.difficulty) &&
                Objects.equals(block, that.block) &&
                Objects.equals(hash, that.hash) &&
                Objects.equals(merkleRoot, that.merkleRoot) &&
                Objects.equals(previousHash, that.previousHash) &&
                Objects.equals(timeStamp, that.timeStamp);
    }

    @Override
    public int hashCode() {

        return Objects.hash(difficulty, block, blockheight, hash, merkleRoot, previousHash, timeStamp, nonce);
    }

    @Override
    public long getApproximateSize() {
        return ClassLayout.parseClass(this.getClass()).instanceSize();
    }

    public @NotEmpty String toString() {
        String sb = "Header : [" +
                System.lineSeparator() +
                "difficulty: " +
                difficulty +
                System.lineSeparator() +
                "prevHash: " +
                previousHash +
                System.lineSeparator() +
                "Hash: " +
                hash +
                System.lineSeparator() +
                "Time: " +
                timeStamp +
                System.lineSeparator() +
                ']' +
                System.lineSeparator();
        return sb;
    }

}
