package com.example.rados.lf_sterowanie;

import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;

public class CompetitionActivity extends AppCompatActivity {
    public MyBluetooth bluetooth;
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
    EditText editText;
    private long timeWhenStopped=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_competition);

        tvSpeed=(TextView)findViewById(R.id.tvSpeed2);
        tvKP=(TextView)findViewById(R.id.tvKP2);
        tvKD=(TextView)findViewById(R.id.tvKD2);
        btnStart=(Button)findViewById(R.id.btnStart2);
        btnStop=(Button)findViewById(R.id.btnStop2);
        btnClear=(Button)findViewById(R.id.btnClear2);
        btnRead=(Button)findViewById(R.id.btnRead);
        btnRead2=(Button)findViewById(R.id.btnRead2);
        btnConnect=(Button)findViewById(R.id.btnConnect2);
        chrono=(chronometer.Chronometer)findViewById(R.id.chronometer2);
        editText=(EditText)findViewById(R.id.editText);

        MyBluetooth.IUpdateUiAfterReceivingData update=new MyBluetooth.IUpdateUiAfterReceivingData() {
            @Override
            public void updateUI(String data) {
                editText.setText(data);
            }
        };

        if(bluetooth==null){
            bluetooth=new MyBluetooth(CompetitionActivity.this,getApplicationContext(),"00:12:6F:6B:C0:A2",update);
        }else {
            bluetooth.setActivityAnDContext(CompetitionActivity.this,getApplicationContext(),update);
        }

        bluetooth.updateUiAfterChangingBluetoothStatus(new MyBluetooth.IUpdateUiAfterChangingBluetoothStatus() {
            @Override
            public void updateUI() {
                if (bluetooth.isBtTurnedOn()) {
                    if (bluetooth.isBtConnected()) {
                        btnConnect.setText("DISCONNECT");
                    } else {
                        btnConnect.setText("CONNECT");
                    }
                } else {
                    btnConnect.setText("TURN ON BLUETOOTH");
                }
            }
        });

    }

    public void startClick(View v) {
        chrono.setBase(SystemClock.elapsedRealtime()+timeWhenStopped);
        chrono.start();
        if(bluetooth.isBtConnected())
            try
            {
                bluetooth.sendData("7");
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
    }

    public void stopClick(View v) {
        chrono.stop();
        timeWhenStopped=0;
        if(bluetooth.isBtConnected()) {
            try
            {
                bluetooth.sendData("0");
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void clearClick(View v) {

    }

    public void readClick(View v) {

    }

    public void read2Click(View v) {

    }

    public void connectClick(View v) {
        if(bluetooth.isBtTurnedOn()) {
            if (bluetooth.isBtConnected()) {
                try
                {
                    bluetooth.disconnect();
                }
                catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                try
                {
                    bluetooth.connect();
                }
                catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }else{
            try
            {
                bluetooth.turnOnBT();
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
