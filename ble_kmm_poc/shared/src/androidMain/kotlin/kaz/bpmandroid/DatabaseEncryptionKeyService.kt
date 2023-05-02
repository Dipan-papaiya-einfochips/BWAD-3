package kaz.bpmandroid

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.security.SecureRandom

class DatabaseEncryptionKeyService(var moContext: Context) {

    fun getDatabaseEncryptionKey(): String {
        val sharedPreferences: SharedPreferences = moContext.getSharedPreferences(
            "BPM",
            Context.MODE_PRIVATE
        )
        val myEdit = sharedPreferences.edit()
        sharedPreferences.getString("DB_KEY", "")?.let {
            Log.e("DB_EKY", it)
            return it
        }

        val key = generateKey()
        myEdit.putString("DB_KEY", key)
        return key
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    private fun generateKey(): String {
        val keyBytes = ByteArray(32)
        SecureRandom().nextBytes(keyBytes)
        return String(keyBytes)
    }
}