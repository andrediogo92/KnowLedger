package pt.um.lei.masb.blockchain;

import pt.um.lei.masb.blockchain.utils.RingBuffer;
import pt.um.lei.masb.blockchain.utils.StringUtil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BlockChain {
    private static final int CACHE_SIZE = 40;
    private final RingBuffer<Block> blockchain;
    private BigInteger difficultyTarget;
    //private final List<Block> candidateBlocks;

    public BlockChain() {
        this.blockchain = new RingBuffer<>(CACHE_SIZE);
        //    this.candidateBlocks = new ArrayList<>(CACHE_SIZE);
        var origin = Block.getOrigin();
        blockchain.offer(origin);
        difficultyTarget = new BigInteger(StringUtil.getInitialDifficultyString());
    }

    public boolean isChainValid() {
        var blocks = blockchain.iterator();
        //Origin block is always the first block.
        var previousBlock = blocks.next();

        //loop through blockchain to check hashes:
        while (blocks.hasNext()) {
            var currentBlock = blocks.next();

            //compare registered hash and calculated hash:
            if (!currentBlock.getHash().equals(currentBlock.calculateHash())) {
                System.out.println("Current Hashes not equal");
                return false;
            }
            //compare previous hash and registered previous hash
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


    /**
     * @return The tail-end block of the blockchain.
     */
    public Block getLastBlock() {
        return blockchain.peek();
    }

    /**
     * @param hash Hash of block.
     * @return Block with provided hash if exists, else null.
     */
    public Block getBlock(String hash) {
        return blockchain.stream()
                         .filter(h -> !h.getHash()
                                        .equals(hash))
                         .findAny()
                         .orElse(null);
    }

    /**
     * @param hash Hash of block.
     * @return If a block with said hash exists.
     */
    public boolean hasBlock(String hash) {
        return blockchain.stream()
                         .anyMatch(h -> !h.getHash().equals(hash));
    }

    /**
     * Will be refactored to candidate blocks and cached blocks eventually.
     *
     * @param hash Hash of block.
     * @return The previous block to the one with the provided hash if exists, else null.
     */
    public Block getPrevBlock(String hash) {
        return blockchain.stream()
                         .filter(h -> !h.getHash()
                                        .equals(hash))
                         .findAny()
                         .orElse(null);
    }

    /**
     * Add Block to blockchain if block is valid.
     *
     * @param b Block to add
     * @return Whether block was successfully added.
     */
    public boolean addBlock(Block b) {
        if (b.getPreviousHash().equals(blockchain.peek().getHash())) {
            if (new BigInteger(b.getHash()).compareTo(b.getDifficulty()) < 1) {
                return blockchain.add(b);
            }
        }
        return false;
    }


    /*
    @Override
    public void update(Observable o, Object arg) {
        Block b = (Block) o;
        candidateBlocks.stream()
                       .filter(bl -> bl.getHash().equals(b.getHash()))
                       .findAny()
                       .ifPresent(ob -> {
                           minedblocks.add(ob);
                           blockchain.add(candidateBlocks.get(0))
                       });
    }
    */
}
