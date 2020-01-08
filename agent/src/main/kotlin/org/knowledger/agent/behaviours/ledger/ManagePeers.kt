package org.knowledger.agent.behaviours.ledger

import jade.core.Agent
import jade.core.behaviours.TickerBehaviour
import org.knowledger.agent.agents.AgentManager
import org.knowledger.agent.agents.ledger.ChainManager
import org.knowledger.agent.agents.ledger.PeerManager
import org.knowledger.agent.agents.ledger.TransactionManager
import org.tinylog.kotlin.Logger
import java.time.Duration

internal class ManagePeers(
    agent: Agent,
    val agentManager: AgentManager,
    val peerManager: PeerManager,
    val chainManager: ChainManager,
    val transactionManager: TransactionManager
) : TickerBehaviour(
    agent,
    Duration.ofSeconds(1).toMillis()
) {
    enum class PeerMode(val run: ManagePeers.() -> Unit) {
        CheckForNewPeers(ManagePeers::checkDF) {
            override fun next(): PeerMode =
                CheckForDead
        },
        CheckForDead(ManagePeers::checkKnownDead) {
            override fun next(): PeerMode =
                RebootPeers
        },
        RebootPeers(ManagePeers::rebootDF) {
            override fun next(): PeerMode =
                CheckForNewPeers
        };

        abstract fun next(): PeerMode
    }

    private var mode: PeerMode =
        PeerMode.CheckForDead


    override fun onTick() {
        //Simple lookup enum for no ifs.
        var old = mode
        do {
            old.run(this)
            old = old.next()
        } while (old != mode)
    }

    private fun checkDF() {
        Logger.debug("Checking Directory")
    }

    private fun checkKnownDead() {
        Logger.debug("Checking expired peers")
    }

    private fun rebootDF() {
        Logger.debug("Checking reboot of directory")

    }
}