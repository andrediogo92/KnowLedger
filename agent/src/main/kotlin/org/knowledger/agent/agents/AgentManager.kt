package org.knowledger.agent.agents

import jade.core.Agent
import jade.core.behaviours.Behaviour
import jade.core.behaviours.ThreadedBehaviourFactory

data class AgentManager(
    val agent: Agent,
    private val activeBehaviours: MutableSet<Behaviour> = mutableSetOf(),
    private val threadedBehaviours: MutableSet<Behaviour> = mutableSetOf()
) {
    private val threadedBehaviourFactory = ThreadedBehaviourFactory()

    val active: Set<Behaviour>
        get() = activeBehaviours

    val threaded: Set<Behaviour>
        get() = threadedBehaviours

    fun trackBehaviour(b: Behaviour) {
        activeBehaviours.add(b)
    }

    fun trackThreaded(b: Behaviour) {
        threadedBehaviours.add(b)
    }

    fun pruneBehaviour(b: Behaviour) {
        activeBehaviours.remove(b)
        agent.removeBehaviour(b)
    }

    fun pruneBehaviours(b: Iterable<Behaviour>) {
        activeBehaviours.removeAll(b)
        b.forEach {
            agent.removeBehaviour(it)
        }
    }

    fun pruneBehaviours() {
        active.forEach {
            agent.removeBehaviour(it)
        }
        activeBehaviours.clear()
    }


    fun pruneThreaded(b: Behaviour) {
        threadedBehaviours.remove(b)
        threadedBehaviourFactory.interrupt(b)
    }

    fun pruneThreaded(threaded: Iterable<Behaviour>) {
        threadedBehaviours.removeAll(threaded)
        threaded.forEach {
            threadedBehaviourFactory.interrupt(it)
        }
    }

    fun pruneThreaded() {
        threadedBehaviours.clear()
        threadedBehaviourFactory.interrupt()
    }


    fun pruneAll() {
        activeBehaviours.forEach {
            agent.removeBehaviour(it)
        }
        activeBehaviours.clear()
        threadedBehaviours.clear()
        threadedBehaviourFactory.interrupt()
    }

}