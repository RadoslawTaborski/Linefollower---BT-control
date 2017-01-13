package com.example.rados.lf_sterowanie;

import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import java.io.IOException;

public class MotionControlActivity extends AppCompatActivity implements ITaskDelegate, SensorEventListener{
    private MyBluetooth bluetooth;
    Button btnStart;
    Button btnConnect;
    ToggleButton btnR;
    TextView tvSpeed;
    SeekBar sbSpeed;
    private Sensor mySensor;
    private SensorManager SM;

    private boolean started=false;
    private boolean checked=false;
    private final String[] turnArray={"f","g","h","j","k","l"};
    private final float[] yValue ={2.0f,3.33f,4.66f,6.0f,7.33f,8.66f,10.0f};
    private final float zRange=6.0f;
    private final String[] speedArray={"\\","r","t","y","u","i","o","p","[","]"};
    private TextView xText;
    private TextView yText;
    private TextView zText;
    String last ="";
    String lastDirection ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motion_control);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        btnStart=(Button)findViewById(R.id.btnStart3);
        btnR=(ToggleButton)findViewById(R.id.tbtnR);
        btnR.setChecked(false);
        btnR.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    checked=true;
                } else {
                    checked=false;
                }
            }
        });
        btnConnect=(Button)findViewById(R.id.btnConnect3);
        sbSpeed=(SeekBar)findViewById(R.id.sbSpeed3);
        tvSpeed=(TextView)findViewById(R.id.tvValue);

        SM = (SensorManager)getSystemService(SENSOR_SERVICE);       // Accelerometer Sensor
        mySensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);        // Register sensor Listener
        SM.registerListener(this, mySensor, SensorManager.SENSOR_DELAY_NORMAL);
        xText = (TextView)findViewById(R.id.xText);
        yText = (TextView)findViewById(R.id.yText);
        zText = (TextView)findViewById(R.id.zText);

        sbSpeed.setMax(9);
        sbSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;
                tvSpeed.setText(String.valueOf(progress));
                float x = (sbSpeed.getThumb().getBounds().centerX()+sbSpeed.getX());
                tvSpeed.setX(x);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                tvSpeed.setText(String.valueOf(progress));
                float x = (sbSpeed.getThumb().getBounds().centerX()+sbSpeed.getX());
                tvSpeed.setX(x);
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
        sbSpeed.setProgress(3);
        tvSpeed.setText(String.valueOf(sbSpeed.getProgress()));
        float x = (sbSpeed.getThumb().getBounds().centerX()+sbSpeed.getX());
        tvSpeed.setX(x);

        MyBluetooth.IUpdateUiAfterReceivingData update=new MyBluetooth.IUpdateUiAfterReceivingData() {
            @Override
            public void updateUI(String data) {
            }
        };
        bluetooth=new MyBluetooth(MotionControlActivity.this,getApplicationContext(),"00:12:6F:6B:C0:A2",update);
        if(bluetooth.isBtConnected())
            afterConnecting();

        bluetooth.updateUiAfterChangingBluetoothStatus(new MyBluetooth.IUpdateAfterChangingBluetoothStatus() {
            @Override
            public void updateUI() {
                if (bluetooth.isBtTurnedOn()) {
                    if (bluetooth.isBtConnected()) {
                        btnConnect.setText("DISCONNECT");
                        sbSpeed.setProgress(3);
                        btnStart.setEnabled(true);
                        btnR.setEnabled(true);
                        sbSpeed.setEnabled(true);
                    } else {
                        btnConnect.setText("CONNECT");
                        btnStart.setEnabled(false);
                        btnR.setEnabled(false);
                        sbSpeed.setEnabled(false);
                    }
                } else {
                    btnConnect.setText("TURN ON BLUETOOTH");
                    btnStart.setEnabled(false);
                    btnR.setEnabled(false);
                    sbSpeed.setEnabled(false);
                }
            }
        });
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

    public void startClick(View v){
        if(started){
            lastDirection=send("0");
            btnStart.setText("START");
            started=false;
        } else {
            btnStart.setText("STOP");
            started=true;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not in use
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x=event.values[0];
        float y=event.values[1];
        float z=event.values[2];
        xText.setText("X: " + x);
        yText.setText("Y: " + y);
        zText.setText("Z: " + z);

        if(z > -zRange && z < zRange && started) {
            if (!checked) {
                for(int i=0; i<6;++i){
                    if(y>=yValue[i] && y<yValue[i+1] && (last!=turnArray[i] || lastDirection!="e")){
                        last=send(turnArray[i]);
                        lastDirection=send("e");
                        break;
                    }
                }
                for(int i=0; i<6;++i){
                    if(y<=-yValue[i] && y>-yValue[i+1] && (last!=turnArray[i] || lastDirection!="q")){
                        last=send(turnArray[i]);
                        lastDirection=send("q");
                        break;
                    }
                }
                 if(y>=-yValue[0] && y<= yValue[0] && lastDirection!="w"){
                    lastDirection=send("w");
                }
            } else {
                for(int i=0; i<6;++i){
                    if(y>=yValue[i] && y<yValue[i+1] && (last!=turnArray[i] || lastDirection!="a")){
                        last=send(turnArray[i]);
                        lastDirection=send("a");
                        break;
                    }
                }
                for(int i=0; i<6;++i){
                    if(y<=-yValue[i] && y>-yValue[i+1] && (last!=turnArray[i] || lastDirection!="d")){
                        last=send(turnArray[i]);
                        lastDirection=send("d");
                        break;
                    }
                }
                if(y>=-yValue[0] && y<= yValue[0] && lastDirection!="s"){
                    lastDirection=send("s");
                }
            }
        }else if(lastDirection!="0"){
            lastDirection=send("0");
        }
    }

    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    public String send(String msg){
        String result="";
        if(bluetooth.isBtConnected()) {
            try {
                bluetooth.clean();
                bluetooth.sendData(msg);
                result=msg;
            } catch (IOException ex) {
                ex.printStackTrace();
                msg("Error");
            }
        }
        return result;
    }

    @Override
    public void onBackPressed() {
        send("0");
        finish();
        return;
    }

    @Override
    protected void onResume() {
        super.onResume();
        final int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(uiOptions);
    }

    private void afterConnecting(){
        send(speedArray[sbSpeed.getProgress()]);
    }

    @Override
    public void TaskCompletionResult(String msg){
        boolean flag=false;
        while(!flag) {
            if (bluetooth.isBtTurnedOn()) {
                try{
                    Thread.sleep(100);
                    afterConnecting();
                    flag=true;
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
