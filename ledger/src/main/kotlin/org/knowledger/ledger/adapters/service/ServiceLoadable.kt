package org.knowledger.ledger.adapters.service

import org.knowledger.ledger.adapters.Loadable
import org.knowledger.ledger.service.ServiceClass
import org.knowledger.ledger.storage.results.LedgerFailure

internal interface ServiceLoadable<out T : ServiceClass> : Loadable<T, LedgerFailure>