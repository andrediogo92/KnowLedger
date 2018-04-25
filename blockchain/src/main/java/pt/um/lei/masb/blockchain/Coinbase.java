package pt.um.lei.masb.blockchain;

import java.util.ArrayList;
import java.util.List;

/**
 * The coinbase transaction. Pays out to contributors to the blockchain.
 */
public final class Coinbase implements Sizeable {
    private TransactionInput coinbase;
    private List<TransactionOutput> payoutTXO;

    /**
     * The coinbase will be continually updated
     * to reflect changes to the block.
     */
    public Coinbase() {
        coinbase = new TransactionInput();
        payoutTXO = new ArrayList<>();
    }

    public List<TransactionOutput> getPayoutTXO() {
        return payoutTXO;
    }

    public TransactionInput getCoinbase() {
        return coinbase;
    }

    /**
     * TODO: Calculate the new payout.
     * @param newT Transaction to contribute to payout.
     * @param latestKnown Transaction to compare for fluctuation.
     */
    protected void addToInput(Transaction newT, Transaction latestKnown) {

    }

    /**
     * TODO: Calculate new outputs based on transaction.
     * @param t Transaction to register in outputs.
     */
    protected void addToOutputs(Transaction t) {

    }
}
