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
import org.knowledger.ledger.core.base.data.LedgerData
import org.knowledger.ledger.database.adapters.AbstractStorageAdapter
import org.tinylog.kotlin.Logger

abstract class SlaveAgent : BaseAgent() {
    private val codec = SLCodec()

    protected val typeAdapter: AbstractStorageAdapter<out LedgerData> =
        arguments[0] as AbstractStorageAdapter<out LedgerData>
    protected val encoder = Cbor(context = SerializersModule {
        typeAdapter.clazz to typeAdapter.serializer
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
            PropagateData(this, peerBook, agentManager, dataManager, typeAdapter, encoder)
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