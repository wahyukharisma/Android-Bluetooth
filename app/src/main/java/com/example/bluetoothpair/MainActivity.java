package com.example.bluetoothpair;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.example.bluetoothpair.Bluetooth.DeviceListActivity;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements Runnable{
    protected static final String TAG = "TAG";
    private Button btn_search,btn_print,btn_disconnect;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mBluetoothSocket;
    private BluetoothDevice mBluetoothDevice;
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private ProgressDialog progressDialog;

    private UUID applicationUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //view ini
        btn_search     = findViewById(R.id.btn_search);
        btn_print      = findViewById(R.id.btn_print);
        btn_disconnect = findViewById(R.id.btn_disconnet);

        //listener
        btn_print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread t = new Thread(){
                  public void run(){
                      try{
                          OutputStream os = mBluetoothSocket
                                  .getOutputStream();
                          String BILL = "";

                          BILL = "                   XXXX MART    \n";
                          os.write(BILL.getBytes());
                      }catch (Exception e){
                          e.printStackTrace();
                      }
                  }
                };
                t.start();
            }
        });

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBluetoothAdapter == null){
                    Toast.makeText(MainActivity.this, "Device not supported bluetooth", Toast.LENGTH_SHORT).show();
                }else{
                    if(!mBluetoothAdapter.isEnabled()){
                        Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBluetooth,REQUEST_ENABLE_BT);
                    }else{
                        ListPairedDevice();
                        Intent connectIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                        startActivityForResult(connectIntent,REQUEST_CONNECT_DEVICE);
                    }
                }
            }
        });

        btn_disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBluetoothAdapter != null){
                    mBluetoothAdapter.disable();
                    Toast.makeText(MainActivity.this, "Bluetooth Disconnect", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
            if(mBluetoothSocket != null){
                mBluetoothSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        try{
            if(mBluetoothSocket != null)
                mBluetoothSocket.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case REQUEST_CONNECT_DEVICE:
                if(resultCode == Activity.RESULT_OK){
                    Bundle mExtra           = data.getExtras();
                    String mDeviceAddress   = mExtra.getString("DeviceAddress");
                    mBluetoothDevice        = mBluetoothAdapter.getRemoteDevice(mDeviceAddress);
                    progressDialog          = ProgressDialog.show(this,"Connecting. . .",mBluetoothDevice.getName() + "(" + mBluetoothDevice.getAddress() + ")",true,false);
                    Thread mBluetoothThread = new Thread(this);
                    mBluetoothThread.start();
                }
                break;

            case REQUEST_ENABLE_BT:
                if(resultCode == Activity.RESULT_OK){
                    ListPairedDevice();

                    Intent connectIntent = new Intent(MainActivity.this,DeviceListActivity.class);
                    startActivityForResult(connectIntent,REQUEST_CONNECT_DEVICE);
                }else{
                    Toast.makeText(this, "Failed to enable bluetooth", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void ListPairedDevice(){
        Set<BluetoothDevice> mPairedDevice = mBluetoothAdapter.getBondedDevices();
        if(mPairedDevice.size() > 0){
            //Print all print device
        }
    }

    private void closeSocket(BluetoothSocket nOpenSocket) {
        try {
            nOpenSocket.close();
            Log.d(TAG, "SocketClosed");
        } catch (IOException ex) {
            Log.d(TAG, "CouldNotCloseSocket");
        }
    }

    public void run() {
        try {
            mBluetoothSocket = mBluetoothDevice
                    .createRfcommSocketToServiceRecord(applicationUUID);
            mBluetoothAdapter.cancelDiscovery();
            mBluetoothSocket.connect();
            mHandler.sendEmptyMessage(0);
        } catch (IOException eConnectException) {
            Log.d(TAG, "CouldNotConnectToSocket", eConnectException);
            closeSocket(mBluetoothSocket);
            return;
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            progressDialog.dismiss();
            Toast.makeText(MainActivity.this, "DeviceConnected", Toast.LENGTH_SHORT).show();
        }
    };
}
