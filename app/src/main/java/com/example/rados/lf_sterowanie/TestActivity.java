package com.example.rados.lf_sterowanie;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.AsyncTask;
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

public class TestActivity extends AppCompatActivity {
    public MyBluetooth bluetooth=new MyBluetooth();
    private ProgressDialog progress;
    public boolean auto=false;
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
            try
            {
                bluetooth.write("7");
            }
            catch (IOException e)
            {
                msg("Error");
            }
    }

    public void stopClick(View v) {
        chrome.stop();
        timeWhenStopped=0;
        if(bluetooth.isBtConnected)
        {
            try
            {
                bluetooth.write("0");
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }

    }

    public void maxClick(View v){

    }

    public void pauseClick(View v){
        timeWhenStopped = chrome.getBase() - SystemClock.elapsedRealtime();
        chrome.stop();
    }

    public void connectClick(View v) throws IOException{
        new ConnectBT().execute();
    }

    public void led1Click(View v){

    }

    public void led2Click(View v){

    }

    public void led12Click(View v){

    }


    private void Disconnect() throws IOException
    {
        try
        {
            bluetooth.Disconnect();
        }
        catch (IOException e)
        {
            msg("Error");
        }
        finish(); //return to the first layout
    }

    // fast way to call Toast
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }


















    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(TestActivity.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (bluetooth.btSocket == null || !bluetooth.isBtConnected)
                {
                    bluetooth.myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = bluetooth.myBluetooth.getRemoteDevice(bluetooth.address);//connects to the device's address and checks if it's available
                    bluetooth.btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(bluetooth.myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    bluetooth.btSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            }
            else
            {
                msg("Connected.");
                bluetooth.isBtConnected = true;
            }
            progress.dismiss();
        }
    }
}
