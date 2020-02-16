package org.knowledger.ledger.serial.display

import org.knowledger.ledger.serial.internal.AbstractChainIdSerializer
import org.knowledger.ledger.serial.internal.HashEncodeForDisplay

internal object ChainIdSerializer : AbstractChainIdSerializer(),
                                    HashEncodeForDisplay