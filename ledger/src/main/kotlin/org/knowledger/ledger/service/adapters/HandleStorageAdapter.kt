package org.knowledger.ledger.service.adapters

import org.knowledger.ledger.core.storage.adapters.SchemaProvider
import org.knowledger.ledger.core.storage.adapters.Storable
import org.knowledger.ledger.service.LedgerConfig

interface HandleStorageAdapter : HandleLoadable,
                                 Storable<LedgerConfig>,
                                 SchemaProvider<LedgerConfig>