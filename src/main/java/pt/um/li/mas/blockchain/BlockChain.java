package pt.um.li.mas.blockchain;

import java.util.ArrayList;

public class BlockChain {
    private static final int INIT_SIZE = 1000;
    private final ArrayList<Block> blockchain;

    public BlockChain() {
        this.blockchain = new ArrayList<>(INIT_SIZE);
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


}
