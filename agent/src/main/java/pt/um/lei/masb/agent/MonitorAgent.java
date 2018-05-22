package pt.um.lei.masb.agent;
import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import jade.core.Agent;
import pt.um.lei.masb.blockchain.BlockChain;
import pt.um.lei.masb.blockchain.Transaction;

import java.util.Properties;

public class MonitorAgent extends Agent {
    private BlockChain bc;

    @Override
    public void setup() {
        var g=new Gson();
        var json = new AdafruitPublishJSON();
        String type="Other";
        while(true) {
            Transaction tl[]=bc.getLastBlock().getData();
            for(Transaction t : tl){
                switch (t.getSensorData().getCategory()){
                    case NOISE:
                        json.setValue(t.getSensorData().getNoiseData().toString());
                        type="SoundSensor";
                        break;
                    case HUMIDITY:
                        json.setValue(t.getSensorData().getHumidityData().toString());
                        type="HumiditySensor";
                        break;
                    case LUMINOSITY:
                        json.setValue(t.getSensorData().getLuminosityData().toString());
                        type="LuminositySensor";
                        break;
                    case TEMPERATURE:
                        json.setValue(t.getSensorData().getTemperatureData().toString());
                        type="TemperatureSensor";
                        break;
                    case OTHER:
                        json.setValue(t.getSensorData().getOtherData().toString());
                        break;
                }
            }

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
}
