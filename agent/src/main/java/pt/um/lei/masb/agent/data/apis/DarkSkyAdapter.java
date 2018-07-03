package pt.um.lei.masb.agent.data.apis;

import pt.um.lei.masb.blockchain.Transaction;

import java.util.Collection;

public class DarkSkyAdapter implements ApiAdapter {

    @Override
    public Collection<Transaction> extractTransactions() {
        return null;
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public Transaction next() {
        return null;
    }
}
