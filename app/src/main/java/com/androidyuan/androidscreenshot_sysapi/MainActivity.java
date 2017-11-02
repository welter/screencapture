package com.androidyuan.androidscreenshot_sysapi;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import com.androidyuan.lib.screenshot.ScreenShotActivity;
import com.androidyuan.lib.screenshot.ScreenShotService;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
    }

    public void onClickShot(View view)
    {

        startActivity(new Intent(this, ScreenShotActivity.class));

    }
    public void onClickStop(View view)
    {
        Intent intent=new Intent(this, ScreenShotService.class);
        stopService(intent);
    }
}
