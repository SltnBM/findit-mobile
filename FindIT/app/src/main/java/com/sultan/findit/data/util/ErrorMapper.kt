package com.sultan.findit.data.util

import org.json.JSONObject
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

fun Throwable.toFriendlyMessage(): String {
    return when (this) {
        is UnknownHostException -> {
            "Tidak bisa terhubung ke server. Periksa koneksi internet, IP backend, atau pastikan Laravel sedang berjalan."
        }

        is ConnectException -> {
            "Server tidak merespons. Pastikan Laravel aktif dan HP berada di jaringan WiFi yang sama dengan laptop."
        }

        is SocketTimeoutException -> {
            "Koneksi terlalu lama. Coba ulangi beberapa saat lagi."
        }

        is HttpException -> {
            val errorBodyString = response()?.errorBody()?.string()
            val serverMessage = try {
                if (!errorBodyString.isNullOrBlank()) {
                    val json = JSONObject(errorBodyString)
                    if (json.has("message")) {
                        json.getString("message")
                    } else if (json.has("errors")) {
                        val errorsObj = json.getJSONObject("errors")
                        val firstKey = errorsObj.keys().next()
                        errorsObj.getJSONArray(firstKey).getString(0)
                    } else {
                        null
                    }
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }

            when (code()) {
                400 -> serverMessage ?: "Request tidak valid. Periksa kembali data yang dikirim."
                401 -> serverMessage ?: "Sesi login sudah habis atau token tidak valid. Silakan login ulang."
                403 -> serverMessage ?: "Akses ditolak. Akun ini tidak memiliki hak admin."
                404 -> serverMessage ?: "Data tidak ditemukan."
                422 -> serverMessage ?: "Input data salah atau terjadi kesalahan validasi."
                500 -> "Terjadi gangguan pada server."
                else -> serverMessage ?: "Terjadi kesalahan server. Kode error: ${code()}."
            }
        }

        else -> {
            val raw = message.orEmpty()

            when {
                raw.contains("timeout", ignoreCase = true) ->
                    "Koneksi terlalu lama. Coba ulangi."

                raw.contains("failed to connect", ignoreCase = true) ->
                    "Gagal terhubung ke server. Periksa IP backend dan jaringan."

                raw.contains("CLEARTEXT", ignoreCase = true) ->
                    "Aplikasi belum mengizinkan koneksi HTTP. Periksa AndroidManifest dan network security config."

                raw.contains("File foto tidak dapat dibaca", ignoreCase = true) ->
                    "Foto tidak dapat dibaca. Pilih ulang foto dari galeri."

                raw.isBlank() ->
                    "Terjadi kesalahan. Silakan coba lagi."

                else ->
                    "Terjadi kesalahan. Silakan coba lagi."
            }
        }
    }
}