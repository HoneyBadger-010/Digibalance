package com.CuriosityLabs.digibalance.util

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Client-side emergency code hashing and verification
 * Since Supabase Free Tier doesn't support BCrypt functions,
 * we handle this on the client side
 */
object EmergencyCodeHelper {
    
    private const val ITERATIONS = 10000
    private const val KEY_LENGTH = 256
    
    /**
     * Hash a password using PBKDF2
     * Format: salt$hash
     */
    fun hashCode(code: String): String {
        val salt = generateSalt()
        val hash = pbkdf2(code, salt)
        return "${bytesToHex(salt)}$${bytesToHex(hash)}"
    }
    
    /**
     * Verify a code against a stored hash
     */
    fun verifyCode(code: String, storedHash: String): Boolean {
        return try {
            val parts = storedHash.split("$")
            if (parts.size != 2) return false
            
            val salt = hexToBytes(parts[0])
            val expectedHash = hexToBytes(parts[1])
            val actualHash = pbkdf2(code, salt)
            
            MessageDigest.isEqual(expectedHash, actualHash)
        } catch (e: Exception) {
            false
        }
    }
    
    private fun generateSalt(): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return salt
    }
    
    private fun pbkdf2(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return factory.generateSecret(spec).encoded
    }
    
    private fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02x".format(it) }
    }
    
    private fun hexToBytes(hex: String): ByteArray {
        return hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }
}
