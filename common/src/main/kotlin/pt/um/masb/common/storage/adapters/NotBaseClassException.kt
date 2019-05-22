package pt.um.masb.common.storage.adapters

/**
 * Thrown in the face of an attempt to auto-derive
 * via reflection an unique name from an anonymous
 * class, which by definition does not have a
 * canonical class name.
 */
class NotBaseClassException : Exception()
