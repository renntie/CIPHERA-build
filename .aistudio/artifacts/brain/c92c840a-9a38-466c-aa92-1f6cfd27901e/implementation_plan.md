# CIPHERA — Zero-Knowledge Offline Cryptographic Vault (Android)

Aplikasi brankas digital kriptografi *client-side* 100% luring (offline) untuk menyimpan kata sandi, PIN/kode rahasia, *seed phrase* (12/24 kata), dan catatan rahasia dengan enkripsi berstandar militer (PBKDF2 600.000 iterasi + AES-256-GCM terikat AAD), autentikasi biometrik opsional, generator TOTP 2FA real-time, dan sinkronisasi manual tanpa konflik (*Smart Merge* file `.ciphera`).

---

## User Review & Critical Decisions

> [!IMPORTANT]
> Keputusan arsitektur dan fungsional yang telah disepakati pada tahap klarifikasi:

- **Arsitektur Utama (Terkonfirmasi)**: Native Android berbasis **Jetpack Compose + Kotlin**, mengimplementasikan algoritma kriptografi PBKDF2 (600.000 iterasi SHA-256) dan AES-256-GCM dengan *Additional Authenticated Data* (AAD) yang 100% kompatibel secara lintas platform dengan spesifikasi Web Crypto CIPHERA.
- **Autentikasi (Terkonfirmasi)**: Autentikasi ganda dengan Master Password sebagai kunci kriptografis utama, serta dukungan **Biometric Unlock (Fingerprint / Face)** melalui `androidx.biometric.BiometricPrompt` untuk pembukaan kunci cepat yang aman.
- **Manajemen File & Ekspor/Impor (Terkonfirmasi)**: Menggunakan Android **Storage Access Framework (SAF)** (`ActivityResultContracts.CreateDocument` dan `OpenDocument`) untuk menyimpan dan membaca file cadangan `.ciphera` secara aman langsung ke/dari memori internal, kartu SD, atau folder unduhan.

---

## 1. Overview & Core Concept

- **Fungsi Utama**: Brankas digital mandiri tanpa server (*zero-knowledge*), di mana semua data pengguna dienkripsi secara lokal sebelum disimpan di memori perangkat. Tidak ada data plaintext atau kunci privat yang pernah dikirim ke internet.
- **Pengguna Target**: Pengguna yang memprioritaskan privasi tinggi, pemilik aset kripto (Web3/Hardware Wallet seed phrases), dan profesional keamanan siber yang membutuhkan brankas luring yang tidak bergantung pada server cloud pihak ketiga.
- **Nilai Utama**: Keamanan kriptografi murni, bebas risiko peretasan cloud, dukungan sinkronisasi manual antar-perangkat menggunakan file `.ciphera` dengan algoritma *Smart Merge* berbasis *Tombstones* dan riwayat versi (*History*) sehingga tidak ada data yang hilang saat terjadi konflik.

---

## 2. User Experience & Visual Design

### Visual Identity & Theme
- **Tema Visual**: *Cyber Security Luxury Dark Mode*.
- **Palet Warna**:
  - Latar Belakang: Slate-950 (`#090D16`) & Zinc-950 (`#0B0F19`)
  - Permukaan Kartu: Slate-900 (`#0F172A`) dengan border halus Slate-800 (`#1E293B`)
  - Aksen Utama Kriptografi: Emerald-500 (`#10B981`) dan Cyan-500 (`#06B6D4`)
  - Aksen Peringatan / Kritis: Rose-500 (`#F43F5E`) dan Amber-500 (`#F59E0B`)
- **Branding & Logo**: Ikon vektor perisai heksagonal bertema sirkuit kriptografi dengan lubang kunci digital di tengahnya (*Custom Compose Canvas / Vector Drawable*).
- **Tipografi**: Font sans-serif modern yang tegas dipadukan dengan tipografi monospaced (`FontFamily.Monospace`) untuk kunci, seed phrase, token TOTP, dan kode rahasia.

### Key User Flows

