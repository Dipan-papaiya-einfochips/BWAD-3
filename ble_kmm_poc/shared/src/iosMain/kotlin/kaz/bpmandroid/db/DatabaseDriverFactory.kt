package kaz.bpmandroid.db

import co.touchlab.sqliter.DatabaseConfiguration
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.drivers.native.NativeSqliteDriver
import com.squareup.sqldelight.drivers.native.wrapConnection

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        /*
        val encryption = DatabaseConfiguration.Encryption("test")
        val config = DatabaseConfiguration(
            name = "HealthyHeart1.db",
            version = AppDatabase.Schema.version,
            create = { connection ->
                wrapConnection(connection) { AppDatabase.Schema.create(it) }
            },
            upgrade = { connection, oldVersion, newVersion ->
                //   logger.debug("trying to execute db migration: $oldVersion -> $newVersion")
                wrapConnection(connection) {
                    try {
                        AppDatabase.Schema.migrate(it, oldVersion, newVersion)
                    } catch (e: Exception) {
                        //logger.error(e)
                        throw e
                    }
                }
            },
            encryptionConfig = encryption
        )

        val driver = NativeSqliteDriver(config)
        return driver */
        return NativeSqliteDriver(AppDatabase.Schema, "HealthyHeart1.db")
    }
}