package pt.um.lei.masb.agent.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.um.lei.masb.agent.messaging.block.ontology.*;
import pt.um.lei.masb.agent.messaging.transaction.ontology.*;
import pt.um.lei.masb.blockchain.*;
import pt.um.lei.masb.blockchain.data.*;
import pt.um.lei.masb.blockchain.utils.StringUtil;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.stream.Collectors;

public class DataConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataConverter.class.getName());

    public DataConverter() {
    }

    //Conversions to Jade types.

    public JBlock convertToJadeBlock(Block b) {
        var data = b.getData()
                    .stream()
                    .map(this::convertToJadeTransaction)
                    .collect(Collectors.toUnmodifiableList());
        return new JBlock(data,
                          convertToJadeCoinbase(b.getCoinbase()),
                          convertToJadeBlockHeader(b.getHeader()),
                          convertToJadeMerkleTree(b.getMerkleTree()));
    }

    private JMerkleTree convertToJadeMerkleTree(MerkleTree merkleTree) {
        return new JMerkleTree(merkleTree.getCollapsedTree(),
                               merkleTree.getIndexes());
    }

    public JBlockHeader convertToJadeBlockHeader(BlockHeader header) {
        return new JBlockHeader(header.getDifficulty().toString(),
                                header.getBlockheight(),
                                header.getHash(),
                                header.getMerkleRoot(),
                                header.getPreviousHash(),
                                header.getTimeStamp().toString(),
                                header.getNonce());
    }

    private JCoinbase convertToJadeCoinbase(Coinbase coinbase) {
        var txos = coinbase.getPayoutTXO()
                           .stream()
                           .map(this::convertToJadeTransactionOutput)
                           .collect(Collectors.toUnmodifiableSet());
        return new JCoinbase(txos, coinbase.getCoinbase().toString(), coinbase.getHashId());
    }

    private JTransactionOutput convertToJadeTransactionOutput(TransactionOutput txo) {
        return new JTransactionOutput(StringUtil.getStringFromKey(txo.getPublicKey()),
                                      txo.getHashId(),
                                      txo.getPrevCoinbase(),
                                      txo.getPayout().toString(),
                                      txo.getTx());
    }

    public JTransaction convertToJadeTransaction(Transaction t) {
        return new JTransaction(t.getHashId(),
                                StringUtil.getStringFromKey(t.getPublicKey()),
                                convertToJadeSensorData(t.getSensorData()),
                                t.getSignature());
    }

    private JSensorData convertToJadeSensorData(SensorData sensorData) {
        JGeoData jGeoData = null;
        switch (sensorData.getCategory()) {
            case NOISE:
                jGeoData = convertToJadeNoiseData(sensorData.getNoiseData());
                break;
            case TEMPERATURE:
                jGeoData = convertToJadeTemperatureData(sensorData.getTemperatureData());
                break;
            case HUMIDITY:
                jGeoData = convertToJadeHumidityData(sensorData.getHumidityData());
                break;
            case LUMINOSITY:
                jGeoData = convertToJadeLuminosityData(sensorData.getLuminosityData());
                break;
            case OTHER:
                jGeoData = convertToJadeOtherData(sensorData.getOtherData());
                break;
        }
        return new JSensorData(sensorData.getCategory(),
                               jGeoData,
                               sensorData.getTimestamp()
                                         .toString());
    }

    private JGeoData convertToJadeOtherData(OtherData<? extends Serializable> otherData) {
        var b = new ByteArrayOutputStream((int) otherData.getApproximateSize());
        byte bts[];
        try (var ob = new ObjectOutputStream(b)) {
            ob.writeObject(otherData.getData());
        } catch (IOException e) {
            LOGGER.error("", e.getMessage());
        }
        return new JOtherData(otherData.getLatitude().toString(),
                              otherData.getLongitude().toString(),
                              otherData.getClass().getName(),
                              b.toByteArray());
    }

    private JGeoData convertToJadeLuminosityData(LuminosityData luminosityData) {
        return new JLuminosityData(luminosityData.getLum().toString(),
                                   luminosityData.getUnit(),
                                   luminosityData.getLatitude().toString(),
                                   luminosityData.getLongitude().toString());
    }

    private JGeoData convertToJadeHumidityData(HumidityData humidityData) {
        return new JHumidityData(humidityData.getHum().toString(),
                                 humidityData.getUnit(),
                                 humidityData.getLatitude().toString(),
                                 humidityData.getLongitude().toString());
    }

    private JGeoData convertToJadeTemperatureData(TemperatureData temperatureData) {
        return new JTemperatureData(temperatureData.getTemperature().toString(),
                                    temperatureData.getUnit(),
                                    temperatureData.getLatitude().toString(),
                                    temperatureData.getLongitude().toString());
    }

    private JGeoData convertToJadeNoiseData(NoiseData noiseData) {
        return new JNoiseData(noiseData.getNoiseLevel().toString(),
                              noiseData.getPeakOrBase().toString(),
                              noiseData.getUnit(),
                              noiseData.getLatitude().toString(),
                              noiseData.getLongitude().toString());
    }


    //Conversions from Jade Types

    public Block convertFromJadeBlock(JBlock b) {
        var data = b.getData().stream().map(this::convertFromJadeTransaction).collect(Collectors.toUnmodifiableList());
        var bl = new Block(data,
                           convertFromJadeCoinbase(b.getCoinbase()),
                           convertFromJadeBlockHeader(b.getHeader()),
                           convertFromJadeMerkleTree(b.getMerkleTree()));
        bl.getHeader().setBlockReferenceOnce(bl);
        return bl;
    }

    private MerkleTree convertFromJadeMerkleTree(JMerkleTree merkleTree) {
        return new MerkleTree(merkleTree.getHashes(),
                              merkleTree.getLevelIndex());
    }

    public BlockHeader convertFromJadeBlockHeader(JBlockHeader header) {
        return new BlockHeader(new BigInteger(header.getDifficulty()),
                               header.getBlockheight(),
                               header.getHash(),
                               header.getMerkleRoot(),
                               header.getPreviousHash(),
                               Instant.parse(header.getTimeStamp()),
                               header.getNonce());
    }

    private Coinbase convertFromJadeCoinbase(JCoinbase coinbase) {
        var data = coinbase.getPayoutTXO()
                           .stream()
                           .map(this::convertFromJadeTransactionOutput)
                           .collect(Collectors.toUnmodifiableSet());
        return new Coinbase(data,
                            new BigDecimal(coinbase.getCoinbase()),
                            coinbase.getHashId());
    }

    private TransactionOutput convertFromJadeTransactionOutput(JTransactionOutput txo) {
        return new TransactionOutput(StringUtil.stringToPublicKey(txo.getPubkey()),
                                     txo.getHashId(),
                                     txo.getPrevHash(),
                                     new BigDecimal(txo.getPayout()),
                                     txo.getTx());
    }


    public Transaction convertFromJadeTransaction(JTransaction t) {
        return new Transaction(StringUtil.stringToPublicKey(t.getPublicKey()),
                               convertFromJadeSensorData(t.getSd()),
                               t.getSignature());
    }

    private SensorData convertFromJadeSensorData(JSensorData sd) {
        SensorData sensorData = null;
        switch (sd.getCategory()) {
            case NOISE:
                sensorData = new SensorData(
                        convertFromJadeNoiseData(
                                (JNoiseData) sd.getData()),
                        Instant.parse(sd.getTimestamp()));
                break;
            case TEMPERATURE:
                sensorData = new SensorData(
                        convertFromJadeTemperatureData(
                                (JTemperatureData) sd.getData()),
                        Instant.parse(sd.getTimestamp())
                );
                break;
            case HUMIDITY:
                sensorData = new SensorData(
                        convertFromJadeHumidityData(
                                (JHumidityData) sd.getData()),
                        Instant.parse(sd.getTimestamp()));
                break;
            case LUMINOSITY:
                sensorData = new SensorData(
                        convertFromJadeLuminosityData(
                                (JLuminosityData) sd.getData()),
                        Instant.parse(sd.getTimestamp()));
                break;
            case OTHER:
                sensorData = new SensorData(
                        convertFromJadeOtherData(
                                (JOtherData) sd.getData()),
                        Instant.parse(sd.getTimestamp()));
                break;
        }
        return sensorData;
    }

    private OtherData convertFromJadeOtherData(JOtherData data) {
        var b = new ByteArrayInputStream(data.getData());
        Serializable res;
        try (var ob = new ObjectInputStream(b)) {
            res = (Serializable) ob.readObject();
            return new OtherData<>(res,
                                   new BigDecimal(data.getLat()),
                                   new BigDecimal(data.getLng()));
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.error("", e.getMessage());
            res = new Serializable() {
            };
        }
        return new OtherData<>(res,
                               new BigDecimal(data.getLat()),
                               new BigDecimal(data.getLng()));
    }

    private LuminosityData convertFromJadeLuminosityData(JLuminosityData data) {
        return new LuminosityData(new BigDecimal(data.getLum()),
                                  data.getUnit(),
                                  new BigDecimal(data.getLat()),
                                  new BigDecimal(data.getLng()));
    }

    private HumidityData convertFromJadeHumidityData(JHumidityData data) {
        return new HumidityData(new BigDecimal(data.getHum()),
                                data.getUnit(),
                                new BigDecimal(data.getLat()),
                                new BigDecimal(data.getLng()));
    }

    private TemperatureData convertFromJadeTemperatureData(JTemperatureData data) {
        return new TemperatureData(new BigDecimal(data.getTemperature()),
                                   data.gettUnit(),
                                   new BigDecimal(data.getLat()),
                                   new BigDecimal(data.getLng()));
    }

    private NoiseData convertFromJadeNoiseData(JNoiseData data) {
        NoiseData noiseData = null;
        switch (data.getUnit()) {
            case RMS:
                noiseData = new NoiseData(new BigDecimal(data.getRelativeOrRMS()),
                                          new BigDecimal(data.getPeak()),
                                          new BigDecimal(data.getLat()),
                                          new BigDecimal(data.getLng()));
                break;
            case DBSPL:
                noiseData = new NoiseData(new BigDecimal(data.getRelativeOrRMS()),
                                          new BigDecimal(data.getLat()),
                                          new BigDecimal(data.getLng()));
                break;
        }
        return noiseData;
    }

}
