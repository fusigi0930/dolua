//
// Created by coder on 12/28/16.
//

#ifndef DO_LUA_COMMON_H_H
#define DO_LUA_COMMON_H_H

extern "C" {
#include "lua-5.3.2/src/lua.h"
#include "lua-5.3.2/src/lualib.h"
#include "lua-5.3.2/src/lauxlib.h"
}

#include <android/log.h>

#define LOG_TAG "DoLua-JNI"
#define ALOG(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

#endif //DO_LUA_LUAREPLACE_H_H
