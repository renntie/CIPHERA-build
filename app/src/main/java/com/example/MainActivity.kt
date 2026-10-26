package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.crypto.BiometricAuthManager
import com.example.data.VaultStorageManager
import com.example.data.model.CipheraContainer
import com.example.ui.screens.AuthGateScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ItemDetailDialog
import com.example.ui.screens.ItemEditorDialog
import com.example.ui.screens.KeyGenDialog
import com.example.ui.screens.MergeReportDialog
import com.example.ui.screens.SyncSecurityDialog
import com.example.ui.theme.CIPHERATheme
import com.example.ui.theme.Slate950
import com.example.viewmodel.VaultViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {

    private val viewModel: VaultViewModel by viewModels()

    private var pendingIncomingContainer: CipheraContainer? by mutableStateOf(null)

    // Storage Access Framework: Export .ciphera
    private val exportCipheraLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            val container = viewModel.uiState.value.storedContainer
            if (container != null) {
                val scope = (application as? android.app.Application)
                // Write container
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                    val success = VaultStorageManager.writeContainerToUri(applicationContext, uri, container)
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        Toast.makeText(
                            applicationContext,
                            if (success) "File cadangan .ciphera berhasil diekspor!" else "Gagal mengekspor file .ciphera.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    // Storage Access Framework: Import / Merge .ciphera
    private val importCipheraLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                val container = VaultStorageManager.readContainerFromUri(applicationContext, uri)
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    if (container != null) {
                        if (viewModel.uiState.value.isUnlocked) {
                            pendingIncomingContainer = container
                            viewModel.openSyncCenter()
                            Toast.makeText(applicationContext, "File .ciphera siap digabungkan (Smart Merge).", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.setImportedContainer(container)
                            Toast.makeText(applicationContext, "File .ciphera dimuat. Masukkan Master Password untuk membuka.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(applicationContext, "File bukan kontainer .ciphera yang valid.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewModel.initialize(applicationContext)

        setContent {
            CIPHERATheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Slate950
                ) {
                    val uiState by viewModel.uiState.collectAsState()
                    val lifecycleOwner = LocalLifecycleOwner.current

                    // Auto-lock when activity is minimized or hidden
                    DisposableEffect(lifecycleOwner) {
                        val observer = LifecycleEventObserver { _, event ->
                            if (event == Lifecycle.Event.ON_STOP) {
                                viewModel.onAppBackgrounded()
                            }
                        }
                        lifecycleOwner.lifecycle.addObserver(observer)
                        onDispose {
                            lifecycleOwner.lifecycle.removeObserver(observer)
                        }
                    }

                    // Toast message observation
                    LaunchedEffect(uiState.toastMessage) {
                        uiState.toastMessage?.let { msg ->
                            Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                            viewModel.dismissToast()
                        }
                    }

                    if (!uiState.isUnlocked) {
                        AuthGateScreen(
                            hasStoredVault = uiState.storedContainer != null,
                            isCryptoBusy = uiState.isCryptoBusy,
                            cryptoStatusMessage = uiState.cryptoStatusMessage,
                            errorMessage = uiState.authError,
                            isBiometricAvailable = uiState.isBiometricAvailable,
                            isBiometricEnabled = uiState.isBiometricEnabled,
                            onUnlockWithPassword = { pw -> viewModel.unlockWithPassword(pw) },
                            onUnlockWithBiometrics = {
                                BiometricAuthManager.authenticateWithBiometrics(
                                    activity = this@MainActivity,
                                    onSuccess = { masterPw ->
                                        viewModel.unlockWithPassword(masterPw)
                                    },
                                    onError = { err ->
                                        Toast.makeText(applicationContext, err, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                            onCreateVault = { pw, bio -> viewModel.createVault(pw, bio, applicationContext) },
                            onImportFileClicked = {
                                importCipheraLauncher.launch(arrayOf("*/*"))
                            },
                            onResetVault = {
                                viewModel.resetVault(applicationContext)
                            }
                        )
                    } else {
                        DashboardScreen(
                            state = uiState,
                            onSearchQueryChange = { q -> viewModel.setSearchQuery(q) },
                            onSelectCategory = { cat -> viewModel.setSelectedCategory(cat) },
                            onToggleFavoriteFilter = { favOnly -> viewModel.setFavoriteOnly(favOnly) },
                            onSelectTag = { tag -> viewModel.setSelectedTag(tag) },
                            onSelectItem = { item -> viewModel.setActiveItem(item) },
                            onToggleFavoriteItem = { item -> viewModel.toggleFavorite(applicationContext, item) },
                            onCopySecret = { item ->
                                viewModel.copyToClipboard(
                                    context = applicationContext,
                                    text = item.secret,
                                    label = "Rahasia (${item.title})"
                                )
                            },
                            onAddNewItem = { viewModel.setEditingItem(null) },
                            onOpenKeyGen = { viewModel.openKeyGen() },
                            onOpenSyncCenter = { viewModel.openSyncCenter() },
                            onLockNow = {
                                viewModel.lockVault("Brankas CIPHERA dikunci & memori sensitif dibersihkan.")
                            },
                            onClearClipboardNow = { viewModel.clearClipboardNow(applicationContext) }
                        )

                        // Dialog: Item Detail
                        uiState.activeItem?.let { item ->
                            ItemDetailDialog(
                                item = item,
                                onClose = { viewModel.setActiveItem(null) },
                                onEdit = { viewModel.setEditingItem(item) },
                                onDelete = { viewModel.deleteItem(applicationContext, item.id) },
                                onCopy = { text, label ->
                                    viewModel.copyToClipboard(applicationContext, text, label)
                                },
                                onRestoreHistory = { hist ->
                                    viewModel.restoreHistory(applicationContext, item, hist)
                                }
                            )
                        }

                        // Dialog: Item Editor
                        if (uiState.isEditingModalOpen) {
                            ItemEditorDialog(
                                initialItem = uiState.editingItem,
                                onClose = { viewModel.closeEditingModal() },
                                onSave = { id, type, title, username, secret, totpSecret, recoveryCodes, notes, tags, favorite ->
                                    viewModel.saveItem(
                                        context = applicationContext,
                                        id = id,
                                        type = type,
                                        title = title,
                                        username = username,
                                        secret = secret,
                                        totpSecret = totpSecret,
                                        recoveryCodes = recoveryCodes,
                                        notes = notes,
                                        tags = tags,
                                        favorite = favorite
                                    )
                                }
                            )
                        }

                        // Dialog: CIPHERA KeyGen
                        if (uiState.isKeyGenOpen) {
                            KeyGenDialog(
                                onClose = { viewModel.closeKeyGen() },
                                onCopy = { key, label ->
                                    viewModel.copyToClipboard(applicationContext, key, label)
                                }
                            )
                        }

                        // Dialog: Sync & Security Center
                        if (uiState.isSyncCenterOpen && uiState.storedContainer != null) {
                            SyncSecurityDialog(
                                storedContainer = uiState.storedContainer!!,
                                isCryptoBusy = uiState.isCryptoBusy,
                                onClose = {
                                    viewModel.closeSyncCenter()
                                    pendingIncomingContainer = null
                                },
                                onExportFileClicked = {
                                    val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                    val filename = "ciphera_backup_$dateStr.ciphera"
                                    exportCipheraLauncher.launch(filename)
                                },
                                onPickIncomingFileClicked = {
                                    importCipheraLauncher.launch(arrayOf("*/*"))
                                },
                                incomingContainer = pendingIncomingContainer,
                                onClearIncomingContainer = {
                                    pendingIncomingContainer = null
                                },
                                onExecuteSmartMerge = { incContainer, incPassword ->
                                    viewModel.executeSmartMerge(
                                        context = applicationContext,
                                        incomingContainer = incContainer,
                                        incomingPassword = incPassword,
                                        onSuccess = {
                                            pendingIncomingContainer = null
                                        },
                                        onError = { err ->
                                            Toast.makeText(applicationContext, err, Toast.LENGTH_LONG).show()
                                        }
                                    )
                                },
                                onChangeMasterPassword = { newPw ->
                                    viewModel.changeMasterPassword(
                                        context = applicationContext,
                                        newPassword = newPw,
                                        onSuccess = {},
                                        onError = { err ->
                                            Toast.makeText(applicationContext, err, Toast.LENGTH_LONG).show()
                                        }
                                    )
                                },
                                onCopy = { text, label ->
                                    viewModel.copyToClipboard(applicationContext, text, label)
                                }
                            )
                        }

                        // Dialog: Smart Merge Report
                        uiState.mergeReport?.let { report ->
                            MergeReportDialog(
                                report = report,
                                onDismiss = { viewModel.dismissMergeReport() }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        viewModel.onUserInteraction()
    }
}
