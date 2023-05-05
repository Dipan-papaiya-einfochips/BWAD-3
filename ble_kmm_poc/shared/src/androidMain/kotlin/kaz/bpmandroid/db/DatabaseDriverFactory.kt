package kaz.bpmandroid.db

import android.content.Context
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import kaz.bpmandroid.DatabaseEncryptionKeyService
import net.sqlcipher.database.SupportFactory

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        var loDBEncryptionKeyService = DatabaseEncryptionKeyService(context)
        var key = loDBEncryptionKeyService.getDatabaseEncryptionKey()
        val passphrase: ByteArray = "test".toByteArray()
        val factory = SupportFactory(passphrase)
        return AndroidSqliteDriver(AppDatabase.Schema, context, "HealthyHeart1.db", factory)
    }
}