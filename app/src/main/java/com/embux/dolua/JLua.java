package com.embux.dolua;

/**
 * Created by coder on 12/28/16.
 */

import android.util.Log;

public class JLua {
	public static final String TAG="JDoLua";
	public static final String ACTION_RUN_LUA_FILE = "com.embux.dolua.action.RUN_LUA_FILE";
	public static final String ACTION_RUN_LUA_SCRIPT = "com.embux.dolua.action.RUN_LUA_SCRIPT";
	public static final String ACTION_STOP_ALL = "com.embux.dolua.action.STOP_ALL";
	public static final String EXTRA_PARAM = "PARAM";

	private long mLua = 0;

	public JLua() {
		mLua=0;
		init();
	}

	public boolean init() {
		if (0 == mLua)
			mLua=initLua();

		return 0 == mLua ? false : true;
	}

	public void close() {
		closeLua(mLua);
	}

	public void runString(String str) {
		luaString(mLua, str);
	}

	public void runFile(String str) {
		final String szFile=str;
		new Runnable() {
			public void run() {
				Log.i(TAG, "runFile: " + szFile);
				luaFile(mLua, szFile);
			}
		}.run();
	}

	// native functions
	private native long initLua();
	private native void closeLua(long lua);
	private native void luaString(long lua, String str);
	private native void luaFile(long lua, String str);
}
