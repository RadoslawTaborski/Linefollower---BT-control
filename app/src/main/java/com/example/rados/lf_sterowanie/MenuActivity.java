package com.example.rados.lf_sterowanie;
        import android.content.Intent;
        import android.content.pm.ActivityInfo;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.view.View;

public class MenuActivity extends AppCompatActivity {
    public MyBluetooth bluetooth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if(MyBluetooth.isEmpty()){
            bluetooth=new MyBluetooth(MenuActivity.this,getApplicationContext(),"00:12:6F:6B:C0:A2",null,null);
        }else {
            bluetooth.setActivityAndContext(MenuActivity.this,getApplicationContext());
        }

        bluetooth.updateUiAfterChangingBluetoothStatus();
    }

    public void testClick(View view) {
        startActivity(new Intent(MenuActivity.this,TestActivity.class));
    }

    public void competitionClick(View view) {
        startActivity(new Intent(MenuActivity.this,CompetitionActivity.class));
    }

    public void motionControlClick(View view) {
        startActivity(new Intent(MenuActivity.this,MotionControlActivity.class));
    }
}