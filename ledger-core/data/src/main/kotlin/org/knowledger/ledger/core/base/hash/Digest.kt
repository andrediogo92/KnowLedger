package org.knowledger.ledger.core.base.hash

import org.knowledger.ledger.core.base.data.Tag

fun <T : Any> T.classDigest(hasher: Hasher): Tag =
    javaClass.classDigest(hasher)


fun <T : Any> Class<in T>.classDigest(hasher: Hasher): Tag {
    var clazz: Class<*>? = this
    while (
        clazz != null &&
        clazz.isInterface &&
        (!clazz.isMemberClass || !clazz.isLocalClass || !clazz.isAnonymousClass || !clazz.isPrimitive)
    ) {
        clazz = clazz.declaringClass
    }
    return if (clazz != null) {
        hasher.applyHash(
            if (clazz.isPrimitive) {
                clazz.canonicalName
            } else {
                clazz.declaredFields.joinToString {
                    "${it.name}${it.type.classDigest(hasher)}"
                }
            })
    } else {
        throw ClassNotFoundException(
            "Couldn't find a base concrete class for ${javaClass.toGenericString()}: $this"
        )
    }
}

inline fun <reified T : Any> classDigest(hasher: Hasher): Tag =
    T::class.java.classDigest(hasher)