package org.knowledger.agent.agents.ledger

import org.knowledger.ledger.builders.ChainBuilder
import org.knowledger.ledger.builders.chainBuilder
import org.knowledger.ledger.data.Tag
import org.knowledger.ledger.service.Identity
import org.knowledger.ledger.service.handles.ChainHandle

data class ChainManager(val identity: Identity) : ChainResolver {
    private val mChainBuilders = mutableListOf<ChainBuilder>()
    private val mChainHandles = mutableListOf<ChainHandle>()

    val chainHandles: List<ChainHandle>
        get() = mChainHandles
    val chainBuilders: List<ChainBuilder>
        get() = mChainBuilders

    override fun findChain(tag: Tag): ChainHandle? =
        chainHandles.find {
            it.id.tag == tag
        }

    override fun findBuilder(tag: Tag): ChainBuilder? =
        chainBuilders.find {
            it.tag == tag
        }

    fun addHandle(handle: ChainHandle): Boolean {
        mChainHandles.add(handle)
        mChainBuilders.add(handle.chainBuilder(identity))
        return true
    }

}