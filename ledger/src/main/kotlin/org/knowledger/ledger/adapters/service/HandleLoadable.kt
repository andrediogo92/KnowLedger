package org.knowledger.ledger.adapters.service

import org.knowledger.ledger.adapters.Loadable
import org.knowledger.ledger.service.handles.LedgerHandle
import org.knowledger.ledger.service.handles.builder.LedgerConfig

internal interface HandleLoadable : Loadable<LedgerConfig, LedgerHandle.Failure>