package org.knowledger.agent.agents

import jade.core.Agent
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.SerialModule
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.knowledger.agent.feed.AdafruitPublish
import org.knowledger.agent.feed.Reduxer
import org.knowledger.base64.base64Encoded
import org.knowledger.ledger.core.data.PhysicalData
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.results.reduce
import org.knowledger.ledger.results.unwrap
import org.knowledger.ledger.service.handles.ChainHandle
import org.knowledger.ledger.service.handles.LedgerHandle
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.block.Block
import org.knowledger.ledger.storage.transaction.HashedTransaction
import org.tinylog.kotlin.Logger
import java.util.*
import java.util.concurrent.atomic.AtomicLong

@UnstableDefault
data class MonitorAgent(
    val id: Hash,
    val serialModule: SerialModule,
    private val reduxers: Map<String, Reduxer>,
    private val ledgerHandle: LedgerHandle,
    private val window: Long = 30
) : Agent() {
    private var publish: AdafruitPublish = AdafruitPublish()
    private var json = Json(
        JsonConfiguration.Default,
        serialModule
    )
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
        mqttClient.connect(connOpts)
        Logger.info("Connected")
        for (cl in ledgerHandle.knownChains.unwrap()) {
            val i = 0L
            var fail = false
            while (!fail) {
                fail = tryLoad(cl.id.tag.base64Encoded(), cl, i)
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
                                it.failable.cause
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
            for (dt in bl.transactions) {
                publishTransaction(redux, dt)
            }
        }
    }

    private fun publishTransaction(rx: Reduxer, dt: HashedTransaction) {
        val topic = "MASBlockchain/feeds/${rx.type()}/json"
        setData(rx, dt.data)
        val content = json.stringify(AdafruitPublish.serializer(), publish)
        val qos = 2

        while (guardCounter.get() >= window) {
            doWait()
        }
        try {
            Logger.info("Publishing message: $content")
            val message = MqttMessage(content.toByteArray())
            message.qos = qos
            mqttClient.publish(topic, message, null, MonitorCallback())
            guardCounter.incrementAndGet()
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
        publish.created_at = t.instant.toString()
        publish.lat = t.coords.latitude.toString()
        publish.lon = t.coords.longitude.toString()
        publish.alt = t.coords.altitude.toString()
        publish.value = rx.reduce(t.data)
    }

    private inner class MonitorCallback : IMqttActionListener {
        override fun onSuccess(token: IMqttToken) {
            guardCounter.decrementAndGet()
            Logger.info { "Message ${token.messageId} sent." }
            this@MonitorAgent.doWake()
        }

        override fun onFailure(
            token: IMqttToken,
            exception: Throwable
        ) {
            guardCounter.decrementAndGet()
            Logger.error(exception) { "Message ${token.messageId} excepted." }
            this@MonitorAgent.doWake()
        }
    }

    companion object {
        const val broker = "tcp://io.adafruit.com:1883"
    }
}

