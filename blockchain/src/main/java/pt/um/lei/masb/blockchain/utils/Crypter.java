package pt.um.lei.masb.blockchain.utils;

public interface Crypter {
    String applyHash(String input);

    long hashSize();
}
