<#
  Set environment variable keystore rilis ZUQrCode (dibaca app/build.gradle).

  Pakai (PowerShell, di laptop yang ada Android Studio-nya):
    powershell -ExecutionPolicy Bypass -File tools\set-keystore-env.ps1          # isi / ganti
    powershell -ExecutionPolicy Bypass -File tools\set-keystore-env.ps1 -Tampil  # cek yang sudah diset
    powershell -ExecutionPolicy Bypass -File tools\set-keystore-env.ps1 -Hapus   # hapus semua

  Variabel disimpan di level USER Windows (HKCU\Environment). Setelah menjalankan skrip ini,
  TUTUP SEMUA jendela Android Studio lalu buka lagi (Gradle daemon membaca env saat AS dibuka).
#>
param([switch]$Hapus, [switch]$Tampil)

$NAMA = 'ZUQR_KEYSTORE_FILE', 'ZUQR_KEYSTORE_PASSWORD', 'ZUQR_KEY_ALIAS', 'ZUQR_KEY_PASSWORD'

if ($Hapus) {
    foreach ($n in $NAMA) { [Environment]::SetEnvironmentVariable($n, $null, 'User') }
    Write-Host 'Semua variabel ZUQR_* dihapus. Restart Android Studio.'
    return
}

if ($Tampil) {
    foreach ($n in $NAMA) {
        $v = [Environment]::GetEnvironmentVariable($n, 'User')
        if ($n -like '*PASSWORD') { $v = if ($v) { '(terisi, ' + $v.Length + ' karakter)' } else { '(kosong)' } }
        elseif (-not $v) { $v = '(kosong)' }
        Write-Host ('{0,-24} {1}' -f $n, $v)
    }
    return
}

function Baca-Rahasia([string]$label) {
    $s = Read-Host $label -AsSecureString
    $p = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($s)
    try { [Runtime.InteropServices.Marshal]::PtrToStringBSTR($p) }
    finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($p) }
}

$file = (Read-Host 'Path file keystore (.jks), mis. C:\kunci\zarda.jks').Trim('"', ' ')
if (-not (Test-Path -LiteralPath $file -PathType Leaf)) { throw "File keystore tidak ditemukan: $file" }
$file = (Resolve-Path -LiteralPath $file).Path

$alias = Read-Host 'Key alias [key0]'
if (-not $alias) { $alias = 'key0' }
$storePass = Baca-Rahasia 'Password keystore'
$keyPass = Baca-Rahasia 'Password key (Enter = sama dengan password keystore)'
if (-not $keyPass) { $keyPass = $storePass }

# Cek keystore bila keytool ada (JBR Android Studio / JAVA_HOME / PATH)
$keytool = @(
    "$env:ProgramFiles\Android\Android Studio\jbr\bin\keytool.exe",
    $(if ($env:JAVA_HOME) { "$env:JAVA_HOME\bin\keytool.exe" }),
    (Get-Command keytool -ErrorAction SilentlyContinue | Select-Object -ExpandProperty Source -First 1)
) | Where-Object { $_ -and (Test-Path $_) } | Select-Object -First 1
if ($keytool) {
    & $keytool -list -keystore $file -storepass $storePass -alias $alias *> $null
    if ($LASTEXITCODE -ne 0) { throw 'keytool gagal membuka keystore: password atau alias salah. Tidak ada yang disimpan.' }
    Write-Host "Keystore dicek dengan keytool: OK"
} else {
    Write-Host 'keytool tidak ditemukan - keystore tidak dicek (akan ketahuan saat build release).'
}

[Environment]::SetEnvironmentVariable('ZUQR_KEYSTORE_FILE', $file, 'User')
[Environment]::SetEnvironmentVariable('ZUQR_KEY_ALIAS', $alias, 'User')
[Environment]::SetEnvironmentVariable('ZUQR_KEYSTORE_PASSWORD', $storePass, 'User')
[Environment]::SetEnvironmentVariable('ZUQR_KEY_PASSWORD', $keyPass, 'User')
Write-Host ''
Write-Host 'Tersimpan. Tutup semua jendela Android Studio lalu buka lagi, kemudian:'
Write-Host '  Build > Generate Signed App Bundle / APK, atau terminal: .\gradlew assembleRelease'
