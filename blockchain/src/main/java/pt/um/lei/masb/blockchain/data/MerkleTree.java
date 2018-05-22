package pt.um.lei.masb.blockchain.data;

import org.openjdk.jol.info.GraphLayout;
import pt.um.lei.masb.blockchain.IHashed;
import pt.um.lei.masb.blockchain.Sizeable;
import pt.um.lei.masb.blockchain.utils.Crypter;
import pt.um.lei.masb.blockchain.utils.StringUtil;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Entity(name = "MerkleTree")
public final class MerkleTree implements Sizeable {
    @NotNull
    private final static Crypter crypter = StringUtil.getDefaultCrypter();
    @NotNull
    private final static Logger LOGGER = Logger.getLogger("MerkleTree");

    @Id
    @GeneratedValue
    private long id;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> hashes;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<Integer> levelIndex;

    protected MerkleTree() {
        hashes = null;
        levelIndex = null;
    }

    /**
     * Build a merkle tree collapsed in a heap for easy navigability from bottom up.
     *
     * @param data  Transactions in the block.
     * @return The corresponding MerkleTree or empty MerkleTree if empty transactions.
     */
    public static @NotNull MerkleTree buildMerkleTree(@NotNull List<? extends IHashed> data) {
        var t = new MerkleTree();
        List<String[]> treeLayer = data.size() == 0 ?
                                   new ArrayList<>() :
                                   new ArrayList<>((int) (Math.log(data.size()) / Math.log(2)) + 1);
        treeLayer.add(initTree(data));
        return buildLoop(t, treeLayer);
    }

    /**
     * Build a merkle tree collapsed in a heap for easy navigability from bottom up.
     * <p>
     * Convenience method for pre-pending coinbase as first element in bottom layer.
     *
     * @param coinbase Coinbase of the block.
     * @param data     Transactions in the block.
     * @return The corresponding MerkleTree or empty MerkleTree if empty transactions.
     */
    public static @NotNull MerkleTree buildMerkleTree(@NotNull IHashed coinbase,
                                                      @NotNull List<? extends IHashed> data) {
        var t = new MerkleTree();
        var treeLayer = data.size() == 0 ?
                        new ArrayList<String[]>() :
                        new ArrayList<String[]>((int) (Math.log(data.size()) / Math.log(2)) + 1);
        treeLayer.add(initTree(coinbase, data));
        return buildLoop(t, treeLayer);
    }


    /**
     * Build loop that builds a Merkle Tree collapsed in a heap.
     * <p>
     * Start at leaves and iteratively builds the next layer at
     * depth-1 until it arrives at root.
     * <p>
     *
     * @param t         The Merkle tree being built.
     * @param treeLayer A container for the successive layers, containing layer = depth.
     * @return The completed merkle tree.
     */
    private static MerkleTree buildLoop(MerkleTree t, List<String[]> treeLayer) {
        int i = 0;
        //Next layer's node count for depth-1
        int count = (treeLayer.get(i).length / 2) + (treeLayer.get(i).length % 2);
        //While we're not at root yet:
        for (; count > 1; count = (count / 2) + (count % 2), i++) {
            treeLayer.add(buildNewLayer(treeLayer.get(i), count));
        }
        if (treeLayer.get(i).length == 2) {
            var len = treeLayer.stream().mapToInt(s -> s.length).sum();
            t.hashes = new ArrayList<>(len + 1);
            t.hashes.add(crypter.applyHash(treeLayer.get(i)[0] + treeLayer.get(i)[1]));
            t.levelIndex = new ArrayList<>(treeLayer.size() + 1);
            t.levelIndex.add(0);
            Collections.reverse(treeLayer);
            count = 1;
            for (String[] s : treeLayer) {
                t.levelIndex.add(count);
                t.hashes.addAll(Arrays.asList(s));
                count += s.length;
            }
        }
        //If the previous layer was already length 1, that means we started at the root.
        else if (treeLayer.get(i).length == 1) {
            t.hashes = new ArrayList<>(1);
            t.hashes.add(treeLayer.get(i)[0]);
        } else {
            LOGGER.log(Level.SEVERE, "Empty merkle tree");
        }
        return t;
    }

    /**
     * Build the next tree layer. Sets proper descendents and ascendents.
     * <p>
     * Checks for oddness, and if odd length create a single left child for remainder.
     *
     * @param previousTreeLayer The previous tree layer, depth+1
     * @param count             The node count for this next layer.
     * @return The new Layer of hashes of their children set.
     */
    private static String[] buildNewLayer(String[] previousTreeLayer,
                                          int count) {
        String treeLayer[] = new String[count];
        var j = 0;
        //While we're inside the bounds of this layer, calculate two by two the hash.
        for (int i = 1; i < previousTreeLayer.length; i += 2, j++) {
            treeLayer[j] = crypter.applyHash(previousTreeLayer[i - 1] +
                                                     previousTreeLayer[i]);
        }
        //If we're still in the layer, there's one left, it's grouped and hashed with itself.
        if (j < treeLayer.length) {
            treeLayer[j] = crypter.applyHash(previousTreeLayer[previousTreeLayer.length - 1] +
                                                     previousTreeLayer[previousTreeLayer.length - 1]);
        }
        return treeLayer;
    }

    /**
     * Initialize the first tree layer, which is the transaction layer.
     * <p>
     * Sets a correspondence from each hash to it's index.
     *
     * @param data The transactions array.
     */
    private static String[] initTree(@NotNull List<? extends IHashed> data) {
        return data.stream()
                   .filter(Objects::nonNull)
                   .map(IHashed::getHashId)
                   .toArray(String[]::new);
    }

