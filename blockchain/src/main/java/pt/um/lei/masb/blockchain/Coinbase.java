package pt.um.lei.masb.blockchain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * The coinbase transaction. Pays out to contributors to the blockchain.
 */
@Entity
public final class Coinbase implements Sizeable {
    @Embedded
    private final TransactionInput coinbase;
    @OneToMany()
    private final List<TransactionOutput> payoutTXO;
    @Id
    @GeneratedValue
    private long id;

    /**
     * The coinbase will be continually updated
     * to reflect changes to the block.
     */
    protected Coinbase() {
        coinbase = new TransactionInput();
        payoutTXO = new ArrayList<>();
    }

    public @NotNull List<TransactionOutput> getPayoutTXO() {
        return payoutTXO;
    }

    public @NotNull TransactionInput getCoinbase() {
        return coinbase;
    }

    /**
     * TODO: Calculate the new payout.
     * @param newT Transaction to contribute to payout.
     * @param latestKnown Transaction to compare for fluctuation.
     */
    protected void addToInput(@NotNull Transaction newT,
                              @NotNull Transaction latestKnown) {

    }

    /**
     * TODO: Calculate new outputs based on transaction.
     * @param t Transaction to register in outputs.
     */
    protected void addToOutputs(@NotNull Transaction t) {

    }
}
