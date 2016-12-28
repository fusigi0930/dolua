//
// Created by coder on 12/28/16.
//

#ifndef DO_LUA_LUALOADER_H_H
#define DO_LUA_LUALOADER_H_H

#include "common.h"

namespace NLuaLoader {
    long initLua();
    void closeLua(long lua_state);

	void runString(long lua_state, char* szLua);
	void runFile(long lua_state, char* file);
}

#endif //DO_LUA_LUALOADER_H_H
