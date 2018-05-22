package pt.um.lei.masb.blockchain;

import pt.um.lei.masb.blockchain.data.*;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.security.PublicKey;
import java.time.temporal.ChronoField;
import java.util.HashSet;
import java.util.Set;

/**
 * The coinbase transaction. Pays out to contributors to the blockchain.
 */
@Entity
public final class Coinbase implements Sizeable, IHashed {
    private static final int TIME_BASE = 5;
    private static final int VALUE_BASE = 2;
    private static final int BASE = 3;
    private static final int THRESHOLD = 100000;
    private static final int OTHER = 50;
    private static final int DATA = 5;
    private static final MathContext mathContext = new MathContext(8, RoundingMode.HALF_EVEN);
    private static final Coinbase origin = Block.getOrigin().getCoinbase();


    @OneToMany(cascade = CascadeType.ALL,
               fetch = FetchType.EAGER,
               orphanRemoval = true)
    private final Set<TransactionOutput> payoutTXO;


    @Basic(optional = false)
    private BigDecimal coinbase;


    @Id
    private String hashId;

    /**
     * The coinbase will be continually updated
     * to reflect changes to the block.
     */
    protected Coinbase() {
        coinbase = new BigDecimal(0);
        payoutTXO = new HashSet<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHashId() {
        return hashId;
    }

    public @NotNull Set<TransactionOutput> getPayoutTXO() {
        return payoutTXO;
    }

    public @NotNull BigDecimal getCoinbase() {
        return coinbase;
    }

    /**
     * @param newT                  Transaction to contribute to payout.
     * @param latestKnown           Transaction to compare for fluctuation.
     * @param latestUTXO            Transaction with last unspent
     *                              transaction output for the new Transaction's publisher.
     *                              <p>
     *                              If it's the first time for this identity, supply the
     *                              origin block coinbase.
     * @param cat                   Category of the transaction's data.
     */
    protected void addToInput(@NotNull Transaction newT,
                              @NotNull Transaction latestKnown,
                              @NotNull Coinbase latestUTXO,
                              @NotNull Category cat) {
        var dt = newT.getSensorData();
        var dt2 = latestKnown.getSensorData();
        var deltaTime = getTimeDelta(dt, dt2);
        BigDecimal deltaValue;
        BigDecimal payout;
        switch (cat) {
            case TEMPERATURE:
                deltaValue = calculateDiffTemperature(dt.getTemperatureData(),
                                                      dt2.getTemperatureData());
                payout = calculateDiff(deltaTime, deltaValue, Coinbase.DATA);
                break;
            case LUMINOSITY:
                deltaValue = calculateDiffLuminosity(dt.getLuminosityData(),
                                                     dt2.getLuminosityData());
                payout = calculateDiff(deltaTime, deltaValue, Coinbase.DATA);
                break;
            case HUMIDITY:
                deltaValue = calculateDiffHumidity(dt.getHumidityData(),
                                                   dt2.getHumidityData());
                payout = calculateDiff(deltaTime, deltaValue, Coinbase.DATA);
                break;
            case NOISE:
                deltaValue = calculateDiffNoise(dt.getNoiseData(),
                                                dt2.getNoiseData());
                payout = calculateDiff(deltaTime, deltaValue, Coinbase.DATA);
                break;
            case OTHER:
                deltaValue = new BigDecimal(0);
                payout = calculateDiff(deltaTime, deltaValue, Coinbase.OTHER);
                break;
            default:
                payout = new BigDecimal(0);
        }
        coinbase = coinbase.add(payout);
        addToOutputs(newT.getPublicKey(), latestUTXO, payout);
    }


    private @NotNull BigDecimal calculateDiffTemperature(@NotNull TemperatureData newTD,
                                                         @NotNull TemperatureData oldTD) {
        var newT = new BigDecimal(newTD.convertToCelsius());
        var oldT = new BigDecimal(oldTD.convertToCelsius());
        return newT.subtract(oldT).divide(oldT, Coinbase.mathContext);
    }

    private @NotNull BigDecimal calculateDiffLuminosity(@NotNull LuminosityData newLD,
                                                        @NotNull LuminosityData oldLD) {
        var newL = new BigDecimal(newLD.getLum());
        var oldL = new BigDecimal(oldLD.getLum());
        return newL.subtract(oldL).divide(oldL, Coinbase.mathContext);
    }

    private @NotNull BigDecimal calculateDiffHumidity(@NotNull HumidityData newHD,
                                                      @NotNull HumidityData oldHD) {
        BigDecimal newH;
        BigDecimal oldH;
        if (newHD.getUnit() == HUnit.RELATIVE) {
            newH = new BigDecimal(newHD.getHum());
            oldH = new BigDecimal(oldHD.getHum());
        } else {
            newH = new BigDecimal(newHD.convertToKGbyKG());
            oldH = new BigDecimal(oldHD.convertToKGbyKG());
        }
        return newH.subtract(oldH).divide(oldH, Coinbase.mathContext);
    }

    private @NotNull BigDecimal calculateDiffNoise(@NotNull NoiseData newND,
                                                   @NotNull NoiseData oldND) {
        var newN = new BigDecimal(newND.getNoiseLevel()).add(new BigDecimal(newND.getPeakOrBase()).abs());
        var oldN = new BigDecimal(oldND.getNoiseLevel()).add(new BigDecimal(oldND.getPeakOrBase()).abs());
        return newN.subtract(oldN)
                   .divide(oldN, Coinbase.mathContext);
    }


    private @NotNull BigDecimal getTimeDelta(@NotNull SensorData dt,
                                             @NotNull SensorData dt2) {
        var stamp1 = new BigDecimal(dt.getTimestamp()
                                      .getEpochSecond() * 1000 + dt.getTimestamp()
                                                                   .get(ChronoField.MILLI_OF_SECOND));
        var stamp2 = new BigDecimal(dt2.getTimestamp()
                                       .getEpochSecond() * 1000 + dt2.getTimestamp()
                                                                     .get(ChronoField.MILLI_OF_SECOND));
        return stamp1.subtract(stamp2)
                     .divide(stamp1, new MathContext(8, RoundingMode.HALF_EVEN));
    }

    /**
     * @param publicKey Public Key of transaction publisher.
     * @param prevUTXO  Coinbase with previous known UTXO.
     * @param payout    Payout amount to publisher.
     */
    private void addToOutputs(@NotNull PublicKey publicKey,
                              @NotEmpty Coinbase prevUTXO,
                              @NotNull BigDecimal payout) {
        payoutTXO.stream()
                 .filter(t -> t.getPublicKey().equals(publicKey))
                 .findAny()
                 .ifPresentOrElse(t -> t.addToPayout(payout),
                                  () -> fillInFromPreviousUTXO(publicKey, prevUTXO, payout));
    }


    /**
     *
     * @param publicKey             The public identity associated with an agent.
     * @param prevUTXO              The coinbase containing the previous transaction
     *                              output due to the agent.
     * @param payout                The new payout to add to previous output.
     */
    private void fillInFromPreviousUTXO(@NotNull PublicKey publicKey,
                                        @NotNull Coinbase prevUTXO,
                                        @NotNull BigDecimal payout) {
        var newPayout = prevUTXO.payoutTXO.stream()
                                          .filter(t -> t.getPublicKey().equals(publicKey))
                                          .findAny()
                                          .map(TransactionOutput::getPayout)
                                          .orElseGet(() -> new BigDecimal(0)).add(payout);
        payoutTXO.add(new TransactionOutput(publicKey,
                                            prevUTXO.hashId,
                                            newPayout));
    }

    private @NotNull BigDecimal calculateDiff(@NotNull BigDecimal deltaTime,
                                              @NotNull BigDecimal deltaValue,
                                              @Positive int constant) {
        var standardDivisor = new BigDecimal(Coinbase.THRESHOLD * constant);
        var timeFactor = deltaTime.multiply(new BigDecimal(Coinbase.TIME_BASE))
                                  .pow(2, Coinbase.mathContext)
                                  .divide(standardDivisor, Coinbase.mathContext);
        var valueFactor = deltaValue.divide(new BigDecimal(2), Coinbase.mathContext)
                                    .multiply(new BigDecimal(Coinbase.VALUE_BASE))
                                    .divide(standardDivisor, Coinbase.mathContext);
        var baseFactor = new BigDecimal(Coinbase.BASE).divide(standardDivisor, Coinbase.mathContext);
        return timeFactor.add(valueFactor).add(baseFactor);
    }
}
