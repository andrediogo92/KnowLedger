package org.knowledger.ledger.core.data

import org.knowledger.ledger.core.serial.HashSerializable

interface LedgerData : SelfInterval,
                       DataCategory,
                       HashSerializable