package com.embux.dolua;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * Created by coder on 12/28/16.
 */

public class LuaService extends IntentService {
	public static final String TAG="DoLuaService";

	private JLua mLua;

	public LuaService() {
		super("LuaService");
		Log.i(TAG, "Start constructor!");
		if (null == mLua) {
			mLua = new JLua();
		}
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "destroy finish service");
		if (null != mLua) {
			mLua.close();
		}

		super.onDestroy();
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

		}
	}
}
