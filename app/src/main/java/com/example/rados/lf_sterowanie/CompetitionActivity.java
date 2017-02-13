package com.example.rados.lf_sterowanie;

import android.content.pm.ActivityInfo;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class CompetitionActivity extends AppCompatActivity implements MyBluetooth.IMyBluetooth{
    public MyBluetooth bluetooth;
    MyBluetooth.IUpdateUiAfterReceivingData afterReceivingData=null;
    MyBluetooth.IUpdateAfterChangingBluetoothStatus afterChangingStatus=null;
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
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

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


        afterReceivingData =new MyBluetooth.IUpdateUiAfterReceivingData() {
            @Override
            public void updateUI(String data) {
                if(data.length()==16){
                    tvKP.setText(Integer.parseInt(data.substring(0,5)));
                    tvKD.setText(Integer.parseInt(data.substring(5,10)));
                    tvKP.setText(Integer.parseInt(data.substring(10,15)));
                }else {
                    if(data.charAt(data.length())=='\r')
                        editText.setText(data);
                }
            }
        };

        afterChangingStatus=new MyBluetooth.IUpdateAfterChangingBluetoothStatus() {
            @Override
            public void updateUI() {
                if (bluetooth.isBtTurnedOn()) {
                    if (bluetooth.isBtConnected()) {
                        btnConnect.setText("DISCONNECT");
                        btnStart.setEnabled(true);
                        btnStop.setEnabled(false);
                        btnClear.setEnabled(true);
                        btnRead.setEnabled(false);
                        btnRead2.setEnabled(false);
                        btnConnect.setEnabled(true);
                    } else {
                        btnConnect.setText("CONNECT");
                        btnStart.setEnabled(false);
                        btnStop.setEnabled(false);
                        btnClear.setEnabled(false);
                        btnRead.setEnabled(false);
                        btnRead2.setEnabled(false);
                        btnConnect.setEnabled(true);
                    }
                } else {
                    btnConnect.setText("TURN ON BLUETOOTH");
                    btnStart.setEnabled(false);
                    btnStop.setEnabled(false);
                    btnClear.setEnabled(false);
                    btnRead.setEnabled(false);
                    btnRead2.setEnabled(false);
                    btnConnect.setEnabled(true);
                }
            }
        };

        bluetooth=new MyBluetooth(CompetitionActivity.this,getApplicationContext(),"00:12:6F:6B:C0:A2",afterReceivingData,afterChangingStatus);
        if(bluetooth.isBtConnected())
            whenConnected();

    }

    public void startClick(View v) {
        chrono.setBase(SystemClock.elapsedRealtime()+timeWhenStopped);
        chrono.start();
        if(bluetooth.isBtConnected()) {
            try {
                bluetooth.sendData("8");
                btnStart.setEnabled(false);
                btnStop.setEnabled(true);
                btnClear.setEnabled(false);
                btnRead.setEnabled(true);
                btnRead2.setEnabled(false);
                btnConnect.setEnabled(false);
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
        if(bluetooth.isBtConnected()) {
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
        if(bluetooth.isBtConnected()) {
            try {
                bluetooth.sendData("3");
                btnStart.setEnabled(false);
                btnStop.setEnabled(true);
                btnClear.setEnabled(false);
                btnRead.setEnabled(false);
                btnRead2.setEnabled(true);
                btnConnect.setEnabled(false);
            } catch (IOException ex) {
                ex.printStackTrace();
                msg("Error");
            }
        }
    }

    public void read2Click(View v) { //TODO: zatrzymywanie odczytu
        if(bluetooth.isBtConnected()) {
            try {
                bluetooth.sendData("z");
                btnStart.setEnabled(false);
                btnStop.setEnabled(true);
                btnClear.setEnabled(false);
                btnRead.setEnabled(true);
                btnRead2.setEnabled(false);
                btnConnect.setEnabled(false);
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

    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
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

    private void whenConnected(){
        if (bluetooth.isBtConnected()) {
            try {
                bluetooth.clean();
                bluetooth.sendData("?");
            } catch (IOException ex) {
                ex.printStackTrace();
                msg("Error");
            }
        }
    }

    @Override
    public void TaskCompletionResult(String msg){
        boolean flag=false;
        while(!flag) {
            if (bluetooth.isBtTurnedOn()) {
                try{
                    Thread.sleep(100);
                    whenConnected();
                    flag=true;
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
