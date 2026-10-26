package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject

enum class VaultItemType(val key: String, val displayName: String) {
    PASSWORD("password", "Password & Login"),
    CODE("code", "PIN & Recovery Code"),
    SEED_PHRASE("seed_phrase", "Seed Phrase (12/24)"),
    NOTE("note", "Catatan Rahasia");

    companion object {
        fun fromKey(key: String): VaultItemType {
            return entries.find { it.key.equals(key, ignoreCase = true) } ?: PASSWORD
        }
    }
}

data class VaultItemHistory(
    val secret: String,
    val notes: String? = null,
    val updated_at: Long
) {
    fun toJsonObject(): JSONObject {
        val obj = JSONObject()
        obj.put("secret", secret)
        if (notes != null) obj.put("notes", notes)
        obj.put("updated_at", updated_at)
        return obj
    }

    companion object {
        fun fromJsonObject(obj: JSONObject): VaultItemHistory {
            return VaultItemHistory(
                secret = obj.optString("secret", ""),
                notes = if (obj.has("notes") && !obj.isNull("notes")) obj.optString("notes") else null,
                updated_at = obj.optLong("updated_at", System.currentTimeMillis())
            )
        }
    }
}

data class VaultItem(
    val id: String,
    val type: VaultItemType,
    val title: String,
    val username: String? = null,
    val secret: String,
    val totp_secret: String? = null,
    val recovery_codes: List<String>? = null,
    val notes: String = "",
    val tags: List<String> = emptyList(),
    val favorite: Boolean = false,
    val created_at: Long,
    val updated_at: Long,
    val history: List<VaultItemHistory> = emptyList()
) {
    fun toJsonObject(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("type", type.key)
        obj.put("title", title)
        if (username != null) obj.put("username", username)
        obj.put("secret", secret)
        if (totp_secret != null) obj.put("totp_secret", totp_secret)
        if (recovery_codes != null) {
            val rcArray = JSONArray()
            recovery_codes.forEach { rcArray.put(it) }
            obj.put("recovery_codes", rcArray)
        }
        obj.put("notes", notes)
        val tagsArray = JSONArray()
        tags.forEach { tagsArray.put(it) }
        obj.put("tags", tagsArray)
        obj.put("favorite", favorite)
        obj.put("created_at", created_at)
        obj.put("updated_at", updated_at)
        val histArray = JSONArray()
        history.forEach { histArray.put(it.toJsonObject()) }
        obj.put("history", histArray)
        return obj
    }

    companion object {
        fun fromJsonObject(obj: JSONObject): VaultItem {
            val rcList = if (obj.has("recovery_codes") && !obj.isNull("recovery_codes")) {
                val arr = obj.getJSONArray("recovery_codes")
                val list = mutableListOf<String>()
                for (i in 0 until arr.length()) {
                    list.add(arr.getString(i))
                }
                list
            } else null

            val tagList = mutableListOf<String>()
            if (obj.has("tags") && !obj.isNull("tags")) {
                val arr = obj.getJSONArray("tags")
                for (i in 0 until arr.length()) {
                    tagList.add(arr.getString(i))
                }
            }

            val histList = mutableListOf<VaultItemHistory>()
            if (obj.has("history") && !obj.isNull("history")) {
                val arr = obj.getJSONArray("history")
                for (i in 0 until arr.length()) {
                    histList.add(VaultItemHistory.fromJsonObject(arr.getJSONObject(i)))
                }
            }

            return VaultItem(
                id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                type = VaultItemType.fromKey(obj.optString("type", "password")),
                title = obj.optString("title", "Untitled"),
                username = if (obj.has("username") && !obj.isNull("username")) obj.optString("username") else null,
                secret = obj.optString("secret", ""),
                totp_secret = if (obj.has("totp_secret") && !obj.isNull("totp_secret")) obj.optString("totp_secret") else null,
                recovery_codes = rcList,
                notes = obj.optString("notes", ""),
                tags = tagList,
                favorite = obj.optBoolean("favorite", false),
                created_at = obj.optLong("created_at", System.currentTimeMillis()),
                updated_at = obj.optLong("updated_at", System.currentTimeMillis()),
                history = histList
            )
        }
    }
}

