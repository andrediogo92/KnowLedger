package pt.um.lei.masb.agent.messaging.block.ontology;

import jade.content.Concept;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Set;

public final class JCoinbase implements Concept {
    private Set<JTransactionOutput> payoutTXO;
    private String coinbase;
    private String hashId;


    public JCoinbase(@NotNull Set<JTransactionOutput> payoutTXO,
                     @NotEmpty String coinbase,
                     @NotEmpty String hashId) {
        this.payoutTXO = payoutTXO;
        this.coinbase = coinbase;
        this.hashId = hashId;
    }

    public @NotNull Set<JTransactionOutput> getPayoutTXO() {
        return payoutTXO;
    }

    public void setPayoutTXO(@NotNull Set<JTransactionOutput> payoutTXO) {
        this.payoutTXO = payoutTXO;
    }

    public @NotEmpty String getCoinbase() {
        return coinbase;
    }

    public void setCoinbase(@NotEmpty String coinbase) {
        this.coinbase = coinbase;
    }

    public @NotEmpty String getHashId() {
        return hashId;
    }

    public void setHashId(@NotEmpty String hashId) {
        this.hashId = hashId;
    }


}