```
┌────────────────────────────────────────────────────────────────────────┐
│                        CIPHERA APP LAUNCH                              │
└──────────────────────────────────┬─────────────────────────────────────┘
                                   │
                    Cek Brankas Lokal Tersimpan?
                   /                            \
           [TIDAK]                               [YA]
             /                                     \
    ┌──────────────────────┐              ┌───────────────────────────────┐
    │  ONBOARDING / CREATE │              │         LOCK SCREEN           │
    │  • Buat Master PW    │              │  • Input Master Password      │
    │  • Meteran Entropi   │              │  • Tombol Biometric Unlock    │
    │  • Impor .ciphera    │              │  • Ganti / Reset Brankas      │
    └──────────┬───────────┘              └───────────────┬───────────────┘
               │ (Enkripsi Awal)                          │ (Dekripsi Sukses)
               └───────────────────┬──────────────────────┘
                                   │
                                   ▼
┌────────────────────────────────────────────────────────────────────────┐
│                       MAIN DASHBOARD (UNLOCKED)                        │
├────────────────────────────────────────────────────────────────────────┤
│ • Header: Status 100% Offline, Search Bar, Indikator Idle Countdown   │
│ • Filter Bar: Semua, Favorit, Password, PIN/Kode, Seed Phrase, Catatan │
│ • Quick Actions: KeyGen (Password/Passphrase), Sync Center, Lock Now   │
│ • Item List: Kartu item terenkripsi dengan quick-copy & TOTP badge     │
│ • Item Detail Modal / Screen:                                          │
│    - Show/Hide secret (efek blur pengaman)                             │
│    - Seed Phrase numbered 12/24 grid view                              │
│    - Live TOTP 2FA widget (animasi lingkaran hitung mundur 30 detik)   │
│    - Recovery codes, tags, dan tab Riwayat Konflik/Versi Lama          │
└──────────────────────────────────┬─────────────────────────────────────┘
                                   │
              Auto-Lock (Idle 2 Menit / Minimize App)
                                   │
                                   ▼
┌────────────────────────────────────────────────────────────────────────┐
│          VAULT LOCKED: State sensitif dibersihkan dari RAM             │
└────────────────────────────────────────────────────────────────────────┘
```

- **Interactive Feedback & Keamanan Tambahan**:
  - **Auto-Clear Clipboard (30 Detik)**: Banner/snackbar hitung mundur visual di bagian bawah layar saat teks rahasia disalin, otomatis membersihkan clipboard Android setelah 30 detik atau tombol pembersih instan.
  - **Auto-Lock System**: Penguncian otomatis setelah 2 menit tanpa sentuhan atau saat aplikasi berpindah ke background (`ON_STOP` / `Lifecycle.Event`).
  - **Tampilan Khusus Seed Phrase**: Grid bernomor (1 s.d. 12 atau 24) dengan efek blur privasi sebelum tombol ungkap ditekan.

---

## 3. Key Product Decisions & Trade-Offs

1. **Native Java/Android Cryptography Architecture (JCA)**:
   - *Pilihan*: Menggunakan `SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")` dengan 600.000 iterasi dan `Cipher.getInstance("AES/GCM/NoPadding")` dengan 128-bit authentication tag serta `cipher.updateAAD(aad)`.
   - *Alasan*: Menjamin kinerja berkecepatan tinggi tanpa dependensi WASM, serta menghasilkan format kontainer JSON `.ciphera` yang 100% kompatibel dengan Web Crypto API.
2. **Biometric Unlock dengan Android Keystore**:
   - *Pilihan*: Master Password disimpan terenkripsi di `EncryptedSharedPreferences` / Android Keystore hardware-backed yang hanya bisa diakses setelah autentikasi biometrik berhasil.
   - *Alasan*: Pengguna tetap mendapatkan kenyamanan fingerprint unlock tanpa mengorbankan integritas zero-knowledge brankas.
3. **Format File Kontainer `.ciphera`**:
   - Struktur JSON terstandarisasi:
     ```json
     {
       "app": "CIPHERA",
       "version": 1,
       "kdf": "PBKDF2-SHA256",
       "kdf_params": {
         "iterations": 600000,
         "salt_b64": "..."
       },
       "cipher": "AES-256-GCM",
       "nonce_b64": "...",
       "ciphertext_b64": "..."
     }
     ```
   - String AAD: `CIPHERA_V1:<iterations>:<salt_b64>:<nonce_b64>`.
