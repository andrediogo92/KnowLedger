package org.knowledger.ledger.service.adapters

import org.knowledger.ledger.adapters.EagerStorable
import org.knowledger.ledger.database.adapters.SchemaProvider
import org.knowledger.ledger.service.handles.builder.LedgerConfig

internal interface HandleStorageAdapter : HandleLoadable,
                                          EagerStorable<LedgerConfig>,
                                          SchemaProvider<LedgerConfig>