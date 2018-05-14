package pt.um.lei.masb.blockchain;

import org.openjdk.jol.info.ClassLayout;
import pt.um.lei.masb.blockchain.data.MerkleTree;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;

@NamedQueries({
                      @NamedQuery(
                              name = "get_block_by_height",
                              query = "SELECT b FROM Block b WHERE b.hd.blockheight = :height"
                      )
                      ,
                      @NamedQuery(
                              name = "get_block_by_hash",
                              query = "SELECT b FROM Block b WHERE b.hd.hash = :hash"
                      )
                      ,
                      @NamedQuery(
                              name = "get_prev_block_by_hash",
                              query = "SELECT b FROM Block b WHERE b.hd.previousHash = :hash"
                      )
                      ,
                      @NamedQuery(
                              name = "get_latest_block",
                              query = "SELECT b FROM Block b WHERE b.hd.blockheight = (SELECT MAX(b2.blockheight) FROM BlockHeader b2)"
                      )
              })
@Entity(name = "Block")
public final class Block implements Sizeable {
    private static final Block origin = new Block(null);
    private static final int MAX_BLOCK_SIZE = 500;
    private static final int MAX_MEM = 2097152;

    @OneToMany(cascade = CascadeType.ALL,
               fetch = FetchType.EAGER,
               orphanRemoval = true)
    private final Transaction data[];
    @OneToOne(cascade = CascadeType.ALL,
              fetch = FetchType.EAGER,
              optional = false,
              orphanRemoval = true)
    private final Coinbase coinbase;
    @MapsId
    @OneToOne(cascade = CascadeType.ALL,
              fetch = FetchType.EAGER,
              optional = false,
              orphanRemoval = true)
    private final BlockHeader hd;
    @Id
    @GeneratedValue
    private long blockid;
    @OneToOne(cascade = CascadeType.ALL,
              fetch = FetchType.EAGER,
              optional = false,
              orphanRemoval = true)
    private MerkleTree merkleTree;


    @Basic(optional = false)
    private int cur;


    @Transient
    private transient final long classSize = ClassLayout.parseClass(this.getClass()).instanceSize();

    @Transient
    private transient long headerSize;

    @Transient
    private transient long transactionsSize;

    //Consider only the class size contribution to size.
    //Makes the total block size in the possible ballpark of 2MB + merkleTree graph size.
    @Transient
    private transient long merkleTreeSize = ClassLayout.parseClass(merkleTree.getClass()).instanceSize();

    private Block(Void v) {
        data = null;
        cur = -1;
        hd = BlockHeader.getOrigin();
        headerSize = hd.getApproximateSize();
        this.merkleTree = null;
        coinbase = null;
    }

    public static int getMaxBlockSize() {
        return MAX_BLOCK_SIZE;
    }

    protected Block() {
        cur = -1;
        data = null;
        hd = null;
        merkleTree = null;
        coinbase = null;
    }

    Block(@NotEmpty String previousHash,
          @NotNull BigInteger difficulty,
          long blockheight) {
        this.hd = new BlockHeader(previousHash, difficulty, blockheight);
        this.data = new Transaction[MAX_BLOCK_SIZE];
        cur = 0;
        headerSize = hd.getApproximateSize();
        this.merkleTree = null;
        coinbase = new Coinbase();
    }

    public static int getMaxMem() {
        return MAX_MEM;
    }

    static Block getOrigin() {
        return origin;
    }

