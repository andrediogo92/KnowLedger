package org.knowledger.ledger.core.adapters

import kotlinx.serialization.Serializable
import org.knowledger.encoding.base64.Base64String
import org.knowledger.ledger.core.data.LedgerData
import org.knowledger.ledger.crypto.Hash

/**
 * A database-safe encoding of an [Hash].
 * Used to uniquely identify [LedgerData] schemas
 * for database use.
 */
@Serializable
data class Tag(val id: Base64String)