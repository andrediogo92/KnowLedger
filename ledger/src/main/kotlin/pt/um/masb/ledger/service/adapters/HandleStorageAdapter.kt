package pt.um.masb.ledger.service.adapters

import pt.um.masb.common.storage.adapters.SchemaProvider
import pt.um.masb.common.storage.adapters.Storable
import pt.um.masb.ledger.service.LedgerConfig

interface HandleStorageAdapter : HandleLoadable,
                                 Storable<LedgerConfig>,
                                 SchemaProvider<LedgerConfig>