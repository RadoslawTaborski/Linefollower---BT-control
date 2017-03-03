package com.example.rados.lf_sterowanie;

import android.content.pm.ActivityInfo;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Locale;

public class TestActivity extends AppCompatActivity implements MyBluetooth.IMyBluetooth {
    public MyBluetooth bluetooth;
    public boolean auto = false;
    private long timeWhenStopped = 0;
   // private static final String TAG = "MyBluetooth";
    TextView textRight;
    TextView textLeft;
    TextView textSpeed;
    TextView tvSpeed;
    TextView tvTurn;
    TextView info;
    Button btnLeftR;
    Button btnRightR;
    Button btnLeft;
    Button btnRight;
    Button btnSpeedUp;
    Button btnSpeedDown;
    Button btnStart;
    Button btnMax;
    Button btnPause;
    Button btnStop;
    Button btnConnect;
    Button btnLed1;
    Button btnLed2;
    Button btnLed12;
    CheckBox cbAuto;
    SeekBar sbSpeed;
    SeekBar sbTurn;
    chronometer.Chronometer chrono;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //Log.i(TAG, "start");
        final String zero = "0";
        textRight = (TextView) findViewById(R.id.textRight);
        textLeft = (TextView) findViewById(R.id.textLeft);
        textSpeed = (TextView) findViewById(R.id.textSpeed);
        textRight.setText(zero);
        textLeft.setText(zero);
        textSpeed.setText(zero);
        info = (TextView) findViewById(R.id.textView);

        tvSpeed = (TextView) findViewById(R.id.tvSpeed);
        tvTurn = (TextView) findViewById(R.id.tvTurn);

        cbAuto = (CheckBox) findViewById(R.id.cbAuto);

