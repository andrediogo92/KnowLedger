package pt.um.lei.masb.blockchain;

import org.openjdk.jol.info.GraphLayout;
import pt.um.lei.masb.blockchain.utils.StringUtil;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.security.PublicKey;
import java.util.HashSet;
import java.util.Set;

@Entity
public class TransactionOutput implements Sizeable, Hashed {
    @Id
    @GeneratedValue
    private long id;

    @Basic(optional = false)
    private final PublicKey publicKey;

    @Basic(optional = false)
    private final String prevCoinbase;
    @Basic(optional = false)
    private String hashId;

    @Basic(optional = false)
    private BigDecimal payout;
    @ElementCollection
    private Set<String> tx;

    protected TransactionOutput() {
        publicKey = null;
        prevCoinbase = null;
    }

    public TransactionOutput(@NotNull PublicKey publicKey,
                             @NotNull String hashId,
                             @NotNull String prevCoinbase,
                             @NotNull BigDecimal cumUTXO,
                             @NotEmpty Set<String> tx) {
        this.publicKey = publicKey;
        this.hashId = hashId;
        this.prevCoinbase = prevCoinbase;
        payout = cumUTXO;
        this.tx = tx;
    }

    TransactionOutput(@NotNull PublicKey publicKey,
                      @NotNull String prevCoinbase,
                      @NotNull BigDecimal cumUTXO,
                      @NotNull String newT,
                      @NotNull String prev) {
        this.publicKey = publicKey;
        this.prevCoinbase = prevCoinbase;
        this.tx = new HashSet<>();
        payout = new BigDecimal("0");
        addToPayout(cumUTXO, newT, prev);
    }

    private String recalculateHash() {
        return StringUtil.getDefaultCrypter()
                         .applyHash(tx.stream()
                                      .reduce("",
                                              String::concat) + StringUtil.getStringFromKey(publicKey));
    }

    @Override
    public long getApproximateSize() {
        return GraphLayout.parseInstance(this).totalSize();
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public BigDecimal getPayout() {
        return payout;
    }

    public void addToPayout(@NotNull BigDecimal payout,
                            @NotNull String tx,
                            @NotNull String prev) {
        this.tx.add(prev + tx);
        this.payout = this.payout.add(payout);
        hashId = recalculateHash();
    }

    public String getPrevCoinbase() {
        return prevCoinbase;
    }

    public Set<String> getTx() {
        return tx;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHashId() {
        return hashId;
    }
}
