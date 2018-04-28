package pt.um.lei.masb.blockchain.test;

import org.junit.jupiter.api.Test;
import pt.um.lei.masb.blockchain.Ident;
import pt.um.lei.masb.blockchain.Transaction;
import pt.um.lei.masb.blockchain.data.*;
import pt.um.lei.masb.blockchain.utils.Crypter;
import pt.um.lei.masb.blockchain.utils.StringUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestMerkleTree {

    @Test
    void testMerkleTreeBalanced() {
        Ident[] id = new Ident[]{new Ident(), new Ident()};
        Transaction ts[] = new Transaction[]{
                new Transaction(id[0].getPublicKey(), new SensorData(new TemperatureData(20, TUnit.CELSIUS))),
                new Transaction(id[1].getPublicKey(), new SensorData(new TemperatureData(20, TUnit.CELSIUS))),
                new Transaction(id[0].getPublicKey(), new SensorData(new TemperatureData(20, TUnit.CELSIUS))),
                new Transaction(id[1].getPublicKey(), new SensorData(new TemperatureData(20, TUnit.CELSIUS))),
                new Transaction(id[0].getPublicKey(), new SensorData(new TemperatureData(20, TUnit.CELSIUS))),
                new Transaction(id[1].getPublicKey(), new SensorData(new TemperatureData(20, TUnit.CELSIUS))),
                new Transaction(id[0].getPublicKey(), new SensorData(new TemperatureData(20, TUnit.CELSIUS))),
                new Transaction(id[1].getPublicKey(), new SensorData(new TemperatureData(20, TUnit.CELSIUS)))
        };
        Crypter cp = StringUtil.getDefaultCrypter();
        MerkleTree tree = MerkleTree.buildMerkleTree(ts, ts.length);
        //Root is present
        assertNotNull(tree.getRoot());
        MerkleNode root = tree.getRoot();
        //Three levels to the left is first transaction.
        assertEquals(root.getLeft().getLeft().getLeft().getHash(), ts[0].getTransactionId());
        //Two levels in to the left is a hash of transaction 1 + 2.
        assertEquals(root.getLeft().getLeft().getHash(),
                     cp.applyHash(ts[0].getTransactionId() + ts[1].getTransactionId()));
        //One level to the left is a hash of the hash of transactions 1 + 2 + hash of transactions 3 + 4
        assertEquals(root.getLeft().getHash(),
                     cp.applyHash(cp.applyHash(ts[0].getTransactionId() + ts[1].getTransactionId()) +
                                          cp.applyHash(ts[2].getTransactionId() + ts[3].getTransactionId())));
    }

    @Test
    void testMerkleTreeUnbalanced() {
        Ident[] id = new Ident[]{new Ident(), new Ident()};
        Transaction ts[] = new Transaction[]{
                new Transaction(id[0].getPublicKey(), new SensorData(new TemperatureData(20, TUnit.CELSIUS))),
                new Transaction(id[1].getPublicKey(), new SensorData(new TemperatureData(20, TUnit.CELSIUS))),
                new Transaction(id[0].getPublicKey(), new SensorData(new TemperatureData(20, TUnit.CELSIUS))),
                new Transaction(id[1].getPublicKey(), new SensorData(new TemperatureData(20, TUnit.CELSIUS))),
                new Transaction(id[0].getPublicKey(), new SensorData(new TemperatureData(20, TUnit.CELSIUS))),
                new Transaction(id[1].getPublicKey(), new SensorData(new TemperatureData(20, TUnit.CELSIUS)))
        };
        Crypter cp = StringUtil.getDefaultCrypter();
        MerkleTree tree = MerkleTree.buildMerkleTree(ts, ts.length);
        //Root is present
        assertNotNull(tree.getRoot());
        MerkleNode root = tree.getRoot();
        //Three levels to the left is first transaction.
        assertEquals(root.getLeft().getLeft().getLeft().getHash(), ts[0].getTransactionId());
        //Two levels in to the left is a hash of transaction 1 + 2.
        assertEquals(root.getLeft().getLeft().getHash(),
                     cp.applyHash(ts[0].getTransactionId() + ts[1].getTransactionId()));
        //One level to the left is a hash of the hash of transactions 1 + 2 + hash of transactions 3 + 4
        assertEquals(root.getLeft().getHash(),
                     cp.applyHash(cp.applyHash(ts[0].getTransactionId() + ts[1].getTransactionId()) +
                                          cp.applyHash(ts[2].getTransactionId() + ts[3].getTransactionId())));
        //One level to the right is a hash of the next level at the left and right (unbalanced duplicate)
        assertEquals(root.getRight().getHash(), cp.applyHash(root.getRight().getLeft().getHash() +
                                                                     root.getRight().getRight().getHash()));
        //One level to the right the hash of next level's left and right are really duplicates of each other.
        assertEquals(root.getRight().getHash(), cp.applyHash(root.getRight().getLeft().getHash() +
                                                                     root.getRight().getLeft().getHash()));
        //There should be three levels the the right.
        assertNotNull(root.getRight().getRight().getRight());

        //The leaf node to the rightmost is the same going through either parent (correct duplication).
        assertEquals(root.getRight().getRight().getRight().getHash(),
                     root.getRight().getLeft().getRight().getHash());
    }

    @Test
    void testMerkleTreeJustRoot() {
        Ident id = new Ident();
        Transaction ts[] = new Transaction[]{
                new Transaction(id.getPublicKey(), new SensorData(new TemperatureData(20, TUnit.CELSIUS)))
        };
        MerkleTree tree = MerkleTree.buildMerkleTree(ts, ts.length);
        assertNotNull(tree.getRoot());
        MerkleNode root = tree.getRoot();
        //Root matches the only transaction.
        assertEquals(root.getHash(), ts[0].getTransactionId());
    }
}
