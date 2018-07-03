package pt.um.lei.masb.agent;
import com.google.gson.Gson;
import jade.core.Agent;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.um.lei.masb.agent.data.feed.AdafruitPublishJSON;
import pt.um.lei.masb.blockchain.BlockChain;
import pt.um.lei.masb.blockchain.Transaction;
import pt.um.lei.masb.blockchain.data.Category;
import pt.um.lei.masb.blockchain.data.SensorData;

import java.util.List;
import java.util.Properties;

public class MonitorAgent extends Agent {
    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorAgent.class);
    private BlockChain bc;
    private AdafruitPublishJSON json;
    private String type;

    @Override
    public void setup() {
        var g=new Gson();
        json = new AdafruitPublishJSON();
        while(true) {
            List<Transaction> tl = bc.getLastBlock().getData();
            tl.stream()
              .map(Transaction::getSensorData)
              .filter(t -> t.getCategory() == Category.OTHER)
              .forEach(this::setData);
            String topic = "MASBlockchain/feeds/"+type+"/json";
            String content = g.toJson(new AdafruitPublishJSON());
            int qos = 2;
            String broker = "tcp://io.adafruit.com:1883";
            String clientId = "MASBlockchain";
            MemoryPersistence persistence = new MemoryPersistence();

            try {
                MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
                MqttConnectOptions connOpts = new MqttConnectOptions();
                connOpts.setCleanSession(true);
                connOpts.setUserName("MASBlockchain");
                connOpts.setPassword("312758ce04d64a6c80fa169860489b6d".toCharArray());
                connOpts.setSSLProperties(new Properties());
                LOGGER.info("Connecting to broker: " + broker);
                sampleClient.connect(connOpts);
                LOGGER.info("Connected");
                LOGGER.info("Publishing message: " + content);
                MqttMessage message = new MqttMessage(content.getBytes());
                message.setQos(qos);
                sampleClient.publish(topic, message);
                LOGGER.info("Message published");
                sampleClient.disconnect();
                LOGGER.info("Disconnected");
                System.exit(0);
            } catch (MqttException me) {
                LOGGER.error("", me);

            }
        }
    }

    /**
     * Collect all the data into the JSON adapter class
     *
     * @param t The sensor data to fill in
     */
    private void setData(SensorData t) {
        switch (t.getCategory()) {
            case NOISE:
                json.setValue(String.valueOf(t.getNoiseData().getNoiseLevel()));
                json.setCreated_at(t.getTimestamp().toString());
                json.setLat(t.getNoiseData().getLatitude().toString());
                json.setLon(t.getNoiseData().getLongitude().toString());
                type = "SoundSensor";
                break;
            case HUMIDITY:
                json.setValue(String.valueOf(t.getHumidityData().getHum()));
                json.setCreated_at(t.getTimestamp().toString());
                json.setLat(t.getHumidityData().getLatitude().toString());
                json.setLon(t.getHumidityData().getLongitude().toString());
                type = "HumiditySensor";
                break;
            case LUMINOSITY:
                json.setValue(String.valueOf(t.getLuminosityData().getLum()));
                json.setCreated_at(t.getTimestamp().toString());
                json.setLat(t.getLuminosityData().getLatitude().toString());
                json.setLon(t.getLuminosityData().getLongitude().toString());
                type = "LuminositySensor";
                break;
            case TEMPERATURE:
                json.setValue(String.valueOf(t.getTemperatureData().getTemperature()));
                json.setCreated_at(t.getTimestamp().toString());
                json.setLat(t.getTemperatureData().getLatitude().toString());
                json.setLon(t.getTemperatureData().getLongitude().toString());
                json.setLon(t.getTemperatureData().getLongitude().toString());
                type = "TemperatureSensor";
                break;
            case OTHER:
                //This one we actually have no guarantees on how to publish, so better not.
                break;
        }
    }
}
