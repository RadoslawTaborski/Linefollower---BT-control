package com.example.rados.lf_sterowanie;

import android.content.pm.ActivityInfo;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class CompetitionActivity extends AppCompatActivity implements MyBluetooth.IMyBluetooth {
    public MyBluetooth bluetooth;
    TextView tvSpeed;
    TextView tvKP;
    TextView tvKD;
    Button btnStart;
    Button btnStop;
    Button btnClear;
    Button btnRead;
    Button btnEeprom;
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
        btnEeprom = (Button) findViewById(R.id.btnRead2);
        btnConnect = (Button) findViewById(R.id.btnConnect2);
        chrono = (chronometer.Chronometer) findViewById(R.id.chronometer2);
        tvSensorsData = (TextView) findViewById(R.id.tvData);
        tvSensorsData.setMovementMethod(new ScrollingMovementMethod());

        bluetooth = new MyBluetooth(CompetitionActivity.this, "00:12:6F:6B:C0:A2");
    }

    public void startClick(View v) {
        chrono.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
        chrono.start();
        if (bluetooth.isBtConnected()) {
            try {
                bluetooth.sendData(ControlCommands.AUTO_START);
                btnStart.setEnabled(false);
                btnStop.setEnabled(true);
                btnClear.setEnabled(false);
                btnRead.setEnabled(true);
                btnRead.setText(R.string.Read2);
                btnEeprom.setEnabled(false);
                btnConnect.setEnabled(false);
                tvSensorsData.setText("");
            } catch (IOException ex) {
                ex.printStackTrace();
                msg("IO Error");
            }
        }
    }

    public void stopClick(View v) {
        chrono.stop();
        timeWhenStopped = 0;
        if (bluetooth.isBtConnected()) {
            try {
                bluetooth.sendData(ControlCommands.STOP);
                btnStart.setEnabled(true);
                btnStop.setEnabled(false);
                btnClear.setEnabled(true);
                btnRead.setEnabled(false);
                btnRead.setText(R.string.Read2);
                btnEeprom.setEnabled(true);
                btnConnect.setEnabled(true);
            } catch (IOException ex) {
                ex.printStackTrace();
                msg("IO Error");
            }
        }
    }

    public void clearClick(View v) {
        if (bluetooth.isBtConnected()) {
            try {
                bluetooth.sendData(ControlCommands.WHEELS_CLEANING);
                btnStart.setEnabled(false);
                btnStop.setEnabled(true);
                btnClear.setEnabled(false);
                btnRead.setEnabled(false);
                btnEeprom.setEnabled(false);
                btnConnect.setEnabled(false);
            } catch (IOException ex) {
                ex.printStackTrace();
                msg("IO Error");
            }
        }
    }

    public void readClick(View v) {
        if (bluetooth.isBtConnected()) {
            if (!readON) {
                try {
                    bluetooth.sendData(ControlCommands.SENSORS_READINGS);
                    btnStart.setEnabled(false);
                    btnStop.setEnabled(true);
                    btnClear.setEnabled(false);
                    btnRead.setText(R.string.ReadStop2);
                    btnConnect.setEnabled(false);
                    readON = true;
                } catch (IOException ex) {
                    ex.printStackTrace();
                    msg("IO Error");
                }
            } else {
                try {
                    bluetooth.sendData(ControlCommands.NOTHING);
                    btnStart.setEnabled(false);
                    btnStop.setEnabled(true);
                    btnClear.setEnabled(false);
                    btnRead.setText(R.string.Read2);
                    btnConnect.setEnabled(false);
                    readON = false;
                } catch (IOException ex) {
                    ex.printStackTrace();
                    msg("IO Error");
                }
            }
        }
    }

    public void eepromClick(View v) {
        if (bluetooth.isBtConnected()) {
            try {
                bluetooth.sendData(ControlCommands.PARAMETERS_LOAD);
                Thread.sleep(ControlCommands.sleepTime1);
                bluetooth.sendData(ControlCommands.PARAMETERS_QUESTION);
            } catch (IOException ex) {
                ex.printStackTrace();
                msg("IO Error");
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                msg("Interrupt Error");
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
                    msg("IO Error");
                }
            } else {
                bluetooth.connect();
            }
        } else {
            try {
                bluetooth.turnOnBT();
            } catch (Exception ex) {
                ex.printStackTrace();
                msg(ex.getMessage());
            }
        }
    }

    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }


    private void ifConnected() {
        if (bluetooth.isBtConnected()) {
            try {
                final byte delimiter = 13;
                bluetooth.startReceiving(delimiter);
                Thread.sleep(ControlCommands.sleepTime2);
                bluetooth.sendData(ControlCommands.STOP);
                Thread.sleep(ControlCommands.sleepTime1);
                bluetooth.sendData(ControlCommands.PARAMETERS_QUESTION);
            } catch (IOException ex) {
                ex.printStackTrace();
                msg("IO Error");
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                msg("Interrupt Error");
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (bluetooth.isBtConnected()) {
            try {
                bluetooth.sendData(ControlCommands.STOP);
            } catch (IOException ex) {
                ex.printStackTrace();
                msg("IO Error");
            }
        }
        bluetooth.stoppedChangingStatus();
        bluetooth.stoppedReceivingData();
        super.onDestroy();
    }

    @Override
    public void AfterConnecting() {
        boolean flag = false;
        while (!flag) {
            if (bluetooth.isBtTurnedOn()) {
                try {
                    Thread.sleep(ControlCommands.sleepTime2);
                    ifConnected();
                    flag = true;
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    msg("Interrupt Error");
                }
            }
        }
        Thread uiThread = new Thread(new Runnable() {
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnConnect.setText(R.string.Disconnect);
                        btnStart.setEnabled(true);
                        btnStop.setEnabled(false);
                        btnClear.setEnabled(true);
                        btnRead.setEnabled(false);
                        btnEeprom.setEnabled(true);
                        btnConnect.setEnabled(true);
                    }
                });
            }
        });
        uiThread.start();
    }

    @Override
    public void AfterDisconnecting() {
        Thread uiThread = new Thread(new Runnable() {
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnConnect.setText(R.string.Connect);
                        btnStart.setEnabled(false);
                        btnStop.setEnabled(false);
                        btnClear.setEnabled(false);
                        btnRead.setEnabled(false);
                        btnEeprom.setEnabled(false);
                        btnConnect.setEnabled(true);
                        chrono.stop();
                    }
                });
            }
        });
        uiThread.start();
    }

    @Override
    public void AfterTurningOnBluetooth() {
    }

    @Override
    public void AfterTurningOffBluetooth() {
        Thread uiThread = new Thread(new Runnable() {
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnConnect.setText(R.string.TurnOnBt);
                        btnStart.setEnabled(false);
                        btnStop.setEnabled(false);
                        btnClear.setEnabled(false);
                        btnRead.setEnabled(false);
                        btnEeprom.setEnabled(false);
                        btnConnect.setEnabled(true);
                    }
                });
            }
        });
        uiThread.start();
    }

    @Override
    public void AfterReceivingData(final String data) {
        Thread uiThread = new Thread(new Runnable() {
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (data.length() == 15) {
                            int tmp = Integer.parseInt(data.substring(0, 5));
                            tvKP.setText(String.valueOf(tmp));
                            tmp = Integer.parseInt(data.substring(5, 10));
                            tvKD.setText(String.valueOf(tmp));
                            tmp = Integer.parseInt(data.substring(10, 15));
                            tvSpeed.setText(String.valueOf(tmp));
                        } else {
                            if (data.charAt(data.length() - 1) == '\n') {
                                tvSensorsData.append("\n" + data);
                                while (tvSensorsData.canScrollVertically(1)) {
                                    tvSensorsData.scrollBy(0, 10);
                                }
                            }
                        }
                    }
                });
            }
        });
        uiThread.start();
    }
}
