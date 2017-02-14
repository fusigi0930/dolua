package com.sotaku.dolua;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by coder on 12/28/16.
 */

public class LuaService extends IntentService {
	public static final String TAG="DoLuaService";
	public static final String DEX_PATH="/data/data/com.sotaku.dolua";

	private JLua mLua;

	public class LSBinder extends Binder {
		public LuaService getService() {
			return LuaService.this;
		}
	};

	private LSBinder mBinder = new LSBinder();
	private PowerManager.WakeLock mWL = null;

	public LuaService() {
		super("LuaService");
		Log.i(TAG, "Start constructor!");
		if (null == mLua) {
			mLua = new JLua(DEX_PATH);
		}

		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		mWL = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		mWL.acquire();
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "destroy finish service");
		if (null != mLua) {
			mLua.close();
		}

		if (null != mWL) {
			mWL.release();
		}

		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.i(TAG, "DoLua service action: " + intent.getAction());

		String action=intent.getAction();

		if (JLua.ACTION_RUN_LUA_FILE.equals(action)) {
			String param=intent.getStringExtra(JLua.EXTRA_PARAM);
			Log.i(TAG, "filename: " + param);

			if (null != mLua) {
				mLua.runFile(param);
				sendBroadcast(new Intent(FloatingControlService.FINISH_RUN_PROCEDURE));
			}
		}
		else if (JLua.ACTION_RUN_LUA_SCRIPT.equals(action)) {
			String param=intent.getStringExtra(JLua.EXTRA_PARAM);
			Log.i(TAG, "script: " + param);

			if (null != mLua) {
				mLua.runString(param);
			}
		}
		else if (JLua.ACTION_STOP_ALL.equals(action)) {
			Log.i(TAG, "stop all");
		}
	}

	public void setLuaFlag(int flag, int value) {
		mLua.setFlag(flag, value);
	}

	public void closeLua() {
		Log.i(TAG, "close lua");
		mLua.stop();
	}
}
