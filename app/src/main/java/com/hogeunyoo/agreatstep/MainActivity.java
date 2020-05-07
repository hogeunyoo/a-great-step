package com.hogeunyoo.agreatstep;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;


public class MainActivity extends AppCompatActivity {
    private BluetoothSPP bt;

    private int steps = 0;
    private float velocity = 0.0F;
    private float distance = 0.00F;
    private float height = 0.0F;
    private float time = 0.0F;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bt = new BluetoothSPP(this); //Initializing

        if (!bt.isBluetoothAvailable()) { //블루투스 사용 불가
            Toast.makeText(getApplicationContext()
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }

        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() { //데이터 수신
            public void onDataReceived(byte[] data, String message) {
                EditText editText = (EditText) findViewById(R.id.editHeight);
                ImageView imageView = (ImageView) findViewById(R.id.imageView2);
                height = Float.parseFloat(editText.getText().toString());
                int rowData = Integer.parseInt(message);
                Log.i("bluetooth", Integer.toString(rowData));
                if (rowData % 10 != 0 ) {
                    time += 0.1;
                    if ((rowData - rowData/100) / 10 != 0) {
                        steps += 1;
                    }
                    if (rowData/100 == 1) {
                        imageView.setImageResource(R.drawable.footprint1);
                    } else if (rowData/100 == 2 ) {
                        imageView.setImageResource(R.drawable.footprint2);
                    }
                }
                distance = steps * (height-100) / 100;
                velocity = 3.6F * distance / time;

                Log.i("bluetooth", Integer.toString(rowData));
                TextView stepsValue = (TextView) findViewById(R.id.stepsValue) ;
                stepsValue.setText(String.valueOf(steps)) ;

                TextView velocityValue = (TextView) findViewById(R.id.velocityValue) ;
                velocityValue.setText(String.format("%.2f", velocity));

                TextView distanceValue = (TextView) findViewById(R.id.distanceValue) ;
                distanceValue.setText(String.valueOf(distance));

                TextView timeValue = (TextView) findViewById(R.id.timeValue) ;
                timeValue.setText(String.valueOf((int)time));
            }
        });

        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() { //연결됐을 때
            public void onDeviceConnected(String name, String address) {
                Toast.makeText(getApplicationContext()
                        , "Connected to " + name + "\n" + address
                        , Toast.LENGTH_SHORT).show();
            }

            public void onDeviceDisconnected() { //연결해제
                Toast.makeText(getApplicationContext()
                        , "Connection lost", Toast.LENGTH_SHORT).show();
            }

            public void onDeviceConnectionFailed() { //연결실패
                Toast.makeText(getApplicationContext()
                        , "Unable to connect", Toast.LENGTH_SHORT).show();
            }
        });

        Button btnConnect = findViewById(R.id.btnConnect); //연결시도
        btnConnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
                    bt.disconnect();
                } else {
                    Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                }
            }
        });
    }

    public void onDestroy() {
        super.onDestroy();
        bt.stopService(); //블루투스 중지
    }

    public void onStart() {
        super.onStart();
        if (!bt.isBluetoothEnabled()) { //
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if (!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER); //DEVICE_ANDROID는 안드로이드 기기 끼리
                setup();
            }
        }
    }

    public void setup() {
        Button btnSend = findViewById(R.id.btnSend); //데이터 전송
        btnSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                bt.send("1", false);
            }
        });

        Button pauseButton = findViewById(R.id.pauseButton); //데이터 전송
        pauseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                bt.send("2", false);
            }
        });

        Button stopButton = findViewById(R.id.stopButton); //데이터 전송
        stopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                bt.send("3", false);
                steps = 0;
                time = 0;
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK)
                bt.connect(data);
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
                setup();
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
