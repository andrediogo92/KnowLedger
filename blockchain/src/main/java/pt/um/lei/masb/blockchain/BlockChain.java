package pt.um.lei.masb.blockchain;

import pt.um.lei.masb.blockchain.utils.RingBuffer;
import pt.um.lei.masb.blockchain.utils.StringUtil;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigInteger;

public final class BlockChain {
    private static final BlockChain blockChain = new BlockChain();

    private static final int CACHE_SIZE = 40;
    private static final int RECALC_TRIGGER = 2048;

    private transient final RingBuffer<Block> blockchain;
    private BigInteger difficultyTarget;
    private int lastRecalc;

    /**
     * Create a geographically unbounded blockchain.
     */
    protected BlockChain() {
        this.blockchain = new RingBuffer<>(CACHE_SIZE);
        //    this.candidateBlocks = new ArrayList<>(CACHE_SIZE);
        var origin = Block.getOrigin();
        blockchain.offer(origin);
        difficultyTarget = StringUtil.getInitialDifficulty();
        lastRecalc = 0;
    }

    public static BlockChain getInstance() {
        return blockChain;
    }

    /**
     * @return The current calculated difficulty target.
     */
    public BigInteger getDifficultyTarget() {
        return difficultyTarget;
    }


    /**
     * Checks integrity of the entire blockchain.
     * @return whether the chain is valid.
     */
    public boolean isChainValid() {
        var blocks = blockchain.iterator();
        // Origin block is always the first block.
        var previousBlock = blocks.next();

        // loop through blockchain to check hashes:
        while (blocks.hasNext()) {
            var currentBlock = blocks.next();

            // compare registered hash and calculated hash:
            if (!currentBlock.getHash().equals(currentBlock.calculateHash())) {
                System.out.println("Current Hashes not equal");
                return false;
            }
            // compare previous hash and registered previous hash
            if (!previousBlock.getHash().equals(currentBlock.getPreviousHash())) {
                System.out.println("Previous Hashes not equal");
                return false;
            }

            var hashTarget = currentBlock.getDifficulty();
            if (new BigInteger(currentBlock.getHash()).compareTo(hashTarget) > 0) {
                System.out.println("Unmined block: " + currentBlock.getHash());
                return false;
            }

            previousBlock = currentBlock;
        }
        return true;
    }

    /** @return The tail-end block of the blockchain. */
    public Block getLastBlock() {
        return blockchain.peek();
    }

    /**
     * @param hash Hash of block.
     * @return Block with provided hash if exists, else null.
     */
    public Block getBlock(@NotNull String hash) {
        return blockchain.stream()
                         .filter(h -> !h.getHash().equals(hash))
                         .findAny()
                         .orElse(null);
    }

    /**
     * @param hash Hash of block.
     * @return If a block with said hash exists.
     */
    public boolean hasBlock(@NotNull String hash) {
        return blockchain.stream()
                         .anyMatch(h -> !h.getHash().equals(hash));
    }

    /**
     * Will be refactored to candidate blocks and cached blocks eventually.
     *
     * @param hash Hash of block.
     * @return The previous block to the one with the provided hash if exists, else null.
     */
    public Block getPrevBlock(@NotNull String hash) {
        return blockchain.stream().filter(h -> !h.getHash().equals(hash)).findAny().orElse(null);
    }

    /**
     * Add Block to blockchain if block is valid.
     *
     * <p>May trigger difficulty recalculation.
     *
     * @param b Block to add
     * @return Whether block was successfully added.
     */
    public boolean addBlock(@NotNull Block b) {
        if (b.getPreviousHash().equals(blockchain.peek().getHash())) {
            if (new BigInteger(b.getHash()).compareTo(b.getDifficulty()) < 1) {
                if (lastRecalc == RECALC_TRIGGER) {
                    recalculateDifficulty();
                    lastRecalc = 0;
                } else {
                    lastRecalc++;
                }
                return blockchain.add(b);
            }
        }
        return false;
    }

    /** TODO: Implement difficulty recalculation. */
    private void recalculateDifficulty() {}

    /**
     * Creates new Block with appropriate difficulty target referencing the last known block.
     *
     * @return A newly created empty block.
     */
    public Block newBlock() {
        return new Block(getLastBlock().getHash(), difficultyTarget, getLastBlock().getBlockHeight() + 1);
    }

    /**
     * Creates new Block with appropriate difficulty target.
     *
     * @param prevHash hash of block to reference as previous in chain.
     * @return A newly created empty block.
     */
    public Block newBlock(@NotEmpty String prevHash) {
        return new Block(prevHash, difficultyTarget, getBlock(prevHash).getBlockHeight() + 1);
    }

}
