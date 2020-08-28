package org.knowledger.ledger.storage.cache

import org.knowledger.ledger.storage.AdapterIds

interface LinkedPairs {
    val key: String
    val element: Any
    val adapterId: AdapterIds
}