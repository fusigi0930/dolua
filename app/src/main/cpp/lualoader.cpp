//
// Created by coder on 12/28/16.
//

#include "lualoader.h"
#include <syslog.h>



long NLuaLoader::initLua() {
#if 1
    lua_State *lua=luaL_newstate();
    if (NULL == lua) {
        return 0;
    }
    luaL_openlibs(lua);

    return reinterpret_cast<long>(lua);
#else
	return 0;
#endif
}

void NLuaLoader::closeLua(long lua_state) {
#if 1
    lua_State *lua=reinterpret_cast<lua_State*>(lua_state);

    if (lua) {
        lua_close(lua);
    }
#endif
}