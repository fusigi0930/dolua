#include <jni.h>
#include <string>
#include "lualoader.h"

JavaVM *g_jvm=NULL;

extern "C" {

JNIEXPORT
jstring
Java_com_embux_dolua_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

JNIEXPORT jlong Java_com_embux_dolua_JLua_initLua(JNIEnv *env, jobject) {
    return static_cast<jlong>(NLuaLoader::initLua());
}

JNIEXPORT void Java_com_embux_dolua_JLua_closeLua(JNIEnv *env, jobject, jlong lua) {
	NLuaLoader::closeLua(static_cast<long>(lua));
}

JNIEXPORT void Java_com_embux_dolua_JLua_luaString(JNIEnv *env, jobject, jlong lua, jstring str) {
	char *szLua = const_cast<char*>(env->GetStringUTFChars(str, NULL));

	NLuaLoader::runString(static_cast<long>(lua), szLua);

	env->ReleaseStringUTFChars(str, szLua);
}

JNIEXPORT void Java_com_embux_dolua_JLua_luaFile(JNIEnv *env, jobject, jlong lua, jstring str) {
	char *szFile = const_cast<char*>(env->GetStringUTFChars(str, NULL));
	ALOG("file name: %s, lua: %ld\n", szFile, static_cast<long>(lua));

	NLuaLoader::runFile(static_cast<long>(lua), szFile);

	env->ReleaseStringUTFChars(str, szFile);
}

JNIEXPORT void Java_com_embux_dolua_JLua_setLuaFlag(JNIEnv *env, jobject, jlong lua, jint flag, jint value) {
	NLuaLoader::setFlag(static_cast<long>(lua), static_cast<int>(flag), static_cast<int>(value));
}

JNIEXPORT void Java_com_embux_dolua_JLua_stopLua(JNIEnv *env, jobject, jlong lua) {
	NLuaLoader::stopLua(lua);
}

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
	g_jvm=vm;

	return JNI_VERSION_1_6;
}

// end of extern C
}