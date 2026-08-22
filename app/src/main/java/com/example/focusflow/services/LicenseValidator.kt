package com.example.focusflow.services

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object LicenseValidator {
    private const val SECRET = "FocusFlow-RuStore-2026-Secret"

    fun validate(code: String): Boolean {
        val normalized = code.trim().uppercase().replace(" ", "")
        if (normalized.isEmpty()) return false
        
        val parts = normalized.split("-")
        if (parts.size != 4 || parts.any { it.length != 4 }) return false
        
        val payload = parts.dropLast(1).joinToString("-")
        val checksum = parts.last()
        
        return expectedChecksum(payload).equals(checksum, ignoreCase = true)
    }

    fun generateCode(payload: String): String {
        val clean = payload.uppercase().replace("-", "").take(12).padEnd(12, 'X')
        val formatted = "${clean.take(4)}-${clean.drop(4).take(4)}-${clean.drop(8).take(4)}"
        return "$formatted-${expectedChecksum(formatted)}"
    }

    private fun expectedChecksum(payload: String): String {
        return try {
            val mac = Mac.getInstance("HmacSHA256")
            mac.init(SecretKeySpec(SECRET.toByteArray(Charsets.UTF_8), "HmacSHA256"))
            val bytes = mac.doFinal(payload.toByteArray(Charsets.UTF_8))
            bytes.take(2).joinToString("") { String.format("%02X", it.toInt() and 0xFF) }
        } catch (e: Exception) {
            "0000"
        }
    }
}