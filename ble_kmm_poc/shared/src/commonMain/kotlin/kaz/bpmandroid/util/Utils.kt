package kaz.bpmandroid.util

import kaz.bpmandroid.ble.BluetoothDevice
import kotlin.math.max

class Utils {

    companion object {
        val PRESSURE_MEASUREMENT_CHAR = "2DB34480-BCE5-4BB7-9F56-55BD202317C5"
        val PRESSURE_INTERMEDIATE_PRESSURE_CHAR = "77AB1C51-2F6B-4A7B-81AF-5E301984BF13"
        val PRESSURE_FEATURE_CHAR = "C3A2ED78-B3F1-4086-AB3B-BF3C5685A745"

        // Kaz BPM Service
        val BPM_SET_TIME_CHAR = "6114AC81-2A71-4ACC-ADA5-555B63E8C5E1"
        val BPM_MEASUREMENT_CONTROL_CHAR = "53EBC2CE-344C-4C5D-9D65-4740AA4660CD"
        val BPM_ERROR_CHAR = "BC305904-03CD-4858-8AE0-B184F2A52898"

        // Write the below char to get the readings
        val BPM_NUM_READINGS_CHAR = "25775D68-EB3F-4EE5-961A-5312516CB0CA"

        //the below char will be notified once the BPM_NUM_READINGS_CHAR gets written
        val BPM_READING_REQUEST_CHAR = "D4A0C8C3-6387-4ACA-BFAB-21AC412E5BE8"
        val BPM_REQUESTED_READING_CHAR = "991DEE0F-75C1-4C7C-93ED-78FEF7F8E6DB"
        val BPM_USER_NAME_CHAR = "FCE1BE76-A487-4331-96B0-53C8097E306C"
        val BPM_DELETE_READING_CHAR = "2DF251D7-B858-450C-9779-32745E5E4727"
        val BPM_FACTORY_RESET_CHAR = "598040AF-4DE1-45F2-853F-9ABA929FCFD2"
        val BPM_PAIRING_CHAR = "DEFFE5DE-90B2-4D5C-9888-76BDAA950C78"

        val CCC_DESCRIPTOR_UUID = "56484AAE-A8EB-4A97-AC19-A8EA6373E05A"

        val UUID_KAZ_BPM_SERVICE = "BB647F01-D352-48DE-9015-D055B1355D7B"
        val UUID_UPDATE_SERVICE = "F000FFC0-0451-4000-B000-000000000000"
        val UUID_DEVICE_INFO_SERVICE = "0000180A-0000-1000-8000-00805F9B34FB"
        val UUID_BLOOD_PRESSURE_SERVICE = "56484AAE-A8EB-4A97-AC19-A8EA6373E05A"
        val UUID_BATTERY_SERVICE = "0000180F-0000-1000-8000-00805F9B34FB"


        fun getParingHash(
            userNumber: Int, appHash: Long
        ): ByteArray {
            val packet = ByteArray(5)
            packet[0] = (userNumber and 0xFF).toByte()
            packet[1] = (appHash and 0xFF000000L shr 24 and 0xFFL).toByte()
            packet[2] = (appHash and 0x00FF0000L shr 16 and 0xFFL).toByte()
            packet[3] = (appHash and 0x0000FF00L shr 8 and 0xFFL).toByte()
            packet[4] = (appHash and 0x000000FFL and 0xFFL).toByte()


            println("Device pairing hash set to ++ $appHash")


            return packet
        }

        fun setUserName(fsName: String): ByteArray {
            var name = fsName

            var userID = 0
            var write = true
            // max len is 17 because of the 2 bytes for index, write flag, and the null character
            val nameLen = max(name.length, 17)
            val nameData = ByteArray(nameLen + 3)
            var index = 0
            nameData[index++] = (userID.toByte().toInt() and 0xFF).toByte()
            nameData[index++] = 1.toByte()
            if (name.isNotBlank() && name.isNotEmpty()) {
                // copy the name into the packet
                var i = 0
                while (i < nameLen && i < name.length) {
                    nameData[index++] = name[i].toByte()
                    i++
                }
            }
            nameData[index] = 0
            println("Name : $nameData")
            return nameData
        }


    }

}