package com.example.rados.lf_sterowanie;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by rados on 19.12.2016.
 */

    //TODO: sprawdzanie czy bluetooth jest wciąż dostępny
    //TODO: odbieranie danych
    //TODO: opcja włączania bluetootha gdy jest wyłączony
public class MyBluetooth {
    private BluetoothAdapter myBluetooth = null;
    private BluetoothSocket btSocket = null;
    public boolean isBtConnected = false;
    private static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private String address = null;
    private ProgressDialog progress;
    Activity activity=null;
    Context context=null;

    public MyBluetooth(Activity act, Context con, String add)
    {
        SetActivityAnDContext(act,con);
        SetAddress(add);
    }

    public void SetActivityAnDContext(Activity act, Context con){
        activity=act;
        context=con;
    }

    public void SetAddress(String add){
        address=add;
    }

    public void write(String msg)
    {
        try
        {
            btSocket.getOutputStream().write(msg.getBytes());
        }
        catch (IOException e)
        {
            msg("Error");
        }
    }

    public void Disconnect()
    {
        if (btSocket!=null) //If the btSocket is busy
        {
            try
            {
                btSocket.close(); //close connection
            }
            catch (IOException e)
            { msg("Error");}
        }
    }

    public void Connect(){
        new ConnectBT().execute();
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
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
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