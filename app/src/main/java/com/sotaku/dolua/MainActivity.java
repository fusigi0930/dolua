package com.sotaku.dolua;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.opengl.GLES20;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.DocumentsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import org.opencv.android.OpenCVLoader;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.IntBuffer;

public class MainActivity extends AppCompatActivity {
    public static final String TAG="DoLua-MainWindow";

    private FloatingControlService m_fcService = null;
    private LuaService m_currentLuaService = null;

    private static final int SELECT_LUA_RESULT = 2001;
    private boolean m_bIsFCServiceStarted = false;
    private String m_szLuaFile;

    public static final String DOLUA_CURRENT_SCRIPT = "dolua_current_script_file";
    public static final String DOLUA_PREFERENCE = "dolua_based_preference";

    SharedPreferences m_pref = null;

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
                Bitmap bmp = getScreen(40, 200, 100, 100);
            }
        });

        if (m_pref == null) {
            m_pref = getSharedPreferences(DOLUA_PREFERENCE, Context.MODE_PRIVATE);
        }

        m_szLuaFile = m_pref.getString(DOLUA_CURRENT_SCRIPT, "");
        Log.i(TAG, "get current lua file: " + m_szLuaFile);

        updateFileInfo();
        supportPermissions();


    }

    private void updateFileInfo() {
        TextView filename = (TextView) findViewById(R.id.txtViewFileName);
        TextView filecontent = (TextView) findViewById(R.id.txtViewFileContent);

        filename.setText(m_szLuaFile);
        String szBuffer=null;

        if (!m_szLuaFile.isEmpty()) {
            try {
                FileInputStream filein = new FileInputStream(m_szLuaFile);
                InputStreamReader reader = new InputStreamReader(filein);
                BufferedReader bufferReader = new BufferedReader(reader);
                StringBuilder content = new StringBuilder();

                while ((szBuffer = bufferReader.readLine()) != null) {
                    content.append(szBuffer + "\n");
                }
                filein.close();
                szBuffer = content.toString();
            }
            catch (FileNotFoundException e) {
                Log.e(TAG, "file: " + m_szLuaFile + " is not found");
            }
            catch (IOException e) {
                Log.e(TAG, "read file: " + m_szLuaFile + " error");
            }

            filecontent.setText(szBuffer);
        }

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

                if (m_pref != null) {
                    SharedPreferences.Editor editor = m_pref.edit();
                    editor.putString(DOLUA_CURRENT_SCRIPT, m_szLuaFile);
                    editor.commit();
                }

                updateFileInfo();

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

    public boolean supportPermissions() {
        int permReadStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int permWriteStroage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permMediaControl = ContextCompat.checkSelfPermission(this, Manifest.permission.MEDIA_CONTENT_CONTROL);
        int permWakeLock = ContextCompat.checkSelfPermission(this, Manifest.permission.WAKE_LOCK);
        int permSystemAlert = ContextCompat.checkSelfPermission(this, Manifest.permission.SYSTEM_ALERT_WINDOW);
        int permBattery = ContextCompat.checkSelfPermission(this, Manifest.permission.BATTERY_STATS);
        int permFlash = ContextCompat.checkSelfPermission(this, Manifest.permission.FLASHLIGHT);

        if (PackageManager.PERMISSION_DENIED == permReadStorage ||
                PackageManager.PERMISSION_DENIED == permWriteStroage ||
                PackageManager.PERMISSION_DENIED == permMediaControl ||
                PackageManager.PERMISSION_DENIED == permWakeLock ||
                PackageManager.PERMISSION_DENIED == permSystemAlert ||
                PackageManager.PERMISSION_DENIED == permBattery ||
                PackageManager.PERMISSION_DENIED == permFlash) {
            // init permissions dialog
            int requestCode=0;
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE,
                                                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                                    Manifest.permission.MEDIA_CONTENT_CONTROL,
                                                                    Manifest.permission.WAKE_LOCK,
                                                                    Manifest.permission.SYSTEM_ALERT_WINDOW,
                                                                    Manifest.permission.BATTERY_STATS,
                                                                    Manifest.permission.FLASHLIGHT
            }, requestCode);

        }

        return true;
    }

    public Bitmap getScreen(int x, int y, int width, int height) {
        Bitmap bmp;
        View v = getWindow().getDecorView().getRootView();
        v.setDrawingCacheEnabled(true);
        bmp = Bitmap.createBitmap(v.getDrawingCache());
        v.setDrawingCacheEnabled(false);

        // save to jpg test
        File file = new File("/sdcard/test.jpg");
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream o = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, o);
            o.flush();
            o.close();
        }
        catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        //
        return bmp;
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
