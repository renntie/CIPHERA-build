package com.example.crypto

import android.util.Base64
import com.example.data.model.CipheraContainer
import com.example.data.model.DecryptedVaultPayload
import com.example.data.model.KdfParams
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object CipheraCryptoEngine {

    const val DEFAULT_ITERATIONS = 600000
    private const val SALT_LENGTH = 16 // 16 bytes
    private const val NONCE_LENGTH = 12 // 12 bytes for AES-GCM
    private const val KEY_LENGTH_BITS = 256
    private const val GCM_TAG_LENGTH_BITS = 128

    private val secureRandom = SecureRandom()

    private fun deriveKey(password: String, salt: ByteArray, iterations: Int): SecretKeySpec {
        val keySpec = PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val secretKey = factory.generateSecret(keySpec)
        return SecretKeySpec(secretKey.encoded, "AES")
    }

    private fun buildAad(iterations: Int, saltB64: String, nonceB64: String): ByteArray {
        val aadString = "CIPHERA_V1:$iterations:$saltB64:$nonceB64"
        return aadString.toByteArray(Charsets.UTF_8)
    }

    suspend fun encryptVault(
        payload: DecryptedVaultPayload,
        masterPassword: String,
        iterations: Int = DEFAULT_ITERATIONS
    ): CipheraContainer = withContext(Dispatchers.Default) {
        val salt = ByteArray(SALT_LENGTH).also { secureRandom.nextBytes(it) }
        val nonce = ByteArray(NONCE_LENGTH).also { secureRandom.nextBytes(it) }

        val saltB64 = Base64.encodeToString(salt, Base64.NO_WRAP)
        val nonceB64 = Base64.encodeToString(nonce, Base64.NO_WRAP)

        val secretKey = deriveKey(masterPassword, salt, iterations)
        val aad = buildAad(iterations, saltB64, nonceB64)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, nonce)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)
        cipher.updateAAD(aad)

        val plainBytes = payload.toJsonString().toByteArray(Charsets.UTF_8)
        val ciphertext = cipher.doFinal(plainBytes)
        val ciphertextB64 = Base64.encodeToString(ciphertext, Base64.NO_WRAP)

        CipheraContainer(
            app = "CIPHERA",
            version = 1,
            kdf = "PBKDF2-SHA256",
            kdf_params = KdfParams(
                iterations = iterations,
                salt_b64 = saltB64
            ),
            cipher = "AES-256-GCM",
            nonce_b64 = nonceB64,
            ciphertext_b64 = ciphertextB64
        )
    }

    suspend fun decryptVault(
        container: CipheraContainer,
        masterPassword: String
    ): DecryptedVaultPayload = withContext(Dispatchers.Default) {
        if (container.app != "CIPHERA" || container.version != 1) {
            throw IllegalArgumentException("Format kontainer .ciphera tidak valid atau versi tidak didukung.")
        }

        val salt = Base64.decode(container.kdf_params.salt_b64, Base64.NO_WRAP)
        val nonce = Base64.decode(container.nonce_b64, Base64.NO_WRAP)
        val ciphertext = Base64.decode(container.ciphertext_b64, Base64.NO_WRAP)
        val iterations = container.kdf_params.iterations

        val secretKey = deriveKey(masterPassword, salt, iterations)
        val aad = buildAad(iterations, container.kdf_params.salt_b64, container.nonce_b64)

        try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, nonce)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
            cipher.updateAAD(aad)

            val decryptedBytes = cipher.doFinal(ciphertext)
            val jsonString = String(decryptedBytes, Charsets.UTF_8)
            DecryptedVaultPayload.fromJsonString(jsonString)
        } catch (e: Exception) {
            throw IllegalArgumentException("Master Password salah atau integritas file .ciphera rusak (AAD Mismatch).", e)
        }
    }
}
