package pt.um.lei.masb.agent;
import jade.core.behaviours.Behaviour;
import pt.um.lei.masb.blockchain.Block;
import pt.um.lei.masb.blockchain.BlockChain;

import java.util.Queue;


public class Mining extends Behaviour {

    private BlockChain bc;
    private Queue<Block> blockQueue;
    private Block block;
    private boolean mining;

    public Mining(BlockChain bc, Queue<Block> blockQueue){
        this.bc=bc;
        mining=true;
        this.blockQueue = blockQueue;
    }

    @Override
    public void action() {
        //just for debugging
        while (mining){
            if(block==null) {
                if(blockQueue.isEmpty()) {
                    mining = false;
                }
                else {
                    block = blockQueue.remove();
                    mining = !block.attemptMineBlock(false, false);
                }
            }
            else {
                mining = !block.attemptMineBlock(false, false);
            }
        }
        block = null;
    }


    @Override
    public boolean done() {
        System.out.println("Finished mining");
        return true;
    }
}
