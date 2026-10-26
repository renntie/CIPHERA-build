package com.example.viewmodel

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.crypto.BiometricAuthManager
import com.example.crypto.CipheraCryptoEngine
import com.example.data.SmartMergeEngine
import com.example.data.VaultStorageManager
import com.example.data.model.CipheraContainer
import com.example.data.model.DecryptedVaultPayload
import com.example.data.model.MergeReport
import com.example.data.model.TombstoneItem
import com.example.data.model.VaultItem
import com.example.data.model.VaultItemHistory
import com.example.data.model.VaultItemType
import java.util.UUID
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VaultUiState(
    val hasLoadedContainer: Boolean = false,
    val storedContainer: CipheraContainer? = null,
    val isUnlocked: Boolean = false,
    val vaultPayload: DecryptedVaultPayload? = null,
    val activeMasterPassword: String = "",
    val authError: String? = null,
    val isCryptoBusy: Boolean = false,
    val cryptoStatusMessage: String = "",
    val selectedCategory: VaultItemType? = null,
    val isFavoriteOnly: Boolean = false,
    val selectedTag: String? = null,
    val searchQuery: String = "",
    val activeItem: VaultItem? = null,
    val editingItem: VaultItem? = null,
    val isEditingModalOpen: Boolean = false,
    val isKeyGenOpen: Boolean = false,
    val isSyncCenterOpen: Boolean = false,
    val mergeReport: MergeReport? = null,
    val idleRemainingSeconds: Int = 120,
    val clipboardRemainingSeconds: Int = 0,
    val clipboardLabel: String = "",
    val toastMessage: String? = null,
    val isBiometricAvailable: Boolean = false,
    val isBiometricEnabled: Boolean = false
)

class VaultViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(VaultUiState())
    val uiState: StateFlow<VaultUiState> = _uiState.asStateFlow()

    private var idleJob: Job? = null
    private var clipboardJob: Job? = null
    private var lastActivityMs = System.currentTimeMillis()

    fun initialize(context: Context) {
        viewModelScope.launch {
            val container = VaultStorageManager.loadStoredContainer(context)
            val canBio = BiometricAuthManager.canAuthenticate(context)
            val bioEnabled = BiometricAuthManager.isBiometricEnabled(context)

            _uiState.update {
                it.copy(
                    hasLoadedContainer = true,
                    storedContainer = container,
                    isBiometricAvailable = canBio,
                    isBiometricEnabled = bioEnabled
                )
            }
        }
    }

    fun onUserInteraction() {
        lastActivityMs = System.currentTimeMillis()
        _uiState.update { it.copy(idleRemainingSeconds = 120) }
    }

    private fun startIdleTimer() {
        idleJob?.cancel()
        lastActivityMs = System.currentTimeMillis()
        idleJob = viewModelScope.launch {
            while (_uiState.value.isUnlocked) {
                delay(1000)
                val elapsed = ((System.currentTimeMillis() - lastActivityMs) / 1000).toInt()
                val left = (120 - elapsed).coerceAtLeast(0)
                _uiState.update { it.copy(idleRemainingSeconds = left) }
                if (left <= 0) {
                    lockVault("CIPHERA dikunci otomatis setelah 2 menit tanpa aktivitas.")
                    break
                }
            }
        }
    }

    fun onAppBackgrounded() {
        if (_uiState.value.isUnlocked) {
            lockVault("CIPHERA dikunci otomatis saat aplikasi diminimize.")
        }
    }

    fun lockVault(reason: String? = null) {
        idleJob?.cancel()
        _uiState.update {
            it.copy(
                isUnlocked = false,
                vaultPayload = null,
                activeMasterPassword = "",
                activeItem = null,
                editingItem = null,
                isEditingModalOpen = false,
                isKeyGenOpen = false,
                isSyncCenterOpen = false,
                authError = null,
                toastMessage = reason
            )
        }
    }

    fun unlockWithPassword(password: String) {
        val container = _uiState.value.storedContainer ?: return
        _uiState.update { it.copy(isCryptoBusy = true, cryptoStatusMessage = "Menurunkan Kunci PBKDF2 (600.000 Iterasi)...", authError = null) }

        viewModelScope.launch {
            try {
                val decrypted = CipheraCryptoEngine.decryptVault(container, password)
                _uiState.update {
                    it.copy(
                        isUnlocked = true,
                        vaultPayload = decrypted,
                        activeMasterPassword = password,
                        isCryptoBusy = false,
                        cryptoStatusMessage = "",
                        authError = null,
                        toastMessage = "Brankas CIPHERA berhasil dibuka."
                    )
                }
                startIdleTimer()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isCryptoBusy = false,
                        cryptoStatusMessage = "",
                        authError = e.message ?: "Gagal membuka brankas."
                    )
                }
            }
        }
    }

    fun createVault(password: String, enableBiometrics: Boolean, context: Context) {
        _uiState.update { it.copy(isCryptoBusy = true, cryptoStatusMessage = "Mengenkripsi Brankas Baru (AES-256-GCM)...", authError = null) }

        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                val initialPayload = DecryptedVaultPayload(
                    items = listOf(
                        VaultItem(
                            id = UUID.randomUUID().toString(),
                            type = VaultItemType.SEED_PHRASE,
                            title = "Hardware Wallet Cold Storage (Contoh)",
                            username = "Ledger Nano X - Main Vault",
                            secret = "abandon ability able about above absent absorb abstract absurd abuse access accident",
                            notes = "Contoh tampilan Grid 12 Kata Seed Phrase di CIPHERA. Anda dapat menghapus item contoh ini kapan saja.",
                            tags = listOf("Crypto", "Web3", "ColdStorage"),
                            favorite = true,
                            created_at = now,
                            updated_at = now,
                            history = emptyList()
                        ),
                        VaultItem(
                            id = UUID.randomUUID().toString(),
                            type = VaultItemType.PASSWORD,
                            title = "Akun Perbankan Prioritas (Contoh)",
                            username = "prioritas_user01",
                            secret = "C1ph3r@_V4ult!99#X",
                            totp_secret = "JBSWY3DPEHPK3PXP",
                            recovery_codes = listOf("8492-1102", "9941-2310", "7712-8834", "5120-4491"),
                            notes = "Dilengkapi Live 2FA Authenticator (TOTP) dan Backup Codes.",
                            tags = listOf("Finance", "Banking"),
                            favorite = true,
                            created_at = now - 5000,
                            updated_at = now - 5000,
                            history = emptyList()
                        )
                    ),
                    deleted_items = emptyList(),
                    last_modified = now
                )

                val container = CipheraCryptoEngine.encryptVault(initialPayload, password)
                VaultStorageManager.saveStoredContainer(context, container)

                if (enableBiometrics && BiometricAuthManager.canAuthenticate(context)) {
                    BiometricAuthManager.storeMasterPasswordForBiometrics(context, password)
                }

                _uiState.update {
                    it.copy(
                        storedContainer = container,
                        isUnlocked = true,
                        vaultPayload = initialPayload,
                        activeMasterPassword = password,
                        isCryptoBusy = false,
                        cryptoStatusMessage = "",
                        authError = null,
                        isBiometricEnabled = enableBiometrics,
                        toastMessage = "Brankas kriptografi CIPHERA berhasil diinisialisasi!"
                    )
                }
                startIdleTimer()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isCryptoBusy = false,
                        cryptoStatusMessage = "",
                        authError = e.message ?: "Gagal membuat brankas."
                    )
                }
            }
        }
    }

    private suspend fun persistVaultChanges(context: Context, newPayload: DecryptedVaultPayload, customPassword: String? = null) {
        val pw = customPassword ?: _uiState.value.activeMasterPassword
        val container = CipheraCryptoEngine.encryptVault(newPayload, pw)
        VaultStorageManager.saveStoredContainer(context, container)
        _uiState.update {
            it.copy(
                storedContainer = container,
                vaultPayload = newPayload,
                activeMasterPassword = pw
            )
        }
    }

    fun saveItem(
        context: Context,
        id: String?,
        type: VaultItemType,
        title: String,
        username: String?,
        secret: String,
        totpSecret: String?,
        recoveryCodes: List<String>?,
        notes: String,
        tags: List<String>,
        favorite: Boolean
    ) {
        val currentPayload = _uiState.value.vaultPayload ?: return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val items = currentPayload.items.toMutableList()

            if (id != null) {
                // Edit existing
                val index = items.indexOfFirst { it.id == id }
                if (index != -1) {
                    val existing = items[index]
                    val history = existing.history.toMutableList()
                    if (existing.secret != secret || existing.notes != notes) {
                        history.add(0, VaultItemHistory(existing.secret, existing.notes, existing.updated_at))
                    }

                    val updatedItem = existing.copy(
                        type = type,
                        title = title,
                        username = username,
                        secret = secret,
                        totp_secret = totpSecret,
                        recovery_codes = recoveryCodes,
                        notes = notes,
                        tags = tags,
                        favorite = favorite,
                        updated_at = now,
                        history = history.take(15)
                    )
                    items[index] = updatedItem
                    if (_uiState.value.activeItem?.id == id) {
                        _uiState.update { it.copy(activeItem = updatedItem) }
                    }
                }
            } else {
                // New item
                val newItem = VaultItem(
                    id = UUID.randomUUID().toString(),
                    type = type,
                    title = title,
                    username = username,
                    secret = secret,
                    totp_secret = totpSecret,
                    recovery_codes = recoveryCodes,
                    notes = notes,
                    tags = tags,
                    favorite = favorite,
                    created_at = now,
                    updated_at = now,
                    history = emptyList()
                )
                items.add(0, newItem)
                _uiState.update { it.copy(activeItem = newItem) }
            }

            val newPayload = currentPayload.copy(items = items, last_modified = now)
            persistVaultChanges(context, newPayload)
            _uiState.update {
                it.copy(
                    isEditingModalOpen = false,
                    editingItem = null,
                    toastMessage = "Item berhasil dienkripsi & disimpan ke brankas."
                )
            }
        }
    }

    fun deleteItem(context: Context, id: String) {
        val currentPayload = _uiState.value.vaultPayload ?: return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val items = currentPayload.items.filter { it.id != id }
            val tombstones = currentPayload.deleted_items.filter { it.id != id } + TombstoneItem(id, now)

            val newPayload = currentPayload.copy(
                items = items,
                deleted_items = tombstones,
                last_modified = now
            )
            persistVaultChanges(context, newPayload)
            _uiState.update {
                it.copy(
                    activeItem = if (it.activeItem?.id == id) null else it.activeItem,
                    toastMessage = "Item dihapus & dicatat pada Tombstone Log."
                )
            }
        }
    }

    fun toggleFavorite(context: Context, item: VaultItem) {
        val currentPayload = _uiState.value.vaultPayload ?: return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val items = currentPayload.items.map {
                if (it.id == item.id) it.copy(favorite = !it.favorite, updated_at = now) else it
            }
            val newPayload = currentPayload.copy(items = items, last_modified = now)
            persistVaultChanges(context, newPayload)
            if (_uiState.value.activeItem?.id == item.id) {
                _uiState.update { it.copy(activeItem = item.copy(favorite = !item.favorite, updated_at = now)) }
            }
        }
    }

    fun restoreHistory(context: Context, item: VaultItem, history: VaultItemHistory) {
        val currentPayload = _uiState.value.vaultPayload ?: return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val newHistList = mutableListOf(VaultItemHistory(item.secret, item.notes, item.updated_at))
            newHistList.addAll(item.history.filter { it.updated_at != history.updated_at })

            val updatedItem = item.copy(
                secret = history.secret,
                notes = history.notes ?: item.notes,
                updated_at = now,
                history = newHistList.take(15)
            )

            val items = currentPayload.items.map { if (it.id == item.id) updatedItem else it }
            val newPayload = currentPayload.copy(items = items, last_modified = now)
            persistVaultChanges(context, newPayload)
            _uiState.update {
                it.copy(
                    activeItem = updatedItem,
                    toastMessage = "Versi rahasia dari riwayat berhasil dipulihkan!"
                )
            }
        }
    }

    fun executeSmartMerge(
        context: Context,
        incomingContainer: CipheraContainer,
        incomingPassword: String,
        onSuccess: (MergeReport) -> Unit,
        onError: (String) -> Unit
    ) {
        val currentPayload = _uiState.value.vaultPayload ?: return
        _uiState.update { it.copy(isCryptoBusy = true, cryptoStatusMessage = "Mendekripsi file incoming...") }

        viewModelScope.launch {
            try {
                val incomingPayload = CipheraCryptoEngine.decryptVault(incomingContainer, incomingPassword)
                val (mergedPayload, report) = SmartMergeEngine.performMerge(currentPayload, incomingPayload)
                persistVaultChanges(context, mergedPayload)

                _uiState.update {
                    it.copy(
                        isCryptoBusy = false,
                        cryptoStatusMessage = "",
                        mergeReport = report,
                        isSyncCenterOpen = false
                    )
                }
                onSuccess(report)
            } catch (e: Exception) {
                _uiState.update { it.copy(isCryptoBusy = false, cryptoStatusMessage = "") }
                onError(e.message ?: "Gagal menggabungkan file .ciphera.")
            }
        }
    }

    fun changeMasterPassword(
        context: Context,
        newPassword: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentPayload = _uiState.value.vaultPayload ?: return
        _uiState.update { it.copy(isCryptoBusy = true, cryptoStatusMessage = "Mengenkripsi ulang brankas...") }

        viewModelScope.launch {
            try {
                persistVaultChanges(context, currentPayload, newPassword)
                if (BiometricAuthManager.isBiometricEnabled(context)) {
                    BiometricAuthManager.storeMasterPasswordForBiometrics(context, newPassword)
                }
                _uiState.update {
                    it.copy(
                        isCryptoBusy = false,
                        cryptoStatusMessage = "",
                        toastMessage = "Master Password & Parameter Kriptografi berhasil diperbarui!"
                    )
                }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isCryptoBusy = false, cryptoStatusMessage = "") }
                onError(e.message ?: "Gagal mengubah Master Password.")
            }
        }
    }

    fun copyToClipboard(context: Context, text: String, label: String) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard.setPrimaryClip(clip)

            _uiState.update {
                it.copy(
                    clipboardRemainingSeconds = 30,
                    clipboardLabel = label
                )
            }

            clipboardJob?.cancel()
            clipboardJob = viewModelScope.launch {
                for (s in 30 downTo 1) {
                    _uiState.update { it.copy(clipboardRemainingSeconds = s) }
                    delay(1000)
                }
                // Wipe clipboard
                try {
                    val emptyClip = ClipData.newPlainText("", "")
                    clipboard.setPrimaryClip(emptyClip)
                } catch (_: Exception) {}
                _uiState.update { it.copy(clipboardRemainingSeconds = 0) }
            }
        } catch (_: Exception) {
            _uiState.update { it.copy(toastMessage = "Gagal menyalin ke clipboard.") }
        }
    }

    fun clearClipboardNow(context: Context) {
        clipboardJob?.cancel()
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val emptyClip = ClipData.newPlainText("", "")
            clipboard.setPrimaryClip(emptyClip)
        } catch (_: Exception) {}
        _uiState.update {
            it.copy(
                clipboardRemainingSeconds = 0,
                toastMessage = "Clipboard telah dibersihkan."
            )
        }
    }

    fun setSelectedCategory(cat: VaultItemType?) {
        _uiState.update { it.copy(selectedCategory = cat, isFavoriteOnly = false) }
    }

    fun setFavoriteOnly(favOnly: Boolean) {
        _uiState.update { it.copy(isFavoriteOnly = favOnly, selectedCategory = null) }
    }

    fun setSelectedTag(tag: String?) {
        _uiState.update { it.copy(selectedTag = tag) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setActiveItem(item: VaultItem?) {
        _uiState.update { it.copy(activeItem = item) }
    }

    fun setEditingItem(item: VaultItem?) {
        _uiState.update { it.copy(editingItem = item, isEditingModalOpen = true) }
    }

    fun closeEditingModal() {
        _uiState.update { it.copy(isEditingModalOpen = false, editingItem = null) }
    }

    fun openKeyGen() {
        _uiState.update { it.copy(isKeyGenOpen = true) }
    }

    fun closeKeyGen() {
        _uiState.update { it.copy(isKeyGenOpen = false) }
    }

    fun openSyncCenter() {
        _uiState.update { it.copy(isSyncCenterOpen = true) }
    }

    fun closeSyncCenter() {
        _uiState.update { it.copy(isSyncCenterOpen = false) }
    }

    fun dismissMergeReport() {
        _uiState.update { it.copy(mergeReport = null) }
    }

    fun dismissToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    fun setImportedContainer(container: CipheraContainer) {
        _uiState.update { it.copy(storedContainer = container) }
    }

    fun resetVault(context: Context) {
        viewModelScope.launch {
            VaultStorageManager.clearVault(context)
            BiometricAuthManager.clearBiometricCredentials(context)
            _uiState.update {
                VaultUiState(
                    hasLoadedContainer = true,
                    storedContainer = null,
                    isBiometricAvailable = BiometricAuthManager.canAuthenticate(context),
                    isBiometricEnabled = false,
                    toastMessage = "Brankas lokal telah direset."
                )
            }
        }
    }
}
