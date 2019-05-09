package pt.um.masb.agent.behaviours

import jade.core.behaviours.Behaviour

class PeerManager(peers: pt.um.masb.agent.data.AgentPeers) : Behaviour() {
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