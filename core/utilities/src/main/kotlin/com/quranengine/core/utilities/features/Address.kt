package com.quranengine.core.utilities.features

/**
 * Returns the identity hash code of an object as a hex string.
 * Useful for debugging to distinguish object instances.
 */
fun address(obj: Any): String =
    "0x${Integer.toHexString(System.identityHashCode(obj))}"

/**
 * Returns the class name and identity hash of an object.
 */
fun nameAndAddress(obj: Any): String =
    "${obj::class.simpleName}@${Integer.toHexString(System.identityHashCode(obj))}"
