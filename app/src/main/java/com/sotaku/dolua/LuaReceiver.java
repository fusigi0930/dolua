package com.sotaku.dolua;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by coder on 12/29/16.
 */

public class LuaReceiver extends BroadcastReceiver {
	public static final String TAG="DoLuaReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "receive doLua action: " + intent.getAction());

		Intent startServiceIntent=new Intent(context, LuaService.class);
		startServiceIntent.setAction(intent.getAction());
		startServiceIntent.putExtras(intent.getExtras());

		context.startService(startServiceIntent);
	}
}