4. **Smart Merge & Tombstone Deletion**:
   - Setiap item yang dihapus dicatat pada `deleted_items: [{ id, deleted_at }]` agar tidak muncul kembali saat disinkronkan dengan file cadangan lama.
   - Konflik diselesaikan berdasarkan timestamp `updated_at` terbaru, sedangkan versi yang lebih lama otomatis diarsipkan ke dalam array `history` (maksimal 15 revisi).

---

## 4. Technical Architecture & Component Structure

```
com.example.ciphera/
├── MainActivity.kt                     # Entry point & biometric prompt integration
├── data/
│   ├── model/
│   │   ├── VaultItem.kt                # Model item, tipe, riwayat, & tombstone
│   │   ├── CipheraContainer.kt         # Model serialisasi kontainer .ciphera
│   │   └── MergeReport.kt              # Model rekapitulasi Smart Merge
│   ├── crypto/
│   │   ├── CipheraCryptoEngine.kt      # PBKDF2 (600K) + AES-GCM-256 + AAD
│   │   ├── TotpEngine.kt               # RFC 6238 HMAC-SHA1 TOTP generator
│   │   └── BiometricAuthManager.kt     # Integrasi BiometricPrompt & Keystore
│   ├── storage/
│   │   └── VaultStorageManager.kt      # Simpanan lokal terenkripsi & SAF I/O
│   └── merge/
│       └── SmartMergeEngine.kt         # Logika penggabungan tanpa konflik
├── ui/
│   ├── theme/                          # Color, Theme, & Typography (Cyber Dark)
│   ├── components/
│   │   ├── CipheraLogo.kt              # Vektor logo perisai kriptografi
│   │   ├── LiveTotpWidget.kt           # Widget 2FA 6-digit real-time
│   │   ├── ClipboardCountdownBar.kt    # Banner timer auto-clear 30 detik
│   │   └── PasswordStrengthMeter.kt    # Pengukur entropi bit kata sandi
│   ├── screens/
│   │   ├── AuthGateScreen.kt           # Layar buka kunci / onboarding brankas
│   │   ├── DashboardScreen.kt          # Layar utama brankas & pencarian
│   │   ├── ItemDetailDialog.kt         # Modal detail rahasia & riwayat
│   │   ├── ItemEditorDialog.kt         # Modal formulir tambah/edit item
│   │   ├── KeyGenDialog.kt             # Generator password & passphrase acak
│   │   └── SyncSecurityDialog.kt       # Modal ekspor, impor SAF, Smart Merge & rotasi PW
│   └── viewmodel/
│       └── VaultViewModel.kt           # State management & auto-lock lifecycle
```

---

## 5. Implementation Steps & Verification Plan

1. **Pembaruan Konfigurasi & Dependensi**:
   - Tambahkan dependensi `androidx.biometric:biometric-ktx` ke `app/build.gradle.kts` dan sinkronkan manifest.
   - Perbarui `metadata.json` dan `strings.xml` dengan identitas nama **CIPHERA**.
2. **Implementasi Lapisan Kriptografi & TOTP**:
   - Buat `CipheraCryptoEngine.kt` (PBKDF2 600K iterasi, AES-256-GCM, AAD).
   - Buat `TotpEngine.kt` (Base32 decoder, HMAC-SHA1, RFC 6238 30 detik).
   - Buat `SmartMergeEngine.kt` (penggabungan tombstone, deteksi konflik, dan riwayat).
3. **Implementasi Penyimpanan & Autentikasi Biometrik**:
   - Buat `VaultStorageManager.kt` dengan dukungan penyimpanan lokal dan integrasi SAF untuk file `.ciphera`.
   - Buat `BiometricAuthManager.kt` untuk otentikasi sidik jari perangkat Android.
4. **Implementasi UI Jetpack Compose & State Management**:
   - Konfigurasi tema cyber dark di `Color.kt` dan `Theme.kt`.
   - Bangun `AuthGateScreen`, `DashboardScreen`, `ItemDetailDialog`, `ItemEditorDialog`, `KeyGenDialog`, dan `SyncSecurityDialog`.
   - Hubungkan `VaultViewModel` dengan timer auto-lock 2 menit dan auto-clear clipboard 30 detik.
5. **Verifikasi & Pengujian Kompilasi**:
   - Jalankan `compile_applet` untuk memastikan seluruh kode terkompilasi 100% tanpa error.