    /**
     * Initialize the first tree layer, which is the transaction layer.
     * <p>
     * Sets a correspondence from each hash to it's index.
     *
     * @param coinbase The coinbase of the block.
     * @param data     The transactions array.
     */
    private static String[] initTree(@NotNull IHashed coinbase,
                                     @NotNull List<? extends IHashed> data) {

        ArrayList<String> res = new ArrayList<>(data.size() + 1);
        res.add(coinbase.getHashId());
        data.stream()
            .filter(Objects::nonNull)
            .map(IHashed::getHashId)
            .forEach(res::add);
        res.trimToSize();
        return res.toArray(new String[0]);
    }

    /**
     * @return The root hash.
     */
    public String getRoot() {
        return hashes.get(0);
    }

    public boolean hasTransaction(@NotEmpty String hash) {
        var res = false;
        //levelIndex[index] points to leftmost node at level index of the tree
        for (int i = hashes.size() - 1; i >= levelIndex.get(levelIndex.size() - 1); i--) {
            if (hashes.get(i).equals(hash)) {
                res = true;
                break;
            }
        }
        return res;
    }

    private Optional<Integer> getTransactionId(@NotEmpty String hash) {
        Optional<Integer> res = Optional.empty();
        //levelIndex[index] points to leftmost node at level index of the tree
        for (int i = hashes.size() - 1; i >= levelIndex.get(levelIndex.size() - 1); i--) {
            if (hashes.get(i).equals(hash)) {
                res = Optional.of(i);
                break;
            }
        }
        return res;
    }

    public List<String> getCollapsedTree() {
        return hashes;
    }

    /**
     * @param hash  hash to verify against merkleTree.
     * @return Whether the transaction is present and
     *              matched all the way up the merkleTree.
     */
    public boolean verifyTransaction(@NotEmpty String hash) {
        var res = false;
        var t = getTransactionId(hash);
        if (t.isPresent()) {
            res = loopUpVerification(t.get(), hash, levelIndex.size() - 1);
        }
        return res;
    }

    private boolean loopUpVerification(int index, String hash, int level) {
        boolean res;
        while ((res = hash.equals(hashes.get(index))) && index != 0) {
            var delta = index - levelIndex.get(level);
            //Is a left leaf
            if (delta % 2 == 0) {
                //Is an edge case left leaf
                if (index + 1 == hashes.size() ||
                        (level + 1 != levelIndex.size() &&
                                index + 1 == levelIndex.get(level + 1))) {
                    hash = crypter.applyHash(hashes.get(index) + hashes.get(index));
                } else {
                    hash = crypter.applyHash(hashes.get(index) + hashes.get(index + 1));
                }
            }
            //Is a right leaf
            else {
                hash = crypter.applyHash(hashes.get(index - 1) + hashes.get(index));
            }
            level--;
            //Index of parent is at the start of the last level
            // + the distance from start of this level / 2
            index = levelIndex.get(level) + (delta / 2);
        }
        return res;
    }

    /**
     * Verifies entire merkleTree against the transaction data.
     *
     * @param coinbase The coinbase transaction.
     * @param data The transaction data.
     * @return Whether the entire merkleTree matches
     * against the transaction data.
     */
    public boolean verifyBlockTransactions(@NotNull IHashed coinbase,
                                           @NotNull List<? extends IHashed> data) {
        var res = checkAllTransactionsPresent(coinbase, data);
        if (hashes.size() != 1) {
            res = loopUpAllVerification(levelIndex.size() - 2);
        }
        return res;
    }


    private boolean loopUpAllVerification(int level) {
        var res = true;
        //2 fors evaluate to essentially checking level by level
        //starting at the second to last.
        //We already checked the last level immediately
        //against the data provided.
        for (int i; res && level >= 0; ) {
            i = levelIndex.get(level);
            for (; i < levelIndex.get(level + 1); i++) {
                //Delta is level index difference + current index + difference
                //to current level index.
                //It checks out to exactly the left child leaf of any node.
                var delta = levelIndex.get(level + 1) -
                        levelIndex.get(level) +
                        i +
                        (i - levelIndex.get(level));
                //Either the child is the last leaf in the next level, or is the last leaf.
                //Since we know delta points to left leafs both these conditions mean
                //edge case leafs.
                if ((level + 2 != levelIndex.size() && delta + 1 == levelIndex.get(level + 2))
                        || delta + 1 == hashes.size()) {
                    if (!hashes.get(i)
                               .equals(crypter.applyHash(hashes.get(delta) +
                                                                 hashes.get(delta)))) {
                        res = false;
                        break;
                    }
                }
                //Then it's a regular left leaf.
                else {
                    if (!hashes.get(i)
                               .equals(crypter.applyHash(hashes.get(delta) +
                                                                 hashes.get(delta + 1)))) {
                        res = false;
                        break;
                    }
                }
            }
            level--;
        }
        return res;
    }

    private boolean checkAllTransactionsPresent(IHashed coinbase, List<? extends IHashed> data) {
        var i = levelIndex.get(levelIndex.size() - 1) + 1;
        var res = true;
        var arr = data.stream()
                      .filter(Objects::nonNull)
                      .map(IHashed::getHashId)
                      .collect(Collectors.toList());
        if (hashes.get(i - 1)
                  .equals(coinbase.getHashId())) {
            for (String it : arr) {
                //There are at least as many transactions
                //They match the ones in the merkle tree.
                if (i < hashes.size() && it.equals(hashes.get(i))) {
                    i++;
                } else {
                    res = false;
                    break;
                }
            }
            //There are less transactions in the provided block
            if (i != hashes.size()) {
                res = false;
            }
        } else {
            res = false;
        }
        return res;
    }


    @Override
    public long getApproximateSize() {
        return GraphLayout.parseInstance(this).totalSize();
    }
}


