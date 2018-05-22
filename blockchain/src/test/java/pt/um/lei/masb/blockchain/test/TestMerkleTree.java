package pt.um.lei.masb.blockchain.test;

import org.junit.jupiter.api.Test;
import pt.um.lei.masb.blockchain.Coinbase;
import pt.um.lei.masb.blockchain.Ident;
import pt.um.lei.masb.blockchain.Transaction;
import pt.um.lei.masb.blockchain.data.MerkleTree;
import pt.um.lei.masb.blockchain.data.SensorData;
import pt.um.lei.masb.blockchain.data.TUnit;
import pt.um.lei.masb.blockchain.data.TemperatureData;
import pt.um.lei.masb.blockchain.utils.StringUtil;

import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class TestMerkleTree {

    private List<Transaction> makeXTransactions(@Size(min = 2, max = 2)
                                                        Ident[] id,
                                                int X,
                                                boolean addNulls) {
        var r = new Random();
        List<Transaction> ts;
        int size;
        if (addNulls) {
            size = X * 3;
            ts = new ArrayList<>(size);
        } else {
            size = X;
            ts = new ArrayList<>(size);
        }
        for (int i = 0; i < size; i++) {
            if (i % 2 == 0) {
                ts.add(new Transaction(id[0].getPrivateKey(),
                                       id[0].getPublicKey(),
                                       new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                          TUnit.CELSIUS,
                                                                          new BigDecimal(0),
                                                                          new BigDecimal(0)))));
            } else {
                ts.add(new Transaction(id[1].getPrivateKey(),
                                       id[1].getPublicKey(),
                                       new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                          TUnit.CELSIUS,
                                                                          new BigDecimal(0),
                                                                          new BigDecimal(0)))));
            }
            if (addNulls) {
                i++;
                ts.add(null);
                i++;
                ts.add(null);
            }
        }
        return ts;
    }

    @Test
    void testMerkleTreeBalanced() {
        Ident[] id = new Ident[]{new Ident(), new Ident()};
        var r = new Random();
        var ts = makeXTransactions(id, 8, false);
        var cp = StringUtil.getDefaultCrypter();
        var tree = MerkleTree.buildMerkleTree(ts);
        //Root is present
        assertNotNull(tree.getRoot());
        List<String> nakedTree = tree.getCollapsedTree();
        //Three levels to the left is first transaction.
        assertEquals(nakedTree.get(7), ts.get(0).getHashId());
        assertEquals(nakedTree.get(8), ts.get(1).getHashId());
        assertEquals(nakedTree.get(9), ts.get(2).getHashId());
        assertEquals(nakedTree.get(10), ts.get(3).getHashId());
        assertEquals(nakedTree.get(11), ts.get(4).getHashId());
        assertEquals(nakedTree.get(12), ts.get(5).getHashId());
        assertEquals(nakedTree.get(13), ts.get(6).getHashId());
        assertEquals(nakedTree.get(14), ts.get(7).getHashId());
        //Two levels in to the left is a hash of transaction 1 + 2.
        assertEquals(nakedTree.get(3),
                     cp.applyHash(ts.get(0).getHashId() + ts.get(1).getHashId()));
        assertEquals(nakedTree.get(4),
                     cp.applyHash(ts.get(2).getHashId() + ts.get(3).getHashId()));
        assertEquals(nakedTree.get(5),
                     cp.applyHash(ts.get(4).getHashId() + ts.get(5).getHashId()));
        assertEquals(nakedTree.get(6),
                     cp.applyHash(ts.get(6).getHashId() + ts.get(7).getHashId()));
        //One level to the left is a hash of the hash of transactions 1 + 2 + hash of transactions 3 + 4
        assertEquals(nakedTree.get(1),
                     cp.applyHash(cp.applyHash(ts.get(0).getHashId() + ts.get(1).getHashId()) +
                                          cp.applyHash(ts.get(2).getHashId() + ts.get(3).getHashId())));
        //One level to the left is a hash of the hash of transactions 1 + 2 + hash of transactions 3 + 4
        assertEquals(nakedTree.get(2),
                     cp.applyHash(cp.applyHash(ts.get(4).getHashId() + ts.get(5).getHashId()) +
                                          cp.applyHash(ts.get(6).getHashId() + ts.get(7).getHashId())));
        assertEquals(tree.getRoot(),
                     cp.applyHash(
                             cp.applyHash(
                                     cp.applyHash(ts.get(0).getHashId() + ts.get(1).getHashId()) +
                                             cp.applyHash(ts.get(2).getHashId() + ts.get(3).getHashId())) +
                                     cp.applyHash(
                                             cp.applyHash(ts.get(4).getHashId() + ts.get(5).getHashId()) +
                                                     cp.applyHash(ts.get(6).getHashId() + ts.get(7).getHashId())))
                    );
    }

    @Test
    void testMerkleTreeUnbalanced() {
        Ident[] id = new Ident[]{new Ident(), new Ident()};
        var ts = makeXTransactions(id, 6, false);
        var cp = StringUtil.getDefaultCrypter();
        var tree = MerkleTree.buildMerkleTree(ts);
        //Root is present
        assertNotNull(tree.getRoot());
        List<String> nakedTree = tree.getCollapsedTree();
        //Three levels to the left is first transaction.
        assertEquals(nakedTree.get(6), ts.get(0).getHashId());
        assertEquals(nakedTree.get(7), ts.get(1).getHashId());
        assertEquals(nakedTree.get(8), ts.get(2).getHashId());
        assertEquals(nakedTree.get(9), ts.get(3).getHashId());
        assertEquals(nakedTree.get(10), ts.get(4).getHashId());
        assertEquals(nakedTree.get(11), ts.get(5).getHashId());
        //Two levels in to the left is a hash of transaction 1 + 2.
        assertEquals(nakedTree.get(3),
                     cp.applyHash(ts.get(0).getHashId() + ts.get(1).getHashId()));
        assertEquals(nakedTree.get(4),
                     cp.applyHash(ts.get(2).getHashId() + ts.get(3).getHashId()));
        assertEquals(nakedTree.get(5),
                     cp.applyHash(ts.get(4).getHashId() + ts.get(5).getHashId()));
        //One level to the left is a hash of the hash of transactions 1 + 2 + hash of transactions 3 + 4
        assertEquals(nakedTree.get(1),
                     cp.applyHash(
                             cp.applyHash(ts.get(0).getHashId() + ts.get(1).getHashId()) +
                                     cp.applyHash(ts.get(2).getHashId() + ts.get(3).getHashId())));
        //One level to the right is a hash of the hash of transactions 1 + 2 * 2
        assertEquals(nakedTree.get(2),
                     cp.applyHash(
                             cp.applyHash(ts.get(4).getHashId() + ts.get(5).getHashId()) +
                                     cp.applyHash(ts.get(4).getHashId() + ts.get(5).getHashId())));
        //Root is everything else.
        assertEquals(tree.getRoot(),
                     cp.applyHash(
                             cp.applyHash(
                                     cp.applyHash(ts.get(0).getHashId() + ts.get(1).getHashId()) +
                                             cp.applyHash(ts.get(2).getHashId() + ts.get(3).getHashId())) +
                                     cp.applyHash(
                                             cp.applyHash(ts.get(4).getHashId() + ts.get(5).getHashId()) +
                                                     cp.applyHash(ts.get(4).getHashId() + ts.get(5).getHashId())))
                    );
    }

    @Test
    void testMerkleTreeJustRoot() {
        var id = new Ident();
        var r = new Random();
        List<Transaction> ts = new ArrayList<>();
        ts.add(new Transaction(id.getPrivateKey(),
                               id.getPublicKey(),
                               new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                  TUnit.CELSIUS,
                                                                  new BigDecimal(0),
                                                                  new BigDecimal(0)))));
        var tree = MerkleTree.buildMerkleTree(ts);
        assertNotNull(tree.getRoot());
        //Root matches the only transaction.
        assertEquals(tree.getRoot(), ts.get(0).getHashId());
        assertEquals(1, tree.getCollapsedTree().size());
    }


    @Test
    void testMerkleSparse() {
        Ident id[] = new Ident[]{new Ident(), new Ident()};
        var ts = makeXTransactions(id, 6, true);
        var tree = MerkleTree.buildMerkleTree(ts);
        //Root is present
        assertNotNull(tree.getRoot());
        var nakedTree = tree.getCollapsedTree();
        //Three levels to the left is first transaction.
        assertEquals(nakedTree.get(6), ts.get(0).getHashId());
        assertEquals(nakedTree.get(7), ts.get(3).getHashId());
        assertEquals(nakedTree.get(8), ts.get(6).getHashId());
        assertEquals(nakedTree.get(9), ts.get(9).getHashId());
        assertEquals(nakedTree.get(10), ts.get(12).getHashId());
        assertEquals(nakedTree.get(11), ts.get(15).getHashId());
        var cp = StringUtil.getDefaultCrypter();
        //Two levels in to the left is a hash of transaction 1 + 2.
        assertEquals(nakedTree.get(3),
                     cp.applyHash(ts.get(0).getHashId() + ts.get(3).getHashId()));
        assertEquals(nakedTree.get(4),
                     cp.applyHash(ts.get(6).getHashId() + ts.get(9).getHashId()));
        assertEquals(nakedTree.get(5),
                     cp.applyHash(ts.get(12).getHashId() + ts.get(15).getHashId()));
        //One level to the left is a hash of the hash of transactions 1 + 2 + hash of transactions 3 + 4
        assertEquals(nakedTree.get(1),
                     cp.applyHash(cp.applyHash(ts.get(0).getHashId() + ts.get(3).getHashId()) +
                                          cp.applyHash(ts.get(6).getHashId() + ts.get(9).getHashId())));
        //One level to the right is a hash of the hash of transactions 1 + 2 * 2
        assertEquals(nakedTree.get(2),
                     cp.applyHash(cp.applyHash(ts.get(12).getHashId() + ts.get(15).getHashId()) +
                                          cp.applyHash(ts.get(12).getHashId() + ts.get(15).getHashId())));
        //Root is everything else.
        assertEquals(tree.getRoot(),
                     cp.applyHash(
                             cp.applyHash(
                                     cp.applyHash(ts.get(0).getHashId() + ts.get(3).getHashId()) +
                                             cp.applyHash(ts.get(6).getHashId() + ts.get(9).getHashId())) +
                                     cp.applyHash(
                                             cp.applyHash(ts.get(12).getHashId() + ts.get(15).getHashId()) +
                                                     cp.applyHash(ts.get(12).getHashId() + ts.get(15).getHashId())))
                    );

    }


    @Test
    void testMerkleSingleVerification() {
        Ident id[] = new Ident[]{new Ident(), new Ident()};
        var ts = makeXTransactions(id, 8, false);
        MerkleTree tree = MerkleTree.buildMerkleTree(ts);
        assertNotNull(tree.getRoot());
        assertTrue(tree.verifyTransaction(ts.get(0).getHashId()));
        assertTrue(tree.verifyTransaction(ts.get(1).getHashId()));
        assertTrue(tree.verifyTransaction(ts.get(2).getHashId()));
        assertTrue(tree.verifyTransaction(ts.get(3).getHashId()));
        assertTrue(tree.verifyTransaction(ts.get(4).getHashId()));
        assertTrue(tree.verifyTransaction(ts.get(5).getHashId()));
        ts = makeXTransactions(id, 5, false);
        tree = MerkleTree.buildMerkleTree(ts);
        assertNotNull(tree.getRoot());
        assertTrue(tree.verifyTransaction(ts.get(0).getHashId()));
        assertTrue(tree.verifyTransaction(ts.get(1).getHashId()));
        assertTrue(tree.verifyTransaction(ts.get(2).getHashId()));
        assertTrue(tree.verifyTransaction(ts.get(3).getHashId()));
        assertTrue(tree.verifyTransaction(ts.get(4).getHashId()));
        ts = makeXTransactions(id, 7, false);
        tree = MerkleTree.buildMerkleTree(ts);
        assertNotNull(tree.getRoot());
        assertTrue(tree.verifyTransaction(ts.get(0).getHashId()));
        assertTrue(tree.verifyTransaction(ts.get(1).getHashId()));
        assertTrue(tree.verifyTransaction(ts.get(2).getHashId()));
        assertTrue(tree.verifyTransaction(ts.get(3).getHashId()));
        assertTrue(tree.verifyTransaction(ts.get(4).getHashId()));
        assertTrue(tree.verifyTransaction(ts.get(5).getHashId()));
        assertTrue(tree.verifyTransaction(ts.get(6).getHashId()));
    }


    @Test
    void testMerkleAllVerification() {
        Ident id[] = new Ident[]{new Ident(), new Ident()};
        var c = new Coinbase();
        c.setHashId(c.calculateHash());
        var ts = makeXTransactions(id, 6, false);
        MerkleTree tree = MerkleTree.buildMerkleTree(c, ts);
        assertNotNull(tree.getRoot());
        assertTrue(tree.verifyBlockTransactions(c, ts));
        ts = makeXTransactions(id, 5, true);
        tree = MerkleTree.buildMerkleTree(ts);
        assertNotNull(tree.getRoot());
        assertTrue(tree.verifyBlockTransactions(c, ts));
        ts = makeXTransactions(id, 7, false);
        tree = MerkleTree.buildMerkleTree(ts);
        assertNotNull(tree.getRoot());
        assertTrue(tree.verifyBlockTransactions(c, ts));
    }


}
