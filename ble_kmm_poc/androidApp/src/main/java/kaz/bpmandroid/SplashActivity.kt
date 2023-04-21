package kaz.bpmandroid

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import kaz.bpmandroid.Repo.DatabaseHandler
import kaz.bpmandroid.Repo.UserRepo
import kaz.bpmandroid.db.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


        var lbExist: Boolean = doesDatabaseExist(this, "HealthyHeart.db")
        Log.e("DB_Exists", "" + lbExist)
        /*if (lbExist) {
            var loUserRepo: UserRepo = UserRepo(this)
            var loDBHelper: DatabaseHandler = DatabaseHandler(this)
            var loUsers: ArrayList<User> = ArrayList()
            CoroutineScope(Dispatchers.IO).launch {
                withContext(Dispatchers.Main) {
                    loUsers = loDBHelper.getUsers()
                }
            }.invokeOnCompletion {
                CoroutineScope(Dispatchers.IO).launch {
                    loUserRepo.insertUser(loUsers[0])
                }.invokeOnCompletion {
                    var loIntent: Intent = Intent(this, MainActivity::class.java)
                    startActivity(loIntent)
                }
            }
        } else {
            var loIntent: Intent = Intent(this, MainActivity::class.java)
            startActivity(loIntent)
        }*/

        var loIntent: Intent = Intent(this, MainActivity::class.java)
        startActivity(loIntent)


    }

    private fun doesDatabaseExist(context: Context, dbName: String): Boolean {
        val dbFile: File = context.getDatabasePath(dbName)
        return dbFile.exists()
    }

    private var requestBluetooth =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                //granted
            } else {
                //deny
            }
        }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d("test006", "${it.key} = ${it.value}")
            }
        }


}