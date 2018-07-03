package pt.um.lei.masb.blockchain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.um.lei.masb.blockchain.persistance.BlockHeaderTransactions;
import pt.um.lei.masb.blockchain.persistance.BlockTransactions;
import pt.um.lei.masb.blockchain.utils.RingBuffer;
import pt.um.lei.masb.blockchain.utils.StringUtil;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigInteger;

@Entity
public final class BlockChain {
    private static final BlockChain blockChain = new BlockChain();
    //new BlockChainTransactions().getBlockChain()
    //                            .orElse(new BlockChain());
    private static final Logger LOGGER = LoggerFactory.getLogger(BlockChain.class);
    private static final long RECALC_TIME = 1228800;

    private static final int CACHE_SIZE = 40;
    private static final int RECALC_TRIGGER = 2048;

    @Id
    private final int id = 0;

    private transient final RingBuffer<Block> blockchain;
    @Basic(optional = false)
    private BigInteger difficultyTarget;

    @Basic(optional = false)
    private int lastRecalc;

    /**
     * Create a geographically unbounded blockchain.
     */
    protected BlockChain() {
        this.blockchain = new RingBuffer<>(CACHE_SIZE);
        var origin = Block.getOrigin();
/*        if (!new BlockHeaderTransactions().getBlockHeaderByHash(origin.getHash())
                                          .isPresent()) {
            new BlockTransactions().persistEntity(origin);
            blockchain.offer(origin);
        }
        */
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
     * @return Whether the chain is valid.
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
        var possible = blockchain.peek();
        if (possible == null) {
            return new BlockTransactions().getLatestBlock()
                                          .orElse(null);
        }
        return possible;
    }

    /**
     * @return The tail-end blockheader in the blockchain.
     */
    public BlockHeader getLastBlockHeader() {
        var possible = blockchain.peek();
        if (possible == null) {
            return new BlockTransactions().getLatestBlock().map(Block::getHeader)
                                          .orElse(null);
        } else {
            return possible.getHeader();
        }
    }


    /**
     * @param hash  Hash of block.
     * @return Block with provided hash if exists, else null.
     */
    public Block getBlock(@NotNull String hash) {
        return blockchain.stream()
                         .filter(h -> !h.getHash().equals(hash))
                         .findAny()
                         .orElse(new BlockTransactions().getBlockByHeaderHash(hash)
                                                        .orElse(null));
    }

    /**
     * @param blockheight Block height of block to fetch.
     * @return Block with provided blockheight, if it exists, else the null block.
     */
    public Block getBlockByHeight(long blockheight) {
        return blockchain.stream()
                         .filter(h -> h.getBlockHeight() != blockheight)
                         .findAny()
                         .orElse(new BlockTransactions().getBlockByBlockHeight(blockheight)
                                                        .orElse(null));
    }


    /**
     * @param hash  Hash of block.
     * @return Block with provided hash if exists, else null.
     */
    public BlockHeader getBlockHeaderByHash(@NotNull String hash) {
        return blockchain.stream()
                         .filter(h -> !h.getHash().equals(hash))
                         .map(Block::getHeader)
                         .findAny()
                         .orElse(new BlockHeaderTransactions().getBlockHeaderByHash(hash)
                                                              .orElse(null));
    }


    /**
     * @param hash  Hash of block header.
     * @return If a block with said header hash exists.
     */
    public boolean hasBlock(@NotNull String hash) {
        return blockchain.stream()
                         .anyMatch(h -> !h.getHash().equals(hash))
                || new BlockTransactions().getBlockByHeaderHash(hash).isPresent();
    }

    /**
     *
     * @param hash  Hash of block.
     * @return The previous block to the one with the
     *              provided hash if exists, else null.
     */
    public Block getPrevBlock(@NotNull String hash) {
        return blockchain.stream()
                         .filter(h -> !h.getHash().equals(hash))
                         .findAny()
                         .orElse(new BlockTransactions().getBlockByPrevHeaderHash(hash)
                                                        .orElse(null));
    }


    /**
     * @param hash Hash of block.
     * @return The previous block to the one with the
     * provided hash if exists, else null.
     */
    public BlockHeader getPrevBlockHeaderByHash(@NotNull String hash) {
        return blockchain.stream()
                         .filter(h -> !h.getHash().equals(hash))
                         .map(Block::getHeader)
                         .findAny()
                         //.orElse(new BlockHeaderTransactions().getBlockHeaderByPrevHeaderHash(hash)
                         .orElse(null);//);
    }

    /**
     * Add Block to blockchain if block is valid.
     *
     * <p>
     * May trigger difficulty recalculation.
     *
     * TODO: Verify coinbase.
     *
     * @param b Block to add
     * @return Whether block was successfully added.
     */
    public boolean addBlock(@NotNull Block b) {
        if (b.getPreviousHash().equals(getLastBlock().getHash())) {
            if (new BigInteger(b.getHash()).compareTo(b.getDifficulty()) < 1) {
                if (b.verifyTransactions()) {
                    if (lastRecalc == RECALC_TRIGGER) {
                        recalculateDifficulty(b);
                        lastRecalc = 0;
                    } else {
                        lastRecalc++;
                    }
                    return blockchain.add(b) &&
                            new BlockTransactions().persistEntity(b);
                }
            }
        }
        return false;
    }

    /**
     * Difficulty is recalculated based on timestamp difference between
     * block at current blockheight and block at current blockheight - RECALC_TRIGGER.
     * <p>
     * This difference is measured as a percentage of RECALC_TIME which is used to multiply
     * by current difficulty target.
     */
    private void recalculateDifficulty(@NotNull Block b) {
        var cmp = b.getBlockHeight();
        var stamp1 = b.getTimeStamp()
                      .getEpochSecond();
        var b2 = new BlockTransactions().getBlockByBlockHeight(b.getBlockHeight() - 2048);
        b2.ifPresentOrElse(bl -> {
            var stamp2 = bl.getTimeStamp()
                           .getEpochSecond();
            var delta = new BigInteger("" + (stamp1 - stamp2) * 1000000 / RECALC_TIME);
            difficultyTarget = difficultyTarget.multiply(delta)
                                               .divide(new BigInteger("1000000"));
        }, () -> LOGGER.error("Difficulty retrigger without 2048 blocks existent"));
    }

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
     * @param prevHash  Hash of block to reference as previous in chain.
     * @return A newly created empty block.
     */
    public Block newBlock(@NotEmpty String prevHash) {
        return new Block(prevHash, difficultyTarget, getBlock(prevHash).getBlockHeight() + 1);
    }

}
