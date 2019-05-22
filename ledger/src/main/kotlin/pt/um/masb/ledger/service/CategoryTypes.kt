package pt.um.masb.ledger.service

import pt.um.masb.common.storage.LedgerContract

data class CategoryTypes(
    internal val categoryTypes: List<String>
) : LedgerContract