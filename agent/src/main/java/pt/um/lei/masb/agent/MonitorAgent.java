package pt.um.lei.masb.agent;
import com.google.gson.Gson;
import jade.core.Agent;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import pt.um.lei.masb.blockchain.BlockChain;
import pt.um.lei.masb.blockchain.Transaction;
import pt.um.lei.masb.blockchain.data.Category;
import pt.um.lei.masb.blockchain.data.SensorData;

import java.util.List;
import java.util.Properties;

public class MonitorAgent extends Agent {
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
                System.out.println("Connecting to broker: " + broker);
                sampleClient.connect(connOpts);
                System.out.println("Connected");
                System.out.println("Publishing message: " + content);
                MqttMessage message = new MqttMessage(content.getBytes());
                message.setQos(qos);
                sampleClient.publish(topic, message);
                System.out.println("Message published");
                sampleClient.disconnect();
                System.out.println("Disconnected");
                System.exit(0);
            } catch (MqttException me) {
                System.out.println("reason " + me.getReasonCode());
                System.out.println("msg " + me.getMessage());
                System.out.println("loc " + me.getLocalizedMessage());
                System.out.println("cause " + me.getCause());
                System.out.println("excep " + me);
                me.printStackTrace();

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
                json.setLat(t.getNoiseData().getLat().toString());
                json.setLon(t.getNoiseData().getLng().toString());
                type = "SoundSensor";
                break;
            case HUMIDITY:
                json.setValue(String.valueOf(t.getHumidityData().getHum()));
                json.setCreated_at(t.getTimestamp().toString());
                json.setLat(t.getHumidityData().getLat().toString());
                json.setLon(t.getHumidityData().getLng().toString());
                type = "HumiditySensor";
                break;
            case LUMINOSITY:
                json.setValue(String.valueOf(t.getLuminosityData().getLum()));
                json.setCreated_at(t.getTimestamp().toString());
                json.setLat(t.getLuminosityData().getLat().toString());
                json.setLon(t.getLuminosityData().getLng().toString());
                type = "LuminositySensor";
                break;
            case TEMPERATURE:
                json.setValue(String.valueOf(t.getTemperatureData().getTemperature()));
                json.setCreated_at(t.getTimestamp().toString());
                json.setLat(t.getTemperatureData().getLat().toString());
                json.setLon(t.getTemperatureData().getLng().toString());
                json.setLon(t.getTemperatureData().getLng().toString());
                type = "TemperatureSensor";
                break;
            case OTHER:
                //This one we actually have no guarantees on how to publish, so better not.
                break;
        }
    }
}
