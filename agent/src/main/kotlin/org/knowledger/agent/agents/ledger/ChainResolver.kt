package org.knowledger.agent.agents.ledger

import org.knowledger.base64.base64DecodedToHash
import org.knowledger.ledger.builders.ChainBuilder
import org.knowledger.ledger.data.Tag
import org.knowledger.ledger.service.handles.ChainHandle

interface ChainResolver {
    fun findChain(tag: Tag): ChainHandle?
    fun findChain(tag: String): ChainHandle? =
        findChain(tag.base64DecodedToHash())

    fun findBuilder(tag: Tag): ChainBuilder?
    fun findBuilder(tag: String): ChainBuilder? =
        findBuilder(tag.base64DecodedToHash())

}