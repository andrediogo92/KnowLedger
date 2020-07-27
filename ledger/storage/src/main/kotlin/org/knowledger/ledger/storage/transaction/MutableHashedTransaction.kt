package org.knowledger.ledger.storage.transaction

import org.knowledger.ledger.storage.mutations.HashUpdateable
import org.knowledger.ledger.storage.mutations.Indexed
import org.knowledger.ledger.storage.mutations.SizeUpdateable

interface MutableHashedTransaction : HashedTransaction,
                                     Indexed,
                                     HashUpdateable,
                                     SizeUpdateable