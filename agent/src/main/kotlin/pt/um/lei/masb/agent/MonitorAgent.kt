package pt.um.lei.masb.agent

import jade.core.Agent
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.JSON
import kotlinx.serialization.serializer
import mu.KLogging
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import pt.um.lei.masb.agent.data.feed.AdafruitPublishJSON
import pt.um.lei.masb.agent.data.feed.Reduxer
import pt.um.lei.masb.blockchain.Block
import pt.um.lei.masb.blockchain.BlockChain
import pt.um.lei.masb.blockchain.Hash
import pt.um.lei.masb.blockchain.Transaction
import pt.um.lei.masb.blockchain.data.PhysicalData
import java.util.*
import java.util.concurrent.atomic.AtomicLong

class MonitorAgent(
    id: Hash,
    private val reduxers: Map<String, Reduxer>
) : Agent() {
    private val bc: BlockChain? = BlockChain.getBlockChainByHash(hash = id)
    private var json: AdafruitPublishJSON = AdafruitPublishJSON()
    private val broker = "tcp://io.adafruit.com:1883"
    private val mqttClient: MqttAsyncClient = MqttAsyncClient(
        broker,
        "MASBlockchain",
        MemoryPersistence()
    )
    private val guardCounter: AtomicLong = AtomicLong(0)


    @ImplicitReflectionSerializer
    public override fun setup() {
        if (bc != null) {
            val connOpts = MqttConnectOptions()
            connOpts.userName = "MASBlockchain"
            connOpts.password = "312758ce04d64a6c80fa169860489b6d".toCharArray()
            connOpts.sslProperties = Properties()
            logger.info("Connecting to broker: $broker")
            mqttClient.connect(connOpts, guardCounter, MonitorCallback())
            logger.info("Connected")
            val clazzes = bc.sidechains.keys
            for (cl in clazzes) {
                val sc = bc.sidechains[cl]
                var i = 0L
                do {
                    val bl = sc?.getBlockByHeight(i)?.let {
                        publishToFeed(cl, it)
                        it
                    }
                    i++
                } while (bl != null)

            }
        }
        mqttClient.disconnect()
        logger.info("Disconnected")
    }

    @ImplicitReflectionSerializer
    private fun publishToFeed(cl: String, bl: Block) {
        if (reduxers.containsKey(cl)) {
            val redux = reduxers[cl]!!
            for (dt in bl.data) {
                publishTransaction(redux, dt)
            }
        }
    }

    @ImplicitReflectionSerializer
    private fun publishTransaction(rx: Reduxer, dt: Transaction) {
        val topic = "MASBlockchain/feeds/${rx.type()}/json"
        setData(rx, dt.data)
        val content = JSON.stringify(AdafruitPublishJSON::class.serializer(), json)
        val qos = 2

        while (guardCounter.get() >= 20) {
            this.doWait()
        }
        try {
            logger.info("Publishing message: $content")
            val message = MqttMessage(content.toByteArray())
            message.qos = qos
            mqttClient.publish(topic, message)
        } catch (me: MqttException) {
            logger.error(me) {}
        }
    }


    /**
     * Collect all the data into the JSON adapter class
     *
     * @param t The sensor data to fill in
     */
    private fun setData(rx: Reduxer, t: PhysicalData) {
        json.createdAt = t.instant.toString()
        json.lat = t.geoCoords?.latitude.toString()
        json.lon = t.geoCoords?.longitude.toString()
        json.alt = t.geoCoords?.altitude.toString()
        json.value = rx.reduce(t.data)
    }

    private inner class MonitorCallback : IMqttActionListener {
        override fun onSuccess(token: IMqttToken) {
            val guardCounter = token.userContext as AtomicLong
            guardCounter.decrementAndGet()
            logger.info { "Message ${token.messageId} sent." }
            this@MonitorAgent.doWake()
        }

        override fun onFailure(
            token: IMqttToken,
            exception: Throwable
        ) {
            val guardCounter = token.userContext as AtomicLong
            guardCounter.decrementAndGet()
            logger.error(exception) { "Message ${token.messageId} excepted." }
            this@MonitorAgent.doWake()
        }
    }

    companion object : KLogging()
}

