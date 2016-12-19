package com.example.rados.lf_sterowanie;

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
//TODO: Sprawdzanie czy BT jest włączone
public class TestActivity extends AppCompatActivity {
    public MyBluetooth bluetooth=null;
    public boolean auto=false;
    public boolean btIsOn=true;
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
    CheckBox cbAuto;
    SeekBar sbSpeed;
    SeekBar sbTurn;
    chronometer.Chronometer chrome;
    int speed=0;
    int RCurve=0;
    int LCurve=0;
    private long timeWhenStopped=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        textRight=(TextView)findViewById(R.id.textRight);
        textLeft=(TextView)findViewById(R.id.textLeft);
        textSpeed=(TextView)findViewById(R.id.textSpeed);
        info=(TextView)findViewById(R.id.textView);
        tvSpeed=(TextView)findViewById(R.id.tvSpeed);
        tvTurn=(TextView)findViewById(R.id.tvTurn);

        cbAuto=(CheckBox)findViewById(R.id.cbAuto);

        sbSpeed=(SeekBar)findViewById(R.id.sbSpeed);
        sbTurn=(SeekBar)findViewById(R.id.sbTurn);

        chrome=(chronometer.Chronometer)findViewById(R.id.chronometer);

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

        textLeft.setVisibility(View.INVISIBLE);
        textRight.setVisibility(View.INVISIBLE);
        textSpeed.setVisibility(View.INVISIBLE);
        chrome.setText("00:00:0");
        info.setText("Sterowanie manualne");

        bluetooth=new MyBluetooth(TestActivity.this,getApplicationContext(),"00:12:6F:6B:C0:A2");

        if(btIsOn) {
            if (bluetooth.isBtConnected) {
                btnConnect.setText("DISCONNECT");
            } else {
                btnConnect.setText("CONNECT");
            }
        }else{
            btnConnect.setText("TURN ON BLUETOOTH");
        }

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
            btnLeft.setText(R.string.KP1);
            btnRight.setText(R.string.KD1);
            btnSpeedUp.setText(R.string.SPEED1);
            btnSpeedDown.setText(R.string.SPEED2);
            btnMax.setText(R.string.CLEAR);
            btnPause.setText(R.string.SAVE);
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
        }
    }

    public void topClick(View v){
        speed++;
        RCurve++;
        LCurve++;
        textRight.setText(String.valueOf(RCurve));
        textLeft.setText(String.valueOf(LCurve));
    }
    public void backClick(View v){
        speed--;
        RCurve--;
        LCurve--;
        textRight.setText(String.valueOf(RCurve));
        textLeft.setText(String.valueOf(LCurve));
    }
    public void rightClick(View v){
        RCurve++;
        LCurve--;
        textRight.setText(String.valueOf(RCurve));
        textLeft.setText(String.valueOf(LCurve));
    }
    public void leftClick(View v){
        RCurve--;
        LCurve++;
        textRight.setText(String.valueOf(RCurve));
        textLeft.setText(String.valueOf(LCurve));
    }
    public void leftRClick(View v){

    }

    public void rightRClick(View v){

    }

    public void startClick(View v)throws IOException{
        chrome.setBase(SystemClock.elapsedRealtime()+timeWhenStopped);
        chrome.start();
        if(bluetooth.isBtConnected)
                bluetooth.write("7");
    }

    public void stopClick(View v) {
        chrome.stop();
        timeWhenStopped=0;
        if(bluetooth.isBtConnected) {
                bluetooth.write("0");
        }

    }

    public void maxClick(View v){

    }

    public void pauseClick(View v){
        timeWhenStopped = chrome.getBase() - SystemClock.elapsedRealtime();
        chrome.stop();
    }

    public void connectClick(View v) {
        if(btIsOn) {
            if (bluetooth.isBtConnected) {
                bluetooth.Disconnect();
                btnConnect.setText("CONNECT");
            } else {
                bluetooth.Connect();
                btnConnect.setText("DISCONNECT");
            }
        }else{
            //TODO: tak żeby odpalało BT
        }
    }

    public void led1Click(View v){

    }

    public void led2Click(View v){

    }

    public void led12Click(View v){

    }


    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }



















}
