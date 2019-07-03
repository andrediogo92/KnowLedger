package org.knowledger.ledger.service.adapters

import org.knowledger.common.storage.adapters.SchemaProvider
import org.knowledger.common.storage.adapters.Storable
import org.knowledger.ledger.service.LedgerConfig

interface HandleStorageAdapter : HandleLoadable,
                                 Storable<LedgerConfig>,
                                 SchemaProvider<LedgerConfig>