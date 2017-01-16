package com.embux.dolua;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {
    public static final String TAG="DoLua-MainWindow";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Writer writer=new BufferedWriter(
                            new OutputStreamWriter(new FileOutputStream(
                                "/sdcard/test.lua"
                            ), "utf-8"));
                    writer.write("for i=1,10 do\n");
                    writer.write("print(\"test: \", i)\n");
                    writer.write("end\n");
                    writer.write("wait(3000)\n");
                    writer.write("send_event(CONST_EVENT_TOUCH, 200, 500, CONST_EVENT_ACTION_TOUCH, 3)\n");
                    writer.write("send_event(CONST_EVENT_TOUCH, 100, 200, CONST_EVENT_ACTION_SWIPE, 500, 600, 2000)\n");
                    writer.write("wait(3000)\n");
                    writer.write("send_event(CONST_EVENT_KEY, CONST_KEYCODE_HOME, CONST_EVENT_ACTION_PRESS, 3)\n");
                    writer.close();

                    Intent startServiceIntent=new Intent(getApplicationContext(), LuaService.class);
                    startServiceIntent.setAction(JLua.ACTION_RUN_LUA_FILE);
                    startServiceIntent.putExtra(JLua.EXTRA_PARAM, "/sdcard/test.lua");

                    startService(startServiceIntent);
                }
                catch (Exception e) {
                    Log.e(TAG, "click exception: " + e.getMessage());
                }
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                 //       .setAction("Action", null).show();
            }
        });

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "opencv not loaded");
        }
        else {
            Log.d(TAG, "load opencv success!!");
        }

    }
}
