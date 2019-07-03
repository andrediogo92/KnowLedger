package org.knowledger.agent.messaging

import jade.content.AgentAction
import jade.content.onto.basic.Action
import jade.core.AID

interface Actionable : AgentAction {
    fun wrap(aid: AID): Action =
        Action(aid, this)
}