package org.knowledger.ledger.service.adapters

import org.knowledger.ledger.adapters.EagerStorable
import org.knowledger.ledger.core.storage.adapters.SchemaProvider
import org.knowledger.ledger.service.LedgerConfig

interface HandleStorageAdapter : HandleLoadable,
                                 EagerStorable<LedgerConfig>,
                                 SchemaProvider<LedgerConfig>