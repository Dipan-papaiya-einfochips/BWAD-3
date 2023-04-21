package kaz.bpmandroid

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.*
import kaz.bpmandroid.Repo.UserRepo
import kaz.bpmandroid.base.BluetoothManager
import kaz.bpmandroid.base.IBluetoothManager
import kaz.bpmandroid.ble.BluetoothDevice
import kaz.bpmandroid.ble.MainBluetoothAdapter
import kaz.bpmandroid.db.User
import kaz.bpmandroid.util.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), IBluetoothManager.BleConnectDisconectListener {

    private lateinit var mainBluetoothAdapter: MainBluetoothAdapter
    var loUsers: List<User> = ArrayList()
    lateinit var tv_name: TextView
    lateinit var moBleManager: BluetoothManager


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainBluetoothAdapter = MainBluetoothAdapter(this)
        moBleManager = BluetoothManager(mainBluetoothAdapter)


        checkBlePermission()

        var loBtnScan = findViewById<Button>(R.id.btn_scan)

        loBtnScan.setOnClickListener { moBleManager.scanAndConnect() }


        tv_name = findViewById<TextView>(R.id.txt_name)

        var loUserRepo: UserRepo = UserRepo(this)
        CoroutineScope(Dispatchers.IO).launch {
            //loUserRepo.insertUser()
        }


        /*CoroutineScope(Dispatchers.IO).launch {
            loUsers = loUserRepo.getAllUsers()
            if (loUsers.isNotEmpty()) {
                runOnUiThread {
                    tv_name.setText(loUsers[0].firstName + " " + loUsers[0].lastName)
                    Toast.makeText(this@MainActivity, "" + loUsers[0].firstName, Toast.LENGTH_LONG)
                        .show()
                    Log.e("Name", loUsers[0].firstName.toString())
                }
            } else {
                tv_name.setText("No User Available")
            }
        }*/

    }

    private fun checkBlePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(
                arrayOf(
                    android.Manifest.permission.BLUETOOTH_SCAN,
                    android.Manifest.permission.BLUETOOTH_CONNECT,
                    android.Manifest.permission.BLUETOOTH_ADMIN
                )
            )
        } else {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetooth.launch(enableBtIntent)
        }
    }

    private var requestBluetooth =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                //granted
                setBluetooth()
            } else {
                //deny
            }
        }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d("test006", "${it.key} = ${it.value}")
                setBluetooth()
            }
        }

    override fun onConnect(device: BluetoothDevice) {
        println("onConnect")
        tv_name.text = "Paired device ${device.name}"
        Toast.makeText(this, "Connected device ${device.name}", Toast.LENGTH_SHORT).show()

        displayUserNameOnDevice(device)
    }

    private fun displayUserNameOnDevice(device: BluetoothDevice) {
        println("displayUserNameOnDevice")
        var loNameByte: Any = Any()
        CoroutineScope(Dispatchers.IO).async {
            loNameByte = Utils.setUserName("Dipan")

            moBleManager.writeDataToDevice(
                device,
                Utils.UUID_KAZ_BPM_SERVICE,
                Utils.BPM_USER_NAME_CHAR,
                loNameByte as ByteArray
            )
        }

    }

    override fun onDisconnect() {
        tv_name.text = "Device disconnected"
    }

    override fun onResume() {
        super.onResume()
        moBleManager.registerListener(this)
    }

    override fun onPause() {
        super.onPause()
        moBleManager.unregisterListener(
            this
        )
    }

    @SuppressLint("ServiceCast")
    fun setBluetooth(): Boolean {
        val bluetoothManager: android.bluetooth.BluetoothManager =
            this.getSystemService(Context.BLUETOOTH_SERVICE) as android.bluetooth.BluetoothManager
        var bluetoothAdapter = bluetoothManager.adapter
        if (!bluetoothAdapter.isEnabled) {
            bluetoothAdapter.isEnabled
        }
        // No need to change bluetooth state
        return true
    }


}
