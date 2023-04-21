package kaz.bpmandroid.Repo

import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kaz.bpmandroid.db.User
import kotlin.collections.ArrayList

class DatabaseHandler(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private val DATABASE_NAME = "HealthyHeart.db"
        private val DATABASE_VERSION = 12
    }

    override fun onCreate(db: SQLiteDatabase?) {
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

    @SuppressLint("Range")
    suspend fun getUsers(): ArrayList<User> {
        var lstUsers: ArrayList<User> = ArrayList()

        val db = readableDatabase

        val result = db.rawQuery(
            "SELECT * FROM User",
            null
        )
        result.moveToFirst()

        // Loop through all records and retrieve user information

        // Loop through all records and retrieve user information
        while (!result.isAfterLast) {
            val user: User = User(
                result.getString(result.getColumnIndex("birthday")),
                result.getLong(result.getColumnIndex("id")),
                result.getString(result.getColumnIndex("firstName")),
                result.getLong(result.getColumnIndex("frequencyPerDay")),
                result.getLong(result.getColumnIndex("frequencyPerWeek")),
                result.getString(result.getColumnIndex("gender")),
                result.getLong(result.getColumnIndex("highDiastolicThreshold")),
                result.getLong(result.getColumnIndex("highPulseThreshold")),
                result.getLong(result.getColumnIndex("highSystolicThreshold")),
                result.getString(result.getColumnIndex("lastName")),
                result.getBlob(result.getColumnIndex("profilePic")),
                result.getLong(result.getColumnIndex("targetDia")),
                result.getLong(result.getColumnIndex("targetSys")),
                1,
                null,
                result.getDouble(result.getColumnIndex("weight")),
                result.getString(result.getColumnIndex("email")),
                null
            )

            /*   user.setID(result.getInt(result.getColumnIndex(utils.DataAccessLayer.TableAdapter.UserTable.COLUMN_ID)))

               // Get Date from user input
               var birthday: Date? = null
               try {
                   birthday = SimpleDateFormat(
                       utils.DataAccessLayer.DATE_FORMAT,
                       Locale.ENGLISH
                   ).parse(result.getString(result.getColumnIndex(utils.DataAccessLayer.TableAdapter.UserTable.COLUMN_BIRTHDAY)))
               } catch (e: ParseException) {
                   // No date was entered.
               }
               if (birthday != null) user.setBirthday(birthday)
               user.setFirstName(result.getString(result.getColumnIndex(utils.DataAccessLayer.TableAdapter.UserTable.COLUMN_FIRST_NAME)))
               user.setFrequencyPerDay(result.getInt(result.getColumnIndex(utils.DataAccessLayer.TableAdapter.UserTable.COLUMN_FREQ_DAY)))
               user.setFrequencyPerWeek(result.getInt(result.getColumnIndex(utils.DataAccessLayer.TableAdapter.UserTable.COLUMN_FREQ_WEEK)))
               user.setGender(result.getString(result.getColumnIndex(utils.DataAccessLayer.TableAdapter.UserTable.COLUMN_GENDER)))
               user.setHighDiastolicThreshold(result.getInt(result.getColumnIndex(utils.DataAccessLayer.TableAdapter.UserTable.COLUMN_HIGH_DIASTOLIC)))
               user.setHighPulseThreshold(result.getInt(result.getColumnIndex(utils.DataAccessLayer.TableAdapter.UserTable.COLUMN_HIGH_PULSE)))
               user.setHighSystolicThreshold(result.getInt(result.getColumnIndex(utils.DataAccessLayer.TableAdapter.UserTable.COLUMN_HIGH_SYSTOLIC)))
               user.setLastName(result.getString(result.getColumnIndex(utils.DataAccessLayer.TableAdapter.UserTable.COLUMN_LAST_NAME)))
               user.setProfilePic(result.getBlob(result.getColumnIndex(utils.DataAccessLayer.TableAdapter.UserTable.COLUMN_PROFILE_PIC)))
               user.setTargetDiastolic(result.getInt(result.getColumnIndex(utils.DataAccessLayer.TableAdapter.UserTable.COLUMN_TARGET_DIASTOLIC)))
               user.setTargetSystolic(result.getInt(result.getColumnIndex(utils.DataAccessLayer.TableAdapter.UserTable.COLUMN_TARGET_SYSTOLIC)))
               user.setTargetWeight(result.getFloat(result.getColumnIndex(utils.DataAccessLayer.TableAdapter.UserTable.COLUMN_TARGET_WEIGHT)))
               user.setWeight(result.getFloat(result.getColumnIndex(utils.DataAccessLayer.TableAdapter.UserTable.COLUMN_WEIGHT)))
               if (result.getInt(result.getColumnIndex(utils.DataAccessLayer.TableAdapter.UserTable.COLUMN_EMAIL_MARKETING)) == 1) {
                   user.setEmailMarketingEnabled(true)
               } else {
                   user.setEmailMarketingEnabled(false)
               }*/


            // Add populated user record to collection
            lstUsers.add(user)
            result.moveToNext()
        }

        return lstUsers
    }


}