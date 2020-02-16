package org.knowledger.ledger.serial.binary

import org.knowledger.ledger.serial.internal.AbstractChainIdSerializer
import org.knowledger.ledger.serial.internal.HashEncodeInBytes

internal object ChainIdByteSerializer : AbstractChainIdSerializer(),
                                        HashEncodeInBytes