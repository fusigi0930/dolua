//
// Created by coder on 12/28/16.
//

#ifndef DO_LUA_LUALOADER_H_H
#define DO_LUA_LUALOADER_H_H

extern "C" {
#include "lua-5.3.2/src/lua.h"
#include "lua-5.3.2/src/lualib.h"
#include "lua-5.3.2/src/lauxlib.h"
}

namespace NLuaLoader {
    long initLua();
    void closeLua(long lua_state);
}

#endif //DO_LUA_LUALOADER_H_H
