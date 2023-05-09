package kaz.bpmandroid

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.security.SecureRandom
import java.util.*

class DatabaseEncryptionKeyService(var moContext: Context) {

    fun getDatabaseEncryptionKey(): String {
        val sharedPreferences: SharedPreferences = moContext.getSharedPreferences(
            "BPM",
            Context.MODE_PRIVATE
        )
        val myEdit = sharedPreferences.edit()
        if (sharedPreferences.getString("DB_KEY", "")!!.isNotEmpty()) {
            sharedPreferences.getString("DB_KEY", "")?.let {
                Log.e("DB_KEY", it)
                return it
            }
        }

        val key = generateKey()
        Log.e("DB_KEY", key)
        myEdit.putString("DB_KEY", key)
        myEdit.commit()
        return key
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    private fun generateKey(): String {
        var uniqueID = UUID.randomUUID().toString()
        return uniqueID
    }
}