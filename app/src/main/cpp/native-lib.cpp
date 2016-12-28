#include <jni.h>
#include <string>
#include "lualoader.h"

extern "C" {
JNIEXPORT
jstring
Java_com_embux_dolua_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

JNIEXPORT jlong Java_com_embux_dolua_MainActivity_initLua(JNIEnv *env, jobject) {
    return static_cast<jlong>(NLuaLoader::initLua());
}

JNIEXPORT void Java_com_embux_dolua_MainActivity_closeLua(JNIEnv *env, jobject, jlong lua) {
	NLuaLoader::closeLua(static_cast<long>(lua));
}

JNIEXPORT void Java_com_embux_dolua_MainActivity_luaString(JNIEnv *env, jobject, jlong lua, jstring str) {
	char *szLua = const_cast<char*>(env->GetStringUTFChars(str, NULL));

	NLuaLoader::runString(static_cast<long>(lua), szLua);

	env->ReleaseStringUTFChars(str, szLua);
}

// end of extern C
}