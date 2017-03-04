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

class MyBluetooth {
    private static boolean btConnected = false;
    private static boolean btTurnedOn = false;
    private static BluetoothAdapter btAdapter = null;
    private static BluetoothSocket btSocket;
    private static OutputStream btOutputStream;
    private static InputStream btInputStream;
    private static String address;
    private final UUID DEVICE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private Thread receivingDataThread;
    private Thread changingStatusThread;
    private Activity activity;
    private Context context;
    private ProgressDialog progress;
    private IMyBluetooth delegate;

    MyBluetooth(IMyBluetooth iMyBluetooth, String add) {
        if (btAdapter == null) {
            btAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        setAddress(add);
        setActivityAndContext(iMyBluetooth);
        delegate = iMyBluetooth;
        updateBluetoothStatus();
    }

    void startReceiving(final byte delimiter) {
        if (isBtConnected()) {
            beginListenForData(delimiter);
        }
    }

    private void setActivityAndContext(IMyBluetooth iMyBluetooth) {
        activity = (Activity) iMyBluetooth;
        context = ((Context) iMyBluetooth).getApplicationContext();
    }

    boolean isBtConnected() {
        return btConnected;
    }

    boolean isBtTurnedOn() {
        return btTurnedOn;
    }

    private void updateBluetoothStatus() {
        Runnable runner = new BluetoothStatusUpdater();
        changingStatusThread = new Thread(runner);
        changingStatusThread.start();
    }

    void stoppedReceivingData() {
        if (receivingDataThread != null)
            if (receivingDataThread.isAlive()) {
                receivingDataThread.interrupt();
            }
    }

    void stoppedChangingStatus() {
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
        receivingDataThread = new Thread(new Runnable() {
            public void run() {
                byte[] readBuffer;
                int readBufferPosition;
                readBufferPosition = 0;
                readBuffer = new byte[1024];
                while (!Thread.currentThread().isInterrupted() && btConnected) {
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
                                    delegate.AfterReceivingData(data);
                                    readBufferPosition = 0;
                                    Thread.sleep(100);
                                } else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        receivingDataThread.start();
    }

    void sendData(String msg) throws IOException {
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
            btSocket = null;
            btOutputStream = null;
            btInputStream = null;
        }
    }


    void connect() {
        new ConnectBluetooth().execute();
    }

    private void toastMsg(String s) {
        Toast.makeText(context, s, Toast.LENGTH_LONG).show();
    }

    /**********************************************************************************************/

    private class ConnectBluetooth extends AsyncTask<Void, Void, Void> {
        private boolean connectSuccess = true;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(activity, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) {
            try {
                if (btSocket == null || !btConnected) {
                    BluetoothDevice btDevice;
                    btDevice = btAdapter.getRemoteDevice(address);
                    btSocket = btDevice.createInsecureRfcommSocketToServiceRecord(DEVICE_UUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();
                    btOutputStream = btSocket.getOutputStream();
                    btInputStream = btSocket.getInputStream();
                }
            } catch (IOException e) {
                e.printStackTrace();
                toastMsg("Error");
                connectSuccess = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (!connectSuccess) {
                toastMsg("Connection Failed");
                btConnected = false;
                btSocket = null;
                btOutputStream = null;
                btInputStream = null;
            } else {
                btConnected = true;
            }
            progress.dismiss();
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
                String action = intent.getAction();
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action) && !btConnected) {
                    toastMsg("Connected");
                } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action) && btConnected) {
                    try {
                        toastMsg("Disconnected");
                        disconnect();
                    } catch (IOException ex) {
                        toastMsg("Error IO");
                        ex.printStackTrace();
                    } catch (Exception ex) {
                        toastMsg("Error");
                        ex.printStackTrace();
                    }
                }
            }
        };

        private BluetoothStatusUpdater() {
            filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
            context.registerReceiver(M_RECEIVER, filter);
        }

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    // if (counter == max) {
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
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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