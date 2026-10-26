package com.example.crypto

import java.nio.ByteBuffer
import java.util.Locale
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object TotpEngine {

    private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
    private const val TIME_STEP_SECONDS = 30L

    data class TotpResult(
        val code: String,
        val remainingSeconds: Int
    )

    fun decodeBase32(base32: String): ByteArray {
        val cleaned = base32.uppercase(Locale.US).replace("[^A-Z2-7]".toRegex(), "")
        if (cleaned.isEmpty()) return ByteArray(0)

        var bits = 0
        var value = 0
        val output = mutableListOf<Byte>()

        for (ch in cleaned) {
            val idx = ALPHABET.indexOf(ch)
            if (idx == -1) continue
            value = (value shl 5) or idx
            bits += 5
            if (bits >= 8) {
                output.add(((value ushr (bits - 8)) and 0xFF).toByte())
                bits -= 8
            }
        }
        return output.toByteArray()
    }

    fun generateTotp(secretBase32: String, timestampMs: Long = System.currentTimeMillis()): TotpResult {
        val keyBytes = decodeBase32(secretBase32)
        if (keyBytes.isEmpty()) {
            throw IllegalArgumentException("Invalid Base32 Secret")
        }

        val epochSeconds = timestampMs / 1000L
        val counter = epochSeconds / TIME_STEP_SECONDS
        val remaining = (TIME_STEP_SECONDS - (epochSeconds % TIME_STEP_SECONDS)).toInt()

        val buffer = ByteBuffer.allocate(8)
        buffer.putLong(counter)
        val counterBytes = buffer.array()

        val mac = Mac.getInstance("HmacSHA1")
        val keySpec = SecretKeySpec(keyBytes, "RAW")
        mac.init(keySpec)
        val hmac = mac.doFinal(counterBytes)

        val offset = hmac[hmac.size - 1].toInt() and 0x0F
        val binary =
            ((hmac[offset].toInt() and 0x7F) shl 24) or
            ((hmac[offset + 1].toInt() and 0xFF) shl 16) or
            ((hmac[offset + 2].toInt() and 0xFF) shl 8) or
            (hmac[offset + 3].toInt() and 0xFF)

        val otp = (binary % 1000000).toString().padStart(6, '0')
        return TotpResult(code = otp, remainingSeconds = remaining)
    }
}
