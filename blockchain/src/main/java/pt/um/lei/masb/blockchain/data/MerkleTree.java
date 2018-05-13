package pt.um.lei.masb.blockchain.data;

import org.openjdk.jol.info.GraphLayout;
import pt.um.lei.masb.blockchain.Sizeable;
import pt.um.lei.masb.blockchain.Transaction;
import pt.um.lei.masb.blockchain.utils.Crypter;
import pt.um.lei.masb.blockchain.utils.StringUtil;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Entity(name = "MerkleTree")
public final class MerkleTree implements Sizeable {
    @NotNull
    private final static Crypter crypter = StringUtil.getDefaultCrypter();
    @NotNull
    private final static Logger LOGGER = Logger.getLogger("MerkleTree");

    @Id
    @GeneratedValue
    private long id;

    @OneToMany(cascade = CascadeType.ALL,
               fetch = FetchType.EAGER,
               orphanRemoval = true)
    private String hashes[];

    @OneToMany(cascade = CascadeType.ALL,
               fetch = FetchType.EAGER,
               orphanRemoval = true)
    private Integer levelIndex[];

    protected MerkleTree() {
        hashes = null;
        levelIndex = null;
    }

    /**
     * Build a Merkle Tree collapsed in a heap for easy navigability from bottom up.
     * <p>
     * Start at leaves and iteratively build next layer at depth-1 until it arrives at root.
     * <p>
     *
     * @param data Transactions in the block.
     * @param size Actual number of transactions used in array.
     * @return the corresponding MerkleTree or empty MerkleTree if empty transactions.
     */
    public static @NotNull MerkleTree buildMerkleTree(@NotNull Transaction[] data,
                                                      @Positive int size) {
        var t = new MerkleTree();
        var treeLayer = new ArrayList<String[]>((int) (Math.log(size) / Math.log(2)) + 1);
        treeLayer.add(initTree(size, data));
        int i = 0;
        //Next layer's node count for depth-1
        int count = (treeLayer.get(i).length / 2) + (treeLayer.get(i).length % 2);
        //While we're not at root yet:
        for (; count > 1; count = (count / 2) + (count % 2), i++) {
            treeLayer.add(buildNewLayer(treeLayer.get(i), count));
        }
        if (treeLayer.get(i).length == 2) {
            var len = treeLayer.stream().mapToInt(s -> s.length).sum();
            t.hashes = new String[len + 1];
            t.levelIndex = new Integer[treeLayer.size() + 1];
            t.levelIndex[0] = 0;
            Collections.reverse(treeLayer);
            count = 1;
            i = 1;
            for (String[] s : treeLayer) {
                t.levelIndex[i] = count;
                for (String s1 : s) {
                    t.hashes[count] = s1;
                    count++;
                }
                i++;
            }
            t.hashes[0] = crypter.applyHash(t.hashes[1] + t.hashes[2]);
        }
        //If the previous layer was already length 1, that means we started at the root.
        else if (treeLayer.get(i).length == 1) {
            t.hashes = treeLayer.get(i);
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
     * @param size The transaction array's effective size.
     * @param data The transactions array.
     */
    private static String[] initTree(@Positive int size,
                                     @NotEmpty Transaction[] data) {
        return Arrays.stream(data).filter(Objects::nonNull)
                     .map(Transaction::getTransactionId)
                     .toArray(String[]::new);
    }


    /**
     * @return The root hash.
     */
    public String getRoot() {
        return hashes[0];
    }

    public boolean hasTransaction(@NotEmpty String hash) {
        var res = false;
        //levelIndex[index] points to leftmost node at level index of the tree
        for (int i = hashes.length; i >= levelIndex[levelIndex.length - 1]; i--) {
            if (hashes[i].equals(hash)) {
                res = true;
                break;
            }
        }
        return res;
    }

    private Optional<Integer> getTransactionId(@NotEmpty String hash) {
        Optional<Integer> res = Optional.empty();
        //levelIndex[index] points to leftmost node at level index of the tree
        for (int i = hashes.length - 1; i >= levelIndex[levelIndex.length - 1]; i--) {
            if (hashes[i].equals(hash)) {
                res = Optional.of(i);
                break;
            }
        }
        return res;
    }

    public String[] getCollapsedTree() {
        return hashes;
    }

    /**
     * @param hash hash to verify against merkleTree.
     * @return
     */
    public boolean verifyTransaction(@NotEmpty String hash) {
        var res = false;
        var t = getTransactionId(hash);
        if (t.isPresent()) {
            System.out.println("TxIndex is: " + t);
            res = loopUpVerification(t.get(), hash, levelIndex.length - 1);
        }
        return res;
    }

    private boolean loopUpVerification(int index, String hash, int level) {
        boolean res;
        while ((res = hash.equals(hashes[index])) && index != 0) {
            var delta = index - levelIndex[level];
            //Is a left leaf
            if (delta % 2 == 0) {
                System.out.println(index + 1 == hashes.length);
                //Is an edge case left leaf
                if (index + 1 == hashes.length ||
                        (level + 1 != levelIndex.length &&
                                index + 1 == levelIndex[level + 1])) {
                    System.out.println(index + " is a Left edge leaf");
                    hash = crypter.applyHash(hashes[index] + hashes[index]);
                } else {
                    System.out.println(index + " is a Left non-edge leaf");
                    hash = crypter.applyHash(hashes[index] + hashes[index + 1]);
                }
            }
            //Is a right leaf
            else {
                System.out.println(index + " is a right leaf");
                hash = crypter.applyHash(hashes[index - 1] + hashes[index]);
            }
            System.out.println(hash);
            System.out.println("index is : " + index);
            System.out.println(hashes[index]);
            level--;
            index = levelIndex[level] + (delta / 2);
        }
        return res;
    }

    @Override
    public long getApproximateSize() {
        return GraphLayout.parseInstance(this).totalSize();
    }
}


