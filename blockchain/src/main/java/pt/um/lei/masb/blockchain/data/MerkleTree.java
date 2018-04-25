package pt.um.lei.masb.blockchain.data;

import org.openjdk.jol.info.ClassLayout;
import pt.um.lei.masb.blockchain.Sizeable;
import pt.um.lei.masb.blockchain.Transaction;
import pt.um.lei.masb.blockchain.utils.Crypter;
import pt.um.lei.masb.blockchain.utils.StringUtil;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.util.HashMap;
import java.util.Map;

@Entity
public final class MerkleTree implements Sizeable {
    private static Crypter crypter = StringUtil.getDefaultCrypter();

    @Id
    @OneToOne
    private MerkleNode root;

    @OneToMany
    private Map<String, MerkleNode> trans;

    protected MerkleTree(){}

    private MerkleTree(int size) {
        this.root = null;
        this.trans = new HashMap<>(size);
    }

    /**
     * Build a Merkle Tree using {@link MerkleNode} for easy navigability from bottom up.
     * <p>
     * Start at leaves and iteratively build next layer at depth-1 until it arrives at root.
     * <p>
     *
     * @param data Transactions in the block.
     * @param size Actual number of transactions used in array (negative numbers interpreted as 0).
     * @return the corresponding MerkleTree or empty MerkleTree if empty transactions.
     */
    public static MerkleTree buildMerkleTree(Transaction[] data, int size) {
        if (size > 0) {

            var t = new MerkleTree(size);
            MerkleNode treeLayer[] = initTree(t, size, data);
            //Next layer's node count for depth-1
            int count = (treeLayer.length / 2) + (treeLayer.length % 2);
            //While we're not at root yet:
            for (; count > 1; count = (count / 2) + (count % 2)) {
                treeLayer = buildNewLayer(treeLayer, count);
            }
            MerkleNode root;
            if (treeLayer.length == 2) {
                root = new MerkleNode(crypter.applyHash(treeLayer[0].getHash() + treeLayer[1].getHash()));
                root.setLeft(treeLayer[0]);
                root.setRight(treeLayer[1]);
                treeLayer[0].setParent(root);
                treeLayer[1].setParent(root);
                treeLayer[0].setSibling(treeLayer[1]);
                treeLayer[1].setSibling(treeLayer[0]);
                t.setRoot(root);
            }
            //If the previous layer was already length 1, that means we started at the root.
            else if (treeLayer.length == 1) {
                t.setRoot(treeLayer[0]);
            }
            return t;
        } else {
            return new MerkleTree(0);
        }
    }

    /**
     * Build the next tree layer. Sets proper descendents and ascendents.
     * <p>
     * Checks for oddness, and if odd length create a single left child for remainder.
     *
     * @param previousTreeLayer The previous tree layer, depth+1
     * @param count             the node count for this next layer.
     * @return
     */
    private static MerkleNode[] buildNewLayer(MerkleNode[] previousTreeLayer, int count) {
        MerkleNode treeLayer[] = new MerkleNode[count];
        var j = 0;
        for (int i = 1; i < previousTreeLayer.length; i += 2, j++) {
            treeLayer[j] = new MerkleNode(crypter.applyHash(previousTreeLayer[i - 1].getHash() +
                                                                    previousTreeLayer[i].getHash()));
            treeLayer[j].setLeft(previousTreeLayer[i - 1]);
            treeLayer[j].setRight(previousTreeLayer[i]);
            previousTreeLayer[i - 1].setParent(treeLayer[j]);
            previousTreeLayer[i].setParent(treeLayer[j]);
        }
        if (j < treeLayer.length) {
            treeLayer[j] = new MerkleNode(previousTreeLayer[previousTreeLayer.length - 1].getHash());
            treeLayer[j].setLeft(previousTreeLayer[previousTreeLayer.length - 1]);
            previousTreeLayer[previousTreeLayer.length - 1].setParent(treeLayer[j]);
        }
        fillSiblings(treeLayer, treeLayer.length);
        return treeLayer;
    }

    /**
     * Initialize the first tree layer, which is the transaction layer.
     * <p>
     * Sets a correspondence from each hash to it's merkle node for consulting.
     *
     * @param t    The merkle tree to initialize.
     * @param size The transaction array's effective size.
     * @param data The transactions array.
     * @return The first node layer of max depth (all leafs).
     */
    private static MerkleNode[] initTree(MerkleTree t, int size, Transaction[] data) {
        MerkleNode baseLayer[] = new MerkleNode[size];
        for (int i = 0; i < size; i++) {
            baseLayer[i] = new MerkleNode(data[i].getTransactionId());
            t.addTransactionNode(baseLayer[i]);
        }
        fillSiblings(baseLayer, size);
        return baseLayer;
    }

    private static void fillSiblings(MerkleNode[] baseLayer, int size) {
        for (int i = 0; i + 1 < size; i += 2) {
            baseLayer[i].setSibling(baseLayer[i + 1]);
        }
        for (int i = 1; i < size; i += 2) {
            baseLayer[i].setSibling(baseLayer[i - 1]);
        }
    }

    public MerkleNode getRoot() {
        return root;
    }

    private void setRoot(MerkleNode root) {
        this.root = root;
    }

    private void addTransactionNode(MerkleNode t) {
        trans.put(t.getHash(), t);
    }

    private MerkleNode getTransactionNode(String hash) {
        if (hash != null && trans.containsKey(hash)) {
            return trans.get(hash);
        }
        return null;
    }

    public boolean verifyTransaction(String hash) {
        var res = true;
        MerkleNode t = getTransactionNode(hash);
        if (t != null) {
            for (MerkleNode s; t != null && t != root; t = t.getParent()) {
                s = t.getSibling();
                if (s != null) {
                    if (!crypter.applyHash(t.getHash() + t.getSibling().getHash())
                                .equals(t.getParent().getHash())) {
                        res = false;
                        t = root;
                    }
                } else {
                    if (!t.getHash().equals(t.getParent().getHash())) {
                        res = false;
                        t = root;
                    }
                }
            }
        } else {
            res = false;
        }
        return res;
    }

    @Override
    public long getApproximateSize() {
        return root.getApproximateSize() +
                ClassLayout.parseClass(MerkleTree.class).instanceSize();
    }
}


