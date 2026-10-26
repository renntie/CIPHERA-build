package com.example.data

import com.example.data.model.DecryptedVaultPayload
import com.example.data.model.MergeReport
import com.example.data.model.TombstoneItem
import com.example.data.model.VaultItem
import com.example.data.model.VaultItemHistory

object SmartMergeEngine {

    fun performMerge(
        local: DecryptedVaultPayload,
        incoming: DecryptedVaultPayload
    ): Pair<DecryptedVaultPayload, MergeReport> {
        var addedCount = 0
        var updatedCount = 0
        var conflictsCount = 0
        var deletedCount = 0

        // 1. Combine Tombstones keeping highest deleted_at timestamp
        val tombstoneMap = mutableMapOf<String, Long>()
        for (t in local.deleted_items) {
            tombstoneMap[t.id] = t.deleted_at
        }
        for (t in incoming.deleted_items) {
            val existing = tombstoneMap[t.id]
            if (existing == null || t.deleted_at > existing) {
                tombstoneMap[t.id] = t.deleted_at
            }
        }

        // 2. Filter Local Items against Tombstones
        val itemMap = mutableMapOf<String, VaultItem>()
        for (item in local.items) {
            val delAt = tombstoneMap[item.id]
            if (delAt != null && delAt > item.updated_at) {
                deletedCount++
                continue
            }
            itemMap[item.id] = item
        }

        // 3. Process Incoming Items
        for (incItem in incoming.items) {
            val delAt = tombstoneMap[incItem.id]
            if (delAt != null && delAt > incItem.updated_at) {
                continue // Already deleted later than this update
            }

            val existing = itemMap[incItem.id]
            if (existing == null) {
                // New item
                itemMap[incItem.id] = incItem
                addedCount++
                if (delAt != null && incItem.updated_at >= delAt) {
                    tombstoneMap.remove(incItem.id)
                }
            } else {
                // Item exists in both
                val isDifferent =
                    existing.secret != incItem.secret ||
                    existing.title != incItem.title ||
                    existing.username != incItem.username ||
                    existing.notes != incItem.notes ||
                    existing.totp_secret != incItem.totp_secret ||
                    (existing.recovery_codes ?: emptyList<String>()) != (incItem.recovery_codes ?: emptyList<String>())

                val combinedHistoryMap = mutableMapOf<String, VaultItemHistory>()
                existing.history.forEach { h ->
                    combinedHistoryMap["${h.updated_at}:${h.secret}"] = h
                }
                incItem.history.forEach { h ->
                    combinedHistoryMap["${h.updated_at}:${h.secret}"] = h
                }

                if (!isDifferent) {
                    val mergedHistory = combinedHistoryMap.values
                        .sortedByDescending { it.updated_at }
                        .take(15)
                    val mergedTags = (existing.tags + incItem.tags).distinct()

                    itemMap[incItem.id] = existing.copy(
                        favorite = existing.favorite || incItem.favorite,
                        tags = mergedTags,
                        history = mergedHistory
                    )
                } else {
                    // Conflict detected: resolve based on latest timestamp
                    val winner = if (incItem.updated_at > existing.updated_at) incItem else existing
                    val loser = if (incItem.updated_at > existing.updated_at) existing else incItem

                    if (loser.secret != winner.secret || loser.notes != winner.notes) {
                        combinedHistoryMap["${loser.updated_at}:${loser.secret}"] = VaultItemHistory(
                            secret = loser.secret,
                            notes = loser.notes,
                            updated_at = loser.updated_at
                        )
                        conflictsCount++
                    }

                    val mergedHistory = combinedHistoryMap.values
                        .sortedByDescending { it.updated_at }
                        .take(15)
                    val mergedTags = (existing.tags + incItem.tags).distinct()

                    itemMap[incItem.id] = winner.copy(
                        tags = mergedTags,
                        history = mergedHistory
                    )

                    if (incItem.updated_at > existing.updated_at) {
                        updatedCount++
                    }
                }
            }
        }

        // Final tombstones list
        val finalTombstones = tombstoneMap.entries
            .filter { !itemMap.containsKey(it.key) }
            .map { TombstoneItem(id = it.key, deleted_at = it.value) }

        val mergedPayload = DecryptedVaultPayload(
            items = itemMap.values.sortedByDescending { it.updated_at },
            deleted_items = finalTombstones,
            last_modified = System.currentTimeMillis()
        )

        val report = MergeReport(
            added = addedCount,
            updated = updatedCount,
            conflictsResolved = conflictsCount,
            deleted = deletedCount
        )

        return Pair(mergedPayload, report)
    }
}
