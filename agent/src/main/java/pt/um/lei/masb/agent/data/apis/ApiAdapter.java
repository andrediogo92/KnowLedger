package pt.um.lei.masb.agent.data.apis;

import pt.um.lei.masb.blockchain.Transaction;

import java.util.Collection;
import java.util.Iterator;

public interface ApiAdapter extends Iterator<Transaction> {
    Collection<Transaction> extractTransactions();
}
