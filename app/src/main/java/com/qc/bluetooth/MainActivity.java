package com.qc.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_ACCESS_COARSE_LOCATION = 2;
    private BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mSocket;
    List<String> mDevices;
    ArrayAdapter<String> mAdapter;
    private Button mConnectBtn;
    /**打开红色Led按钮*/
    private Button mTurnOnRedBtn;
    /**关闭红色Led按钮*/
    private Button mTurnOffRedBtn;
    /**打开黄色Led按钮*/
    private Button mTurnOnYellowBtn;
    /**关闭黄色Led按钮*/
    private Button mTurnOffYellowBtn;
    /**打开绿色Led按钮*/
    private Button mTurnOnGreenBtn;
    /**关闭绿色Led按钮*/
    private Button mTurnOffGreenBtn;
    private ListView mDeviceListView;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("onreceive", "onReceive: ");
            String action = intent.getAction();
            if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){

            }
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String name = device.getName();
                String address = device.getAddress();
                mDevices.add(name+"   "+address);
                mAdapter.notifyDataSetChanged();
                Log.i("tag", "onReceive: name="+name);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化控件
        initViews();
        //初始化回调事件
        initEvents();
        //check Bluetooth support
        mBluetoothAdapter  = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter==null){
            Toast.makeText(MainActivity.this,"There is no Bluetooth device.",Toast.LENGTH_LONG).show();
            finish();
        }
        //check is Bluetooth enable
        if(!mBluetoothAdapter.isEnabled()){
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent,REQUEST_ENABLE_BT);
        } else {
            IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mReceiver,intentFilter);
        }
    }

    /**
     * 初始化控件
     */
    private void initViews(){
        mConnectBtn = findViewById(R.id.btn_connect);
        mTurnOnRedBtn = findViewById(R.id.btn_turn_on_red);
        mTurnOffRedBtn = findViewById(R.id.btn_turn_off_red);
        mTurnOnYellowBtn = findViewById(R.id.btn_turn_on_yellow);
        mTurnOffYellowBtn = findViewById(R.id.btn_turn_off_yellow);
        mTurnOnGreenBtn = findViewById(R.id.btn_turn_on_green);
        mTurnOffGreenBtn = findViewById(R.id.btn_turn_off_green);

        mDeviceListView = new ListView(MainActivity.this);
        TextView chooseDevice = new TextView(this);
        chooseDevice.setText("choose device");
        mDeviceListView.addHeaderView(chooseDevice);
        Button cancelScanBtn = new Button(this);
        cancelScanBtn.setText("cancel");
        mDeviceListView.addFooterView(cancelScanBtn);
        mDevices = new ArrayList<>();
        mAdapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_list_item_1,mDevices);
        mDeviceListView.setAdapter(mAdapter);
    }

    /**
     * 初始化事件
     */
    private void initEvents(){
        setBtnCallBack(mTurnOnRedBtn);
        setBtnCallBack(mTurnOffRedBtn);
        setBtnCallBack(mTurnOnYellowBtn);
        setBtnCallBack(mTurnOffYellowBtn);
        setBtnCallBack(mTurnOnGreenBtn);
        setBtnCallBack(mTurnOffGreenBtn);
        mConnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String macAddr = "20:15:05:25:02:43";
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(macAddr);
                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
                try {
                    mSocket = device.createRfcommSocketToServiceRecord(uuid);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                new Thread(){
                    @Override
                    public void run() {
                        mBluetoothAdapter.cancelDiscovery();
                        try {
                            mSocket.connect();
                            new ConnectedThread().start();
                        } catch (IOException e) {
                            try {
                                mSocket.close();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                            e.printStackTrace();
                        }
                        super.run();
                    }
                }.start();
            }
        });
    }

    /**
     * 设置按钮回调
     * @param btn 按钮
     */
    private void setBtnCallBack(final Button btn){
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String signal = null;
                switch (btn.getId()){
                    case R.id.btn_turn_on_red:
                        signal= "1";
                        break;
                    case R.id.btn_turn_off_red:
                        signal="2";
                        break;
                    case R.id.btn_turn_on_yellow:
                        signal= "3";
                        break;
                    case R.id.btn_turn_off_yellow:
                        signal="4";
                        break;
                    case R.id.btn_turn_on_green:
                        signal= "5";
                        break;
                    case R.id.btn_turn_off_green:
                        signal="6";
                        break;
                }
                try {
                    OutputStream os = mSocket.getOutputStream();
                    os.write(signal.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQUEST_ACCESS_COARSE_LOCATION){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){

                if(mBluetoothAdapter.isDiscovering()){
                    mBluetoothAdapter.cancelDiscovery();
                }
                mBluetoothAdapter.startDiscovery();
                AlertDialog scanDialog = new AlertDialog.Builder(MainActivity.this)
                        .setView(mDeviceListView)
                        .create();
                scanDialog.show();

            } else {
                Toast.makeText(MainActivity.this,"action found is not granted.",Toast.LENGTH_LONG).show();
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_ENABLE_BT){
            if(resultCode==RESULT_OK){
                Toast.makeText(MainActivity.this,"Bluetooth permission is enabled.",Toast.LENGTH_LONG).show();
                IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(mReceiver,intentFilter);

            } else {
                Toast.makeText(MainActivity.this,"Bluetooth permission is not enabled.",Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.scan_menu:
                //scan device
                //check permission
                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(MainActivity.this,"搜索回调权限已开启",Toast.LENGTH_SHORT).show();
                    if(mBluetoothAdapter.isDiscovering()){
                        mBluetoothAdapter.cancelDiscovery();
                    }
                    mBluetoothAdapter.startDiscovery();
                    AlertDialog scanDialog = new AlertDialog.Builder(MainActivity.this)
                            .setView(mDeviceListView)
                            .create();
                    scanDialog.show();
                }else{
                    Toast.makeText(MainActivity.this,"搜索回调权限未开启",Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},REQUEST_ACCESS_COARSE_LOCATION);
                }

                return true;
            case R.id.about_menu:
                //
                AlertDialog aboutDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("title")
                        .setMessage("about content")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).create();
                aboutDialog.show();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    private final static String TAG = "ConnectedThred";
    class ConnectedThread extends Thread{
        @Override
        public void run() {
            try {
                OutputStream os = mSocket.getOutputStream();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
}