data class TombstoneItem(
    val id: String,
    val deleted_at: Long
) {
    fun toJsonObject(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("deleted_at", deleted_at)
        return obj
    }

    companion object {
        fun fromJsonObject(obj: JSONObject): TombstoneItem {
            return TombstoneItem(
                id = obj.optString("id", ""),
                deleted_at = obj.optLong("deleted_at", System.currentTimeMillis())
            )
        }
    }
}

data class DecryptedVaultPayload(
    val items: List<VaultItem> = emptyList(),
    val deleted_items: List<TombstoneItem> = emptyList(),
    val last_modified: Long = System.currentTimeMillis()
) {
    fun toJsonString(): String {
        val root = JSONObject()
        val itemsArray = JSONArray()
        items.forEach { itemsArray.put(it.toJsonObject()) }
        root.put("items", itemsArray)

        val deletedArray = JSONArray()
        deleted_items.forEach { deletedArray.put(it.toJsonObject()) }
        root.put("deleted_items", deletedArray)

        root.put("last_modified", last_modified)
        return root.toString()
    }

    companion object {
        fun fromJsonString(jsonStr: String): DecryptedVaultPayload {
            val root = JSONObject(jsonStr)
            val itemsList = mutableListOf<VaultItem>()
            if (root.has("items")) {
                val arr = root.getJSONArray("items")
                for (i in 0 until arr.length()) {
                    itemsList.add(VaultItem.fromJsonObject(arr.getJSONObject(i)))
                }
            }

            val deletedList = mutableListOf<TombstoneItem>()
            if (root.has("deleted_items")) {
                val arr = root.getJSONArray("deleted_items")
                for (i in 0 until arr.length()) {
                    deletedList.add(TombstoneItem.fromJsonObject(arr.getJSONObject(i)))
                }
            }

            return DecryptedVaultPayload(
                items = itemsList,
                deleted_items = deletedList,
                last_modified = root.optLong("last_modified", System.currentTimeMillis())
            )
        }
    }
}

data class KdfParams(
    val iterations: Int,
    val salt_b64: String
) {
    fun toJsonObject(): JSONObject {
        val obj = JSONObject()
        obj.put("iterations", iterations)
        obj.put("salt_b64", salt_b64)
        return obj
    }

    companion object {
        fun fromJsonObject(obj: JSONObject): KdfParams {
            return KdfParams(
                iterations = obj.optInt("iterations", 600000),
                salt_b64 = obj.optString("salt_b64", "")
            )
        }
    }
}

data class CipheraContainer(
    val app: String = "CIPHERA",
    val version: Int = 1,
    val kdf: String = "PBKDF2-SHA256",
    val kdf_params: KdfParams,
    val cipher: String = "AES-256-GCM",
    val nonce_b64: String,
    val ciphertext_b64: String
) {
    fun toJsonString(): String {
        val obj = JSONObject()
        obj.put("app", app)
        obj.put("version", version)
        obj.put("kdf", kdf)
        obj.put("kdf_params", kdf_params.toJsonObject())
        obj.put("cipher", cipher)
        obj.put("nonce_b64", nonce_b64)
        obj.put("ciphertext_b64", ciphertext_b64)
        return obj.toString()
    }

    companion object {
        fun fromJsonString(jsonStr: String): CipheraContainer {
            val obj = JSONObject(jsonStr)
            val app = obj.optString("app", "")
            val version = obj.optInt("version", 0)
            if (app != "CIPHERA" || version != 1) {
                throw IllegalArgumentException("Format kontainer .ciphera tidak valid atau versi tidak didukung.")
            }
            val kdfParamsObj = obj.getJSONObject("kdf_params")
            return CipheraContainer(
                app = app,
                version = version,
                kdf = obj.optString("kdf", "PBKDF2-SHA256"),
                kdf_params = KdfParams.fromJsonObject(kdfParamsObj),
                cipher = obj.optString("cipher", "AES-256-GCM"),
                nonce_b64 = obj.getString("nonce_b64"),
                ciphertext_b64 = obj.getString("ciphertext_b64")
            )
        }
    }
}

data class MergeReport(
    val added: Int,
    val updated: Int,
    val conflictsResolved: Int,
    val deleted: Int
)
