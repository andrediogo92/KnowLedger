package org.knowledger.agent.data

import jade.core.AID
import org.knowledger.ledger.core.secondsFromNow
import org.knowledger.ledger.data.Tag
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentSkipListSet

/**
 * Class in charge of bookkeeping of agents identities selected for peering.
 */
internal data class PeerBook(
    val id: AID
) {
    private val nPeers: NavigableSet<PeerSet> =
        ConcurrentSkipListSet()

    val peers: Set<PeerSet>
        get() = nPeers


    fun renew(aid: AID, slave: Boolean) {
        nPeers.firstOrNull { it.peer == aid }?.renew()
    }

    fun renewAll() {
        nPeers.forEach {
            it.renew()
        }
    }

    fun renewOrExpire(): Int =
        nPeers.partition {
            it.expired()
        }.let { partition ->
            partition.second.forEach {
                it.renew()
            }
            nPeers.removeAll(partition.first)
            partition.first.size
        }

    fun supportsTag(aid: AID, tag: Tag): Boolean =
        nPeers.find {
            it.peer == aid
        }?.contains(tag) ?: false

    fun peersByTagSupport(tag: Tag): List<AID> =
        nPeers
            .asSequence()
            .filter {
                it.contains(tag)
            }
            .map { it.peer }
            .toList()


    fun registerSet(
        tags: MutableSet<Tag>, peer: AID
    ): Boolean =
        nPeers.add(PeerSet(tags, peer))

    internal data class PeerSet(
        private val tags: MutableSet<Tag>,
        val peer: AID,
        private var expiry: Instant = Instant.now()
    ) : Set<Tag> {
        fun expired(): Boolean =
            expiry.secondsFromNow() > 5

        fun renew() {
            expiry = Instant.now()
        }

        fun removeExpired(tag: Tag): Boolean =
            synchronized(tags) {
                tags.remove(tag)
            }

        override val size: Int
            get() = synchronized(tags) {
                tags.size
            }

        override fun contains(element: Tag): Boolean =
            synchronized(tags) {
                tags.contains(element)
            }

        override fun containsAll(elements: Collection<Tag>): Boolean =
            synchronized(tags) {
                tags.containsAll(elements)
            }

        override fun isEmpty(): Boolean =
            synchronized(tags) {
                tags.isEmpty()
            }

        override fun iterator(): Iterator<Tag> =
            synchronized(tags) {
                tags.iterator()
            }


    }

}