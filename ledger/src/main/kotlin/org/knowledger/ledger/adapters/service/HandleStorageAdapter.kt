package org.knowledger.ledger.adapters.service

import org.knowledger.ledger.adapters.EagerStorable
import org.knowledger.ledger.adapters.StorageAdapter
import org.knowledger.ledger.database.adapters.SchemaProvider
import org.knowledger.ledger.service.handles.LedgerHandle
import org.knowledger.ledger.service.handles.builder.LedgerConfig

internal interface HandleStorageAdapter : StorageAdapter<LedgerConfig, LedgerHandle.Failure>,
                                          HandleLoadable,
                                          EagerStorable<LedgerConfig>,
                                          SchemaProvider