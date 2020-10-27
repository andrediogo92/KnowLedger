package org.knowledger.ledger.adapters.service

import org.knowledger.ledger.adapters.Loadable
import org.knowledger.ledger.chain.ServiceClass

internal interface ServiceLoadable<out T : ServiceClass> : Loadable<T>