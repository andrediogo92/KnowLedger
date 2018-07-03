package pt.um.lei.masb.agent.messaging.block.ontology;

import jade.content.Concept;
import pt.um.lei.masb.agent.messaging.transaction.ontology.JTransaction;

import java.util.List;

public final class JBlock implements Concept {
    private List<JTransaction> data;
    private JCoinbase coinbase;
    private JBlockHeader header;
    private JMerkleTree merkleTree;

    public JBlock(List<JTransaction> data,
                  JCoinbase coinbase,
                  JBlockHeader header,
                  JMerkleTree merkleTree) {
        this.data = data;
        this.coinbase = coinbase;
        this.header = header;
        this.merkleTree = merkleTree;
    }

    public JMerkleTree getMerkleTree() {
        return merkleTree;
    }

    public void setMerkleTree(JMerkleTree merkleTree) {
        this.merkleTree = merkleTree;
    }

    public List<JTransaction> getData() {
        return data;
    }

    public void setData(List<JTransaction> data) {
        this.data = data;
    }

    public JCoinbase getCoinbase() {
        return coinbase;
    }

    public void setCoinbase(JCoinbase coinbase) {
        this.coinbase = coinbase;
    }

    public JBlockHeader getHeader() {
        return header;
    }

    public void setHeader(JBlockHeader header) {
        this.header = header;
    }
}
