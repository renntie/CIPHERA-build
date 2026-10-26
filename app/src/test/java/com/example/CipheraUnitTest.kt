package com.example

import com.example.crypto.KeyGenEngine
import com.example.crypto.TotpEngine
import com.example.data.SmartMergeEngine
import com.example.data.model.DecryptedVaultPayload
import com.example.data.model.TombstoneItem
import com.example.data.model.VaultItem
import com.example.data.model.VaultItemType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CipheraUnitTest {

    @Test
    fun totp_generatesValid6DigitCode() {
        val secret = "JBSWY3DPEHPK3PXP"
        val result = TotpEngine.generateTotp(secret, timestampMs = 1700000000000L)
        assertNotNull(result)
        assertEquals(6, result.code.length)
        assertTrue(result.code.all { it.isDigit() })
        assertTrue(result.remainingSeconds in 1..30)
    }

    @Test
    fun keygen_generatesExpectedLengthAndEntropy() {
        val pw = KeyGenEngine.generateRandomPassword(length = 24)
        assertEquals(24, pw.length)

        val eval = KeyGenEngine.evaluateStrength(pw)
        assertTrue(eval.bits > 60)

        val passphrase = KeyGenEngine.generatePassphrase(wordCount = 5)
        val words = passphrase.split("-")
        assertEquals(5, words.size)
        assertTrue(words.all { it.isNotBlank() })
    }

    @Test
    fun smartMerge_resolvesConflictByTimestampAndSavesHistory() {
        val now = 1000000L
        val itemLocal = VaultItem(
            id = "item-1",
            type = VaultItemType.PASSWORD,
            title = "Email Account",
            secret = "old_secret_123",
            notes = "Old note",
            created_at = now - 5000,
            updated_at = now - 1000
        )
        val payloadLocal = DecryptedVaultPayload(
            items = listOf(itemLocal),
            deleted_items = emptyList()
        )

        val itemIncoming = VaultItem(
            id = "item-1",
            type = VaultItemType.PASSWORD,
            title = "Email Account",
            secret = "new_secret_456",
            notes = "New note",
            created_at = now - 5000,
            updated_at = now // newer timestamp
        )
        val payloadIncoming = DecryptedVaultPayload(
            items = listOf(itemIncoming),
            deleted_items = emptyList()
        )

        val (merged, report) = SmartMergeEngine.performMerge(payloadLocal, payloadIncoming)
        assertEquals(1, merged.items.size)
        assertEquals("new_secret_456", merged.items[0].secret)
        assertEquals(1, report.conflictsResolved)
        assertEquals(1, report.updated)

        // Verify older secret was preserved in history
        assertEquals(1, merged.items[0].history.size)
        assertEquals("old_secret_123", merged.items[0].history[0].secret)
    }

    @Test
    fun smartMerge_respectsTombstoneDeletion() {
        val now = 1000000L
        val itemLocal = VaultItem(
            id = "item-2",
            type = VaultItemType.CODE,
            title = "ATM PIN",
            secret = "1234",
            created_at = now - 10000,
            updated_at = now - 5000
        )
        val payloadLocal = DecryptedVaultPayload(
            items = listOf(itemLocal),
            deleted_items = emptyList()
        )

        // Incoming has tombstone for item-2 deleted more recently than itemLocal updated_at
        val payloadIncoming = DecryptedVaultPayload(
            items = emptyList(),
            deleted_items = listOf(TombstoneItem(id = "item-2", deleted_at = now - 1000))
        )

        val (merged, report) = SmartMergeEngine.performMerge(payloadLocal, payloadIncoming)
        assertEquals(0, merged.items.size)
        assertEquals(1, report.deleted)
        assertTrue(merged.deleted_items.any { it.id == "item-2" })
    }
}
