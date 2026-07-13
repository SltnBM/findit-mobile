package com.sultan.findit.data.util

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom

object DatabaseKeyManager {
    private const val PREFS_NAME = "secure_db_prefs"
    private const val KEY_PASSPHRASE = "db_passphrase"

    fun getPassphrase(context: Context): ByteArray {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        var passphraseString = sharedPreferences.getString(KEY_PASSPHRASE, null)

        if (passphraseString == null) {
            val secureRandom = SecureRandom()
            val bytes = ByteArray(32)
            secureRandom.nextBytes(bytes)

            passphraseString = Base64.encodeToString(bytes, Base64.NO_WRAP)

            sharedPreferences.edit().putString(KEY_PASSPHRASE, passphraseString).apply()
        }

        return passphraseString.toByteArray()
    }
}