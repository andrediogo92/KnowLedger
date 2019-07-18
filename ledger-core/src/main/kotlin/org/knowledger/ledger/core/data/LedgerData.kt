package org.knowledger.ledger.core.data

import org.knowledger.ledger.core.hash.Hashable
import java.io.Serializable

interface LedgerData : SelfInterval,
                       DataCategory,
                       org.knowledger.ledger.core.Sizeable,
                       Hashable,
                       Serializable