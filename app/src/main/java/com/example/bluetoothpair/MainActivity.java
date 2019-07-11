package com.example.bluetoothpair;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.example.bluetoothpair.Printer.Command;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
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

    private static OutputStream outputStream;

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
                OutputStream opstream = null;
                try {
                    opstream = mBluetoothSocket.getOutputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                outputStream = opstream;

                //print command
                try {
                    try {
                        Thread.sleep(6000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    outputStream = mBluetoothSocket.getOutputStream();

                    //print title
                    printCustom("Wahyu Kharisma POS",3,1);
                    printNewLine();
                    printCustom(leftRightAlign("Testing","Succcess"),0,1);
                    printNewLine();
                    printCustom("Thank You",1,1);
                    printText("--------------------------------"); // total 32 char in a single line
                    //resetPrint(); //reset printer
                    printNewLine();
                    printNewLine();

                    outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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


    // Printing Rule
    //print custom
    private void printCustom(String msg, int size, int align) {
        //Print config "mode"
        byte[] cc = new byte[]{0x1B,0x21,0x03};  // 0- normal size text
        //byte[] cc1 = new byte[]{0x1B,0x21,0x00};  // 0- normal size text
        byte[] bb = new byte[]{0x1B,0x21,0x08};  // 1- only bold text
        byte[] bb2 = new byte[]{0x1B,0x21,0x20}; // 2- bold with medium text
        byte[] bb3 = new byte[]{0x1B,0x21,0x10}; // 3- bold with large text
        try {
            switch (size){
                case 0:
                    outputStream.write(cc);
                    break;
                case 1:
                    outputStream.write(bb);
                    break;
                case 2:
                    outputStream.write(bb2);
                    break;
                case 3:
                    outputStream.write(bb3);
                    break;
            }

            switch (align){
                case 0:
                    //left align
                    outputStream.write(Command.ESC_ALIGN_LEFT);
                    break;
                case 1:
                    //center align
                    outputStream.write(Command.ESC_ALIGN_CENTER);
                    break;
                case 2:
                    //right align
                    outputStream.write(Command.ESC_ALIGN_RIGHT);
                    break;
            }
            outputStream.write(msg.getBytes());
            outputStream.write(Command.LF);
            //outputStream.write(cc);
            //printNewLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    //print new line
    private void printNewLine() {
        try {
            outputStream.write(Command.FEED_LINE);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void resetPrint() {
        try{
            outputStream.write(Command.ESC_FONT_COLOR_DEFAULT);
            outputStream.write(Command.FS_FONT_ALIGN);
            outputStream.write(Command.ESC_ALIGN_LEFT);
            outputStream.write(Command.ESC_CANCEL_BOLD);
            outputStream.write(Command.LF);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //print text
    private void printText(String msg) {
        try {
            // Print normal text
            outputStream.write(msg.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //print byte[]
    private void printText(byte[] msg) {
        try {
            // Print normal text
            outputStream.write(msg);
            printNewLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private String leftRightAlign(String str1, String str2) {
        String ans = str1+str2;
        if(ans.length() <31){
            int n = (31 - ans.length());
            ans = str1 + new String(new char[n]).replace("\0", " ") + str2;
        }
        return ans;
    }
}
