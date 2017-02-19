package com.example.rados.lf_sterowanie;

import android.content.pm.ActivityInfo;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class CompetitionActivity extends AppCompatActivity implements MyBluetooth.IMyBluetooth {
    public MyBluetooth bluetooth;
    private static final String TAG = "MyBluetooth";
    MyBluetooth.IUpdateUiAfterReceivingData afterReceivingData = null;
    MyBluetooth.IUpdateAfterChangingBluetoothStatus afterChangingStatus = null;
    TextView tvSpeed;
    TextView tvKP;
    TextView tvKD;
    Button btnStart;
    Button btnStop;
    Button btnClear;
    Button btnRead;
    Button btnRead2;
    Button btnConnect;
    chronometer.Chronometer chrono;
    TextView tvSensorsData;
    private long timeWhenStopped = 0;
    boolean readON = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_competition);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        final String zero = "0";

        tvSpeed = (TextView) findViewById(R.id.tvSpeed2);
        tvKP = (TextView) findViewById(R.id.tvKP2);
        tvKD = (TextView) findViewById(R.id.tvKD2);
        tvSpeed.setText(zero);
        tvKP.setText(zero);
        tvKD.setText(zero);
        btnStart = (Button) findViewById(R.id.btnStart2);
        btnStop = (Button) findViewById(R.id.btnStop2);
        btnClear = (Button) findViewById(R.id.btnClear2);
        btnRead = (Button) findViewById(R.id.btnRead);
        btnRead2 = (Button) findViewById(R.id.btnRead2);
        btnConnect = (Button) findViewById(R.id.btnConnect2);
        chrono = (chronometer.Chronometer) findViewById(R.id.chronometer2);
        tvSensorsData = (TextView) findViewById(R.id.tvData);
        tvSensorsData.setMovementMethod(new ScrollingMovementMethod());


        afterReceivingData = new MyBluetooth.IUpdateUiAfterReceivingData() {
            @Override
            public void updateUI(String data) {
                if (data.length() == 15) {
                    Log.i(TAG, "odebrano " + data);
                    int tmp = Integer.parseInt(data.substring(0, 5));
                    tvKP.setText(String.valueOf(tmp));
                    tmp = Integer.parseInt(data.substring(5, 10));
                    tvKD.setText(String.valueOf(tmp));
                    tmp = Integer.parseInt(data.substring(10, 15));
                    tvSpeed.setText(String.valueOf(tmp));
                } else {
                    if (data.charAt(data.length() - 1) == '\r')
                        Log.i(TAG, "odebrano " + data);
                    tvSensorsData.setText(tvSensorsData.getText() + "\n" + data);
                }
            }
        };

        afterChangingStatus = new MyBluetooth.IUpdateAfterChangingBluetoothStatus() {
            @Override
            public void updateUI() {
                if (bluetooth.isBtTurnedOn()) {
                    if (bluetooth.isBtConnected()) {
                        btnConnect.setText(R.string.Disconnect);
                        btnStart.setEnabled(true);
                        btnStop.setEnabled(false);
                        btnClear.setEnabled(true);
                        btnRead.setEnabled(false);
                        btnRead2.setEnabled(false);
                        btnConnect.setEnabled(true);
                    } else {
                        btnConnect.setText(R.string.Connect);
                        btnStart.setEnabled(false);
                        btnStop.setEnabled(false);
                        btnClear.setEnabled(false);
                        btnRead.setEnabled(false);
                        btnRead2.setEnabled(false);
                        btnConnect.setEnabled(true);
                        chrono.stop();
                    }
                } else {
                    btnConnect.setText(R.string.TurnOnBt);
                    btnStart.setEnabled(false);
                    btnStop.setEnabled(false);
                    btnClear.setEnabled(false);
                    btnRead.setEnabled(false);
                    btnRead2.setEnabled(false);
                    btnConnect.setEnabled(true);
                }
            }
        };

        bluetooth = new MyBluetooth(CompetitionActivity.this, getApplicationContext(), "00:12:6F:6B:C0:A2", afterReceivingData, afterChangingStatus);
        bluetooth.updateUiAfterChangingBluetoothStatus();
        ifConnected();

    }

    public void startClick(View v) {
        chrono.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
        chrono.start();
        if (bluetooth.isBtConnected()) {
            try {
                bluetooth.sendData("8");
                btnStart.setEnabled(false);
                btnStop.setEnabled(true);
                btnClear.setEnabled(false);
                btnRead.setEnabled(true);
                btnRead2.setEnabled(false);
                btnConnect.setEnabled(false);
                tvSensorsData.setText("");
            } catch (IOException ex) {
                ex.printStackTrace();
                msg("Error");
            }
        }
    }

    public void stopClick(View v) {
        chrono.stop();
        timeWhenStopped = 0;
        if (bluetooth.isBtConnected()) {
            try {
                bluetooth.sendData("0");
                btnStart.setEnabled(true);
                btnStop.setEnabled(false);
                btnClear.setEnabled(true);
                btnRead.setEnabled(false);
                btnRead2.setEnabled(false);
                btnConnect.setEnabled(true);
            } catch (IOException ex) {
                ex.printStackTrace();
                msg("Error");
            }
        }
    }

    public void clearClick(View v) {
        if (bluetooth.isBtConnected()) {
            try {
                bluetooth.sendData("7");
                btnStart.setEnabled(false);
                btnStop.setEnabled(true);
                btnClear.setEnabled(false);
                btnRead.setEnabled(false);
                btnRead2.setEnabled(false);
                btnConnect.setEnabled(false);
            } catch (IOException ex) {
                ex.printStackTrace();
                msg("Error");
            }
        }
    }

    public void readClick(View v) {
        if (bluetooth.isBtConnected()) {
            if (!readON) {
                try {
                    bluetooth.sendData("3");
                    btnStart.setEnabled(false);
                    btnStop.setEnabled(true);
                    btnClear.setEnabled(false);
                    btnRead.setText(R.string.Read2);
                    btnConnect.setEnabled(false);
                    readON = true;
                } catch (IOException ex) {
                    ex.printStackTrace();
                    msg("Error");
                }
            } else {
                try {
                    bluetooth.sendData("z");
                    btnStart.setEnabled(false);
                    btnStop.setEnabled(true);
                    btnClear.setEnabled(false);
                    btnRead.setText(R.string.ReadStop2);
                    btnConnect.setEnabled(false);
                    readON = false;
                } catch (IOException ex) {
                    ex.printStackTrace();
                    msg("Error");
                }
            }
        }
    }

    public void eepromClick(View v) {
        if (bluetooth.isBtConnected()) {
            try {
                bluetooth.sendData(".");
                Thread.sleep(50);
                bluetooth.sendData("?");
            } catch (IOException ex) {
                ex.printStackTrace();
                msg("IO Error");
            } catch (InterruptedException e) {
                e.printStackTrace();
                msg("Interrupted Error");
            }
        }
    }

    public void connectClick(View v) {
        if (bluetooth.isBtTurnedOn()) {
            if (bluetooth.isBtConnected()) {
                try {
                    bluetooth.disconnect();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                try {
                    bluetooth.connect(this);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        } else {
            try {
                bluetooth.turnOnBT();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }


    private void ifConnected() {
        if (bluetooth.isBtConnected()) {
            try {
                bluetooth.startReceiving();
                Thread.sleep(50);
                bluetooth.sendData("?");
            } catch (IOException ex) {
                ex.printStackTrace();
                msg("Error");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (bluetooth.isBtConnected()) {
            try {
                bluetooth.sendData("0");
            } catch (IOException ex) {
                ex.printStackTrace();
                msg("Error");
            }
        }
        bluetooth.stoppedChangingStatus();
        bluetooth.stoppedReceivingData();
        super.onDestroy();
    }

    @Override
    public void TaskCompletionResult(String msg) {
        boolean flag = false;
        while (!flag) {
            if (bluetooth.isBtTurnedOn()) {
                try {
                    Thread.sleep(100);
                    ifConnected();
                    flag = true;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
