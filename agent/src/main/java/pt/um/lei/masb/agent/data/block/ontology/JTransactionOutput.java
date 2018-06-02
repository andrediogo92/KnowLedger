package pt.um.lei.masb.agent.data.block.ontology;

import jade.content.Concept;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class JTransactionOutput implements Concept {
    private String pubkey;
    private String prevHash;
    private String payout;

    public JTransactionOutput(@NotEmpty String pubkey,
                              @NotEmpty String prevHash,
                              @NotNull String payout) {
        this.pubkey = pubkey;
        this.prevHash = prevHash;
        this.payout = payout;
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

    public String getPayout() {
        return payout;
    }

    public void setPayout(String payout) {
        this.payout = payout;
    }
}
