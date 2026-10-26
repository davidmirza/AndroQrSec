# ZUQrCode (AndroQrSec)  (APK pembaca QR qrsec)

Aplikasi Android pembaca QR/barcode yang juga membaca **secret tersembunyi** pada QR buatan **qrsec**
(DLL `mrzha` / aplikasi QrSec Designer), dan menampilkan apakah QR itu **bertanda tangan sah**.

- QR biasa: terbaca seperti scanner lain.
- QR qrsec: menampilkan **main** (teks yang terlihat semua scanner), **secret**, dan **indikator signing key**.

| Dokumen | Isi |
|---|---|
| **README.md** (file ini) | Gambaran umum, struktur kode, alur, cara build |
| [CATATAN_QRSEC.md](CATATAN_QRSEC.md) | Rincian perubahan 2026-10-26: migrasi AGP 9, dependensi, perilaku targetSdk 36 |
| [CATATAN_SERVER.md](CATATAN_SERVER.md) | Cara mengaktifkan server verifikasi (kontrak API, tabel SQL) |
| `..\m_david-qrcodec-907e614c719c\QrSecDesigner\README.md` | Aplikasi Windows pembuat QR + signing key |

---

## 1. Kebutuhan & membuka project

| Komponen | Versi |
|---|---|
| Android Studio | Rabbit 1 \| 2026.2.1 (mendukung AGP 7.1 - 9.4) |
| Android Gradle Plugin | 9.4.0 |
| Gradle | 9.6.0 (diunduh otomatis) |
| JDK | Embedded JDK Android Studio (17 ke atas) |
| compileSdk / targetSdk / minSdk | 36 / 36 / 24 (Android 7.0 ke atas) |
| Bahasa | Java (+ `Check.kt`, dikompilasi Kotlin bawaan AGP 9) |

1. **File > Open**: pilih folder ini, lalu tunggu **Gradle Sync** (butuh internet).
2. Bila diminta, pasang **Android SDK Platform 36** lewat SDK Manager.
3. `local.properties` berisi `sdk.dir` milik PC lama; Android Studio akan menggantinya. Bila tidak, hapus barisnya.
4. Jalankan ke HP/emulator dengan tombol **Run**.

`gradle-wrapper.jar` belum ada. Android Studio tidak membutuhkannya; untuk `.\gradlew` di terminal, buat sekali
lewat panel **Gradle** (kanan) → *ZUQrCode* → *Tasks* → *build setup* → klik dua kali **wrapper**.

---

## 2. Struktur project

```
androidzarda-zuqrcode-cf60f3d6806a/
├── app/
│   ├── build.gradle                 ← versi aplikasi, dependensi, keystore (env ZUQR_*)
│   └── src/main/
│       ├── AndroidManifest.xml      ← daftar layar, izin, AdMob App ID
│       ├── java/com/zarda/simpleqrcodereader/   ← SEMUA KODE LOGIKA (lihat 2.1)
│       └── res/
│           ├── layout/              ← tampilan layar (XML)
│           ├── values/ values-v35/  ← teks, warna, tema (v35 = Android 15+)
│           └── drawable/ anim/ font/
├── nativetemplates/                 ← modul tampilan iklan native (TemplateView)
├── tools/set-keystore-env.ps1       ← mengisi environment variable keystore
├── build.gradle  settings.gradle  gradle.properties
└── gradle/wrapper/gradle-wrapper.properties
```

### 2.1 Kode logika: `app/src/main/java/com/zarda/simpleqrcodereader/`

| File / folder | Fungsi |
|---|---|
| **`QrSec.java`** | Inti qrsec: mengambil secret dari `rawBytes`, mendeteksi & memverifikasi signing key, **daftar `PUBLIC_KEYS`** |
| `ServerQr.java` | Klien server verifikasi (siap pakai, pemanggilnya masih dikomentari) |
| `Check.kt` | Launcher (layar pertama), langsung membuka `MainActivity` |
| **`MainActivity.java`** | Layar scanner kamera; `handleResult` memproses hasil scan |
| `acHasil.java` | Layar hasil: main, indikator signing key, secret, iklan native |
| `acthasil.java` | Layar riwayat scan |
| `acBout.java` | Layar About (versi aplikasi) |
| `pandora.java` | Meminta izin kamera |
| `Histori`, `HistoDao`, `Database`, `HistoRepository`, `ViewModel`, `HistoriAdapter` | Riwayat scan (Room, file `tbhis.db`) |
| `libs/` | Mesin kamera: `CodeScanner`, `CodeScannerView` (preview tidak tertarik), `Decoder`, `DecodeTask` |
| `qrcode/`, `common/` | ZXing tertanam: deteksi & decode QR, binarizer, Reed-Solomon |
| `aztec/` `datamatrix/` `oned/` `pdf417/` `maxicode/` `multi/` `result/` + kelas di root (`Result`, `MultiFormatReader`, …) | ZXing untuk format barcode lain. **Sebaiknya tidak diubah.** |

