package com.example.vinaysingh.bluetoothprintercode;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {


    TextView printerName, adtprnt;


    EditText edittextPtr;
    int readBufferPosition;
    byte[] readbuffer;
    volatile boolean stopeWarker;
    BluetoothSocket bluetoothSocket;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private InputStream inputStream;
    private OutputStream outputStream;
    private Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        printerName = findViewById(R.id.printerName);
        adtprnt = findViewById(R.id.adtprnt);
        edittextPtr = findViewById(R.id.edittextPtr);

        findViewById(R.id.connectPtr).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                findBluetoothDevice();
               openBluetoothPrinter();

            }
        });


        findViewById(R.id.disconnectPtr).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDisconnnectBt();

            }
        });


        findViewById(R.id.printPtr).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                printdata();
            }
        });

    }

    private void findBluetoothDevice() {

        try {

            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {

                edittextPtr.setText("No bluetooth device found..");
            }


            if (bluetoothAdapter.isEnabled()) {

                Intent intentbt = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

                startActivityForResult(intentbt, 0);

            }

            Set<BluetoothDevice> bluetoothDevices = bluetoothAdapter.getBondedDevices();


            if (bluetoothDevices != null && bluetoothDevices.size() > 0) {


                for (BluetoothDevice p : bluetoothDevices) {

                    if (p.getName().equals("BPTFO9LM")) {

                        bluetoothDevice = p;

                        edittextPtr.setText("Printer Name: " + p.getName());

                    }

                }

                adtprnt.setText("Printer  attached");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getCoonectPrinter() {


    }

    void openBluetoothPrinter() {


        UUID uuid = UUID.fromString("");

        try {
            bluetoothSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);


            bluetoothSocket.connect();
            outputStream = bluetoothSocket.getOutputStream();
            inputStream = bluetoothSocket.getInputStream();


            begainLestinData();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void begainLestinData() {


        try {
            final Handler handler = new Handler();

            final byte delimit = 10;
            stopeWarker = false;
            readBufferPosition = 0;
            readbuffer = new byte[1024];
            thread = new Thread(new Runnable() {
                @Override
                public void run() {

                    while (!Thread.currentThread().isInterrupted() && !stopeWarker) {
                        try {

                            int byteAvailable = inputStream.available();
                            if (byteAvailable > 0) {
                                byte[] packatByts = new byte[byteAvailable];
                                inputStream.read(packatByts);

                                for (int i = 0; i < byteAvailable; i++) {

                                    byte b = packatByts[i];


                                    if (b == delimit) {

                                        byte[] encodedByte = new byte[readBufferPosition];

                                        System.arraycopy(readbuffer, 0, encodedByte, 0, encodedByte.length);

                                        final String data = new String(encodedByte, "US-ASCII");

                                        readBufferPosition = 0;
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {

                                                printerName.setText(data);
                                            }
                                        });


                                    } else {
                                        readbuffer[readBufferPosition++] = b;

                                    }

                                }


                            }


                        } catch (Exception e) {
                            stopeWarker = true;

                            e.printStackTrace();
                        }
                    }

                }
            });

            thread.start();


        } catch (Exception e) {

            e.printStackTrace();
        }

    }


    public void printdata() {

        try{

            String  data =  edittextPtr.getText().toString();

            data+="\n";
            outputStream.write(data.getBytes());
            edittextPtr.setText("printing text...");

        }catch (Exception e){
            e.printStackTrace();
        }

    }


    public   void getDisconnnectBt(){

       try {

           stopeWarker = true;
           outputStream.close();
           inputStream.close();
           bluetoothSocket.close();
           edittextPtr.setText("printer disconnected");


       }catch (Exception e){
           e.printStackTrace();
       }
    }
}
