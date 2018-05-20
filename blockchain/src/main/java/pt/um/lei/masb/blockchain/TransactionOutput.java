package pt.um.lei.masb.blockchain;

import org.openjdk.jol.info.ClassLayout;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.security.PublicKey;

@Entity
public class TransactionOutput implements Sizeable {
    @Id
    @GeneratedValue
    private long id;

    @Basic(optional = false)
    private final PublicKey publicKey;

    @Basic(optional = false)
    private final String prevUTXO;

    @Basic(optional = false)
    private BigDecimal payout;

    protected TransactionOutput() {
        publicKey = null;
        prevUTXO = null;
    }

    protected TransactionOutput(@NotNull PublicKey publicKey,
                                @NotNull String prevUTXO,
                                @NotNull BigDecimal cumUTXO) {
        this.publicKey = publicKey;
        this.prevUTXO = prevUTXO;
        payout = cumUTXO;
    }

    @Override
    public long getApproximateSize() {
        return ClassLayout.parseClass(this.getClass()).instanceSize();
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public BigDecimal getPayout() {
        return payout;
    }

    public void addToPayout(@NotNull BigDecimal payout) {
        this.payout.add(payout);
    }
}
