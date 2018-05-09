package com.example.a510.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class BluetoothReceiver extends BroadcastReceiver {
    public String sName, sAddress;

    public BluetoothReceiver(String sName){
        this.sName = sName;
        this.sAddress = "";
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //throw new UnsupportedOperationException("Not yet implemented");
        if(intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
            BluetoothDevice device;
            device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            //Toast.makeText(context, sName + "is found: "+ sAddress, Toast.LENGTH_SHORT).show();
            if(device.getName().equalsIgnoreCase(sName)){
                sAddress = device.getAddress();
                Toast.makeText(context, sName + "is found: "+ sAddress, Toast.LENGTH_SHORT).show();
            }
        }else if(intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
            Toast.makeText(context, "Discovery is finished", Toast.LENGTH_SHORT).show();
        }
    }
}
