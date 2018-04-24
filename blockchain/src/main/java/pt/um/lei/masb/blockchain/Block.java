package pt.um.lei.masb.blockchain;

import org.openjdk.jol.info.ClassLayout;
import pt.um.lei.masb.blockchain.data.MerkleTree;
import pt.um.lei.masb.blockchain.stringutils.StringUtil;

import javax.persistence.*;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;


@Entity
public final class Block implements Sizeable {
    private static Block origin;
    private static int MAX_BLOCK_SIZE = 500;
    private static int MAX_MEM = 2097152;

    static {
        origin = new Block("0");
    }

    static Block getOrigin() {
        return origin;
    }


    @Id
    @GeneratedValue
    private long id;

    @OneToMany
    private final Transaction data[];

    @Transient
    private transient final String target;

    @Embedded
    private final BlockHeader hd;

    private int cur;
    private transient final long classSize = ClassLayout.parseClass(this.getClass()).instanceSize();
    private transient long headerSize;
    private transient long transactionsSize;

    private Block() {
        cur = -1;
        data = null;
        hd = null;
        target = null;
    }

    public Block(String previousHash, int difficulty) {
        this.hd = new BlockHeader(previousHash, difficulty);
        this.data = new Transaction[MAX_BLOCK_SIZE];
        cur = 0;
        this.target = StringUtil.getDifficultyString(difficulty);
        headerSize = hd.getApproximateSize();
    }

    private Block(String s) {
        data = null;
        cur = -1;
        target = "0";
        hd = BlockHeader.getOrigin();
        headerSize = hd.getApproximateSize();
    }

    /**
     * Attempt one nonce calculation.
     *
     * @param invalidate Whether to invalidate the nonce and MerkleTree in case block has changed.
     * @param time       Whether to invalidate block calculations due to timestamp (every couple of seconds).
     * @return Whether the block was successfully mined.
     */
    public boolean attemptMineBlock(boolean invalidate, boolean time) {
        boolean res = false;
        //Can't mine origin block.
        if (this == origin) {
            return res;
        }
        if (invalidate && time) {
            hd.setMerkleTree(MerkleTree.buildMerkleTree(data, cur));
            hd.setTimeStamp(ZonedDateTime.now(ZoneOffset.UTC).toString());
            hd.zeroNonce();
            headerSize = hd.getApproximateSize();
        } else if (invalidate) {
            hd.setMerkleTree(MerkleTree.buildMerkleTree(data, cur));
            hd.zeroNonce();
            headerSize = hd.getApproximateSize();
        } else if (time) {
            hd.setTimeStamp(ZonedDateTime.now(ZoneOffset.UTC).toString());
            hd.zeroNonce();
        }
        hd.updateHash();
        if (hd.getHash().substring(0, hd.getDifficulty()).equals(target)) {
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
    public boolean addTransaction(Transaction transaction) {
        //process transaction and check if valid, unless block is genesis block then ignore.
        if (transaction == null) {
            return false;
        }

        var transactionSize = transaction.getApproximateSize();
        if (transactionsSize + headerSize + classSize + transactionSize < MAX_MEM) {
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

    public String getTimeStamp() {
        return hd.getTimeStamp();
    }

    public int getDifficulty() {
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
     * 1. You need to calculate the size after deserialization
     * 2. You need to calculate the size after retrieval
     * of a block from a database.
     */
    public void resetApproximateSize() {
        headerSize = hd.getApproximateSize();
        transactionsSize = Arrays.stream(data)
                                 .limit(cur)
                                 .mapToLong(Transaction::getApproximateSize)
                                 .sum();
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
