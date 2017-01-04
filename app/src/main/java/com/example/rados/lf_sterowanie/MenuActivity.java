package com.example.rados.lf_sterowanie;
        import android.content.Intent;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.view.View;

public class MenuActivity extends AppCompatActivity {
    public static MyBluetooth bluetooth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        if(bluetooth==null){
            bluetooth=new MyBluetooth(MenuActivity.this,getApplicationContext(),"00:12:6F:6B:C0:A2");
        }else {
            bluetooth.setActivityAnDContext(MenuActivity.this,getApplicationContext());
        }

        bluetooth.updateUiAfterChangingBluetoothStatus(new MyBluetooth.IUpdateUiAfterChangingBluetoothStatus() {
            @Override
            public void updateUI() { }
        });

    }

    public void bntClick(View view) {
        startActivity(new Intent(MenuActivity.this,TestActivity.class));
    }
}