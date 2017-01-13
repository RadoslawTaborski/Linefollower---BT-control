package com.example.rados.lf_sterowanie;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MyBluetooth {
    private static boolean btConnected = false;
    private static boolean btTurnedOn =false;
    private static boolean stopWorker = false;
    private static BluetoothAdapter btAdapter=null;
    private static BluetoothSocket btSocket;
    private static OutputStream btOutputStream;
    private static InputStream btInputStream;
    private final UUID DEVICE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String address;

    private Thread workerThread;
    private Activity activity;
    private Context context;
    private byte[] readBuffer;
    private int readBufferPosition;
    private ProgressDialog progress;
    private IUpdateUiAfterReceivingData iUpdateReceiveUI;
    private IUpdateAfterChangingBluetoothStatus iUpdateAfterChangingStatus;

    public MyBluetooth(Activity act, Context con, String add, IUpdateUiAfterReceivingData update) {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        setActivityAndContext(act,con,update);
        setAddress(add);
    }

    public MyBluetooth(Activity act, Context con, String add) {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        setActivityAndContext(act,con,null);
        setAddress(add);
    }

    public void setActivityAndContext(Activity act, Context con, IUpdateUiAfterReceivingData update){
        activity=act;
        context=con;
        setMethodToUpdateUiAfterReceivingData(update);
        if(isBtConnected()){
            beginListenForData();
        }
    }

    public void setActivityAndContext(Activity act, Context con){
        activity=act;
        context=con;
        setMethodToUpdateUiAfterReceivingData(null);
    }

    public boolean isBtConnected(){
        return btConnected;
    }

    public boolean isBtTurnedOn(){
        return btTurnedOn;
    }

    public static boolean isEmpty(){
        if(btAdapter==null) {
            return true;
        } else{
            return false;
        }
    }

    private void setMethodToUpdateUiAfterReceivingData(IUpdateUiAfterReceivingData update){
        if(update==null){
            iUpdateReceiveUI=new IUpdateUiAfterReceivingData() {
                @Override
                public void updateUI(String data) {
                }
            };
        }else{
            iUpdateReceiveUI=update;
        }
    }

    public void updateUiAfterChangingBluetoothStatus(IUpdateAfterChangingBluetoothStatus update)
    {
        Runnable runner = new BluetoothStatusUpdater(update);
        Thread thread=new Thread(runner);
        thread.start();
    }

    private void setAddress(String add){
        address=add;
    }

    public void turnOnBT() throws Exception
    {
        if(btAdapter != null) {
            if (!btAdapter.isEnabled()) {
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(enableBluetooth,0);
            }
        }else{
            throw new Exception("No bluetooth adapter available");
        }
    }

    private void beginListenForData()
    {
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

                                    Thread uiThread=new Thread(new Runnable() {
                                        public void run() {
                                            activity.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    iUpdateReceiveUI.updateUI(data);
                                                }
                                            });
                                        }
                                    });

                                    uiThread.start();
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
                        ex.printStackTrace();
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

    void disconnect() throws IOException
    {
        btConnected =false;
        workerThread.interrupt();
        stopWorker = true;
        if(btSocket!=null) {
            if(btOutputStream!=null)
                btOutputStream.close();
            if(btInputStream!=null)
                btInputStream.close();
            btSocket.close();
        }
    }


    public void connect(ITaskDelegate delegate) throws IOException{
        new ConnectBluetooth(delegate).execute();
    }

    public void clean() throws IOException{
        if(isBtTurnedOn() && isBtConnected()) {
            btOutputStream.flush();
        }
    }

    private void toastMsg(String s)
    {
        Toast.makeText(context,s,Toast.LENGTH_LONG).show();
    }

    /**********************************************************************************************/

    private class ConnectBluetooth extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean connectSuccess = true; //if it's here, it's almost connected
        private ITaskDelegate delegate;

        public ConnectBluetooth(ITaskDelegate _delegate){
            delegate=_delegate;
        }

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(activity, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try {
                if (btSocket == null || !btConnected) {
                    BluetoothDevice btDevice;
                    btDevice = btAdapter.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = btDevice.createInsecureRfcommSocketToServiceRecord(DEVICE_UUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                    btOutputStream = btSocket.getOutputStream();
                    btInputStream = btSocket.getInputStream();
                    beginListenForData();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
                toastMsg("Error");
                connectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!connectSuccess)
            {
                toastMsg("Connection Failed");
                activity.finish();
            }
            else
            {
                btConnected = true;
                //delegate.TaskCompletionResult("");
            }
            progress.dismiss();
        }
    }

    /**********************************************************************************************/

    private class BluetoothStatusUpdater implements Runnable {
        private IntentFilter filter;
        private boolean first=true;
        private boolean first2=isBtConnected();
        private boolean[] lastState={isBtTurnedOn(),isBtConnected()};

        private final BroadcastReceiver M_RECEIVER = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action) && !btConnected) {
                    btConnected =true;
                    toastMsg("Connected");
                } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action) && btConnected) {
                    try
                    {
                        disconnect();
                        toastMsg("Disconnected");
                    }
                    catch (IOException ex) {
                        toastMsg("Error");
                        ex.printStackTrace();
                    }
                }
            }
        };

        public BluetoothStatusUpdater(IUpdateAfterChangingBluetoothStatus update){
            iUpdateAfterChangingStatus = update;
            filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
            context.registerReceiver(M_RECEIVER, filter);
        }

        @Override
        public void run() {
            try {
                while(true) {
                    Thread.sleep(300);
                    if(btAdapter != null) {
                        if (!btAdapter.isEnabled()) {
                            btTurnedOn =false;
                            btConnected =false;
                            if(btSocket!=null) {
                                if(btOutputStream!=null)
                                    btOutputStream.close();
                                if(btInputStream!=null)
                                    btInputStream.close();
                                btSocket.close();
                            }
                        }else {
                            btTurnedOn =true;
                        }
                    }else{
                        throw new Exception("No bluetooth adapter available");
                    }
                    if(isBtTurnedOn()!=lastState[0] || isBtConnected()!=lastState[1] || first) {
                        Thread uiThread = new Thread(new Runnable() {
                            public void run() {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        iUpdateAfterChangingStatus.updateUI();
                                    }
                                });
                            }
                        });
                        uiThread.start();

                        lastState[0]=isBtTurnedOn();
                        lastState[1]=isBtConnected();
                        first=false;
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
                toastMsg("Error");
                btConnected =false;
            }
        }
    }

    /**********************************************************************************************/

    public interface IUpdateAfterChangingBluetoothStatus {
        void updateUI();
    }

    /**********************************************************************************************/

    public interface IUpdateUiAfterReceivingData {
        void updateUI(String data);
    }

    /**********************************************************************************************/
}