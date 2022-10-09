package uz.ilhomjon.arduinobluetoothcontroller

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import uz.ilhomjon.arduinobluetoothcontroller.adapters.QurilmaAdapter
import uz.ilhomjon.arduinobluetoothcontroller.adapters.RvClick
import uz.ilhomjon.arduinobluetoothcontroller.broadcast.MyBluetoothBroadcast
import uz.ilhomjon.arduinobluetoothcontroller.databinding.ActivityMainBinding
import uz.ilhomjon.arduinobluetoothcontroller.databinding.ItemDialogBinding
import uz.ilhomjon.arduinobluetoothcontroller.models.Qurilma
import uz.ilhomjon.arduinobluetoothcontroller.utils.MyData
import uz.ilhomjon.arduinobluetoothcontroller.utils.MyData.btSocket
import uz.ilhomjon.arduinobluetoothcontroller.utils.MyData.myBluetooth
import uz.ilhomjon.arduinobluetoothcontroller.utils.MyData.myUUID
import java.lang.Exception
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var myBluetoothBroadcast: MyBluetoothBroadcast

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //qurilmada bluetooth sozmi ishlaydimi shuni tekshirish
        binding.btnTest.setOnClickListener {
            qurilmaBleutoothniIshlataOladimi()
        }
        binding.btnTest.setOnLongClickListener {
            AlertDialog.Builder(this)
                .setMessage("Bu tugma ushbu qurilmada bluetooth ishlaydimi yo'qmi shuni tekshiradi")
                .show()
            true
        }


        //bluettothni yoqish va o'chirish
        binding.switchBluetooth.setClickable(false);
        binding.tvBluetoothOnOf.setOnClickListener {
            bluetoothniYoqish()
        }

        //bluetoothga ulangan qurilmalarni ko'rsatish va tanlanganiga ulanish
        binding.btnConnectDevice.setOnClickListener {
            barchaQurilmalarVaTanlanganigaUlanish()
        }
        binding.btnConnectDevice.setOnLongClickListener {
            AlertDialog.Builder(this)
                .setMessage("Bu tugma bluetooth orqali ulangan qurilmalarni ko'rsatadi va birortasini tanlasangiz shunga ulanadi")
                .show()
            true
        }

        //bluetoothni yoqilganini yoki o'chirilganini eshituvchi broadcast
        useBroadcast()

    }


    lateinit var handler: Handler
    var delay = 1000
    private fun readAndWriteSocket() {
        val runnable = object : Runnable {
            override fun run() {
                val r = readInfoSocket()
                if (r == "") {
                    binding.tvMessage.text = "No message"
                } else {
                    binding.tvMessage.text = r
                }
                handler.postDelayed(this, delay.toLong())
            }
        }

        handler = Handler(Looper.getMainLooper())
        handler.postDelayed(runnable, delay.toLong())

        //necha sekundda ma'lumot kelishini o'zgartirish
        binding.btnSave.setOnClickListener {
            try {
                delay = binding.edtCount.text.toString().toInt()
            }catch (e: Exception){
                Toast.makeText(this, "Raqam not'g'ri kiritnldi", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSend.setOnClickListener {
            sendSocket(this, binding.edtSend.text.toString())
            Toast.makeText(this, "Send ${binding.edtSend.text.toString()}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun useBroadcast() {
        myBluetoothBroadcast = MyBluetoothBroadcast()
        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(myBluetoothBroadcast, filter);

        MyData.bluetoothOnOffLive.observe(this) {
            binding.switchBluetooth.isChecked = it
        }
    }

    fun qurilmaBleutoothniIshlataOladimi() {
        val bluetoothManager: BluetoothManager =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getSystemService(BluetoothManager::class.java)
            } else {
                TODO("VERSION.SDK_INT < M")
            }
        myBluetooth = bluetoothManager.getAdapter()
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Bluetooth haqida ma'lumot")
        if (myBluetooth == null) {
            dialog.setMessage("Bluetoothni qurilmangiz ishlata olmaydi")
        } else {
            dialog.setMessage("Bluetoothni qurilmangiz ishlata oladi")
        }
        dialog.show()
    }


    @SuppressLint("MissingPermission")
    fun bluetoothniYoqish() {
        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        myBluetooth = bluetoothManager.getAdapter()
        if (myBluetooth?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 1)
        } else {
            myBluetooth?.disable()
        }
    }


    @SuppressLint("MissingPermission")
    fun barchaQurilmalarVaTanlanganigaUlanish() {
        myBluetooth = BluetoothAdapter.getDefaultAdapter()
//        var address = ""
        val list = ArrayList<Qurilma>()
        try {
//            address = myBluetooth?.getAddress()!!
            val pairedDevices = myBluetooth?.getBondedDevices()
            if (pairedDevices?.size!! > 0) {
                for (bt in pairedDevices!!) {
                    list.add(Qurilma(
                        bt.name.toString(),
                        bt.address.toString()
                    ))
                }
                val dialog = AlertDialog.Builder(this).create()
                val itemDialogBinding = ItemDialogBinding.inflate(layoutInflater)
                dialog.setView(itemDialogBinding.root)
                itemDialogBinding.rvDialog.adapter = QurilmaAdapter(list, object : RvClick {
                    override fun onClick(qurilma: Qurilma) {
                        dialog.cancel()
                        try {
                            val dispositivo =
                                myBluetooth!!.getRemoteDevice(qurilma.address) //connects to the device's address and checks if it's available
                             btSocket =
                                dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID) //create a RFCOMM (SPP) connection
                            btSocket!!.connect()
                            Toast.makeText(this@MainActivity,
                                "Connect ${qurilma.name}",
                                Toast.LENGTH_SHORT).show()
//            return "BT Name: $name\nBT Address: $address"

                            //ma'lumot almashinish socket orqali
                            readAndWriteSocket()

                        } catch (e: java.lang.Exception) {
                            Toast.makeText(this@MainActivity,
                                "Error \n ${e.message}",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                })
                dialog.show()
            }
        } catch (we: java.lang.Exception) {

        }
    }

        //ma'lumot almashinish
        fun sendSocket(context: Context, message:String){
            try {
                btSocket?.outputStream?.write(message.toByteArray())
            } catch (e: java.lang.Exception) {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
        }

        fun readInfoSocket():String{
            val inputStream = btSocket?.inputStream
            var byteCount = inputStream?.available()!!
            if (byteCount>0){
                val rawBytes = ByteArray(byteCount)
                inputStream.read(rawBytes)
                val string = String(rawBytes, Charset.forName("UTF-8"))
                Log.d("Aurdino", "readInfo: $string")
                return string
            }else{
                return ""
            }
        }
}