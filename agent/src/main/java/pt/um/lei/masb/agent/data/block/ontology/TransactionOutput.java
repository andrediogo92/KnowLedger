package pt.um.lei.masb.agent.data.block.ontology;

import jade.content.Concept;

import javax.validation.constraints.NotEmpty;

public class TransactionOutput implements Concept {
    private String pubkey;
    private String prevHash;

    public TransactionOutput(@NotEmpty String pubkey,
                             @NotEmpty String prevHash) {
        this.pubkey = pubkey;
        this.prevHash = prevHash;
    }

    public @NotEmpty String getPubkey() {
        return pubkey;
    }

    public void setPubkey(@NotEmpty String pubkey) {
        this.pubkey = pubkey;
    }

    public @NotEmpty String getPrevHash() {
        return prevHash;
    }

    public void setPrevHash(@NotEmpty String prevHash) {
        this.prevHash = prevHash;
    }
}
