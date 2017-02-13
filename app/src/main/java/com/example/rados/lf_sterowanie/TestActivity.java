package com.example.rados.lf_sterowanie;

import android.content.pm.ActivityInfo;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;


//TODO: Dodanie kalibracji (sprawdzenie czy nie jest w eeprom)
public class TestActivity extends AppCompatActivity implements MyBluetooth.IMyBluetooth {
    public MyBluetooth bluetooth;
    public boolean auto=false;
    private long timeWhenStopped=0;
    private final String[] turnArray={"f","g","h","j","k","l"};
    private final String[] speedArray={"\\","r","t","y","u","i","o","p","[","]"};
    MyBluetooth.IUpdateUiAfterReceivingData afterReceivingData=null;
    MyBluetooth.IUpdateAfterChangingBluetoothStatus afterChangingStatus=null;
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

        textRight=(TextView)findViewById(R.id.textRight);
        textLeft=(TextView)findViewById(R.id.textLeft);
        textSpeed=(TextView)findViewById(R.id.textSpeed);
        info=(TextView)findViewById(R.id.textView);

        tvSpeed=(TextView)findViewById(R.id.tvSpeed);
        tvTurn=(TextView)findViewById(R.id.tvTurn);

        cbAuto=(CheckBox)findViewById(R.id.cbAuto);

