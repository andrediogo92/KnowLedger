package pt.um.lei.masb.blockchain.data;

import pt.um.lei.masb.blockchain.Sizeable;

/**
 * Sensor data must be categorized in order to allow serialization and de-serialization to and from JSON.
 * It can't be any arbitrary object.
 *
 * Supported categories for prototyping will be: noise, temperature, humidity and luminosity.
 */
public class SensorData implements Sizeable {
  private final Category category;
  private final NoiseData nd;
  private final TemperatureData td;
  private final HumidityData hd;
  private final LuminosityData ld;

  private SensorData() {
    category = null;
    nd = null;
    td = null;
    hd = null;
    ld = null;
  }

  public SensorData(NoiseData nd) {
    category = Category.NOISE;
    this.nd = nd;
    td = null;
    hd = null;
    ld = null;
  }

  public SensorData(TemperatureData td) {
    category = Category.TEMPERATURE;
    nd = null;
    this.td = td;
    hd = null;
    ld = null;
  }

  public SensorData(HumidityData hd) {
    category = Category.HUMIDITY;
    nd = null;
    td = null;
    this.hd = hd;
    ld = null;
  }

  public SensorData(LuminosityData ld) {
    category = Category.LUMINOSITY;
    nd = null;
    td = null;
    hd = null;
    this.ld = ld;
  }

  public Category getCategory() {
    return category;
  }

  public NoiseData getNoiseData() {
    return nd;
  }

  public TemperatureData getTemperatureData() {
    return td;
  }

  public HumidityData getHumidityData() {
    return hd;
  }

  public LuminosityData getLuminosityData() {
    return ld;
  }

  public int getApproximateSize() {
    switch (category) {
      case NOISE:
        return nd.getApproximateSize();
      case HUMIDITY:
        return hd.getApproximateSize();
      case TEMPERATURE:
        return td.getApproximateSize();
      case LUMINOSITY:
        return ld.getApproximateSize();
      default:
        return 0;
    }
  }
}
