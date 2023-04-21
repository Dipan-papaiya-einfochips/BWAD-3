package kaz.bpmandroid.Repo

import android.content.Context
import kaz.bpmandroid.db.Database
import kaz.bpmandroid.db.DatabaseDriverFactory
import kaz.bpmandroid.db.User

class UserRepo(var foContext: Context) {
    var loDBFactory = DatabaseDriverFactory(foContext)
    var loDb = Database(loDBFactory)

    suspend fun insertUser(loUser:User) {
      /*  var loUser: User = User(
            "20",
            1,
            "Jason",
            0,
            0,
            "Male",
            0,
            0,
            0,
            "Wanga",
            null,
            0L,
            0L,
            1,
            null,
            0.0,
            "2019jonsnowgot@gmail.com",
            null
        )*/

        loDb.insertUsers(users = loUser)

    }

    suspend fun getAllUsers(): List<User> {
        return loDb.getAllUsers()
    }


}