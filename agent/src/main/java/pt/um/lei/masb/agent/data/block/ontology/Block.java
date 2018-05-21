package pt.um.lei.masb.agent.data.block.ontology;

import jade.content.Concept;
import pt.um.lei.masb.agent.data.transaction.ontology.Transaction;
import pt.um.lei.masb.blockchain.Coinbase;

public final class Block implements Concept {
    private Transaction data[];
    private Coinbase coinbase;
    private BlockHeader header;

    public Block(Transaction[] data, Coinbase coinbase, BlockHeader header) {
        this.data = data;
        this.coinbase = coinbase;
        this.header = header;
    }

    public Transaction[] getData() {
        return data;
    }

    public void setData(Transaction[] data) {
        this.data = data;
    }

    public Coinbase getCoinbase() {
        return coinbase;
    }

    public void setCoinbase(Coinbase coinbase) {
        this.coinbase = coinbase;
    }

    public BlockHeader getHeader() {
        return header;
    }

    public void setHeader(BlockHeader header) {
        this.header = header;
    }
}
