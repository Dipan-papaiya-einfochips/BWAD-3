package kaz.bpmandroid.ble

const val UUID_BLOOD_PRESSURE_SERVICE = "56484AAE-A8EB-4A97-AC19-A8EA6373E05A"

const val BLUETOOTH_CONNECTED = "Scan completed. Connected to device."
const val BLUETOOTH_DISCONNECTED = "Disconnected from WISP base."
const val NO_DEVICE_FOUND = "Scan completed. No WISP base found."
const val SCANNING = "Scanning, Please wait..."
const val CONNECTING = "Connecting to WISP device..."
const val CONNECTION_TIMEOUT = "Failed to connect. Please try again later."
const val NO_SERVICE_FOUND = "No service found. please try again later."
const val BLE_GATT_ERROR = "Lost connection, Re-Connecting to WISP base."

enum class KAZDeviceStatus(private var statusText: String) {
    scanning(SCANNING),
    connecting(CONNECTING),
    connected(CONNECTING),
    disconnected(BLUETOOTH_DISCONNECTED),
    connectionTimeOut(CONNECTION_TIMEOUT),
    noServiceFound(NO_SERVICE_FOUND),
    noDeviceFound(NO_DEVICE_FOUND),
    bleGattError(BLE_GATT_ERROR),
    characteristicsDiscovered(BLUETOOTH_CONNECTED);

    fun getStatusText(): String = statusText
}