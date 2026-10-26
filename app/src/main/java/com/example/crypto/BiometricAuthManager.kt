package com.example.crypto

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object BiometricAuthManager {

    private const val PREF_FILE = "ciphera_biometric_prefs"
    private const val KEY_ENCRYPTED_MASTER_PW = "enc_master_pw"
    private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"

    fun canAuthenticate(context: Context): Boolean {
        val manager = BiometricManager.from(context)
        val canAuth = manager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
        )
        return canAuth == BiometricManager.BIOMETRIC_SUCCESS
    }

    private fun getEncryptedPrefs(context: Context) = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            PREF_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (_: Exception) {
        null
    }

    fun isBiometricEnabled(context: Context): Boolean {
        val prefs = getEncryptedPrefs(context) ?: return false
        val enabled = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
        val hasPw = !prefs.getString(KEY_ENCRYPTED_MASTER_PW, "").isNullOrEmpty()
        return enabled && hasPw
    }

    fun storeMasterPasswordForBiometrics(context: Context, masterPassword: String) {
        val prefs = getEncryptedPrefs(context) ?: return
        prefs.edit()
            .putBoolean(KEY_BIOMETRIC_ENABLED, true)
            .putString(KEY_ENCRYPTED_MASTER_PW, masterPassword)
            .apply()
    }

    fun clearBiometricCredentials(context: Context) {
        val prefs = getEncryptedPrefs(context) ?: return
        prefs.edit().clear().apply()
    }

    fun authenticateWithBiometrics(
        activity: FragmentActivity,
        title: String = "Buka Kunci CIPHERA",
        subtitle: String = "Gunakan sensor sidik jari atau wajah",
        onSuccess: (masterPassword: String) -> Unit,
        onError: (message: String) -> Unit
    ) {
        val context = activity.applicationContext
        val prefs = getEncryptedPrefs(context)
        val savedPw = prefs?.getString(KEY_ENCRYPTED_MASTER_PW, "") ?: ""

        if (savedPw.isEmpty()) {
            onError("Biometrik belum diatur dengan Master Password.")
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess(savedPw)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                        errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON
                    ) {
                        onError(errString.toString())
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onError("Autentikasi biometrik tidak cocok.")
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText("Gunakan Master Password")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
            )
            .build()

        prompt.authenticate(promptInfo)
    }
}
