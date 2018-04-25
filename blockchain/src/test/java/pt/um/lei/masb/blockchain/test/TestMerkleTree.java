package pt.um.lei.masb.blockchain.test;

import org.junit.jupiter.api.Test;
import pt.um.lei.masb.blockchain.Ident;
import pt.um.lei.masb.blockchain.Transaction;
import pt.um.lei.masb.blockchain.data.MerkleNode;
import pt.um.lei.masb.blockchain.data.MerkleTree;
import pt.um.lei.masb.blockchain.data.SensorData;
import pt.um.lei.masb.blockchain.data.TemperatureData;
import pt.um.lei.masb.blockchain.utils.Crypter;
import pt.um.lei.masb.blockchain.utils.StringUtil;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestMerkleTree {

    @Test
    void testMerkleTreeBalanced() {
        Ident[] id = new Ident[]{new Ident(), new Ident()};
        Transaction ts[] = new Transaction[]{
                new Transaction(id[0].getPublicKey(), new SensorData(new TemperatureData()), new ArrayList<>()),
                new Transaction(id[1].getPublicKey(), new SensorData(new TemperatureData()), new ArrayList<>()),
                new Transaction(id[0].getPublicKey(), new SensorData(new TemperatureData()), new ArrayList<>()),
                new Transaction(id[1].getPublicKey(), new SensorData(new TemperatureData()), new ArrayList<>()),
                new Transaction(id[0].getPublicKey(), new SensorData(new TemperatureData()), new ArrayList<>()),
                new Transaction(id[1].getPublicKey(), new SensorData(new TemperatureData()), new ArrayList<>()),
                new Transaction(id[0].getPublicKey(), new SensorData(new TemperatureData()), new ArrayList<>()),
                new Transaction(id[1].getPublicKey(), new SensorData(new TemperatureData()), new ArrayList<>())
        };
        Crypter cp = StringUtil.getDefaultCrypter();
        MerkleTree tree = MerkleTree.buildMerkleTree(ts, ts.length);
        assertNotNull(tree.getRoot());
        MerkleNode root = tree.getRoot();
        assertEquals(root.getLeft().getLeft().getLeft().getHash(), ts[0].getTransactionId());
        assertEquals(root.getLeft().getLeft().getHash(),
                     cp.applyHash(ts[0].getTransactionId() + ts[1].getTransactionId()));
        assertEquals(root.getLeft().getHash(),
                     cp.applyHash(cp.applyHash(ts[0].getTransactionId() + ts[1].getTransactionId()) +
                                          cp.applyHash(ts[2].getTransactionId() + ts[3].getTransactionId())));
    }

    @Test
    void testMerkleTreeUnbalanced() {
        Ident[] id = new Ident[]{new Ident(), new Ident()};
        Transaction ts[] = new Transaction[]{
                new Transaction(id[0].getPublicKey(), new SensorData(new TemperatureData()), new ArrayList<>()),
                new Transaction(id[1].getPublicKey(), new SensorData(new TemperatureData()), new ArrayList<>()),
                new Transaction(id[0].getPublicKey(), new SensorData(new TemperatureData()), new ArrayList<>()),
                new Transaction(id[1].getPublicKey(), new SensorData(new TemperatureData()), new ArrayList<>()),
                new Transaction(id[0].getPublicKey(), new SensorData(new TemperatureData()), new ArrayList<>()),
                new Transaction(id[1].getPublicKey(), new SensorData(new TemperatureData()), new ArrayList<>())
        };
        Crypter cp = StringUtil.getDefaultCrypter();
        MerkleTree tree = MerkleTree.buildMerkleTree(ts, ts.length);
        assertNotNull(tree.getRoot());
        MerkleNode root = tree.getRoot();
        assertEquals(root.getLeft().getLeft().getLeft().getHash(), ts[0].getTransactionId());
        assertEquals(root.getLeft().getLeft().getHash(),
                     cp.applyHash(ts[0].getTransactionId() + ts[1].getTransactionId()));
        assertEquals(root.getLeft().getHash(),
                     cp.applyHash(cp.applyHash(ts[0].getTransactionId() + ts[1].getTransactionId()) +
                                          cp.applyHash(ts[2].getTransactionId() + ts[3].getTransactionId())));
        assertEquals(root.getRight().getHash(), root.getRight().getLeft().getHash());
        assertNull(root.getRight().getRight());
    }

    @Test
    void testMerkleTreeJustRoot() {
        Ident id = new Ident();
        Transaction ts[] = new Transaction[]{
                new Transaction(id.getPublicKey(), new SensorData(new TemperatureData()), new ArrayList<>())
        };
        MerkleTree tree = MerkleTree.buildMerkleTree(ts, ts.length);
        Crypter cp = StringUtil.getDefaultCrypter();
        assertNotNull(tree.getRoot());
        MerkleNode root = tree.getRoot();
        assertEquals(root.getHash(), ts[0].getTransactionId());
    }
}
