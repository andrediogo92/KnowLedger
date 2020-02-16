package org.knowledger.ledger.serial.binary

import org.knowledger.ledger.serial.internal.AbstractCoinbaseParamsSerializer
import org.knowledger.ledger.serial.internal.HashEncodeInBytes

internal object CoinbaseParamsByteSerializer : AbstractCoinbaseParamsSerializer(),
                                               HashEncodeInBytes