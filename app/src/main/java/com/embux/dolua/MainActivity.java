package com.embux.dolua;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.DocumentsContract;
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

    private FloatingControlService m_fcService = null;

    private static final int SELECT_LUA_RESULT = 2001;
    private boolean m_bIsFCServiceStarted = false;
    private String m_szLuaFile;

    private ServiceConnection m_servConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (FloatingControlService.class.getName().equals(name.getClassName())) {
                FloatingControlService.FCBinder fcBinder = (FloatingControlService.FCBinder) service;
                m_fcService = fcBinder.getService();
                m_fcService.m_szLuaFile = m_szLuaFile;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            m_servConn = null;
        }
    };

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
                /*
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
                */
                try {
                    Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                    i.setType("*/*");
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(i, SELECT_LUA_RESULT);

                }
                catch (android.content.ActivityNotFoundException e) {
                    Log.e(TAG, e.getMessage());
                }

                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                 //       .setAction("Action", null).show();
            }

        });

        fab.setEnabled(m_bIsFCServiceStarted);

        FloatingActionButton ctrlservice = (FloatingActionButton) findViewById(R.id.ctrlservice);
        ctrlservice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FloatingActionButton ctrlservice = (FloatingActionButton) findViewById(R.id.ctrlservice);
                if (m_bIsFCServiceStarted) {
                    // init floating control service
                    Log.i(TAG, "stop floating control service");
                    ctrlservice.setImageResource(android.R.drawable.presence_online);
                    Intent i = new Intent(MainActivity.this, FloatingControlService.class);
                    unbindService(m_servConn);
                    stopService(i);
                    m_bIsFCServiceStarted=false;
                    m_fcService = null;

                }
                else {
                    // init floating control service
                    Log.i(TAG, "start floating control service");
                    ctrlservice.setImageResource(android.R.drawable.presence_offline);

                    Intent i = new Intent(MainActivity.this, FloatingControlService.class);
                    startService(i);
                    bindService(i, m_servConn, Context.BIND_AUTO_CREATE);
                    m_bIsFCServiceStarted=true;

                    if (m_fcService != null) {
                        Log.i(TAG, "set service lua file path");
                        m_fcService.m_szLuaFile = m_szLuaFile;
                    }
                    else Log.i(TAG, "service reference is null");
                }

                FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
                fab.setEnabled(m_bIsFCServiceStarted);
            }
        });

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(m_servConn);
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

    @Override
    protected void onActivityResult(int resultCode, int requestCode, Intent data) {
        Log.i(TAG, "onActivityResult: " + resultCode + ", " + requestCode);
        switch (resultCode) {
            default: break;

            case SELECT_LUA_RESULT:
                Uri uri = data.getData();

                Log.i(TAG, "data: " + uri.toString());
                m_szLuaFile=getPathFromUri(uri);
                if (m_fcService != null) {
                    m_fcService.m_szLuaFile = m_szLuaFile;
                }
                break;
        }
    }

    public String getPathFromUri(Uri uri) {
        if ("com.android.externalstorage.documents".equals(uri.getAuthority())) {
            final String docid=DocumentsContract.getDocumentId(uri);
            Log.i(TAG, "docid: " + docid);

            final String[] split = docid.split(":");
            final String type = split[0];

            if ("primary".equalsIgnoreCase(type)) {
                Log.i(TAG, Environment.getExternalStorageDirectory().getAbsolutePath());
                return Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + split[1];
            }
            else if (Environment.isExternalStorageRemovable()) {
                return System.getenv("EXTERNAL_STORAGE") + "/" + split[1];
            }
            else {
                return "/storage/" + type + "/" + split[1];
            }
        }

        return "";
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
