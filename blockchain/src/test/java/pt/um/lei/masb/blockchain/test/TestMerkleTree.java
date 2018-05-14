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

import java.math.BigDecimal;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class TestMerkleTree {

    @Test
    void testMerkleTreeBalanced() {
        Ident[] id = new Ident[]{new Ident(), new Ident()};
        Random r = new Random();
        Transaction ts[] = new Transaction[]{
                new Transaction(id[0].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                new Transaction(id[1].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                new Transaction(id[0].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                new Transaction(id[1].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                new Transaction(id[0].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                new Transaction(id[1].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                new Transaction(id[0].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                new Transaction(id[1].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0))))
        };
        Crypter cp = StringUtil.getDefaultCrypter();
        MerkleTree tree = MerkleTree.buildMerkleTree(ts, ts.length);
        //Root is present
        assertNotNull(tree.getRoot());
        String[] nakedTree = tree.getCollapsedTree();
        //Three levels to the left is first transaction.
        assertEquals(nakedTree[7], ts[0].getTransactionId());
        assertEquals(nakedTree[8], ts[1].getTransactionId());
        assertEquals(nakedTree[9], ts[2].getTransactionId());
        assertEquals(nakedTree[10], ts[3].getTransactionId());
        assertEquals(nakedTree[11], ts[4].getTransactionId());
        assertEquals(nakedTree[12], ts[5].getTransactionId());
        assertEquals(nakedTree[13], ts[6].getTransactionId());
        assertEquals(nakedTree[14], ts[7].getTransactionId());
        //Two levels in to the left is a hash of transaction 1 + 2.
        assertEquals(nakedTree[3],
                     cp.applyHash(ts[0].getTransactionId() + ts[1].getTransactionId()));
        assertEquals(nakedTree[4],
                     cp.applyHash(ts[2].getTransactionId() + ts[3].getTransactionId()));
        assertEquals(nakedTree[5],
                     cp.applyHash(ts[4].getTransactionId() + ts[5].getTransactionId()));
        assertEquals(nakedTree[6],
                     cp.applyHash(ts[6].getTransactionId() + ts[7].getTransactionId()));
        //One level to the left is a hash of the hash of transactions 1 + 2 + hash of transactions 3 + 4
        assertEquals(nakedTree[1],
                     cp.applyHash(cp.applyHash(ts[0].getTransactionId() + ts[1].getTransactionId()) +
                                          cp.applyHash(ts[2].getTransactionId() + ts[3].getTransactionId())));
        //One level to the left is a hash of the hash of transactions 1 + 2 + hash of transactions 3 + 4
        assertEquals(nakedTree[2],
                     cp.applyHash(cp.applyHash(ts[4].getTransactionId() + ts[5].getTransactionId()) +
                                          cp.applyHash(ts[6].getTransactionId() + ts[7].getTransactionId())));
        assertEquals(tree.getRoot(),
                     cp.applyHash(
                             cp.applyHash(
                                     cp.applyHash(ts[0].getTransactionId() + ts[1].getTransactionId()) +
                                             cp.applyHash(ts[2].getTransactionId() + ts[3].getTransactionId())) +
                                     cp.applyHash(
                                             cp.applyHash(ts[4].getTransactionId() + ts[5].getTransactionId()) +
                                                     cp.applyHash(ts[6].getTransactionId() + ts[7].getTransactionId())))
                    );
    }

    @Test
    void testMerkleTreeUnbalanced() {
        Ident[] id = new Ident[]{new Ident(), new Ident()};
        Random r = new Random();
        Transaction ts[] = new Transaction[]{
                new Transaction(id[0].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                new Transaction(id[1].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                new Transaction(id[0].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                new Transaction(id[1].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                new Transaction(id[0].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                new Transaction(id[1].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0))))
        };
        Crypter cp = StringUtil.getDefaultCrypter();
        MerkleTree tree = MerkleTree.buildMerkleTree(ts, ts.length);
        //Root is present
        assertNotNull(tree.getRoot());
        String[] nakedTree = tree.getCollapsedTree();
        //Three levels to the left is first transaction.
        assertEquals(nakedTree[6], ts[0].getTransactionId());
        assertEquals(nakedTree[7], ts[1].getTransactionId());
        assertEquals(nakedTree[8], ts[2].getTransactionId());
        assertEquals(nakedTree[9], ts[3].getTransactionId());
        assertEquals(nakedTree[10], ts[4].getTransactionId());
        assertEquals(nakedTree[11], ts[5].getTransactionId());
        //Two levels in to the left is a hash of transaction 1 + 2.
        assertEquals(nakedTree[3],
                     cp.applyHash(ts[0].getTransactionId() + ts[1].getTransactionId()));
        assertEquals(nakedTree[4],
                     cp.applyHash(ts[2].getTransactionId() + ts[3].getTransactionId()));
        assertEquals(nakedTree[5],
                     cp.applyHash(ts[4].getTransactionId() + ts[5].getTransactionId()));
        //One level to the left is a hash of the hash of transactions 1 + 2 + hash of transactions 3 + 4
        assertEquals(nakedTree[1],
                     cp.applyHash(cp.applyHash(ts[0].getTransactionId() + ts[1].getTransactionId()) +
                                          cp.applyHash(ts[2].getTransactionId() + ts[3].getTransactionId())));
        //One level to the right is a hash of the hash of transactions 1 + 2 * 2
        assertEquals(nakedTree[2],
                     cp.applyHash(cp.applyHash(ts[4].getTransactionId() + ts[5].getTransactionId()) +
                                          cp.applyHash(ts[4].getTransactionId() + ts[5].getTransactionId())));
        //Root is everything else.
        assertEquals(tree.getRoot(),
                     cp.applyHash(
                             cp.applyHash(
                                     cp.applyHash(ts[0].getTransactionId() + ts[1].getTransactionId()) +
                                             cp.applyHash(ts[2].getTransactionId() + ts[3].getTransactionId())) +
                                     cp.applyHash(
                                             cp.applyHash(ts[4].getTransactionId() + ts[5].getTransactionId()) +
                                                     cp.applyHash(ts[4].getTransactionId() + ts[5].getTransactionId())))
                    );
    }

    @Test
    void testMerkleTreeJustRoot() {
        Ident id = new Ident();
        Random r = new Random();
        Transaction ts[] = new Transaction[]{
                new Transaction(id.getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                      TUnit.CELSIUS,
                                                                                      new BigDecimal(0),
                                                                                      new BigDecimal(0))))
        };
        MerkleTree tree = MerkleTree.buildMerkleTree(ts, ts.length);
        assertNotNull(tree.getRoot());
        //Root matches the only transaction.
        assertEquals(tree.getRoot(), ts[0].getTransactionId());
        assertEquals(1, tree.getCollapsedTree().length);
    }


    @Test
    void testMerkleSparse() {
        Ident id[] = new Ident[]{new Ident(), new Ident()};
        Random r = new Random();
        Transaction ts[] = new Transaction[]{
                new Transaction(id[0].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                null,
                null,
                new Transaction(id[1].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                null,
                null,
                new Transaction(id[0].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                null,
                null,
                new Transaction(id[1].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                null,
                null,
                new Transaction(id[0].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                null,
                null,
                new Transaction(id[1].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0))))
        };
        MerkleTree tree = MerkleTree.buildMerkleTree(ts, ts.length);
        //Root is present
        assertNotNull(tree.getRoot());
        String[] nakedTree = tree.getCollapsedTree();
        //Three levels to the left is first transaction.
        assertEquals(nakedTree[6], ts[0].getTransactionId());
        assertEquals(nakedTree[7], ts[3].getTransactionId());
        assertEquals(nakedTree[8], ts[6].getTransactionId());
        assertEquals(nakedTree[9], ts[9].getTransactionId());
        assertEquals(nakedTree[10], ts[12].getTransactionId());
        assertEquals(nakedTree[11], ts[15].getTransactionId());
        Crypter cp = StringUtil.getDefaultCrypter();
        //Two levels in to the left is a hash of transaction 1 + 2.
        assertEquals(nakedTree[3],
                     cp.applyHash(ts[0].getTransactionId() + ts[3].getTransactionId()));
        assertEquals(nakedTree[4],
                     cp.applyHash(ts[6].getTransactionId() + ts[9].getTransactionId()));
        assertEquals(nakedTree[5],
                     cp.applyHash(ts[12].getTransactionId() + ts[15].getTransactionId()));
        //One level to the left is a hash of the hash of transactions 1 + 2 + hash of transactions 3 + 4
        assertEquals(nakedTree[1],
                     cp.applyHash(cp.applyHash(ts[0].getTransactionId() + ts[3].getTransactionId()) +
                                          cp.applyHash(ts[6].getTransactionId() + ts[9].getTransactionId())));
        //One level to the right is a hash of the hash of transactions 1 + 2 * 2
        assertEquals(nakedTree[2],
                     cp.applyHash(cp.applyHash(ts[12].getTransactionId() + ts[15].getTransactionId()) +
                                          cp.applyHash(ts[12].getTransactionId() + ts[15].getTransactionId())));
        //Root is everything else.
        assertEquals(tree.getRoot(),
                     cp.applyHash(
                             cp.applyHash(
                                     cp.applyHash(ts[0].getTransactionId() + ts[3].getTransactionId()) +
                                             cp.applyHash(ts[6].getTransactionId() + ts[9].getTransactionId())) +
                                     cp.applyHash(
                                             cp.applyHash(ts[12].getTransactionId() + ts[15].getTransactionId()) +
                                                     cp.applyHash(
                                                             ts[12].getTransactionId() + ts[15].getTransactionId())))
                    );

    }


    @Test
    void testMerkleSingleVerification() {
        Ident id[] = new Ident[]{new Ident(), new Ident()};
        Random r = new Random();
        Transaction ts[] = new Transaction[]{
                new Transaction(id[0].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                new Transaction(id[1].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                new Transaction(id[0].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                new Transaction(id[1].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                new Transaction(id[0].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                new Transaction(id[1].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0))))
        };
        MerkleTree tree = MerkleTree.buildMerkleTree(ts, ts.length);
        assertNotNull(tree.getRoot());
        assertTrue(tree.verifyTransaction(ts[0].getTransactionId()));
        assertTrue(tree.verifyTransaction(ts[1].getTransactionId()));
        assertTrue(tree.verifyTransaction(ts[2].getTransactionId()));
        assertTrue(tree.verifyTransaction(ts[3].getTransactionId()));
        assertTrue(tree.verifyTransaction(ts[4].getTransactionId()));
        assertTrue(tree.verifyTransaction(ts[5].getTransactionId()));
        ts = new Transaction[]{
                new Transaction(id[0].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                new Transaction(id[1].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                new Transaction(id[0].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                new Transaction(id[1].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                new Transaction(id[0].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0))))
        };
        tree = MerkleTree.buildMerkleTree(ts, ts.length);
        assertNotNull(tree.getRoot());
        assertTrue(tree.verifyTransaction(ts[0].getTransactionId()));
        assertTrue(tree.verifyTransaction(ts[1].getTransactionId()));
        assertTrue(tree.verifyTransaction(ts[2].getTransactionId()));
        assertTrue(tree.verifyTransaction(ts[3].getTransactionId()));
        assertTrue(tree.verifyTransaction(ts[4].getTransactionId()));
        ts = new Transaction[]{
                new Transaction(id[0].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                new Transaction(id[1].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                new Transaction(id[0].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                new Transaction(id[1].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                new Transaction(id[0].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                new Transaction(id[0].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                new Transaction(id[0].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0))))
        };
        tree = MerkleTree.buildMerkleTree(ts, ts.length);
        assertNotNull(tree.getRoot());
        assertTrue(tree.verifyTransaction(ts[0].getTransactionId()));
        assertTrue(tree.verifyTransaction(ts[1].getTransactionId()));
        assertTrue(tree.verifyTransaction(ts[2].getTransactionId()));
        assertTrue(tree.verifyTransaction(ts[3].getTransactionId()));
        assertTrue(tree.verifyTransaction(ts[4].getTransactionId()));
        assertTrue(tree.verifyTransaction(ts[5].getTransactionId()));
        assertTrue(tree.verifyTransaction(ts[6].getTransactionId()));
    }


    @Test
    void testMerkleAllVerification() {
        Ident id[] = new Ident[]{new Ident(), new Ident()};
        Random r = new Random();
        Transaction ts[] = new Transaction[]{
                new Transaction(id[0].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                new Transaction(id[1].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                new Transaction(id[0].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                new Transaction(id[1].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                new Transaction(id[0].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                new Transaction(id[1].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0))))
        };
        MerkleTree tree = MerkleTree.buildMerkleTree(ts, ts.length);
        assertNotNull(tree.getRoot());
        assertTrue(tree.verifyBlockTransactions(ts, ts.length));
        ts = new Transaction[]{
                new Transaction(id[0].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                new Transaction(id[1].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                new Transaction(id[0].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                new Transaction(id[1].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                new Transaction(id[0].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                null,
                null,
                null
        };
        tree = MerkleTree.buildMerkleTree(ts, 5);
        assertNotNull(tree.getRoot());
        assertTrue(tree.verifyBlockTransactions(ts, 5));
        ts = new Transaction[]{
                new Transaction(id[0].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                new Transaction(id[1].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                new Transaction(id[0].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                new Transaction(id[1].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                new Transaction(id[0].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                new Transaction(id[0].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                new Transaction(id[0].getPublicKey(), new SensorData(new TemperatureData(r.nextDouble() * 100,
                                                                                         TUnit.CELSIUS,
                                                                                         new BigDecimal(0),
                                                                                         new BigDecimal(0)))),
                null,
                null,
                null
        };
        tree = MerkleTree.buildMerkleTree(ts, 7);
        assertNotNull(tree.getRoot());
        assertTrue(tree.verifyBlockTransactions(ts, 7));
    }
}