        sbSpeed=(SeekBar)findViewById(R.id.sbSpeed);
        sbSpeed.setMax(9);
        sbSpeed.setProgress(3);
        tvSpeed.setText("SPEED = "+sbSpeed.getProgress());
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
                tvSpeed.setText("SPEED = "+progress);
                if(bluetooth.isBtConnected()) {
                    try {
                        bluetooth.sendData(speedArray[progress]);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        msg("Error");
                    }
                }
            }
        });

        sbTurn=(SeekBar)findViewById(R.id.sbTurn);
        sbTurn.setMax(5);
        sbTurn.setProgress(3);
        tvTurn.setText("TURN = "+sbTurn.getProgress());
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
                tvTurn.setText("TURN = "+progress);
                if(bluetooth.isBtConnected()) {
                    try {
                        bluetooth.sendData(turnArray[progress]);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        msg("Error");
                    }
                }
            }
        });

        chrono =(chronometer.Chronometer)findViewById(R.id.chronometer);

        btnLeftR=(Button)findViewById(R.id.btnLeftR);
        btnRightR=(Button)findViewById(R.id.btnRightR);
        btnLeft=(Button)findViewById(R.id.btnLeft);
        btnRight=(Button)findViewById(R.id.btnRight);
        btnSpeedUp=(Button)findViewById(R.id.btnSpeedUp);
        btnSpeedDown=(Button)findViewById(R.id.btnSpeedDown);
        btnStart=(Button)findViewById(R.id.btnStart);
        btnMax=(Button)findViewById(R.id.btnMax);
        btnPause=(Button)findViewById(R.id.btnPause);
        btnStop=(Button)findViewById(R.id.btnStop);
        btnConnect=(Button)findViewById(R.id.btnConnect);

        btnLed1=(Button)findViewById(R.id.btnLED1);
        btnLed2=(Button)findViewById(R.id.btnLED2);
        btnLed12=(Button)findViewById(R.id.btnLED12);

        textLeft.setVisibility(View.INVISIBLE);
        textRight.setVisibility(View.INVISIBLE);
        textSpeed.setVisibility(View.INVISIBLE);
        chrono.setText("00:00:0");
        info.setText("Sterowanie manualne");

        afterReceivingData=new MyBluetooth.IUpdateUiAfterReceivingData() {
            @Override
            public void updateUI(String data) {
                if(data.length()==16){
                    textLeft.setText(Integer.parseInt(data.substring(0,5)));
                    textRight.setText(Integer.parseInt(data.substring(5,10)));
                    textSpeed.setText(Integer.parseInt(data.substring(10,15)));
                }
            }
        };

        afterChangingStatus=new MyBluetooth.IUpdateAfterChangingBluetoothStatus() {
            @Override
            public void updateUI() {
                if (bluetooth.isBtTurnedOn()) {
                    if (bluetooth.isBtConnected()) {
                        btnConnect.setText("DISCONNECT");
                        setOthers(true,true);
                        setDirectionalButtons(false,false,false,false,false,false);
                        setOtherButtons(true,true,false,false,true);
                        setLedsButtons(true,true,true);
                        sbTurn.setProgress(3);
                        sbSpeed.setProgress(3);
                        tvTurn.setText("TURN = "+sbTurn.getProgress());
                        tvSpeed.setText("SPEED = "+sbSpeed.getProgress());
                    } else {
                        btnConnect.setText("CONNECT");
                        setOthers(false,false);
                        setDirectionalButtons(false,false,false,false,false,false);
                        setOtherButtons(false,false,false,false,true);
                        setLedsButtons(false,false,false);
                    }
                } else {
                    btnConnect.setText("TURN ON BLUETOOTH");
                    setOthers(false,false);
                    setDirectionalButtons(false,false,false,false,false,false);
                    setOtherButtons(false,false,false,false,true);
                    setLedsButtons(false,false,false);
                }
            }
        };

        bluetooth=new MyBluetooth(TestActivity.this,getApplicationContext(),"00:12:6F:6B:C0:A2",afterReceivingData, afterChangingStatus);
        ifConnected();
    }

    public void changedClick(View v){
        if(cbAuto.isChecked()){
            info.setText("Sterowanie automatyczne");
            auto=true;
            textLeft.setVisibility(View.VISIBLE);
            textRight.setVisibility(View.VISIBLE);
            textSpeed.setVisibility(View.VISIBLE);
            sbSpeed.setVisibility(View.INVISIBLE);
            sbTurn.setVisibility(View.INVISIBLE);
            tvSpeed.setVisibility(View.INVISIBLE);
            tvTurn.setVisibility(View.INVISIBLE);
            btnLeftR.setText(R.string.KP2);
            btnRightR.setText(R.string.KD2);
            btnSpeedDown.setText("SPEED-");
            btnLeftR.setVisibility(View.INVISIBLE);
            btnRightR.setVisibility(View.INVISIBLE);
            btnSpeedDown.setVisibility(View.INVISIBLE);
            btnLeft.setText(R.string.KP1);
            btnRight.setText(R.string.KD1);
            btnSpeedUp.setText(R.string.SPEED1);
            btnMax.setText(R.string.CLEAR);
            btnPause.setText(R.string.SAVE);
            if(bluetooth.isBtConnected()) {
                try {
                    bluetooth.sendData("?");
                } catch (IOException ex) {
                    ex.printStackTrace();
                    msg("Error");
                }
            }
            setOthers(true,false);
            setDirectionalButtons(true,true,true,false,false,false);
            setOtherButtons(true,true,true,false,true);
        }else{
            info.setText("Sterowanie manualne");
            auto=false;
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
            btnMax.setText(R.string.Max);
            btnPause.setText(R.string.Pauza);
            btnLeftR.setVisibility(View.VISIBLE);
            btnRightR.setVisibility(View.VISIBLE);
            btnSpeedDown.setVisibility(View.VISIBLE);
            setOthers(true,true);
            setDirectionalButtons(false,false,false,false,false,false);
            setOtherButtons(true,true,false,false,true);
        }
    }

    public void topClick(View v){
        if(bluetooth.isBtConnected()) {
            try {
                if (cbAuto.isChecked()) {
                    bluetooth.sendData(",");
                    bluetooth.sendData("?");
                    setOtherButtons(true,true,true,false,true);
                } else {
                    bluetooth.sendData("w");
                    setDirectionalButtons(true,false,true,true,true,true);
                    setOtherButtons(false,true,true,true,false);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                msg("Error");
            }
        }
    }
    public void backClick(View v){
        if(bluetooth.isBtConnected()) {
            try {
                if (cbAuto.isChecked()) {
                    //bluetooth.sendData("");
                } else {
                    bluetooth.sendData("s");
                    setDirectionalButtons(true,true,true,true,false,true);
                    setOtherButtons(false,true,true,true,false);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                msg("Error");
            }
        }
    }
    public void rightClick(View v){
        if(bluetooth.isBtConnected()) {
            try {
                if (cbAuto.isChecked()) {
                    bluetooth.sendData("+");
                    bluetooth.sendData("?");
                    setOtherButtons(true,true,true,false,true);
                } else {
                    bluetooth.sendData("e");
                    setDirectionalButtons(true,true,false,true,true,true);
                    setOtherButtons(false,true,true,true,false);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                msg("Error");
            }
        }
    }
    public void leftClick(View v){
        if(bluetooth.isBtConnected()) {
            try {
                if (cbAuto.isChecked()) {
                    bluetooth.sendData("-");
                    bluetooth.sendData("?");
                    setOtherButtons(true,true,true,false,true);
                } else {
                    bluetooth.sendData("q");
                    setDirectionalButtons(false,true,true,true,true,true);
                    setOtherButtons(false,true,true,true,false);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                msg("Error");
            }
        }
    }
    public void leftRClick(View v){
        if(bluetooth.isBtConnected()) {
            try {
                if (cbAuto.isChecked()) {
                   // bluetooth.sendData("");
                } else {
                    bluetooth.sendData("a");
                    setDirectionalButtons(true,true,true,false,true,true);
                    setOtherButtons(false,true,true,true,false);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                msg("Error");
            }
        }
    }

    public void rightRClick(View v){
        if(bluetooth.isBtConnected()) {
            try {
                if (cbAuto.isChecked()) {

                } else {
                    bluetooth.sendData("d");
                    setDirectionalButtons(true,true,true,true,true,false);
                    setOtherButtons(false,true,true,true,false);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                msg("Error");
            }
        }
    }

    public void startClick(View v)throws IOException{
        chrono.setBase(SystemClock.elapsedRealtime()+timeWhenStopped);
        chrono.start();
        if(bluetooth.isBtConnected()) {
            try {
                if (cbAuto.isChecked()) {
                    bluetooth.sendData("8");
                    setOthers(false,true);
                    setDirectionalButtons(false,false,false,true,true,true);
                    setOtherButtons(false,false,false,true,false);
                } else {
                    setOthers(false,true);
                    setDirectionalButtons(true,true,true,true,true,true);
                    setOtherButtons(false,true,true,true,false);
                   // bluetooth.sendData("7");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                msg("Error");
            }
        }
    }

    public void stopClick(View v) {
        chrono.stop();
        timeWhenStopped=0;
        if(bluetooth.isBtConnected()) {
            try {
                if (cbAuto.isChecked()) {
                    bluetooth.sendData("0");
                    setOthers(true,true);
                    setDirectionalButtons(true,true,true,false,false,false);
                    setOtherButtons(true,true,true,false,true);
                } else {
                    bluetooth.sendData("x");
                    setOthers(true,true);
                    setDirectionalButtons(false,false,false,false,false,false);
                    setOtherButtons(true,true,false,false,true);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                msg("Error");
            }
        }
    }

    public void maxClick(View v){
        try
        {
            if(cbAuto.isChecked()){
                bluetooth.sendData("7");
                setOthers(false,true);
                setDirectionalButtons(false,false,false,false,false,false);
                setOtherButtons(false,false,false,true,false);
            }else{
                sbSpeed.setProgress(speedArray.length-1);
                tvSpeed.setText("SPEED = "+ (speedArray.length-1));
                bluetooth.sendData(speedArray[speedArray.length-1]);
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void pauseClick(View v){
        if(bluetooth.isBtConnected()) {
            try {
                if (cbAuto.isChecked()) {
                    bluetooth.sendData("9");
                    btnPause.setEnabled(false);
                } else {
                    bluetooth.sendData("x");
                    setDirectionalButtons(true,true,true,true,true,true);
                    btnPause.setEnabled(false);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                msg("Error");
            }
        }
    }

    public void connectClick(View v) {
        if(bluetooth.isBtTurnedOn()) {
            if (bluetooth.isBtConnected()) {
                try {
                    bluetooth.disconnect();
                }catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                try {
                    bluetooth.connect(this);
                }catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }else {
            try {
                bluetooth.turnOnBT();
            }catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void led1Click(View v){
        if(bluetooth.isBtConnected()) {
            try {
                bluetooth.sendData("b");
            } catch (IOException ex) {
                ex.printStackTrace();
                msg("Error");
            }
        }
    }

    public void led2Click(View v){
        if(bluetooth.isBtConnected()) {
            try {
                bluetooth.sendData("n");
            } catch (IOException ex) {
                ex.printStackTrace();
                msg("Error");
            }
        }
    }

    public void led12Click(View v){
        if(bluetooth.isBtConnected()) {
            try {
                bluetooth.sendData("m");
            } catch (IOException ex) {
                ex.printStackTrace();
                msg("Error");
            }
        }
    }

    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    private void setOthers(boolean cb, boolean sb){
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

    private void setOtherButtons(boolean start, boolean max, boolean pause, boolean stop, boolean connect){
        btnStart.setEnabled(start);
        btnMax.setEnabled(max);
        btnPause.setEnabled(pause);
        btnStop.setEnabled(stop);
        btnConnect.setEnabled(connect);
    }

    private void setLedsButtons(boolean led1, boolean led2, boolean led12){
        btnLed1.setEnabled(led1);
        btnLed2.setEnabled(led2);
        btnLed12.setEnabled(led12);
    }

    private void ifConnected(){
        if (bluetooth.isBtConnected()) {
            try {
                bluetooth.clean();
                bluetooth.sendData(speedArray[sbSpeed.getProgress()]);
                bluetooth.sendData(turnArray[sbTurn.getProgress()]);
            } catch (IOException ex) {
                ex.printStackTrace();
                msg("Error");
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(bluetooth.isBtConnected()) {
            try {
                bluetooth.sendData("0");
            } catch (IOException ex) {
                ex.printStackTrace();
                msg("Error");
            }
        }
        finish();
        return;
    }

    @Override
    public void TaskCompletionResult(String msg){
        boolean flag=false;
        while(!flag) {
            if (bluetooth.isBtTurnedOn()) {
                try{
                    Thread.sleep(100);
                    ifConnected();
                    flag=true;
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
