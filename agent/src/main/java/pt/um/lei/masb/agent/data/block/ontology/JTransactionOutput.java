package pt.um.lei.masb.agent.data.block.ontology;

import jade.content.Concept;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Set;

public class JTransactionOutput implements Concept {
    private String pubkey;
    private String hashId;
    private String prevHash;
    private String payout;
    private Set<String> tx;

    public JTransactionOutput(@NotNull String pubkey,
                              @NotNull String hashId,
                              @NotNull String prevHash,
                              @NotNull String payout,
                              @NotEmpty Set<String> tx) {
        this.pubkey = pubkey;
        this.hashId = hashId;
        this.prevHash = prevHash;
        this.payout = payout;
        this.tx = tx;
    }

    public @NotNull String getPubkey() {
        return pubkey;
    }

    public void setPubkey(@NotNull String pubkey) {
        this.pubkey = pubkey;
    }

    public @NotNull String getPrevHash() {
        return prevHash;
    }

    public void setPrevHash(@NotNull String prevHash) {
        this.prevHash = prevHash;
    }

    public @NotNull String getPayout() {
        return payout;
    }

    public void setPayout(@NotNull String payout) {
        this.payout = payout;
    }

    public @NotNull String getHashId() {
        return hashId;
    }

    public void setHashId(@NotNull String hashId) {
        this.hashId = hashId;
    }

    public @NotEmpty Set<String> getTx() {
        return tx;
    }

    public void setTx(@NotEmpty Set<String> tx) {
        this.tx = tx;
    }
}
