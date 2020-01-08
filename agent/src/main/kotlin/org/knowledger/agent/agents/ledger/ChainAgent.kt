package org.knowledger.agent.agents.ledger

import jade.core.behaviours.ThreadedBehaviourFactory
import org.knowledger.agent.agents.BaseAgent
import org.knowledger.agent.agents.registerLedger
import org.knowledger.agent.behaviours.ledger.InitializePeering
import org.knowledger.agent.behaviours.ledger.ManagePeers
import org.knowledger.agent.core.ontologies.BlockOntology
import org.knowledger.agent.core.ontologies.LedgerOntology
import org.knowledger.ledger.crypto.service.Identity
import org.knowledger.ledger.data.LedgerData
import org.knowledger.ledger.database.adapters.AbstractStorageAdapter
import org.knowledger.ledger.results.flatMapFailure
import org.knowledger.ledger.results.peekFailure
import org.knowledger.ledger.results.peekSuccess
import org.knowledger.ledger.results.unwrap
import org.knowledger.ledger.service.handles.LedgerHandle
import org.knowledger.ledger.service.results.LedgerFailure

@Suppress("UNCHECKED_CAST")
internal class ChainAgent : BaseAgent() {

    //agent will try maximizing reward
    //sessionRewards could later be translated into user reward
    private var sessionRewards: Double = 0.0

    private val handle: LedgerHandle =
        arguments[1] as LedgerHandle

    private val typesHandled: MutableList<AbstractStorageAdapter<out LedgerData>> =
        arguments[2] as MutableList<AbstractStorageAdapter<out LedgerData>>

    private val identity: Identity =
        handle.getIdentityByTag(name).unwrap()

    private val transactionManager = TransactionManager()
    private val peerManager = PeerManager(aid)
    private val chainManager: ChainManager =
        ChainManager(identity)
    private val threadedBehaviourFactory = ThreadedBehaviourFactory()

    override fun setup() {
        super.setup()
        contentManager.registerOntology(BlockOntology)
        contentManager.registerOntology(LedgerOntology)
        registerLedger()

        typesHandled.forEach { abs ->
            handle
                .getChainHandleOf(abs)
                .flatMapFailure {
                    when (it) {
                        is LedgerFailure.NonExistentData ->
                            handle.registerNewChainHandleOf(abs)
                        is LedgerFailure.UnknownFailure ->
                            it.unwrap()
                        else ->
                            it.unwrap()
                    }
                }.peekFailure {
                    when (it) {
                        is LedgerFailure.UnknownFailure ->
                            it.unwrap()
                        else ->
                            it.unwrap()
                    }
                }.peekSuccess {
                    chainManager.addHandle(it)
                }
        }
        registerBehaviours()
    }

    override fun beforeMove() {
        super.beforeMove()
        agentManager.pruneThreaded()
    }

    override fun afterMove() {
        super.afterMove()
        agentManager.trackThreaded(
            threadedBehaviourFactory.wrap(
                ManagePeers(
                    this, agentManager, peerManager,
                    chainManager, transactionManager
                )
            )
        )
    }

    override fun takeDown() {
        agentManager.pruneAll()
        super.takeDown()
    }

    private fun registerBehaviours(
    ) {
        addBehaviour(
            InitializePeering(
                this, agentManager, peerManager,
                chainManager, transactionManager,
                threadedBehaviourFactory
            )
        )
    }
}
