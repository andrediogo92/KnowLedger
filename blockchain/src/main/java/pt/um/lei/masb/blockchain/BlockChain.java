package pt.um.lei.masb.blockchain;

import pt.um.lei.masb.blockchain.stringutils.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class BlockChain {
    private static final int INIT_SIZE = 1000;
    private final List<Block> blockchain;

    public BlockChain() {
        this.blockchain = new ArrayList<>(INIT_SIZE);
        Block origin = Block.getOrigin();
        blockchain.add(origin);
    }

    public boolean isChainValid() {
        Block currentBlock;
        Block previousBlock;

        //loop through blockchain to check hashes:
        for(int i=1; i < blockchain.size(); i++) {
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i-1);
            String hashTarget = new String(new char[currentBlock.getDifficulty()]);
            hashTarget = hashTarget.replace('\0','0');

            //compare registered hash and calculated hash:
            if(!currentBlock.getHash().equals(currentBlock.calculateHash()) ){
                System.out.println("Current Hashes not equal");
                return false;
            }
            //compare previous hash and registered previous hash
            if(!previousBlock.getHash().equals(currentBlock.getPreviousHash()) ) {
                System.out.println("Previous Hashes not equal");
                return false;
            }
            if(!currentBlock.getHash()
                            .substring(0, currentBlock.getDifficulty())
                            .equals(hashTarget)) {
                System.out.println("Unmined block: " + i);
                return false;
            }
        }
        return true;
    }

    /**
     * @return The tail-end block of the blockchain.
     */
    public Block getLastBlock() {
        return blockchain.get(blockchain.size()-1);
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
        return blockchain.stream().anyMatch(h -> !h.getHash().equals(hash));
    }

    /**
     * Will be refactored to candidate blocks and cached blocks eventually.
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
     * @param b Block to add
     * @return Whether block was successfully added.
     */
    public boolean addBlock(Block b) {
        if(b.getPreviousHash().equals(blockchain.get(blockchain.size()-1).getHash())) {
            if(b.getHash()
                .substring( 0, b.getDifficulty())
                .equals(StringUtil.getDifficultyString(b.getDifficulty()))) {

                return blockchain.add(b);
            }
        }
        return false;
    }
}
