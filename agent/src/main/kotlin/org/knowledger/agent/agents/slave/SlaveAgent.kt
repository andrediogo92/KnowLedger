package org.knowledger.agent.agents.slave

import jade.content.lang.sl.SLCodec
import jade.domain.DFService
import jade.domain.FIPAException
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.modules.SerializersModule
import org.knowledger.agent.agents.BaseAgent
import org.knowledger.agent.agents.registerSlave
import org.knowledger.agent.behaviours.slave.AcceptConnections
import org.knowledger.agent.behaviours.slave.PropagateData
import org.knowledger.agent.data.PeerBook
import org.knowledger.ledger.core.adapters.AbstractStorageAdapter
import org.knowledger.ledger.core.base.data.LedgerData
import org.tinylog.kotlin.Logger

abstract class SlaveAgent : BaseAgent() {
    private val codec = SLCodec()

    @Suppress("UNCHECKED_CAST")
    protected val typeAdapters: Set<AbstractStorageAdapter<out LedgerData>> =
        arguments[0] as Set<AbstractStorageAdapter<out LedgerData>>

    protected val encoder = Cbor(context = SerializersModule {
        typeAdapters.forEach { adapter ->
            adapter.clazz to adapter.serializer
        }
    })
    private val peerBook: PeerBook = PeerBook(aid)
    protected val dataManager: DataManager =
        DataManager()

    override fun setup() {
        super.setup()
        registerSlave()
        registerSlaveOnlyBehaviours()
        registerBehaviours()
    }

    private fun registerSlaveOnlyBehaviours() {
        addBehaviour(
            PropagateData(this, peerBook, agentManager, dataManager, typeAdapters, encoder)
        )
        addBehaviour(
            AcceptConnections(this, peerBook)
        )
    }

    abstract fun registerBehaviours()

    override fun takeDown() {
        super.takeDown()
        try {
            DFService.deregister(this)
        } catch (ex: FIPAException) {
            Logger.error(ex)
        }
    }

}