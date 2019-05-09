package pt.um.masb.agent

import com.squareup.moshi.Moshi
import jade.core.Agent
import mu.KLogging
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import pt.um.masb.agent.data.feed.AdafruitPublish
import pt.um.masb.agent.data.feed.Reduxer
import pt.um.masb.common.Hash
import pt.um.masb.common.results.Failable
import pt.um.masb.ledger.Block
import pt.um.masb.ledger.Transaction
import pt.um.masb.ledger.data.PhysicalData
import pt.um.masb.ledger.results.checkSealed
import pt.um.masb.ledger.service.ChainHandle
import pt.um.masb.ledger.service.LedgerHandle
import pt.um.masb.ledger.service.results.LedgerListResult
import pt.um.masb.ledger.service.results.LoadResult
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
        logger.info("Connecting to broker: $broker")
        mqttClient.connect(connOpts, guardCounter, MonitorCallback())
        logger.info("Connected")
        var chains: List<ChainHandle> = listOf()
        val res = ledgerHandle.knownChains
        when (res) {
            is LedgerListResult.Success -> chains = res.data
            is LedgerListResult.QueryFailure,
            is LedgerListResult.NonExistentData,
            is LedgerListResult.NonMatchingCrypter,
            is LedgerListResult.UnregisteredCrypter,
            is LedgerListResult.Propagated -> logger.error {
                (res as Failable).cause
            }
        }.checkSealed()
        for (cl in chains) {
            var i = 0L
            var fail = false
            while (!fail) {
                fail = tryLoad(cl.clazz, cl, i, 0)
            }
        }
        mqttClient.disconnect()
        logger.info("Disconnected")
    }

    private tailrec fun tryLoad(cl: String, ch: ChainHandle, i: Long, l: Int): Boolean =
        if (l < 3) {
            val bl = ch.getBlockByHeight(i)
            when (bl) {
                is LoadResult.NonExistentData ->
                    false
                is LoadResult.Success -> {
                    publishToFeed(cl, bl.data)
                    true
                }
                is LoadResult.QueryFailure -> {
                    tryLoad(cl, ch, i, l + 1)
                }
                is Failable -> {
                    logger.warn { bl.cause }
                    true
                }
                else ->
                    true
            }
        } else {
            false
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
        val adapter = moshi.adapter<AdafruitPublish>(AdafruitPublish::class.java)
        val content = adapter.toJson(json)
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

