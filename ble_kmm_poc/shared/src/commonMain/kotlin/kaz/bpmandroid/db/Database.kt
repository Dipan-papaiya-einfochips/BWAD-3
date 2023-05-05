package kaz.bpmandroid.db

import kaz.bpmandroid.db.AppDatabase
import kaz.bpmandroid.db.DatabaseDriverFactory
import kaz.bpmandroid.db.User

class Database(databaseDriverFactory: DatabaseDriverFactory) {
    private val database = AppDatabase(databaseDriverFactory.createDriver())
    private val dbUserQueries = database.userQueries

    fun insertUsers(users: User) {
        dbUserQueries.transaction {
            dbUserQueries.insert(users)
        }
    }

    fun getAllUsers(): List<User> {
        return dbUserQueries.getAllUsers().executeAsList()
    }
}