package uz.ilhomjon.arduinobluetoothcontroller.broadcast

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import uz.ilhomjon.arduinobluetoothcontroller.utils.MyData

class MyBluetoothBroadcast : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.getAction();
        val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
//        val device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

//        val dialog = AlertDialog.Builder(context)

        if (state == BluetoothAdapter.STATE_TURNING_ON){
//            dialog.setMessage("Bluetooth yoqildi")
            MyData.bluetoothOnOffLive.postValue(true)
        }else if (state == BluetoothAdapter.STATE_TURNING_OFF){
//            dialog.setMessage("Bluetooth o'chirildi")
            MyData.bluetoothOnOffLive.postValue(false)
        }

        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
             //Device found
//            dialog.setMessage("Device found - qurilma topildi")
        } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
             //Device is now connected
//            dialog.setMessage("Device is now connected - qurilma endi ulangan")
        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
             //Done searching
//            dialog.setMessage("Done searching - qidiruv amalga oshirildi")
        } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
             //Device is about to disconnect
//            dialog.setMessage("Device is about disconnected - Qurilma uzilib qolgan")
        } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
             //Device has disconnected
//            dialog.setMessage("Device has disconnected - qurilma uzildi")
        }

//        dialog.show()
    }
}
