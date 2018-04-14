package pt.um.lei.masb.blockchain;

import pt.um.lei.masb.blockchain.data.MerkleTree;
import pt.um.lei.masb.blockchain.stringutils.StringUtil;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Observable;


public final class Block extends Observable {
    private static Block origin;
    private static int MAX_BLOCK_SIZE = 500;
    private static int MAX_MEM = 2097152;

    static {
        origin = new Block("0");
    }

    private final Transaction data[];
    private transient final String target;
    private BlockHeader hd;
    private int cur;

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
    }

    private Block(String s) {
        data = null;
        cur = -1;
        target = "0";
        hd = BlockHeader.getOrigin();
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
            int lastsize = hd.getMerkleTree().getApproximateSize();
            hd.setMerkleTree(MerkleTree.buildMerkleTree(data, cur));
            hd.updateByteSize(hd.getMerkleTree().getApproximateSize() - lastsize);
            hd.setTimeStamp(ZonedDateTime.now(ZoneOffset.UTC).toString());
            hd.zeroNonce();
        } else if (invalidate) {
            int lastsize = hd.getMerkleTree().getApproximateSize();
            hd.setMerkleTree(MerkleTree.buildMerkleTree(data, cur));
            hd.updateByteSize(hd.getMerkleTree().getApproximateSize() - lastsize);
            hd.zeroNonce();
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

        if (hd.getApproximateSize() + transaction.getApproximateSize() < MAX_MEM) {
            if (cur < MAX_BLOCK_SIZE) {
                if (transaction.processTransaction()) {
                    data[cur] = transaction;
                    cur++;
                    hd.updateByteSize(transaction.getApproximateSize());
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
