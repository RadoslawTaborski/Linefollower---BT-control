package com.example.rados.lf_sterowanie;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.ParcelUuid;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Created by rados on 19.12.2016.
 */

public class MyBluetooth {
    public BluetoothAdapter myBluetooth = null;
    public BluetoothSocket btSocket = null;
    public boolean isBtConnected = false;
    public static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public String address = "00:12:6F:6B:C0:A2";

    public void write(String msg) throws IOException
    {
        if (btSocket!=null)
        {
                btSocket.getOutputStream().write(msg.getBytes());
        }
    }

    public void Disconnect() throws IOException
    {
        if (btSocket!=null)
        {
            btSocket.close();
        }
    }


}