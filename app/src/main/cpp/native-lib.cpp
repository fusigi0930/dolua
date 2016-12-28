#include <jni.h>
#include <string>
#include "lualoader.h"

extern "C" {
jstring
Java_com_embux_dolua_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
}