`splash.java` dan `Checj.java` terdaftar di manifest tetapi tidak pernah dibuka (kode sisa versi lama).

---

## 3. Alur aplikasi

### 3.1 Perpindahan layar

```
Check (launcher, 1 detik) ──► MainActivity (scanner) ──scan──► acHasil (hasil)
                                   │
                                   ├── tombol riwayat ──► acthasil
                                   └── tombol about   ──► acBout ──back──► MainActivity
```

- `MainActivity.onCreate`: meminta izin kamera (`pandora`), menyiapkan `CodeScanner`, iklan, dan ViewModel riwayat.
- `onResume`: preview kamera dimulai (bila izin sudah ada). `onPause`: kamera dilepas.
- Tombol BACK memakai `OnBackPressedCallback` (wajib sejak targetSdk 36 / predictive back).

### 3.2 Saat QR di-scan

```
Frame kamera (NV21)
  └► libs/CodeScanner.PreviewCallback ──► libs/Decoder (thread "cs-decoder")
       └► DecodeTask: putar sesuai orientasi + potong ke bingkai
            └► ZXing tertanam: HybridBinarizer → Detector (finder pattern, perspektif)
                 → Decoder (format/versi, unmask, Reed-Solomon) → DecodedBitStreamParser
                 └► Result: getText() = main   |   getRawBytes() = seluruh data QR
                      └► QrSec.periksa(text, rawBytes)
                           └► MainActivity.handleResult
                                ├► simpan riwayat (Room)
                                ├► [SERVER-1] kirim ke server (nonaktif)
                                └► buka acHasil (status, secret, keyId)
```

ZXing membaca QR **secara standar** dan berhenti di terminator, sehingga `getText()` hanya berisi main.
Secret diambil terpisah oleh `QrSec` dari `getRawBytes()`, yaitu data yang sudah melewati koreksi error.

### 3.3 Format data qrsec di dalam QR

```
[ECI UTF-8][mode BYTE][jumlah byte main][main][0x00][N+33][amplop N byte][padding EC 11 EC 11 ...]
                                               ▲ scanner biasa berhenti di sini

Amplop bertanda tangan : [0x01][keyId][secret UTF-8][tanda tangan ECDSA 64 byte]
Amplop tanpa signing   : [secret UTF-8]
```

Tanda tangan ECDSA P-256 / SHA-256 dibuat atas `[0x01][keyId][panjang main 2 byte][main][secret]`.
Main ikut ditandatangani, jadi secret dari QR asli tidak bisa dipindah ke QR lain.

### 3.4 Status hasil `QrSec.periksa`

| Status | Kondisi | Tampilan di `acHasil` |
|---|---|---|
| `SAH` | Ada tanda tangan, public key keyId-nya terdaftar, tanda tangan cocok | 🔑 Signing key: **DITEMUKAN** (keyId N), ✔ ASLI (hijau) + secret |
| `TANPA_TANDA` | Dicetak dengan signing dimatikan / format lama | 🔓 Signing key: **TIDAK AKTIF** (abu-abu) + secret |
| `PALSU` | Ada tanda tangan tetapi tidak cocok, atau keyId tidak terdaftar | 🔑 Signing key: DITEMUKAN, ✖ TIDAK SAH (merah) + secret "tidak terjamin" |
| `BIASA` | QR dari aplikasi lain | Hanya teks main (tampilan lama) |

Riwayat menyimpan main + label status, mis. `BBBHWF0504841 [SAH]`.

---

## 4. Signing key (public key)

APK hanya menyimpan **public key**. Kunci privatnya ada di PC pencetak (file `.key` QrSec Designer).

`QrSec.java`:

```java
static {
    PUBLIC_KEYS.put(1, "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE....");
}
```

