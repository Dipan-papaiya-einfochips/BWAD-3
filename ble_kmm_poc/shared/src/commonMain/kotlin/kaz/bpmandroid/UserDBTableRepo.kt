package kaz.bpmandroid.db


class UserDBTableRepo(databaseDriverFactory: DatabaseDriverFactory) {
    private val database = Database(databaseDriverFactory)

    suspend fun insertUser() {
        var loUser: User = User(
            "01/08/96",
            1,
            "Mohini",
            0,
            1,
            "",
            0,
            0,
            1,
            "Bhavsar",
            null,
            0L,
            0,
            null,
            0.0,
            1.0,
            "2019jonsnowgot@gmail.com",
            "",
        )

        database.insertUsers(users = loUser)

    }

    suspend fun getAllUsers(): List<User> {
        return database.getAllUsers()
    }
}