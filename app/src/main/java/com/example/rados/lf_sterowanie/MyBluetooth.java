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
import java.util.Locale;
import java.util.UUID;

class MyBluetooth {
    private static boolean btConnected = false;
    private static boolean btTurnedOn = false;
    private static BluetoothAdapter btAdapter = null;
    private static BluetoothSocket btSocket;
    private static OutputStream btOutputStream;
    private static InputStream btInputStream;
    private static String address;
    private final UUID DEVICE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    //private final String TAG = "MyBluetooth";
    //private static int counterThread =0;

    private Thread receivingDataThread;
    private Thread changingStatusThread;
    private Activity activity;
    private Context context;
    private ProgressDialog progress;
    private IMyBluetooth delegate;

    MyBluetooth(IMyBluetooth iMyBluetooth, String add) {
       // Log.i(TAG, "MyBluetooth() start");
        if (btAdapter == null) {
            btAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        setAddress(add);
        setActivityAndContext(iMyBluetooth);
        delegate = iMyBluetooth;
       // Log.i(TAG, "MyBluetooth() koniec");
    }

    void startReceiving(final byte delimiter) {
       // Log.i(TAG, "startReceiving() start");
        if (isBtConnected()) {
           // Log.i(TAG, "startReceiving() gdy połączone");
            beginListenForData(delimiter);
        }
       // Log.i(TAG, "startReceiving() koniec");
    }

    private void setActivityAndContext(IMyBluetooth iMyBluetooth) {
       // Log.i(TAG, "setActivityAndContext() start");
        activity = (Activity) iMyBluetooth;
        context = ((Context) iMyBluetooth).getApplicationContext();
       // Log.i(TAG, "setActivityAndContext() koniec");
    }

    boolean isBtConnected() {
        return btConnected;
    }

    boolean isBtTurnedOn() {
        return btTurnedOn;
    }

    void updateUiAfterChangingBluetoothStatus() { //TODO: zastanowić sięczy nazwa sensowna i czy wg potrzebne
       // Log.i(TAG, "updateUiAfterChangingBluetoothStatus() start");
        Runnable runner = new BluetoothStatusUpdater();
        changingStatusThread = new Thread(runner);
        changingStatusThread.start();
       // Log.i(TAG, "updateUiAfterChangingBluetoothStatus() koniec");
    }

    void stoppedReceivingData() {
       // Log.i(TAG, "stoppedReceivingData() start");
        if (receivingDataThread != null)
            if (receivingDataThread.isAlive()) {
                receivingDataThread.interrupt();
               // Log.i(TAG,"stoppedReceivingData() zatrzymano");
            }
       // Log.i(TAG,"stoppedReceivingData() koniec");
    }

    void stoppedChangingStatus() {
       // Log.i(TAG, "stoppedChangingStatus()");
        if (changingStatusThread != null)
            if (changingStatusThread.isAlive())
                changingStatusThread.interrupt();
    }

    private void setAddress(String add) {
        address = add;
    }

    void turnOnBT() throws Exception {
        if (btAdapter != null) {
            if (!btAdapter.isEnabled()) {
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(enableBluetooth, 0);
            }
        } else {
            throw new Exception("No bluetooth adapter available");
        }
    }

    private void beginListenForData(final byte delimiter) {
      //  Log.i(TAG, "beginListenForData() start");
        receivingDataThread = new Thread(new Runnable() {
            public void run() {
               // counterThread++;
               // int num=counterThread;
                byte[] readBuffer;
                int readBufferPosition;
                readBufferPosition = 0;
                readBuffer = new byte[1024];
                int counter = 1000000;
                while (!Thread.currentThread().isInterrupted() && btConnected) {
                   // if (counter == 1000000) {
                       // Log.i(TAG, String.format(Locale.getDefault(), "run() %s działa",  Integer.toString(num)));
                       // counter = 0;
                   // }
                    counter++;
                    try {
                        int bytesAvailable = btInputStream.available();
                        if (bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            btInputStream.read(packetBytes);
                            for (int i = 0; i < bytesAvailable; i++) {
                                byte b = packetBytes[i];
                                if (b == delimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                   // Log.i(TAG, String.format(Locale.getDefault(), "run() %s odebrano: %s",  Integer.toString(num), data ));
                                    delegate.AfterReceivingData(data);
                                    readBufferPosition = 0;
                                    Thread.sleep(100);
                                } else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    } catch (IOException ex) {
                      //  Log.i(TAG, String.format(Locale.getDefault(), "run() %s catch IO %s",  Integer.toString(num),ex.getMessage()));
                        ex.printStackTrace();
                    } catch (Exception ex) {
                       // Log.i(TAG, String.format(Locale.getDefault(), "run() %s catch %s",  Integer.toString(num),ex.getMessage()));
                        ex.printStackTrace();
                    }
                }
              //  Log.i(TAG, String.format(Locale.getDefault(), "run() %s koniec",  Integer.toString(num)));
            }
        });

        receivingDataThread.start();
      //  Log.i(TAG, "beginListenForData() koniec");
    }

    void sendData(String msg) throws IOException {
      //  Log.i(TAG, "sendData(" + msg + ")");
        btOutputStream.write(msg.getBytes());
    }

    void disconnect() throws IOException {
        btConnected = false;
        stoppedReceivingData();
        if (btSocket != null) {
            if (btOutputStream != null)
                btOutputStream.close();
            if (btInputStream != null)
                btInputStream.close();
            btSocket.close();
        }
      //  Log.i(TAG, "disconnect()");
    }


    void connect() {
      //  Log.i(TAG, "connect() start");
        new ConnectBluetooth().execute();
     //   Log.i(TAG, "connect() koniec");
    }

    private void toastMsg(String s) {
        Toast.makeText(context, s, Toast.LENGTH_LONG).show();
    }

    /**********************************************************************************************/

    private class ConnectBluetooth extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean connectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute() {
          //  Log.i(TAG, "onPreExecute() start");
            progress = ProgressDialog.show(activity, "Connecting...", "Please wait!!!");  //show a progress dialog
          //  Log.i(TAG, "onPreExecute() koniec");
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
           // Log.i(TAG, "doInBackground() start");
            try {
                if (btSocket == null || !btConnected) {
                  //  Log.i(TAG, "doInBackground() gdy niepołączone");
                    BluetoothDevice btDevice;
                    btDevice = btAdapter.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = btDevice.createInsecureRfcommSocketToServiceRecord(DEVICE_UUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                    btOutputStream = btSocket.getOutputStream();
                    btInputStream = btSocket.getInputStream();
                }
            } catch (IOException e) {
                //Log.i(TAG, "doInBackground() " + e.getMessage());
                e.printStackTrace();
                toastMsg("Error");
                connectSuccess = false;//if the try failed, you can check the exception here
            }
           // Log.i(TAG, "doInBackground() koniec");
            return null;
        }

        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            //Log.i(TAG, "onPostExecute() start");
            super.onPostExecute(result);

            if (!connectSuccess) {
                //Log.i(TAG, "onPostExecute() nie połączono");
                toastMsg("Connection Failed");
                btConnected = false;
                btSocket = null;
                btOutputStream = null;
                btInputStream = null;
            } else {
                //Log.i(TAG, "onPostExecute() połączono");
                btConnected = true;
                //delegate.StatusChanging(MyBluetooth.Status.CONNECTED, "");
            }
            progress.dismiss();
            //Log.i(TAG, "onPostExecute() koniec");
        }
    }

    /**********************************************************************************************/

    private class BluetoothStatusUpdater implements Runnable {
        private IntentFilter filter;
        private boolean first = true;
        private boolean[] lastState = {isBtTurnedOn(), isBtConnected()};

        private final BroadcastReceiver M_RECEIVER = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Log.i(TAG, "onReceive() start");
                String action = intent.getAction();
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action) && !btConnected) {
                    btConnected = true;
                    toastMsg("Connected");
                   // Log.i(TAG, "onReceive() connected");
                } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action) && btConnected) {
                    try {
                        disconnect();
                        toastMsg("Disconnected");
                       // Log.i(TAG, "onReceive() disconnected");
                    } catch (IOException ex) {
                       // Log.i(TAG, "onReceive() catch IO " + ex.getMessage());
                        toastMsg("Error");
                        ex.printStackTrace();
                    } catch (Exception ex) {
                        //Log.i(TAG, "onReceive() catch " + ex.getMessage());
                    }
                }
                //Log.i(TAG, "onReceive() koniec");
            }
        };

        private BluetoothStatusUpdater() {
            //Log.i(TAG, "BluetoothStatusUpdater() start");
            filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
            context.registerReceiver(M_RECEIVER, filter);
           // Log.i(TAG, "BluetoothStatusUpdater() koniec");
        }

        @Override
        public void run() {
            int max = 2000000;
            int counter = max;
           // Log.i(TAG, "BSU run() start");
            try {
                while (!Thread.currentThread().isInterrupted()) {
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
                            //Log.i(TAG, "BSU run() zmiana stanu");
                            if (isBtTurnedOn() != lastState[0] && isBtTurnedOn()) {
                                delegate.AfterTurningOnBluetooth();
                            }
                            if (isBtTurnedOn()) {
                                if (isBtConnected()) {
                                    delegate.AfterConnecting();
                                } else {
                                    delegate.AfterDisconnecting();
                                }
                            } else {
                                delegate.AfterTurningOffBluetooth();
                            }
                            lastState[0] = isBtTurnedOn();
                            lastState[1] = isBtConnected();
                            first = false;
                        }
                        //Log.i(TAG, "BSU run() działa");
                        counter = 0;
                    }
                    counter++;
                }
            } catch (IOException e) {
                //Log.i(TAG, "BSU run() catch io " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                //Log.i(TAG, "BSU run() catch " + e.getMessage());
                e.printStackTrace();
            }
            //Log.i(TAG, "BSU run() koniec");
        }
    }

    /**********************************************************************************************/

    interface IMyBluetooth {
        void AfterConnecting();

        void AfterDisconnecting();

        void AfterTurningOnBluetooth();

        void AfterTurningOffBluetooth();

        void AfterReceivingData(final String data);
    }
}