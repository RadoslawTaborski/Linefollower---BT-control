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
import android.util.Log;
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
    private static final String TAG = "MyBluetooth";

    private Thread recevingDataThread;
    private Thread changingStatusThread;
    private Activity activity;
    private Context context;
    private byte[] readBuffer;
    private int readBufferPosition;
    private ProgressDialog progress;
    private IUpdateUiAfterReceivingData iUpdateReceiveUI;
    private IUpdateAfterChangingBluetoothStatus iUpdateAfterChangingStatus;

    public MyBluetooth(Activity act, Context con, String add, IUpdateUiAfterReceivingData update, IUpdateAfterChangingBluetoothStatus update2) {
        Log.i(TAG, "MyBluetooth() start");
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        setAddress(add);
        setActivityAndContext(act,con,update,update2);
        Log.i(TAG, "MyBluetooth() koniec");
    }

    public void startReceiving(){
        Log.i(TAG, "startReceiving() start");
        if(isBtConnected()){
            Log.i(TAG, "startReceiving() gdy połączone");
            beginListenForData();
        }
        Log.i(TAG, "startReceiving() koniec");
    }

    private void setActivityAndContext(Activity act, Context con, IUpdateUiAfterReceivingData update, IUpdateAfterChangingBluetoothStatus update2){
        Log.i(TAG, "setActivityAndContext() start");
        activity=act;
        context=con;
        setMethodToUpdateUiAfterReceivingData(update);
        setMethodToUpdateUiAfterChangingBluetoothStatus(update2);
        Log.i(TAG, "setActivityAndContext() koniec");
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
        Log.i(TAG, "setMethodToUpdateUiAfterReceivingData() start");
        if(update==null){
            Log.i(TAG, "setMethodToUpdateUiAfterReceivingData() gdy null");
            iUpdateReceiveUI=new IUpdateUiAfterReceivingData() {
                @Override
                public void updateUI(String data) {
                }
            };
        }else{
            Log.i(TAG, "setMethodToUpdateUiAfterReceivingData() gdy nie null");
            iUpdateReceiveUI=update;
        }
        Log.i(TAG, "setMethodToUpdateUiAfterReceivingData() koniec");
    }

    private void setMethodToUpdateUiAfterChangingBluetoothStatus(IUpdateAfterChangingBluetoothStatus update){
        Log.i(TAG, "setMethodToUpdateUiAfterChangingBluetoothStatus() start");
        if(update==null){
            Log.i(TAG, "setMethodToUpdateUiAfterChangingBluetoothStatus() gdy null");
            iUpdateAfterChangingStatus=new IUpdateAfterChangingBluetoothStatus() {
                @Override
                public void updateUI() {
                }
            };
        }else{
            Log.i(TAG, "setMethodToUpdateUiAfterChangingBluetoothStatus() gdy nie null");
            iUpdateAfterChangingStatus=update;
        }
        Log.i(TAG, "setMethodToUpdateUiAfterChangingBluetoothStatus() koniec");
    }

    public void updateUiAfterChangingBluetoothStatus()
    {
        Log.i(TAG, "updateUiAfterChangingBluetoothStatus() start");
        Runnable runner = new BluetoothStatusUpdater(iUpdateAfterChangingStatus);
        changingStatusThread=new Thread(runner);
        changingStatusThread.start();
        Log.i(TAG, "updateUiAfterChangingBluetoothStatus() koniec");
    }

    public void stoppedReceivingData(){
        Log.i(TAG,"stoppedReceivingData()");
        if(recevingDataThread!=null)
        if(recevingDataThread.isAlive())
            recevingDataThread.interrupt();
    }

    public void stoppedChangingStatus(){
        Log.i(TAG,"stoppedChangingStatus()");
        if(changingStatusThread!=null)
        if(changingStatusThread.isAlive())
            changingStatusThread.interrupt();
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
        Log.i(TAG, "beginListenForData() start");
        final byte delimiter = 13; //TODO: dodać wybór
        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        recevingDataThread = new Thread(new Runnable()
        {
            public void run()
            {
                int counter=1000000;
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    if(counter == 1000000) {
                        Log.i(TAG, "run() działa");
                        counter=0;
                    }
                    counter++;
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
                                    Log.i(TAG, "run() odebrano: "+data);
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
                    catch (IOException ex){
                        Log.i(TAG,"run() catch " +ex.getMessage());
                        ex.printStackTrace();
                        stopWorker = true;
                    }catch (Exception e){}

                }
                Log.i(TAG,"run() koniec");
            }
        });

        recevingDataThread.start();
        Log.i(TAG, "beginListenForData() koniec");
    }

    void sendData(String msg) throws IOException
    {
        Log.i(TAG, "sendData(" + msg + ")");
        btOutputStream.write(msg.getBytes());
    }

    void disconnect() throws IOException
    {
        btConnected =false;
        stoppedReceivingData();
        stopWorker = true;
        if(btSocket!=null) {
            if(btOutputStream!=null)
                btOutputStream.close();
            if(btInputStream!=null)
                btInputStream.close();
            btSocket.close();
        }
        Log.i(TAG, "disconnect()");
    }


    public void connect(IMyBluetooth delegate) throws IOException{
        Log.i(TAG, "connect() start");
        new ConnectBluetooth(delegate).execute();
        Log.i(TAG, "connect() koniec");
    }

    public void clean() throws IOException{
        Log.i(TAG, "clean() start");
        if(isBtTurnedOn() && isBtConnected()) {
            btOutputStream.flush();
        }
        Log.i(TAG, "clean() koniec");
    }

    private void toastMsg(String s)
    {
        Toast.makeText(context,s,Toast.LENGTH_LONG).show();
    }

    /**********************************************************************************************/

    private class ConnectBluetooth extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean connectSuccess = true; //if it's here, it's almost connected
        private IMyBluetooth delegate;

        public ConnectBluetooth(IMyBluetooth _delegate){
            Log.i(TAG, "ConnectBluetooth() start");
            delegate=_delegate;
            Log.i(TAG, "ConnectBluetooth() koniec");
        }

        @Override
        protected void onPreExecute()
        {
            Log.i(TAG, "onPreExecute() start");
            progress = ProgressDialog.show(activity, "Connecting...", "Please wait!!!");  //show a progress dialog
            Log.i(TAG, "onPreExecute() koniec");
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            Log.i(TAG, "doInBackground() start");
            try {
                if (btSocket == null || !btConnected) {
                    Log.i(TAG, "doInBackground() gdy niepołączone");
                    BluetoothDevice btDevice;
                    btDevice = btAdapter.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = btDevice.createInsecureRfcommSocketToServiceRecord(DEVICE_UUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                    btOutputStream = btSocket.getOutputStream();
                    btInputStream = btSocket.getInputStream();
                }
            }
            catch (IOException e)
            {
                Log.i(TAG, "doInBackground() " + e.getMessage());
                e.printStackTrace();
                toastMsg("Error");
                connectSuccess = false;//if the try failed, you can check the exception here
            }
            Log.i(TAG, "doInBackground() koniec");
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            Log.i(TAG, "onPostExecute() start");
            super.onPostExecute(result);

            if (!connectSuccess)
            {
                Log.i(TAG, "onPostExecute() nie połączono");
                toastMsg("Connection Failed");
                btConnected=false;
                btAdapter=null;
                btSocket=null;
                btOutputStream=null;
                btInputStream=null;
            }
            else
            {
                Log.i(TAG, "onPostExecute() połączono");
                btConnected = true;
                delegate.TaskCompletionResult(""); //TODO: edycja gui w ten sposób
            }
            progress.dismiss();
            Log.i(TAG, "onPostExecute() koniec");
        }
    }

    /**********************************************************************************************/

    private class BluetoothStatusUpdater implements Runnable {
        private IntentFilter filter;
        private boolean first=true;
        private boolean[] lastState={isBtTurnedOn(),isBtConnected()};

        private final BroadcastReceiver M_RECEIVER = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(TAG, "onReceive() start");
                String action = intent.getAction();
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action) && !btConnected) {
                    btConnected =true;
                    toastMsg("Connected");
                } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action) && btConnected) { //TODO: tutaj sie wywala activity
                    try
                    {
                        disconnect();
                        toastMsg("Disconnected");
                    }
                    catch (IOException ex) {
                        Log.i(TAG, "onReceive() catch " + ex.getMessage());
                        toastMsg("Error");
                        ex.printStackTrace();
                    }
                    catch (Exception ex){
                        Log.i(TAG, "onReceive() catch " + ex.getMessage());
                    }
                }
                Log.i(TAG, "onReceive() koniec");
            }
        };

        public BluetoothStatusUpdater(IUpdateAfterChangingBluetoothStatus update){
            Log.i(TAG, "BluetoothStatusUpdater() start");
            iUpdateAfterChangingStatus = update;
            filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
            context.registerReceiver(M_RECEIVER, filter);
            Log.i(TAG, "BluetoothStatusUpdater() koniec");
        }

        @Override
        public void run() {
            int max=2000000;
            int counter=max;
            Log.i(TAG, "BSU run() start");
            try {
                while(!Thread.currentThread().isInterrupted()) {
                    if (counter == max) {
                        if (btAdapter != null) {
                            if (!btAdapter.isEnabled()) {
                                btTurnedOn = false;
                                btConnected = false;
                                if (btSocket != null) {
                                    if (btOutputStream != null)
                                        btOutputStream.close();
                                    if (btInputStream != null)
                                        btInputStream.close();
                                    btSocket.close();
                                }
                            } else {
                                btTurnedOn = true;
                            }
                        } else {
                            throw new Exception("No bluetooth adapter available");
                        }

                        if (isBtTurnedOn() != lastState[0] || isBtConnected() != lastState[1] || first) {
                            Log.i(TAG, "BSU run() zmiana stanu");
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

                            lastState[0] = isBtTurnedOn();
                            lastState[1] = isBtConnected();
                            first = false;
                        }
                        Log.i(TAG, "BSU run() działa");
                        counter = 0;
                    }
                    counter++;
                }
            } catch (IOException e) {
                Log.i(TAG, "BSU run() catch io "+e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                Log.i(TAG, "BSU run() catch "+e.getMessage());
                e.printStackTrace();
            }
            Log.i(TAG, "BSU run() koniec");
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

    public interface IMyBluetooth{
        void TaskCompletionResult(String result);
    }
}