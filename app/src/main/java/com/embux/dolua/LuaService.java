package com.embux.dolua;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * Created by coder on 12/28/16.
 */

public class LuaService extends IntentService {
	public static final String TAG="LuaService";

	public LuaService() {
		super("LuaService");
		Log.i(TAG, "Start constructor!");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onHandleIntent(Intent intent) {

	}
}
