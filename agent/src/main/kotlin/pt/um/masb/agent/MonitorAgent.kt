package pt.um.masb.agent

import com.squareup.moshi.Moshi
import io.ktor.util.Hash
import jade.core.Agent
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.tinylog.kotlin.Logger
import pt.um.masb.agent.data.feed.AdafruitPublish
import pt.um.masb.agent.data.feed.Reduxer
import pt.um.masb.common.results.reduce
import pt.um.masb.common.results.unwrap
import pt.um.masb.ledger.data.PhysicalData
import pt.um.masb.ledger.service.ChainHandle
import pt.um.masb.ledger.service.LedgerHandle
import pt.um.masb.ledger.service.results.LoadFailure
import pt.um.masb.ledger.storage.Block
import pt.um.masb.ledger.storage.Transaction
import java.util.*
import java.util.concurrent.atomic.AtomicLong

class MonitorAgent(
    id: Hash,
    private val reduxers: Map<String, Reduxer>,
    private val ledgerHandle: LedgerHandle
) : Agent() {
    private var json: AdafruitPublish = AdafruitPublish()
    private var moshi = Moshi.Builder().build()
    private val broker = "tcp://io.adafruit.com:1883"
    private val mqttClient: MqttAsyncClient = MqttAsyncClient(
        broker,
        "MASBlockchain",
        MemoryPersistence()
    )
    private val guardCounter: AtomicLong = AtomicLong(0)


    public override fun setup() {
        val connOpts = MqttConnectOptions()
        connOpts.userName = "MASBlockchain"
        connOpts.password = "312758ce04d64a6c80fa169860489b6d".toCharArray()
        connOpts.sslProperties = Properties()
        Logger.info("Connecting to broker: $broker")
        mqttClient.connect(connOpts, guardCounter, MonitorCallback())
        Logger.info("Connected")
        for (cl in ledgerHandle.knownChains.unwrap()) {
            val i = 0L
            var fail = false
            while (!fail) {
                fail = tryLoad(cl.clazz, cl, i)
            }
        }
        mqttClient.disconnect()
        Logger.info("Disconnected")
    }

    private fun tryLoad(
        cl: String, ch: ChainHandle,
        i: Long
    ): Boolean {
        var l = 0L
        var result = true
        while (l < 3 && result) {
            ch.getBlockByHeight(i).reduce(
                {
                    publishToFeed(cl, it)
                },
                {
                    when (it) {
                        is LoadFailure.NonExistentData ->
                            result = false
                        is LoadFailure.UnknownFailure -> {
                            l += 1
                        }
                        else -> {
                            Logger.warn {
                                it.cause
                            }
                        }
                    }
                }
            )
        }
        return if (l >= 3) {
            false
        } else {
            result
        }
    }

    private fun publishToFeed(cl: String, bl: Block) {
        if (reduxers.containsKey(cl)) {
            val redux = reduxers.getValue(cl)
            for (dt in bl.data) {
                publishTransaction(redux, dt)
            }
        }
    }

    private fun publishTransaction(rx: Reduxer, dt: Transaction) {
        val topic = "MASBlockchain/feeds/${rx.type()}/json"
        setData(rx, dt.data)
        val adapter = moshi.adapter(AdafruitPublish::class.java)
        val content = adapter.toJson(json)
        val qos = 2

        while (guardCounter.get() >= 20) {
            this.doWait()
        }
        try {
            Logger.info("Publishing message: $content")
            val message = MqttMessage(content.toByteArray())
            message.qos = qos
            mqttClient.publish(topic, message)
        } catch (me: MqttException) {
            Logger.error(me)
        }
    }


    /**
     * Collect all the store into the JSON adapter class
     *
     * @param t The sensor store to fill in
     */
    private fun setData(rx: Reduxer, t: PhysicalData) {
        json.created_at = t.instant.toString()
        json.lat = t.geoCoords?.latitude.toString()
        json.lon = t.geoCoords?.longitude.toString()
        json.alt = t.geoCoords?.altitude.toString()
        json.value = rx.reduce(t.data)
    }

    private inner class MonitorCallback : IMqttActionListener {
        override fun onSuccess(token: IMqttToken) {
            val guardCounter = token.userContext as AtomicLong
            guardCounter.decrementAndGet()
            Logger.info { "Message ${token.messageId} sent." }
            this@MonitorAgent.doWake()
        }

        override fun onFailure(
            token: IMqttToken,
            exception: Throwable
        ) {
            val guardCounter = token.userContext as AtomicLong
            guardCounter.decrementAndGet()
            Logger.error(exception) { "Message ${token.messageId} excepted." }
            this@MonitorAgent.doWake()
        }
    }
}

