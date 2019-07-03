package org.knowledger.common.data

import org.knowledger.common.Sizeable
import org.knowledger.common.hash.Hashable
import java.io.Serializable

interface LedgerData : SelfInterval,
                       DataCategory,
                       Sizeable,
                       Hashable,
                       Serializable