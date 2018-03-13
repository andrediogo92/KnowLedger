package pt.um.lei.masb.agent;
import jade.core.behaviours.Behaviour;
import pt.um.lei.masb.blockchain.BlockChain;


public class Mining extends Behaviour {

    private BlockChain bc;

    public Mining(BlockChain bc){
        this.bc=bc;
    }

    @Override
    public void action() {
        System.out.println("Mining...");
        bc.getLastBlock().attemptMineBlock(false,false);
        System.out.println("Finished mining");
    }


    @Override
    public boolean done() {
        return true;
    }
}
