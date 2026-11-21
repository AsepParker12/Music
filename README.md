# Music

Pemutar musik offline untuk Android. Semua lagu berasal dari file yang tersimpan di perangkatmu sendiri — tanpa akun, tanpa streaming, tanpa iklan, dan tanpa pelacak.

## Unduh

Ambil APK yang sesuai dengan perangkatmu:

| File | Untuk |
| --- | --- |
| `music-<versi>-release-arm64-v8a.apk` | Hampir semua HP Android modern (2017 ke atas). Ukuran paling kecil — **ini yang disarankan**. |
| `music-<versi>-release-universal.apk` | Semua perangkat, kalau ragu pilih yang mana. |
| `music-<versi>-release-armeabi-v7a.apk` | HP lama dengan prosesor ARM 32-bit. |
| `music-<versi>-release-x86_64.apk` / `music-<versi>-release-x86.apk` | Emulator dan perangkat x86. |

Saat pertama kali memasang, Android akan meminta izin memasang aplikasi dari browser atau pengelola berkas. Setelah terpasang, pembaruan berikutnya bisa dicek dari dalam aplikasi (Settings › Updates).

## Fitur utama

- **Pustaka lokal** — trek, artis, album, genre, dan folder dari penyimpanan perangkat; tersinkron otomatis saat file berubah.
- **Playlist** — buat, urutkan dengan seret, impor/ekspor M3U, plus Favorit, Terakhir diputar, dan Paling sering diputar.
- **Pemutar lengkap** — antrean yang bisa diatur ulang, shuffle, repeat, sleep timer, dan equalizer bawaan.
- **Lirik tersinkron** — membaca lirik dari file audio (LRC, TTML, QRC) dengan sorotan per baris dan per kata.
- **Tampilan Material 3** — warna tema mengikuti sampul lagu yang sedang diputar, mode terang dan gelap.
- **Cadangan & pemulihan** — simpan playlist, favorit, riwayat, dan pengaturan; bisa terenkripsi dan bisa tersambung ke Google Drive.
- **Unduhan & transkrip** — tersedia sebagai fitur tambahan; transkrip memakai kunci API milikmu sendiri.
- **Privasi** — tidak ada akun, tidak ada analitik, tidak ada iklan. Rincian lengkap ada di [PRIVACY.md](PRIVACY.md).

## Persyaratan

- Android 8.0 (API 26) atau lebih baru.
- File musik tersimpan di perangkat.

## Bangun dari sumber

Siapkan JDK 17 atau lebih baru (disarankan 21) dan Android SDK platform 37, lalu:

```bash
git clone https://github.com/AsepParker12/Music.git
cd Music
./gradlew :app:assembleDebug
```

Hasilnya ada di `app/build/outputs/apk/`. Build debug terpasang sebagai **Music Debug** (`com.music.app.debug`), terpisah dari versi release.

Untuk build release (per-ABI + universal, dengan minify dan shrink):

```bash
./gradlew :app:assembleRelease
```

APK release hanya dapat dipasang bila ditandatangani. Penandatanganan diatur lewat file `keystore.properties` di root proyek (formatnya dijelaskan di `app/build.gradle.kts`); tanpa file itu, hasil build release tidak bertanda tangan dan tidak bisa dipasang.

## Struktur proyek

| Modul | Isi |
| --- | --- |
| `:app` | Kelas aplikasi, aktivitas utama, layar pembuka |
| `:core:model` · `:core:common` | Model data dan utilitas bersama |
| `:core:database` · `:core:data` · `:core:datastore` | Basis data Room, repositori pustaka, pengaturan pengguna |
| `:core:designsystem` · `:core:navigation` | Tema, komponen UI, navigasi |
| `:feature:scanner` · `:feature:playback` · `:feature:player` | Pemindaian media, layanan putar, antarmuka pemutar |
| `:feature:library` · `:feature:settings` · `:feature:equalizer` | Pustaka & playlist, pengaturan, equalizer |
| `:feature:update` · `:feature:backup` · `:feature:download` | Pembaruan dalam aplikasi, cadangan, unduhan |

## Lisensi

Music adalah perangkat lunak bebas berlisensi **GNU General Public License v3.0** — lihat [LICENSE](LICENSE). Komponen pihak ketiga di dalamnya tunduk pada lisensi masing-masing; daftar lengkapnya tersedia di aplikasi (Settings › About › Licenses).
