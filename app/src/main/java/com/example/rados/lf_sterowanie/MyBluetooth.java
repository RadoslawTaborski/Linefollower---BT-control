package com.example.rados.lf_sterowanie;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Created by rados on 19.12.2016.
 */

//TODO: sprawdzanie czy bluetooth jest sparowany
//TODO: odbieranie danych
public class MyBluetooth {
    public BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothSocket btSocket = null;
    public boolean isBtConnected = false;
    public boolean stopWorker = false;
    OutputStream btOutputStream;
    InputStream btInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    private static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private String address = null;

    private ProgressDialog progress;
    Activity activity=null;
    Context context=null;

    public MyBluetooth(Activity act, Context con, String add)
    {
        SetActivityAnDContext(act,con);
        SetAddress(add);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void SetActivityAnDContext(Activity act, Context con){
        activity=act;
        context=con;
    }

    public void SetAddress(String add){
        address=add;
    }

    public void turnOnBT() throws Exception
    {
        if(mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(enableBluetooth,0);
            }
        }else{
            throw new Exception("No bluetooth adapter available");
        }
    }

    private void beginListenForData()
    {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        int bytesAvailable = btInputStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            btInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            //myLabel.setText(data);
                                            //lblReceived.setText(data);
                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }

    void sendData(String msg) throws IOException
    {
        btOutputStream.write(msg.getBytes());
    }

    void Disconnect() throws IOException
    {
        isBtConnected=false;
        stopWorker = true;
        btOutputStream.close();
        btInputStream.close();
        btSocket.close();
    }


    public void Connect(boolean receive) throws IOException{
        new ConnectBT().execute();
       /* if(receive){
            beginListenForData();
        }*/
    }

    private void msg(String s)
    {
        Toast.makeText(context,s,Toast.LENGTH_LONG).show();
    }


    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(activity, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    BluetoothDevice dispositivo = mBluetoothAdapter.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                    btOutputStream = btSocket.getOutputStream();
                    btInputStream = btSocket.getInputStream();
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                activity.finish();
            }
            else
            {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }
}