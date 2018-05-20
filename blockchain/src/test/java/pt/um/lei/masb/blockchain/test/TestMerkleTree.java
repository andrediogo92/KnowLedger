package pt.um.lei.masb.blockchain.test;

import org.junit.jupiter.api.Test;
import pt.um.lei.masb.blockchain.Ident;
import pt.um.lei.masb.blockchain.Transaction;
import pt.um.lei.masb.blockchain.data.MerkleTree;
import pt.um.lei.masb.blockchain.data.SensorData;
import pt.um.lei.masb.blockchain.data.TUnit;
import pt.um.lei.masb.blockchain.data.TemperatureData;
import pt.um.lei.masb.blockchain.utils.Crypter;
import pt.um.lei.masb.blockchain.utils.StringUtil;

import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class TestMerkleTree {

    private Transaction[] makeXTransactions(@Size(min = 2, max = 2)
                                                    Ident[] id,
                                            int X,
                                            boolean addNulls) {
        Random r = new Random();
        Transaction ts[];
        int size;
        if (addNulls) {
            size = X * 3;
            ts = new Transaction[size];
        } else {
            size = X;
            ts = new Transaction[size];
        }
        for (int i = 0; i < size; i++) {
            if (i % 2 == 0) {
                ts[i] = new Transaction(id[0].getPrivateKey(),
                                        id[0].getPublicKey(),
                                        new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                           TUnit.CELSIUS,
                                                                           new BigDecimal(0),
                                                                           new BigDecimal(0))));
            } else {
                ts[i] = new Transaction(id[1].getPrivateKey(),
                                        id[1].getPublicKey(),
                                        new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                           TUnit.CELSIUS,
                                                                           new BigDecimal(0),
                                                                           new BigDecimal(0))));
            }
            if (addNulls) {
                i++;
                ts[i] = null;
                i++;
                ts[i] = null;
            }
        }
        return ts;
    }

    @Test
    void testMerkleTreeBalanced() {
        Ident[] id = new Ident[]{new Ident(), new Ident()};
        Random r = new Random();
        var ts = makeXTransactions(id, 8, false);
        Crypter cp = StringUtil.getDefaultCrypter();
        MerkleTree tree = MerkleTree.buildMerkleTree(ts, ts.length);
        //Root is present
        assertNotNull(tree.getRoot());
        String[] nakedTree = tree.getCollapsedTree();
        //Three levels to the left is first transaction.
        assertEquals(nakedTree[7], ts[0].getHashId());
        assertEquals(nakedTree[8], ts[1].getHashId());
        assertEquals(nakedTree[9], ts[2].getHashId());
        assertEquals(nakedTree[10], ts[3].getHashId());
        assertEquals(nakedTree[11], ts[4].getHashId());
        assertEquals(nakedTree[12], ts[5].getHashId());
        assertEquals(nakedTree[13], ts[6].getHashId());
        assertEquals(nakedTree[14], ts[7].getHashId());
        //Two levels in to the left is a hash of transaction 1 + 2.
        assertEquals(nakedTree[3],
                     cp.applyHash(ts[0].getHashId() + ts[1].getHashId()));
        assertEquals(nakedTree[4],
                     cp.applyHash(ts[2].getHashId() + ts[3].getHashId()));
        assertEquals(nakedTree[5],
                     cp.applyHash(ts[4].getHashId() + ts[5].getHashId()));
        assertEquals(nakedTree[6],
                     cp.applyHash(ts[6].getHashId() + ts[7].getHashId()));
        //One level to the left is a hash of the hash of transactions 1 + 2 + hash of transactions 3 + 4
        assertEquals(nakedTree[1],
                     cp.applyHash(cp.applyHash(ts[0].getHashId() + ts[1].getHashId()) +
                                          cp.applyHash(ts[2].getHashId() + ts[3].getHashId())));
        //One level to the left is a hash of the hash of transactions 1 + 2 + hash of transactions 3 + 4
        assertEquals(nakedTree[2],
                     cp.applyHash(cp.applyHash(ts[4].getHashId() + ts[5].getHashId()) +
                                          cp.applyHash(ts[6].getHashId() + ts[7].getHashId())));
        assertEquals(tree.getRoot(),
                     cp.applyHash(
                             cp.applyHash(
                                     cp.applyHash(ts[0].getHashId() + ts[1].getHashId()) +
                                             cp.applyHash(ts[2].getHashId() + ts[3].getHashId())) +
                                     cp.applyHash(
                                             cp.applyHash(ts[4].getHashId() + ts[5].getHashId()) +
                                                     cp.applyHash(ts[6].getHashId() + ts[7].getHashId())))
                    );
    }

    @Test
    void testMerkleTreeUnbalanced() {
        Ident[] id = new Ident[]{new Ident(), new Ident()};
        Random r = new Random();
        var ts = makeXTransactions(id, 6, false);
        Crypter cp = StringUtil.getDefaultCrypter();
        MerkleTree tree = MerkleTree.buildMerkleTree(ts, ts.length);
        //Root is present
        assertNotNull(tree.getRoot());
        String[] nakedTree = tree.getCollapsedTree();
        //Three levels to the left is first transaction.
        assertEquals(nakedTree[6], ts[0].getHashId());
        assertEquals(nakedTree[7], ts[1].getHashId());
        assertEquals(nakedTree[8], ts[2].getHashId());
        assertEquals(nakedTree[9], ts[3].getHashId());
        assertEquals(nakedTree[10], ts[4].getHashId());
        assertEquals(nakedTree[11], ts[5].getHashId());
        //Two levels in to the left is a hash of transaction 1 + 2.
        assertEquals(nakedTree[3],
                     cp.applyHash(ts[0].getHashId() + ts[1].getHashId()));
        assertEquals(nakedTree[4],
                     cp.applyHash(ts[2].getHashId() + ts[3].getHashId()));
        assertEquals(nakedTree[5],
                     cp.applyHash(ts[4].getHashId() + ts[5].getHashId()));
        //One level to the left is a hash of the hash of transactions 1 + 2 + hash of transactions 3 + 4
        assertEquals(nakedTree[1],
                     cp.applyHash(cp.applyHash(ts[0].getHashId() + ts[1].getHashId()) +
                                          cp.applyHash(ts[2].getHashId() + ts[3].getHashId())));
        //One level to the right is a hash of the hash of transactions 1 + 2 * 2
        assertEquals(nakedTree[2],
                     cp.applyHash(cp.applyHash(ts[4].getHashId() + ts[5].getHashId()) +
                                          cp.applyHash(ts[4].getHashId() + ts[5].getHashId())));
        //Root is everything else.
        assertEquals(tree.getRoot(),
                     cp.applyHash(
                             cp.applyHash(
                                     cp.applyHash(ts[0].getHashId() + ts[1].getHashId()) +
                                             cp.applyHash(ts[2].getHashId() + ts[3].getHashId())) +
                                     cp.applyHash(
                                             cp.applyHash(ts[4].getHashId() + ts[5].getHashId()) +
                                                     cp.applyHash(ts[4].getHashId() + ts[5].getHashId())))
                    );
    }

    @Test
    void testMerkleTreeJustRoot() {
        Ident id = new Ident();
        Random r = new Random();
        Transaction ts[] = new Transaction[]{
                new Transaction(id.getPrivateKey(),
                                id.getPublicKey(),
                                new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                      TUnit.CELSIUS,
                                                                                      new BigDecimal(0),
                                                                                      new BigDecimal(0))))
        };
        MerkleTree tree = MerkleTree.buildMerkleTree(ts, ts.length);
        assertNotNull(tree.getRoot());
        //Root matches the only transaction.
        assertEquals(tree.getRoot(), ts[0].getHashId());
        assertEquals(1, tree.getCollapsedTree().length);
    }


    @Test
    void testMerkleSparse() {
        Ident id[] = new Ident[]{new Ident(), new Ident()};
        Random r = new Random();
        var ts = makeXTransactions(id, 6, true);
        MerkleTree tree = MerkleTree.buildMerkleTree(ts, ts.length);
        //Root is present
        assertNotNull(tree.getRoot());
        String[] nakedTree = tree.getCollapsedTree();
        //Three levels to the left is first transaction.
        assertEquals(nakedTree[6], ts[0].getHashId());
        assertEquals(nakedTree[7], ts[3].getHashId());
        assertEquals(nakedTree[8], ts[6].getHashId());
        assertEquals(nakedTree[9], ts[9].getHashId());
        assertEquals(nakedTree[10], ts[12].getHashId());
        assertEquals(nakedTree[11], ts[15].getHashId());
        Crypter cp = StringUtil.getDefaultCrypter();
        //Two levels in to the left is a hash of transaction 1 + 2.
        assertEquals(nakedTree[3],
                     cp.applyHash(ts[0].getHashId() + ts[3].getHashId()));
        assertEquals(nakedTree[4],
                     cp.applyHash(ts[6].getHashId() + ts[9].getHashId()));
        assertEquals(nakedTree[5],
                     cp.applyHash(ts[12].getHashId() + ts[15].getHashId()));
        //One level to the left is a hash of the hash of transactions 1 + 2 + hash of transactions 3 + 4
        assertEquals(nakedTree[1],
                     cp.applyHash(cp.applyHash(ts[0].getHashId() + ts[3].getHashId()) +
                                          cp.applyHash(ts[6].getHashId() + ts[9].getHashId())));
        //One level to the right is a hash of the hash of transactions 1 + 2 * 2
        assertEquals(nakedTree[2],
                     cp.applyHash(cp.applyHash(ts[12].getHashId() + ts[15].getHashId()) +
                                          cp.applyHash(ts[12].getHashId() + ts[15].getHashId())));
        //Root is everything else.
        assertEquals(tree.getRoot(),
                     cp.applyHash(
                             cp.applyHash(
                                     cp.applyHash(ts[0].getHashId() + ts[3].getHashId()) +
                                             cp.applyHash(ts[6].getHashId() + ts[9].getHashId())) +
                                     cp.applyHash(
                                             cp.applyHash(ts[12].getHashId() + ts[15].getHashId()) +
                                                     cp.applyHash(
                                                             ts[12].getHashId() + ts[15].getHashId())))
                    );

    }


    @Test
    void testMerkleSingleVerification() {
        Ident id[] = new Ident[]{new Ident(), new Ident()};
        Random r = new Random();
        var ts = makeXTransactions(id, 8, false);
        MerkleTree tree = MerkleTree.buildMerkleTree(ts, ts.length);
        assertNotNull(tree.getRoot());
        assertTrue(tree.verifyTransaction(ts[0].getHashId()));
        assertTrue(tree.verifyTransaction(ts[1].getHashId()));
        assertTrue(tree.verifyTransaction(ts[2].getHashId()));
        assertTrue(tree.verifyTransaction(ts[3].getHashId()));
        assertTrue(tree.verifyTransaction(ts[4].getHashId()));
        assertTrue(tree.verifyTransaction(ts[5].getHashId()));
        ts = makeXTransactions(id, 5, false);
        tree = MerkleTree.buildMerkleTree(ts, ts.length);
        assertNotNull(tree.getRoot());
        assertTrue(tree.verifyTransaction(ts[0].getHashId()));
        assertTrue(tree.verifyTransaction(ts[1].getHashId()));
        assertTrue(tree.verifyTransaction(ts[2].getHashId()));
        assertTrue(tree.verifyTransaction(ts[3].getHashId()));
        assertTrue(tree.verifyTransaction(ts[4].getHashId()));
        ts = makeXTransactions(id, 7, false);
        tree = MerkleTree.buildMerkleTree(ts, ts.length);
        assertNotNull(tree.getRoot());
        assertTrue(tree.verifyTransaction(ts[0].getHashId()));
        assertTrue(tree.verifyTransaction(ts[1].getHashId()));
        assertTrue(tree.verifyTransaction(ts[2].getHashId()));
        assertTrue(tree.verifyTransaction(ts[3].getHashId()));
        assertTrue(tree.verifyTransaction(ts[4].getHashId()));
        assertTrue(tree.verifyTransaction(ts[5].getHashId()));
        assertTrue(tree.verifyTransaction(ts[6].getHashId()));
    }


    @Test
    void testMerkleAllVerification() {
        Ident id[] = new Ident[]{new Ident(), new Ident()};
        Random r = new Random();
        var ts = makeXTransactions(id, 6, false);
        MerkleTree tree = MerkleTree.buildMerkleTree(ts, ts.length);
        assertNotNull(tree.getRoot());
        assertTrue(tree.verifyBlockTransactions(ts, ts.length));
        ts = makeXTransactions(id, 5, true);
        tree = MerkleTree.buildMerkleTree(ts, ts.length);
        assertNotNull(tree.getRoot());
        assertTrue(tree.verifyBlockTransactions(ts, 5));
        ts = makeXTransactions(id, 7, false);
        tree = MerkleTree.buildMerkleTree(ts, ts.length);
        assertNotNull(tree.getRoot());
        assertTrue(tree.verifyBlockTransactions(ts, 7));
    }
}
