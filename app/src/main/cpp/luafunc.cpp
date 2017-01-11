//
// Created by coder on 12/28/16.
//

#include "luareplace.h"
#include <sstream>

int NLuaFunc::luaPrint(lua_State *L) {
	std::stringstream s;

	lua_gettop(L);

	for (int i=1; i<=lua_gettop(L); i++) {
		switch(lua_type(L, i)) {
			default: break;
			case LUA_TSTRING:
				s << lua_tostring(L, i);
				break;
			case LUA_TNUMBER:
				s << lua_tointeger(L, i);
				break;
		}
	}

	ALOG("%s\n", s.str().c_str());
	return 0;
}

int NLuaFunc::luaSendEvent(lua_State *L) {
	// argument 0: event type
	// argument 1: event value
	// argument 2: event action
	// argument 3: event duration or repeat times

	if (NULL == g_jvm) {
		ALOG("jvm is null\n");
		return 0;
	}

	if (NULL == L) {
		ALOG("lua state is null\n");
		return 0;
	}

	JNIEnv *jniEnv=NULL;
	g_jvm->GetEnv(reinterpret_cast<void**>(&jniEnv), JNI_VERSION_1_6);

	if (NULL == jniEnv) {
		ALOG("jni env is null\n");
		return 0;
	}

	jclass JLua=jniEnv->FindClass("com/embux/dolua/JLua");
	if (NULL == JLua) {
		ALOG("JLua class is not found\n");
		return 0;
	}

	jmethodID javaSendEvent=jniEnv->GetStaticMethodID(JLua, "nativeSendEvent", "([Ljava/lang/String;)V");
	if (NULL == javaSendEvent) {
		ALOG("java nativeSendEvent function is not found\n");
		return 0;
	}

	lua_gettop(L);
	std::stringstream s;

	jobjectArray jArray = jniEnv->NewObjectArray(lua_gettop(L), jniEnv->FindClass("java/lang/String"), jniEnv->NewStringUTF(""));

	for (int i=0; i<lua_gettop(L); i++) {
		switch(lua_type(L, i+1)) {
			default: break;
			case LUA_TSTRING:
				jniEnv->SetObjectArrayElement(jArray, i, jniEnv->NewStringUTF(lua_tostring(L, i+1)));
				break;
			case LUA_TNUMBER:
				s.str("");
				s << lua_tointeger(L, i+1);
				jniEnv->SetObjectArrayElement(jArray, i, jniEnv->NewStringUTF(s.str().c_str()));
				break;
		}
	}

	jniEnv->CallStaticVoidMethod(JLua, javaSendEvent, jArray);

	return 0;
}