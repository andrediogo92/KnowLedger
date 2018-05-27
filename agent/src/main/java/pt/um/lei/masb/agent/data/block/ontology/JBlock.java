package pt.um.lei.masb.agent.data.block.ontology;

import jade.content.Concept;
import pt.um.lei.masb.agent.data.transaction.ontology.JTransaction;
import pt.um.lei.masb.blockchain.Coinbase;

public final class JBlock implements Concept {
    private JTransaction data[];
    private Coinbase coinbase;
    private JBlockHeader header;

    public JBlock(JTransaction[] data, Coinbase coinbase, JBlockHeader header) {
        this.data = data;
        this.coinbase = coinbase;
        this.header = header;
    }

    public JTransaction[] getData() {
        return data;
    }

    public void setData(JTransaction[] data) {
        this.data = data;
    }

    public Coinbase getCoinbase() {
        return coinbase;
    }

    public void setCoinbase(Coinbase coinbase) {
        this.coinbase = coinbase;
    }

    public JBlockHeader getHeader() {
        return header;
    }

    public void setHeader(JBlockHeader header) {
        this.header = header;
    }
}
