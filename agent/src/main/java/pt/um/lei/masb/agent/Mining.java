package pt.um.lei.masb.agent;
import jade.core.behaviours.Behaviour;
import pt.um.lei.masb.blockchain.BlockChain;


public class Mining extends Behaviour {

    private BlockChain bc;
    private boolean mining,init;

    public Mining(BlockChain bc){
        this.bc=bc;
        mining=true;
        init=true;
    }

    @Override
    public void action() {
        //just for debugging
        if (init){
            System.out.println("Mining...");
            init=false;
        }
        mining=!bc.getLastBlock().attemptMineBlock(false, false);
    }


    @Override
    public boolean done() {
        if (!mining)System.out.println("Finished mining");
        return mining==false;
    }
}
