package org.knowledger.ledger.crypto.digest

import org.knowledger.ledger.crypto.Hash
import kotlin.reflect.KClass

internal typealias SchemaHash = Hash
internal typealias InterningMap = MutableMap<InterningEnum, Hash>
internal typealias SecondaryMap = MutableMap<KClass<*>, Hash>
internal typealias BuilderInterningMap = MutableMap<DigestNode, StringBuilder>
