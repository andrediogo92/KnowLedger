package org.knowledger.ledger.storage.transaction

import org.knowledger.ledger.storage.HashUpdateable
import org.knowledger.ledger.storage.Indexed
import org.knowledger.ledger.storage.SizeUpdateable

internal interface MutableHashedTransaction : HashedTransaction, Indexed,
                                              HashUpdateable, SizeUpdateable