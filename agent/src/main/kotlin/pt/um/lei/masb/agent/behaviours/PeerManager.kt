package pt.um.lei.masb.agent.behaviours

import jade.core.behaviours.Behaviour
import pt.um.lei.masb.agent.data.AgentPeers

class PeerManager(peers: AgentPeers) : Behaviour() {
    private val lookups: Array<() -> Unit> = arrayOf(
        this::checkDF,
        this::checkKnownDead,
        this::rebootDF
    )
    private val mode = 0


    override fun action() {
        //Simple lookup table for no ifs.
        lookups[mode]()
    }

    override fun done(): Boolean =
        mode > 3

    private fun checkDF() {

    }

    private fun checkKnownDead() {

    }

    private fun rebootDF() {

    }
}