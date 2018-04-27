package pt.um.lei.masb.blockchain;

import org.openjdk.jol.info.ClassLayout;
import pt.um.lei.masb.blockchain.data.MerkleTree;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;


public final class Block implements Sizeable {
    private static Block origin = new Block("0");
    private static int MAX_BLOCK_SIZE = 500;
    private static int MAX_MEM = 2097152;

    private MerkleTree merkleTree;
    private final Transaction data[];
    private final Coinbase coinbase;
    private BlockHeader hd;
    private int cur;

    private transient final long classSize = ClassLayout.parseClass(this.getClass()).instanceSize();
    private transient long headerSize;
    private transient long transactionsSize;

    //Consider only the class size contribution to size.
    //Makes the total block size in the possible ballpark of 2MB + merkleTree graph size.
    private transient long merkleTreeSize = ClassLayout.parseClass(merkleTree.getClass()).instanceSize();


    Block() {
        cur = -1;
        data = null;
        hd = null;
        merkleTree = null;
        coinbase = null;
    }

    Block(@NotEmpty String previousHash,
          @NotNull BigInteger difficulty) {
        this.hd = new BlockHeader(previousHash, difficulty);
        this.data = new Transaction[MAX_BLOCK_SIZE];
        cur = 0;
        headerSize = hd.getApproximateSize();
        this.merkleTree = null;
        coinbase = new Coinbase();
    }

    private Block(String s) {
        data = null;
        cur = -1;
        hd = BlockHeader.getOrigin();
        headerSize = hd.getApproximateSize();
        this.merkleTree = null;
        coinbase = null;
    }

    static Block getOrigin() {
        return origin;
    }

    /**
     * Attempt one nonce calculation.
     *
     * @param invalidate Whether to invalidate the nonce and MerkleTree in case block has changed.
     * @param time       Whether to invalidate block calculations due to timestamp (every couple of seconds).
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
            hd.setMerkleRoot(merkleTree.getRoot().getHash());
            hd.setTimeStamp(ZonedDateTime.now(ZoneOffset.UTC).toInstant());
            hd.zeroNonce();
        } else if (invalidate) {
            merkleTree = MerkleTree.buildMerkleTree(data, cur);
            hd.setMerkleRoot(merkleTree.getRoot().getHash());
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
     * Checks if block is sized correctly.
     * Checks if the transaction is valid.
     *
     * @param transaction to add.
     * @return whether transaction was valid.
     */
    public boolean addTransaction(@NotNull Transaction transaction) {
        var transactionSize = transaction.getApproximateSize();
        if (transactionsSize + headerSize + classSize + merkleTreeSize + transactionSize < MAX_MEM) {
            if (cur < MAX_BLOCK_SIZE) {
                if (transaction.processTransaction()) {
                    data[cur] = transaction;
                    cur++;
                    transactionsSize += transactionSize;
                    System.out.println("Transaction Successfully added to Block");
                    return true;
                }
            }
        }
        System.out.println("Transaction failed to process. Discarded.");
        return false;
    }

    public String getHash() {
        return hd.getHash();
    }

    public Transaction[] getData() {
        return data;
    }

    public String getPreviousHash() {
        return hd.getPreviousHash();
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

    @Override
    public long getApproximateSize() {
        return classSize + transactionsSize + headerSize;
    }

    /**
     * Recalculates the entire block size.
     *
     * Is somewhat time consuming and only necessary if:
     *
     * 1. You need to calculate the effective block size after deserialization
     * 2. You need to calculate the effective block size after retrieval
     * of a block from a database.
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
}
