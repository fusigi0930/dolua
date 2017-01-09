package com.embux.dolua;

/**
 * Created by coder on 12/28/16.
 */

import android.util.Log;
import com.android.monkeyrunner.MonkeyDevice;
import com.android.monkeyrunner.MonkeyRunner;
import com.android.monkeyrunner.MonkeyImage;
import com.android.chimpchat.adb.AdbBackend;

import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.os.SystemClock;

public class JLua {
	public static final String TAG="JDoLua";
	public static final String ACTION_RUN_LUA_FILE = "com.embux.dolua.action.RUN_LUA_FILE";
	public static final String ACTION_RUN_LUA_SCRIPT = "com.embux.dolua.action.RUN_LUA_SCRIPT";
	public static final String ACTION_STOP_ALL = "com.embux.dolua.action.STOP_ALL";
	public static final String EXTRA_PARAM = "PARAM";

	public static final String CONST_EVENT_KEY = "0";
	public static final String CONST_EVENT_MOUSE = "1";
	public static final String CONST_EVENT_TOUCH = "2";
	public static final String CONST_EVENT_CUSTOM = "3";

	public static final String CONST_EVENT_ACTION_DOWN = "0";
	public static final String CONST_EVENT_ACTION_UP = "1";
	public static final String CONST_EVENT_ACTION_CLICK = "2";
	public static final String CONST_EVENT_ACTION_RDOWN = "3";
	public static final String CONST_EVENT_ACTION_RUP = "4";
	public static final String CONST_EVENT_ACTION_RCLICK = "5";
	public static final String CONST_EVENT_ACTION_MOVE = "6";

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

	public static void nativeSendEvent(String [] arg) {
		Log.i(TAG, arg[0]);
		// argument 0: event type, key, mouse, touch
		// argument 1: event value
		// argument 2: event action
		// argument 3: event duration or repeat times

		if (CONST_EVENT_KEY.equals(arg[0])) {
			nativeSendKey(arg);
		}
		else if (CONST_EVENT_MOUSE.equals(arg[0])) {
			nativeSendMouse(arg);
		}
		else if (CONST_EVENT_TOUCH.equals(arg[0])) {
			nativeSendTouch(arg);
		}
		else if (CONST_EVENT_CUSTOM.equals(arg[0])) {

		}
	}

	// private functions
	private static void nativeSendKey(String [] arg) {
		// argument 1: event value
		// argument 2: event action
		// argument 3: event duration or repeat times

		Log.i(TAG, "send_event key: " + arg[1]);
	}

	private static void nativeSendMouse(String [] arg) {
		Log.i(TAG, "send_event mouse");
	}

	private static void nativeSendTouch(String [] arg) {
		// argument 1: event x
		// argument 2: event y
		// argument 3: event action
		// argument 4: event duration or repeat times
		Log.i(TAG, "send_event touch");

		AdbBackend adb=new AdbBackend();
		MonkeyDevice device = new MonkeyDevice(adb.waitForConnection());
		MonkeyImage img=device.takeSnapshot();
		img.getImpl().writeToFile("/sdcard/1.png", "png");

	}

	// native functions
	private native long initLua();
	private native void closeLua(long lua);
	private native void luaString(long lua, String str);
	private native void luaFile(long lua, String str);
}
