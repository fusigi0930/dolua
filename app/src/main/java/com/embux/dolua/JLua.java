package com.embux.dolua;

/**
 * Created by coder on 12/28/16.
 */

import android.util.Log;

public class JLua {
	public static final String TAG="JDoLua";
	private long mLua = 0;


	public JLua() {

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

	// native functions
	public native long initLua();
	public native void closeLua(long lua);
	public native void luaString(long lua, String str);
}