    /**
     * Attempt one nonce calculation.
     *
     * @param invalidate    Whether to invalidate the nonce and MerkleTree
     *                      in case block has changed.
     * @param time          Whether to invalidate block calculations due to
     *                      timestamp (every couple of seconds).
     * @return Whether the block was successfully mined.
     */
    public boolean attemptMineBlock(boolean invalidate, boolean time) {
        //Can't mine origin block.
        if (this == origin) {
            return false;
        }
        boolean res = false;
        if (invalidate && time) {
            merkleTree = MerkleTree.buildMerkleTree(data, cur);
            hd.setMerkleRoot(merkleTree.getRoot());
            hd.setTimeStamp(ZonedDateTime.now(ZoneOffset.UTC).toInstant());
            hd.zeroNonce();
        } else if (invalidate) {
            merkleTree = MerkleTree.buildMerkleTree(data, cur);
            hd.setMerkleRoot(merkleTree.getRoot());
            hd.zeroNonce();
        } else if (time) {
            hd.setTimeStamp(ZonedDateTime.now(ZoneOffset.UTC).toInstant());
            hd.zeroNonce();
        }
        hd.updateHash();
        if (new BigInteger(hd.getHash()).compareTo(hd.getDifficulty()) < 1) {
            res = true;
            System.out.println("Block Mined!!! : " + hd.getHash());
            System.out.println("Block contains: " + toString());
        } else {
            hd.incNonce();
        }
        return res;
    }

    /**
     * Add a single new transaction.
     * <p>
     * Checks if block is sized correctly.
     * <p>
     * Checks if the transaction is valid.
     *
     * @param transaction   Transaction to attempt to add to the block.
     * @return Whether the transaction was valid and cprrectly inserted.
     */
    public boolean addTransaction(@NotNull Transaction transaction) {
        var transactionSize = transaction.getApproximateSize();
        if (transactionsSize + headerSize + classSize + merkleTreeSize + transactionSize < MAX_MEM) {
            if (cur < MAX_BLOCK_SIZE) {
                if (transaction.processTransaction()) {
                    insertSorted(transaction);
                    transactionsSize += transactionSize;
                    System.out.println("Transaction Successfully added to Block");
                    return true;
                }
            }
        }
        System.out.println("Transaction failed to process. Discarded.");
        return false;
    }

    /**
     * Transactions are sorted in descending order of data timestamp.
     *
     * @param transaction Transaction to insert in descending order.
     */
    private void insertSorted(Transaction transaction) {
        var i = cur - 1;
        for (; i >= 0 && transaction.getSensorData()
                                    .getTimestamp()
                                    .isBefore(data[i].getSensorData()
                                                     .getTimestamp()); i--) {
            data[i + 1] = data[i];       // shift elements forward
        }
        data[i + 1] = transaction;
    }

    public String getHash() {
        return hd.getHash();
    }

    public Transaction[] getData() {
        return Arrays.copyOf(data, cur, Transaction[].class);
    }

    public String getPreviousHash() {
        return hd.getPreviousHash();
    }

    public long getBlockid() {
        return blockid;
    }

    public long getBlockHeight() {
        return hd.getBlockheight();
    }

    public Instant getTimeStamp() {
        return hd.getTimeStamp();
    }

    public BigInteger getDifficulty() {
        return hd.getDifficulty();
    }

    public String calculateHash() {
        return hd.calculateHash();
    }

    public Coinbase getCoinbase() {
        return coinbase;
    }

    @Override
    public long getApproximateSize() {
        return classSize + transactionsSize + headerSize;
    }

    /**
     * Recalculates the entire block size.
     * <p>
     * Is somewhat time consuming and only necessary if:
     * <ol>
     *  <li>    You need to calculate the effective block size after deserialization
     *  <li>    You need to calculate the effective block size after retrieval
     *          of a block from a database.
     * </ol>
     */
    public void resetApproximateSize() {
        headerSize = hd.getApproximateSize();
        transactionsSize = Arrays.stream(data)
                                 .limit(cur)
                                 .mapToLong(Transaction::getApproximateSize)
                                 .sum();
        merkleTreeSize = merkleTree.getApproximateSize();
    }


    public String toString() {
        var sb = new StringBuilder();
        sb.append('{')
          .append(' ')
          .append(hd.toString())
          .append("Transactions: [")
          .append(System.lineSeparator());
        for (int i = 0; i < cur; i++) {
            System.out.println(data[i]);
            sb.append(data[i].toString());
        }
        sb.append(" ]")
          .append(System.lineSeparator()).append('}');
        return sb.toString();
    }

    public boolean verifyTransactions() {
        return merkleTree.verifyBlockTransactions(data, cur);
    }
}
