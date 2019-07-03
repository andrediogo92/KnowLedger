package org.knowledger.ledger.service

import org.knowledger.common.storage.LedgerContract

data class CategoryTypes(
    internal val categoryTypes: List<String>
) : LedgerContract