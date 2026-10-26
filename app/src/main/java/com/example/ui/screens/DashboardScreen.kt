package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VaultItem
import com.example.data.model.VaultItemType
import com.example.ui.components.CipheraLogo
import com.example.ui.components.ClipboardCountdownBar
import com.example.ui.theme.Amber400
import com.example.ui.theme.Amber950
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Cyan950
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald950
import com.example.ui.theme.Purple400
import com.example.ui.theme.Purple950
import com.example.ui.theme.Rose400
import com.example.ui.theme.Rose950
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.viewmodel.VaultUiState

@Composable
fun DashboardScreen(
    state: VaultUiState,
    onSearchQueryChange: (String) -> Unit,
    onSelectCategory: (VaultItemType?) -> Unit,
    onToggleFavoriteFilter: (Boolean) -> Unit,
    onSelectTag: (String?) -> Unit,
    onSelectItem: (VaultItem) -> Unit,
    onToggleFavoriteItem: (VaultItem) -> Unit,
    onCopySecret: (VaultItem) -> Unit,
    onAddNewItem: () -> Unit,
    onOpenKeyGen: () -> Unit,
    onOpenSyncCenter: () -> Unit,
    onLockNow: () -> Unit,
    onClearClipboardNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allItems = state.vaultPayload?.items ?: emptyList()

    // Filter items based on Category, Favorite, Tag, and Search Query
    val filteredItems = remember(allItems, state.selectedCategory, state.isFavoriteOnly, state.selectedTag, state.searchQuery) {
        allItems.filter { item ->
            if (state.isFavoriteOnly && !item.favorite) return@filter false
            if (state.selectedCategory != null && item.type != state.selectedCategory) return@filter false
            if (state.selectedTag != null && !item.tags.contains(state.selectedTag)) return@filter false

            if (state.searchQuery.isNotBlank()) {
                val q = state.searchQuery.lowercase()
                val matchTitle = item.title.lowercase().contains(q)
                val matchUser = (item.username ?: "").lowercase().contains(q)
                val matchNotes = item.notes.lowercase().contains(q)
                val matchTags = item.tags.any { it.lowercase().contains(q) }
                matchTitle || matchUser || matchNotes || matchTags
            } else {
                true
            }
        }
    }

    val allTags = remember(allItems) {
        allItems.flatMap { it.tags }.distinct().sorted()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Slate950,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNewItem,
                containerColor = Emerald400,
                contentColor = Slate950,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = if (state.clipboardRemainingSeconds > 0) 70.dp else 0.dp)
                    .testTag("fab_add_secret")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah Rahasia Baru",
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        bottomBar = {
            if (state.clipboardRemainingSeconds > 0) {
                ClipboardCountdownBar(
                    remainingSeconds = state.clipboardRemainingSeconds,
                    label = state.clipboardLabel,
                    onClearNow = onClearClipboardNow
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header TopBar
            Surface(
                color = Slate900,
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Top Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CipheraLogo(size = 32.dp)
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "CIPHERA",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 17.sp,
                                        color = Color.White,
                                        letterSpacing = 1.5.sp
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Emerald400.copy(alpha = 0.15f))
                                            .border(1.dp, Emerald400.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(5.dp)
                                                    .clip(CircleShape)
                                                    .background(Emerald400)
                                            )
                                            Text(
                                                text = "100% OFFLINE",
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold,
                                                color = Emerald400
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Right Top Actions
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Idle Timer Badge
                            val idleMin = state.idleRemainingSeconds / 60
                            val idleSec = state.idleRemainingSeconds % 60
                            val idleStr = String.format("%d:%02d", idleMin, idleSec)

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Slate950)
                                    .border(1.dp, Slate800, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = "Auto-Lock Timer",
                                        tint = Cyan400,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = idleStr,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate300
                                    )
                                }
                            }

                            // KeyGen Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Slate950)
                                    .border(1.dp, Cyan400.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .clickable { onOpenKeyGen() }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("keygen_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Cyan400,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "KeyGen",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Cyan400
                                    )
                                }
                            }

                            // Sync Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Emerald950.copy(alpha = 0.4f))
                                    .border(1.dp, Emerald400.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .clickable { onOpenSyncCenter() }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("sync_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Sync,
                                        contentDescription = null,
                                        tint = Emerald400,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "Sync",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Emerald400
                                    )
                                }
                            }

                            // Lock Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Rose950.copy(alpha = 0.4f))
                                    .border(1.dp, Rose400.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .clickable { onLockNow() }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("lock_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = Rose400,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "Lock",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Rose400
                                    )
                                }
                            }
                        }
                    }

                    // Search Bar
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Cari judul, username, catatan, tag...", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Slate400,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (state.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Bersihkan pencarian",
                                        tint = Slate400,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Emerald400.copy(alpha = 0.6f),
                            unfocusedBorderColor = Slate800,
                            focusedContainerColor = Slate950,
                            unfocusedContainerColor = Slate950,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("search_bar")
                    )
                }
            }

            // Categories Filter Bar
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Semua
                item {
                    CategoryPill(
                        label = "Semua",
                        count = allItems.size,
                        icon = Icons.Default.Layers,
                        isSelected = state.selectedCategory == null && !state.isFavoriteOnly,
                        onClick = { onSelectCategory(null) }
                    )
                }

                // Favorit
                item {
                    CategoryPill(
                        label = "Favorit",
                        count = allItems.count { it.favorite },
                        icon = Icons.Default.Star,
                        isSelected = state.isFavoriteOnly,
                        onClick = { onToggleFavoriteFilter(true) }
                    )
                }

                // Password
                item {
                    CategoryPill(
                        label = "Password",
                        count = allItems.count { it.type == VaultItemType.PASSWORD },
                        icon = Icons.Default.Key,
                        isSelected = state.selectedCategory == VaultItemType.PASSWORD,
                        onClick = { onSelectCategory(VaultItemType.PASSWORD) }
                    )
                }

                // PIN / Code
                item {
                    CategoryPill(
                        label = "PIN / Code",
                        count = allItems.count { it.type == VaultItemType.CODE },
                        icon = Icons.Default.Password,
                        isSelected = state.selectedCategory == VaultItemType.CODE,
                        onClick = { onSelectCategory(VaultItemType.CODE) }
                    )
                }

                // Seed Phrase
                item {
                    CategoryPill(
                        label = "Seed Phrase",
                        count = allItems.count { it.type == VaultItemType.SEED_PHRASE },
                        icon = Icons.Default.GridView,
                        isSelected = state.selectedCategory == VaultItemType.SEED_PHRASE,
                        onClick = { onSelectCategory(VaultItemType.SEED_PHRASE) }
                    )
                }

                // Note
                item {
                    CategoryPill(
                        label = "Catatan",
                        count = allItems.count { it.type == VaultItemType.NOTE },
                        icon = Icons.Default.Description,
                        isSelected = state.selectedCategory == VaultItemType.NOTE,
                        onClick = { onSelectCategory(VaultItemType.NOTE) }
                    )
                }
            }

            // Tags Filter Chips (if any)
            if (allTags.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(allTags) { tag ->
                        val isSelected = state.selectedTag == tag
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Cyan400.copy(alpha = 0.2f) else Slate900)
                                .border(
                                    1.dp,
                                    if (isSelected) Cyan400 else Slate800,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onSelectTag(if (isSelected) null else tag) }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (isSelected) Cyan400 else Slate400
                            )
                        }
                    }
                }
            }

            // Items List
            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Slate900)
                                .border(1.dp, Slate800, RoundedCornerShape(16.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Slate500,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Text(
                            text = "Tidak Ada Item Ditemukan",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate300
                        )
                        Text(
                            text = "Gunakan tombol (+) di pojok kanan bawah untuk menambah rahasia baru, atau bersihkan filter pencarian.",
                            fontSize = 12.sp,
                            color = Slate500,
                            modifier = Modifier.padding(horizontal = 30.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("vault_item_list"),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        VaultItemCard(
                            item = item,
                            onClick = { onSelectItem(item) },
                            onToggleFavorite = { onToggleFavoriteItem(item) },
                            onQuickCopy = { onCopySecret(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryPill(
    label: String,
    count: Int,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) Emerald400.copy(alpha = 0.15f) else Slate900)
            .border(
                1.dp,
                if (isSelected) Emerald400.copy(alpha = 0.5f) else Slate800,
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Emerald400 else Slate400,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Emerald400 else Slate300
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Slate950)
                    .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
                Text(
                    text = count.toString(),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Slate400
                )
            }
        }
    }
}

@Composable
private fun VaultItemCard(
    item: VaultItem,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onQuickCopy: () -> Unit
) {
    val (iconBg, iconColor, iconVector) = when (item.type) {
        VaultItemType.PASSWORD -> Triple(Emerald950.copy(alpha = 0.5f), Emerald400, Icons.Default.Key)
        VaultItemType.SEED_PHRASE -> Triple(Cyan950.copy(alpha = 0.5f), Cyan400, Icons.Default.GridView)
        VaultItemType.CODE -> Triple(Purple950.copy(alpha = 0.5f), Purple400, Icons.Default.Password)
        VaultItemType.NOTE -> Triple(Amber950.copy(alpha = 0.5f), Amber400, Icons.Default.Description)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Slate900)
            .border(1.dp, Slate800, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(14.dp)
            .testTag("vault_card_${item.id}")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Card Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(iconBg)
                            .border(1.dp, iconColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = item.username ?: if (item.type == VaultItemType.SEED_PHRASE) "12/24 Kata Mnemonic Seed" else "Tanpa Username",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Slate400,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (item.favorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Favorit",
                        tint = if (item.favorite) Amber400 else Slate500,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Badges & Quick Action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (item.totp_secret != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Emerald950.copy(alpha = 0.5f))
                                .border(1.dp, Emerald400.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "2FA TOTP",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Emerald400,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (item.history.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Slate950)
                                .border(1.dp, Slate800, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${item.history.size} rev",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Slate400
                            )
                        }
                    }

                    item.tags.take(2).forEach { t ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Slate950)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "#$t",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Cyan400
                            )
                        }
                    }
                }

                // Quick Copy Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate950)
                        .border(1.dp, Slate800, RoundedCornerShape(8.dp))
                        .clickable { onQuickCopy() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("quick_copy_${item.id}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Salin",
                            tint = Slate300,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Salin",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate300
                        )
                    }
                }
            }
        }
    }
}
