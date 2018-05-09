package com.example.a510.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.renderscript.ScriptGroup;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    protected  static  final int BTH_ENABLE = 1010;
    protected String sBthName = "MINJI2";
    protected BluetoothAdapter bthAdapter;
    protected BluetoothDevice bthDevice;
    protected BluetoothManager bthManager;
    protected BluetoothReceiver bthReciver;
    protected AceBluetoothSerialService bthService;
    protected Button btfind,btconnect,btread,btwrite,btvss0;
    protected EditText etWrite;
    protected TextView tvread;
    protected StringTok stSensorInput = new StringTok("");
    protected ArrayList<Double> arSensor0,arSensor1,arSensor2;

    protected  void showMsg(String str){
        Toast.makeText(this,str,Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == BTH_ENABLE){
            if(resultCode == RESULT_OK)
                showMsg("Bluetooth is enalbled by a user");
            else showMsg("Bluetooth is disabled by a user");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bthManager = (BluetoothManager)getSystemService(BLUETOOTH_SERVICE);
        if(bthManager == null)return;
        showMsg("BluetoothManager is found.");
        bthAdapter = bthManager.getAdapter();
        if (bthAdapter == null) return;
        showMsg("BluetoothAdapter is found.");
        if (bthAdapter.isEnabled()){
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent,BTH_ENABLE);
            showMsg("Bluetooth is not enabled.");
        } else showMsg("Bluetooth is enabled.");

        btfind =(Button)findViewById(R.id.btfind);
        btconnect = (Button)findViewById(R.id.btconnect);
        btread=(Button)findViewById(R.id.btread);
        btwrite=(Button)findViewById(R.id.btwrite);
        etWrite=(EditText)findViewById(R.id.etWrite);
        tvread=(TextView)findViewById(R.id.tvread);
        btvss0=(Button)findViewById(R.id.btvss0);


        btfind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bthAdapter.isDiscovering()) bthAdapter.cancelDiscovery();
                bthAdapter.startDiscovery();
                showMsg("Discovering...");
            }
        });

        btconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bthReciver.sAddress.isEmpty()) {
                    showMsg("MAC address is empty.");
                }else{
                    bthDevice = bthAdapter.getRemoteDevice(bthReciver.sAddress);
                    bthService.connect(bthDevice);
                    showMsg(sBthName+"is connected.");
                }
            }
        });
        btwrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = etWrite.getText().toString();
                bthService.println(str);
            }
        });
        btread.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = bthService.getSerialInput();
                stSensorInput.appendString(str);
                tvread.setText(stSensorInput.toString()); //받은 결과가 잘 저장되어 있는가
            }
        });
        btvss0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(arSensor0.size()>0){
                    Double[] arDouble = new Double[arSensor0.size()];
                    arDouble = arSensor0.toArray(arDouble);
                    String str = "";
                    for(Double x : arDouble){
                        str += String.format("%g ",x);
                    }
                    showMsg(str);
                } else showMsg("arSensor0 is empty.");
            }
        });


        bthReciver = new BluetoothReceiver(sBthName);
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bthReciver, intentFilter);
        intentFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(bthReciver, intentFilter);

        /*Set<BluetoothDevice> setDevice = bthAdapter.getBondedDevices();
        if(setDevice != null){
            for(BluetoothDevice device : setDevice){
                if(device.getName().equalsIgnoreCase(sBthName)){
                    bthReciver.sAddress = device.getAddress();
                    showMsg("MAC address of" + sBthName + "is set.");
                }

            }
        }*/

        bthService = new AceBluetoothSerialService(this, bthAdapter);

        arSensor0 = new ArrayList<Double>();
        arSensor1 = new ArrayList<Double>();
        arSensor2 = new ArrayList<Double>();
    }
    private void parseSensor(StringTok stSensorInput){
        while(stSensorInput.hasLine()){
            String sLine = stSensorInput.cutLine();
            parseSensorLine(sLine);
        }
    }
    private void parseSensorLine(String sLine){
        StringTok stInput = new StringTok(sLine);
        StringTok stToken = stInput.getToken();
        if(stToken.toString().equals("getset")){
            stToken = stInput.getToken(); //Sensor#
            long nSensor = stInput.toLong();
            stToken = stInput.getToken(); //Sensor value
            double sensorVal = stToken.toDouble();
            showMsg(String.format("%d:%g",nSensor, sensorVal));
        }
    }
    private  void saveSensorVal(long nSensor, double sensorVal){
        if(nSensor==0) arSensor0.add(sensorVal);
            else if(nSensor ==1) arSensor1.add(sensorVal);
            else if(nSensor ==2) arSensor2.add(sensorVal);
    }

    @Override
    protected void onDestroy() {
        if(bthAdapter.isDiscovering())bthAdapter.cancelDiscovery();
        unregisterReceiver(bthReciver);
        super.onDestroy();
    }
}
