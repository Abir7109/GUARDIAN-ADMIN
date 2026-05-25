package com.securphone.app.utils

import java.security.MessageDigest
import java.security.SecureRandom

object CryptoUtils {
    fun generateSalt(): String {
        val random = SecureRandom()
        val saltBytes = ByteArray(16)
        random.nextBytes(saltBytes)
        return saltBytes.joinToString("") { "%02x".format(it) }
    }

    fun hashPin(pin: String, salt: String): String {
        val input = "$salt$pin"
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPin(pin: String, salt: String, expectedHash: String): Boolean {
        return hashPin(pin, salt) == expectedHash
    }
}
