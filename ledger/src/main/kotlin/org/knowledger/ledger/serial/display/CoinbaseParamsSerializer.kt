package org.knowledger.ledger.serial.display

import org.knowledger.ledger.serial.internal.AbstractCoinbaseParamsSerializer
import org.knowledger.ledger.serial.internal.HashEncodeForDisplay

internal object CoinbaseParamsSerializer : AbstractCoinbaseParamsSerializer(),
                                           HashEncodeForDisplay