package org.knowledger.ledger.service

import org.knowledger.ledger.storage.LedgerContract


data class CategoryTypes(
    internal val categoryTypes: List<String>
) : LedgerContract