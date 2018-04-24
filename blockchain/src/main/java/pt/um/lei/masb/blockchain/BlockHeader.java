package pt.um.lei.masb.blockchain;

import org.openjdk.jol.info.ClassLayout;
import pt.um.lei.masb.blockchain.data.MerkleTree;
import pt.um.lei.masb.blockchain.stringutils.Crypter;
import pt.um.lei.masb.blockchain.stringutils.StringUtil;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.time.LocalDateTime;
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
    private final int difficulty;
    private String hash;

    @OneToOne
    private MerkleTree merkleTree;

    private String previousHash;
    private String timeStamp;
    private int nonce;

    /**
     * Origin block specialty constructor
     *
     * @param origin special string "0"
     */
    private BlockHeader(String origin) {
        hash = origin;
        merkleTree = null;
        previousHash = "";
        timeStamp = ZonedDateTime.of(2018, 3, 13, 0, 0, 0, 0, ZoneOffset.UTC)
                                 .toString();
        nonce = 0;
        difficulty = 1;
    }

    BlockHeader(String previousHash, int difficulty) {
        nonce = 0;
        this.difficulty = difficulty;
        this.timeStamp = ZonedDateTime.now(ZoneOffset.UTC).toString();
        this.previousHash = previousHash;
        merkleTree = null;
        hash = null; //Making sure we do this after we set the other values.
    }

    private BlockHeader() {
        this.difficulty = -1;
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
          .append(merkleTree.getRoot())
          .append(getApproximateSize());
        return crypter.applyHash(sb.toString());
    }

    protected String getHash() {
        return hash;
    }


    protected MerkleTree getMerkleTree() {
        return merkleTree;
    }

    protected void setMerkleTree(MerkleTree merkleTree) {
        this.merkleTree = merkleTree;
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

    protected int getDifficulty() {
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
        if(merkleTree != null) {
            return ClassLayout.parseClass(this.getClass()).instanceSize() + merkleTree.getApproximateSize();
        }
        else {
            return ClassLayout.parseClass(this.getClass()).instanceSize();
        }
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
