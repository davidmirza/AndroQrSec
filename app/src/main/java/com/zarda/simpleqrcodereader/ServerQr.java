package com.zarda.simpleqrcodereader;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * [SERVER] Klien verifikasi scan ke server - SIAP PAKAI TETAPI BELUM DIPANGGIL.
 * Pemanggilnya masih dikomentari di MainActivity (cari "[SERVER]"). Cara mengaktifkan: CATATAN_SERVER.md
 *
 * Kontrak (POST JSON ke URL_SCAN):
 *   kirim : {"main","rahasia","amplop"(base64),"status","adaTandaTangan","keyId","lat","lon","akurasi","perangkat","waktuMs"}
 *   balas : {"hasil":"ASLI|SALINAN_DICURIGAI|TIDAK_TERDAFTAR|PALSU|TANPA_TANDA","jumlahScan":12,"pesan":"..."}
 *
 * Java murni (tanpa API Android) supaya bisa diuji di JVM. Wajib dipanggil dari thread latar.
 */
public final class ServerQr {

    /** [SERVER] Ganti dengan alamat API server Anda (pakai https). */
    public static final String URL_SCAN = "https://GANTI-SERVER-ANDA/api/qr/scan";
    public static final int TIMEOUT_MS = 10000;

    private static final Charset UTF8 = Charset.forName("UTF-8");

    public static final class Balasan {
        /** true bila server membalas HTTP 2xx dan JSON terbaca */
        public final boolean berhasil;
        public final String hasil;
        public final int jumlahScan;
        public final String pesan;

        Balasan(boolean berhasil, String hasil, int jumlahScan, String pesan) {
            this.berhasil = berhasil;
            this.hasil = hasil;
            this.jumlahScan = jumlahScan;
            this.pesan = pesan;
        }
    }

    private ServerQr() {
    }

    /**
     * Kirim hasil scan ke server. lat/lon/akurasi boleh null (izin lokasi ditolak / tidak ada).
     * Tidak pernah melempar exception: kegagalan jaringan dikembalikan sebagai Balasan(berhasil=false).
     */
    public static Balasan kirim(String url, QrSec.Hasil hasil, Double lat, Double lon, Float akurasi,
                                String perangkat) {
        HttpURLConnection con = null;
        try {
            byte[] body = buatJson(hasil, lat, lon, akurasi, perangkat, System.currentTimeMillis()).getBytes(UTF8);
            con = (HttpURLConnection) URI.create(url).toURL().openConnection();
            con.setConnectTimeout(TIMEOUT_MS);
            con.setReadTimeout(TIMEOUT_MS);
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setFixedLengthStreamingMode(body.length);
            OutputStream out = con.getOutputStream();
            try {
                out.write(body);
            } finally {
                out.close();
            }
            int code = con.getResponseCode();
            InputStream in = code >= 400 ? con.getErrorStream() : con.getInputStream();
            String balas = in == null ? "" : baca(in);
            return bacaJson(code, balas);
        } catch (Exception e) {
            return new Balasan(false, "GAGAL_KONEKSI", -1, "Server tidak bisa dihubungi: " + e.getMessage());
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }

    static String buatJson(QrSec.Hasil h, Double lat, Double lon, Float akurasi, String perangkat, long waktuMs) {
        StringBuilder sb = new StringBuilder(256);
        sb.append('{');
        teks(sb, "main", h.main).append(',');
        teks(sb, "rahasia", h.rahasia).append(',');
        teks(sb, "amplop", h.amplop == null ? null : QrSec.base64Encode(h.amplop)).append(',');
        teks(sb, "status", h.status.name()).append(',');
        sb.append("\"adaTandaTangan\":").append(h.adaTandaTangan).append(',');
        sb.append("\"keyId\":").append(h.keyId).append(',');
        angka(sb, "lat", lat).append(',');
        angka(sb, "lon", lon).append(',');
        angka(sb, "akurasi", akurasi == null ? null : akurasi.doubleValue()).append(',');
        teks(sb, "perangkat", perangkat).append(',');
        sb.append("\"waktuMs\":").append(waktuMs);
        sb.append('}');
        return sb.toString();
    }

    static Balasan bacaJson(int code, String json) {
        String hasil = ambilTeks(json, "hasil");
        String pesan = ambilTeks(json, "pesan");
        Integer jumlah = ambilAngka(json, "jumlahScan");
        boolean ok = code >= 200 && code < 300 && hasil != null;
        if (!ok && pesan == null) {
            pesan = "Balasan server tidak valid (HTTP " + code + ")";
        }
        return new Balasan(ok, hasil == null ? "ERROR" : hasil, jumlah == null ? -1 : jumlah, pesan);
    }

    private static StringBuilder teks(StringBuilder sb, String kunci, String nilai) {
        sb.append('"').append(kunci).append("\":");
        if (nilai == null) {
            return sb.append("null");
        }
        sb.append('"');
        for (int i = 0; i < nilai.length(); i++) {
            char c = nilai.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format(Locale.ROOT, "\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.append('"');
    }

    private static StringBuilder angka(StringBuilder sb, String kunci, Double nilai) {
        sb.append('"').append(kunci).append("\":");
        return nilai == null ? sb.append("null") : sb.append(String.format(Locale.ROOT, "%.7f", nilai));
    }

    private static String ambilTeks(String json, String kunci) {
        Matcher m = Pattern.compile("\"" + kunci + "\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").matcher(json);
        if (!m.find()) {
            return null;
        }
        return m.group(1).replace("\\\"", "\"").replace("\\n", "\n").replace("\\\\", "\\");
    }

    private static Integer ambilAngka(String json, String kunci) {
        Matcher m = Pattern.compile("\"" + kunci + "\"\\s*:\\s*(-?\\d+)").matcher(json);
        return m.find() ? Integer.valueOf(m.group(1)) : null;
    }

    private static String baca(InputStream in) throws java.io.IOException {
        try {
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            byte[] b = new byte[4096];
            int n;
            while ((n = in.read(b)) > 0) {
                buf.write(b, 0, n);
            }
            return new String(buf.toByteArray(), UTF8);
        } finally {
            in.close();
        }
    }
}
