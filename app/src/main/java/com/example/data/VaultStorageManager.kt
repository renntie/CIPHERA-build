package com.example.data

import android.content.Context
import android.net.Uri
import com.example.data.model.CipheraContainer
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object VaultStorageManager {

    private const val VAULT_FILENAME = "ciphera_vault_data.json"

    suspend fun loadStoredContainer(context: Context): CipheraContainer? = withContext(Dispatchers.IO) {
        val file = File(context.filesDir, VAULT_FILENAME)
        if (!file.exists()) return@withContext null

        try {
            val jsonStr = file.readText(Charsets.UTF_8)
            CipheraContainer.fromJsonString(jsonStr)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun saveStoredContainer(context: Context, container: CipheraContainer) = withContext(Dispatchers.IO) {
        val file = File(context.filesDir, VAULT_FILENAME)
        file.writeText(container.toJsonString(), Charsets.UTF_8)
    }

    suspend fun clearVault(context: Context) = withContext(Dispatchers.IO) {
        val file = File(context.filesDir, VAULT_FILENAME)
        if (file.exists()) {
            file.delete()
        }
    }

    suspend fun writeContainerToUri(context: Context, uri: Uri, container: CipheraContainer): Boolean =
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(container.toJsonString().toByteArray(Charsets.UTF_8))
                    outputStream.flush()
                }
                true
            } catch (_: Exception) {
                false
            }
        }

    suspend fun readContainerFromUri(context: Context, uri: Uri): CipheraContainer? =
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
                    val content = reader.readText()
                    CipheraContainer.fromJsonString(content)
                }
            } catch (_: Exception) {
                null
            }
        }
}