        sbSpeed = (SeekBar) findViewById(R.id.sbSpeed);
        sbSpeed.setMax(9);
        sbSpeed.setProgress(3);
        tvSpeed.setText(String.format(Locale.getDefault(), "%s%s", getString(R.string.Speed1), Integer.toString(sbSpeed.getProgress())));
        sbSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                tvSpeed.setText(String.format(Locale.getDefault(), "%s%s", getString(R.string.Speed1), Integer.toString(progress)));
                if (bluetooth.isBtConnected()) {
                    try {
                        bluetooth.sendData(ControlCommands.SPEED_ARRAY[progress]);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        msg("Error");
                    }
                }
            }
        });

        sbTurn = (SeekBar) findViewById(R.id.sbTurn);
        sbTurn.setMax(5);
        sbTurn.setProgress(3);
        tvTurn.setText(String.format(Locale.getDefault(), "%s%s", getString(R.string.Turn1), Integer.toString(sbTurn.getProgress())));
        sbTurn.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                tvTurn.setText(String.format(Locale.getDefault(), "%s%s", getString(R.string.Turn1), Integer.toString(progress)));
                if (bluetooth.isBtConnected()) {
                    try {
                        bluetooth.sendData(ControlCommands.TURN_ARRAY[progress]);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        msg("Error");
                    }
                }
            }
        });

        chrono = (chronometer.Chronometer) findViewById(R.id.chronometer);

        btnLeftR = (Button) findViewById(R.id.btnLeftR);
        btnRightR = (Button) findViewById(R.id.btnRightR);
        btnLeft = (Button) findViewById(R.id.btnLeft);
        btnRight = (Button) findViewById(R.id.btnRight);
        btnSpeedUp = (Button) findViewById(R.id.btnSpeedUp);
        btnSpeedDown = (Button) findViewById(R.id.btnSpeedDown);
        btnStart = (Button) findViewById(R.id.btnStart);
        btnMax = (Button) findViewById(R.id.btnMax);
        btnPause = (Button) findViewById(R.id.btnPause);
        btnStop = (Button) findViewById(R.id.btnStop);
        btnConnect = (Button) findViewById(R.id.btnConnect);

        btnLed1 = (Button) findViewById(R.id.btnLED1);
        btnLed2 = (Button) findViewById(R.id.btnLED2);
        btnLed12 = (Button) findViewById(R.id.btnLED12);

        textLeft.setVisibility(View.INVISIBLE);
        textRight.setVisibility(View.INVISIBLE);
        textSpeed.setVisibility(View.INVISIBLE);

        final String time = "00:00:0";
        chrono.setText(time);
        info.setText(R.string.Manual1);

        bluetooth = new MyBluetooth(TestActivity.this, "00:12:6F:6B:C0:A2");
        bluetooth.updateUiAfterChangingBluetoothStatus();
    }

    public void changedClick(View v) {
        if (cbAuto.isChecked()) {
            info.setText(R.string.Auto1);
            auto = true;
            textLeft.setVisibility(View.VISIBLE);
            textRight.setVisibility(View.VISIBLE);
            textSpeed.setVisibility(View.VISIBLE);
            sbSpeed.setVisibility(View.INVISIBLE);
            sbTurn.setVisibility(View.INVISIBLE);
            tvSpeed.setVisibility(View.INVISIBLE);
            tvTurn.setVisibility(View.INVISIBLE);
            btnLeftR.setVisibility(View.INVISIBLE);
            btnRightR.setVisibility(View.INVISIBLE);
            btnLeft.setText(R.string.Kp1);
            btnRight.setText(R.string.Kd1);
            btnSpeedUp.setText(R.string.Speed1a);
            btnSpeedDown.setText(R.string.Eeprom1);
            btnMax.setText(R.string.Clear1);
            btnPause.setText(R.string.Save1);
            if (bluetooth.isBtConnected()) {
                try {
                    bluetooth.sendData(ControlCommands.PARAMETERS_QUESTION);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    msg("Error");
                }
            }
            setOthers(true, false);
            setDirectionalButtons(true, true, true, false, true, false);
            setOtherButtons(true, true, true, false, true);
        } else {
            info.setText(R.string.Manual1);
            auto = false;
            textLeft.setVisibility(View.INVISIBLE);
            textRight.setVisibility(View.INVISIBLE);
            textSpeed.setVisibility(View.INVISIBLE);
            sbSpeed.setVisibility(View.VISIBLE);
            sbTurn.setVisibility(View.VISIBLE);
            tvSpeed.setVisibility(View.VISIBLE);
            tvTurn.setVisibility(View.VISIBLE);
            btnLeftR.setText(R.string.LCurveR);
            btnRightR.setText(R.string.RCurveR);
            btnLeft.setText(R.string.LCurve);
            btnRight.setText(R.string.RCurve);
            btnSpeedUp.setText(R.string.Up);
            btnSpeedDown.setText(R.string.Down);
            btnMax.setText(R.string.Max1);
            btnPause.setText(R.string.Pauza1);
            btnLeftR.setVisibility(View.VISIBLE);
            btnRightR.setVisibility(View.VISIBLE);
            setOthers(true, true);
            setDirectionalButtons(false, false, false, false, false, false);
            setOtherButtons(true, true, false, false, true);
        }
    }

    public void topClick(View v) {
        if (bluetooth.isBtConnected()) {
            try {
                if (cbAuto.isChecked()) {
                    bluetooth.sendData(ControlCommands.SPEED_INCREASE);
                    Thread.sleep(ControlCommands.sleepTime1);
                    bluetooth.sendData(ControlCommands.PARAMETERS_QUESTION);
                    setOtherButtons(true, true, true, false, true);
                } else {
                    bluetooth.sendData(ControlCommands.FORWARD);
                    setDirectionalButtons(true, false, true, true, true, true);
                    setOtherButtons(false, true, true, true, false);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                msg("Error");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void backClick(View v) {
        if (bluetooth.isBtConnected()) {
            try {
                if (!cbAuto.isChecked()) {
                    bluetooth.sendData(ControlCommands.REVERSE);
                    setDirectionalButtons(true, true, true, true, false, true);
                    setOtherButtons(false, true, true, true, false);
                } else {
                    bluetooth.sendData(ControlCommands.PARAMETERS_LOAD);
                    Thread.sleep(ControlCommands.sleepTime1);
                    bluetooth.sendData(ControlCommands.PARAMETERS_QUESTION);
                    setOtherButtons(true, true, true, false, true);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                msg("IO Error");
            } catch (InterruptedException e) {
                e.printStackTrace();
                msg("Interrupted Error");
            }
        }
    }

    public void rightClick(View v) {
        if (bluetooth.isBtConnected()) {
            try {
                if (cbAuto.isChecked()) {
                    bluetooth.sendData(ControlCommands.KD_INCREASE);
                    Thread.sleep(ControlCommands.sleepTime1);
                    bluetooth.sendData(ControlCommands.PARAMETERS_QUESTION);
                    setOtherButtons(true, true, true, false, true);
                } else {
                    bluetooth.sendData(ControlCommands.TURNING_RIGHT);
                    setDirectionalButtons(true, true, false, true, true, true);
                    setOtherButtons(false, true, true, true, false);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                msg("Error");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void leftClick(View v) {
        if (bluetooth.isBtConnected()) {
            try {
                if (cbAuto.isChecked()) {
                    bluetooth.sendData(ControlCommands.KP_INCREASE);
                    Thread.sleep(ControlCommands.sleepTime1);
                    bluetooth.sendData(ControlCommands.PARAMETERS_QUESTION);
                    setOtherButtons(true, true, true, false, true);
                } else {
                    bluetooth.sendData(ControlCommands.TURNING_LEFT);
                    setDirectionalButtons(false, true, true, true, true, true);
                    setOtherButtons(false, true, true, true, false);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                msg("Error");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void leftRClick(View v) {
        if (bluetooth.isBtConnected()) {
            try {
                if (!cbAuto.isChecked()) {
                    bluetooth.sendData(ControlCommands.TURNING_LEFT_R);
                    setDirectionalButtons(true, true, true, false, true, true);
                    setOtherButtons(false, true, true, true, false);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                msg("Error");
            }
        }
    }

    public void rightRClick(View v) {
        if (bluetooth.isBtConnected()) {
            try {
                if (!cbAuto.isChecked()) {
                    bluetooth.sendData(ControlCommands.TURNING_RIGHT_R);
                    setDirectionalButtons(true, true, true, true, true, false);
                    setOtherButtons(false, true, true, true, false);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                msg("Error");
            }
        }
    }

    public void startClick(View v) throws IOException {
        chrono.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
        chrono.start();
        if (bluetooth.isBtConnected()) {
            try {
                if (cbAuto.isChecked()) {
                    bluetooth.sendData(ControlCommands.AUTO_START);
                    setOthers(false, true);
                    setDirectionalButtons(false, false, false, true, false, true);
                    setOtherButtons(false, false, false, true, false);
                } else {
                    setOthers(false, true);
                    setDirectionalButtons(true, true, true, true, true, true);
                    setOtherButtons(false, true, true, true, false);
                }
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
                if (cbAuto.isChecked()) {
                    bluetooth.sendData(ControlCommands.STOP);
                    setOthers(true, true);
                    setDirectionalButtons(true, true, true, false, true, false);
                    setOtherButtons(true, true, true, false, true);
                } else {
                    bluetooth.sendData(ControlCommands.STOP);
                    setOthers(true, true);
                    setDirectionalButtons(false, false, false, false, false, false);
                    setOtherButtons(true, true, false, false, true);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                msg("Error");
            }
        }
    }

    public void maxClick(View v) {
        try {
            if (cbAuto.isChecked()) {
                bluetooth.sendData(ControlCommands.WHEELS_CLEANING);
                setOthers(false, true);
                setDirectionalButtons(false, false, false, false, false, false);
                setOtherButtons(false, false, false, true, false);
            } else {
                sbSpeed.setProgress(ControlCommands.SPEED_ARRAY.length - 1);
                tvSpeed.setText(String.format(Locale.getDefault(), "%s%s", getString(R.string.Speed1), Integer.toString(ControlCommands.SPEED_ARRAY.length - 1)));
                bluetooth.sendData(ControlCommands.SPEED_ARRAY[ControlCommands.SPEED_ARRAY.length - 1]);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void pauseClick(View v) {
        if (bluetooth.isBtConnected()) {
            try {
                if (cbAuto.isChecked()) {
                    bluetooth.sendData(ControlCommands.PARAMETERS_SAVE);
                    btnPause.setEnabled(false);
                } else {
                    bluetooth.sendData(ControlCommands.STOP);
                    setDirectionalButtons(true, true, true, true, true, true);
                    btnPause.setEnabled(false);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                msg("Error");
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
                bluetooth.connect();
            }
        } else {
            try {
                bluetooth.turnOnBT();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void led1Click(View v) {
        if (bluetooth.isBtConnected()) {
            try {
                bluetooth.sendData(ControlCommands.LED1);
            } catch (IOException ex) {
                ex.printStackTrace();
                msg("Error");
            }
        }
    }

    public void led2Click(View v) {
        if (bluetooth.isBtConnected()) {
            try {
                bluetooth.sendData(ControlCommands.LED2);
            } catch (IOException ex) {
                ex.printStackTrace();
                msg("Error");
            }
        }
    }

    public void led12Click(View v) {
        if (bluetooth.isBtConnected()) {
            try {
                bluetooth.sendData(ControlCommands.LED12);
            } catch (IOException ex) {
                ex.printStackTrace();
                msg("Error");
            }
        }
    }

    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    private void setOthers(boolean cb, boolean sb) {
        cbAuto.setEnabled(cb);
        sbSpeed.setEnabled(sb);
        sbTurn.setEnabled(sb);
    }

    private void setDirectionalButtons(boolean left, boolean speedUp, boolean right, boolean leftR, boolean speedDown, boolean rightR) {
        btnLeft.setEnabled(left);
        btnSpeedUp.setEnabled(speedUp);
        btnRight.setEnabled(right);
        btnLeftR.setEnabled(leftR);
        btnSpeedDown.setEnabled(speedDown);
        btnRightR.setEnabled(rightR);
    }

    private void setOtherButtons(boolean start, boolean max, boolean pause, boolean stop, boolean connect) {
        btnStart.setEnabled(start);
        btnMax.setEnabled(max);
        btnPause.setEnabled(pause);
        btnStop.setEnabled(stop);
        btnConnect.setEnabled(connect);
    }

    private void setLedsButtons(boolean led1, boolean led2, boolean led12) {
        btnLed1.setEnabled(led1);
        btnLed2.setEnabled(led2);
        btnLed12.setEnabled(led12);
    }

    private void ifConnected() {
        if (bluetooth.isBtConnected()) {
            try {
                final byte delimiter = 13;
                bluetooth.startReceiving(delimiter);
                Thread.sleep(ControlCommands.sleepTime2);
                bluetooth.sendData(ControlCommands.STOP);
                Thread.sleep(ControlCommands.sleepTime1);
                bluetooth.sendData(ControlCommands.SPEED_ARRAY[sbSpeed.getProgress()]);
                Thread.sleep(ControlCommands.sleepTime1);
                bluetooth.sendData(ControlCommands.TURN_ARRAY[sbTurn.getProgress()]);
                Thread.sleep(ControlCommands.sleepTime1);
                bluetooth.sendData(ControlCommands.PARAMETERS_QUESTION);
            } catch (InterruptedException e) {
                e.printStackTrace();
                msg("Interrupted Error");
            } catch (IOException e) {
                e.printStackTrace();
                msg("Send Error");
            }
        }
    }

    @Override
    protected void onDestroy() {
       // Log.i(TAG, "onDestroy()");
        if (bluetooth.isBtConnected()) {
            try {
                bluetooth.sendData(ControlCommands.STOP);
            } catch (IOException ex) {
                ex.printStackTrace();
                msg("Send Error");
            }
        }
        bluetooth.stoppedChangingStatus();
        bluetooth.stoppedReceivingData();
        super.onDestroy();

    }

    @Override
    public void AfterConnecting() {
       // Log.i(TAG, "StatusChanging CONNECTED");
        boolean flag = false;
        while (!flag) {
            if (bluetooth.isBtTurnedOn()) {
                try {
                    Thread.sleep(ControlCommands.sleepTime2);
                    ifConnected();
                    flag = true;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        Thread uiThread = new Thread(new Runnable() {
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnConnect.setText(R.string.Disconnect);
                        if (!cbAuto.isChecked()) {
                            setOthers(true, true);
                            setDirectionalButtons(false, false, false, false, false, false);
                            setOtherButtons(true, true, false, false, true);
                            setLedsButtons(true, true, true);
                        } else {
                            setOthers(true, true);
                            setDirectionalButtons(true, true, true, false, true, false);
                            setOtherButtons(true, true, true, false, true);
                            setLedsButtons(true, true, true);
                        }
                        sbTurn.setProgress(3);
                        sbSpeed.setProgress(3);
                        tvTurn.setText(String.format(Locale.getDefault(), "%s%s", getString(R.string.Turn1), Integer.toString(sbTurn.getProgress())));
                        tvSpeed.setText(String.format(Locale.getDefault(), "%s%s", getString(R.string.Speed1), Integer.toString(sbSpeed.getProgress())));
                    }
                });
            }
        });
        uiThread.start();
    }

    @Override
    public void AfterDisconnecting() {
       // Log.i(TAG, "StatusChanging DISCONNECTED");
        Thread uiThread = new Thread(new Runnable() {
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnConnect.setText(R.string.Connect);
                        setOthers(false, false);
                        setDirectionalButtons(false, false, false, false, false, false);
                        setOtherButtons(false, false, false, false, true);
                        setLedsButtons(false, false, false);
                        chrono.stop();
                    }
                });
            }
        });
        uiThread.start();
    }

    @Override
    public void AfterTurningOnBluetooth() {
        //Log.i(TAG, "StatusChanging BT_ON");
    }

    @Override
    public void AfterTurningOffBluetooth() {
        //Log.i(TAG, "StatusChanging BT_OFF");
        Thread uiThread = new Thread(new Runnable() {
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnConnect.setText(R.string.TurnOnBt);
                        setOthers(false, false);
                        setDirectionalButtons(false, false, false, false, false, false);
                        setOtherButtons(false, false, false, false, true);
                        setLedsButtons(false, false, false);
                    }
                });
            }
        });
        uiThread.start();
    }

    @Override
    public void AfterReceivingData(final String data) {
       // Log.i(TAG, "StatusChanging RECEIVED");
        if (data.length() == 15) {
            Thread uiThread = new Thread(new Runnable() {
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int tmp = Integer.parseInt(data.substring(0, 5));
                            textLeft.setText(String.valueOf(tmp));
                            tmp = Integer.parseInt(data.substring(5, 10));
                            textRight.setText(String.valueOf(tmp));
                            tmp = Integer.parseInt(data.substring(10, 15));
                            textSpeed.setText(String.valueOf(tmp));
                        }
                    });
                }
            });
            uiThread.start();
        }
    }
}