> Bawaan project masih berisi **kunci uji** keyId 1. Ganti dengan kunci Anda sebelum rilis.

Langkah (sekali per kunci):
1. QrSec Designer → **Buat kunci baru...** (atau muat kunci lalu **Salin public key untuk APK**).
2. Tempel baris `PUBLIC_KEYS.put(...)` di blok di atas, lalu hapus baris kunci uji.
3. Build ulang dan pasang APK.

Kunci pengganti memakai **keyId baru**. Barisnya **ditambahkan**, baris lama dibiarkan, supaya label lama tetap sah.
Gejala "signing key DITEMUKAN tetapi TIDAK SAH" pada QR baru hampir selalu berarti langkah 2 belum dilakukan.

---

## 5. Build release & keystore

Password keystore **tidak** ditulis di `build.gradle`; dibaca dari environment variable:

| Variabel | Isi |
|---|---|
| `ZUQR_KEYSTORE_FILE` | Path file `.jks` |
| `ZUQR_KEYSTORE_PASSWORD` | Password keystore |
| `ZUQR_KEY_ALIAS` | Alias key (dulu `key0`) |
| `ZUQR_KEY_PASSWORD` | Password key |

Mengisi (PowerShell, di folder project):

```powershell
powershell -ExecutionPolicy Bypass -File tools\set-keystore-env.ps1
```

Opsi `-Tampil` untuk mengecek, `-Hapus` untuk menghapus. Setelah mengisi, **tutup semua jendela Android Studio lalu buka lagi**.
Alternatif: tulis keempat nilai yang sama di `local.properties` (sudah di-ignore).

Build: **Build > Generate Signed App Bundle / APK**, atau `.\gradlew assembleRelease` / `.\gradlew bundleRelease`.
Tanpa keystore, build debug tetap jalan dan build release dibuat tanpa tanda tangan (Gradle menampilkan peringatan `ZUQR: keystore belum diset`).

Sebelum upload ke Play Store, naikkan `versionCode` / `versionName` di `app/build.gradle` (sekarang 12 / 2.6).

---

## 6. Server verifikasi (belum aktif)

Kode klien sudah ada di `ServerQr.java`. Pemanggilnya dikomentari di `MainActivity.java` (blok `[SERVER-1]`, `[SERVER-2]`,
`[SERVER-3]`), begitu juga izin lokasi di `AndroidManifest.xml`. Langkah mengaktifkan, format JSON, dan saran logika
server (verifikasi ulang tanda tangan, counter scan, deteksi salinan): **[CATATAN_SERVER.md](CATATAN_SERVER.md)**.

---

## 7. Status pengujian

| Bagian | Status |
|---|---|
| `QrSec`, `ServerQr`, ZXing tertanam | Dikompilasi & diuji di JDK dengan 18 gambar QR dan server tiruan |
| Activity, layout, Gradle (AGP 9.4) | **Belum pernah di-build di Android Studio**; diperiksa manual |
| Tampilan di HP (preview kamera, edge-to-edge, iklan) | Belum dicoba di perangkat |

Bila Gradle Sync / build gagal, catat pesan error lengkapnya dari jendela **Build**.

---

## 8. Masalah umum

| Gejala | Penyebab & solusi |
|---|---|
| QR qrsec: signing key DITEMUKAN tetapi **TIDAK SAH** | Public key kunci pencetak belum ada di `PUBLIC_KEYS` (bagian 4). Pesan "Signing key N tidak dikenal" = keyId N belum didaftarkan. |
| Kamera hitam / toast "Kamera tidak bisa dibuka" | Izin kamera ditolak. Buka **Pengaturan HP > Aplikasi > ZUQrCode > Izin > Kamera**. |
| QR sangat rapat sulit terbaca | Dekatkan HP, nyalakan flash, atau cetak QR lebih besar dengan EC Q/H di QrSec Designer. |
| Peringatan `ZUQR: keystore belum diset` | Normal untuk build debug. Untuk release, isi keystore (bagian 5) dan buka ulang Android Studio. |
| Gradle Sync meminta SDK / platform | Pasang **Android SDK Platform 36** lewat SDK Manager. |
| `.\gradlew` tidak jalan | `gradle-wrapper.jar` belum ada; jalankan task **wrapper** dari panel Gradle (bagian 1), atau gunakan menu Build. |
