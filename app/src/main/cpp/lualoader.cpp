//
// Created by coder on 12/28/16.
//

#include "lualoader.h"
#include "luareplace.h"
#include <sstream>

static const struct luaL_Reg override_func[] = {
		{ "print", NLuaFunc::luaPrint },
		{ NULL, NULL }
};

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

static void registerFunc(lua_State *L) {
	if (NULL == L) return;

	for (int i=0; NULL != override_func[i].func; i++) {
		lua_pushcfunction(L, override_func[i].func);
		lua_setglobal(L, override_func[i].name);
	}
}

long NLuaLoader::initLua() {
	lua_State *lua=luaL_newstate();
	if (NULL == lua) {
		return 0;
	}
	luaL_openlibs(lua);
	registerFunc(lua);

	return reinterpret_cast<long>(lua);
}

void NLuaLoader::closeLua(long lua_state) {
	lua_State *lua=reinterpret_cast<lua_State*>(lua_state);

	if (lua) {
		lua_close(lua);
	}
}

void NLuaLoader::runString(long lua_state, char *szLua) {
	lua_State *lua=reinterpret_cast<lua_State*>(lua_state);
	if (NULL == lua) return;
	if (NULL == szLua) return;

	luaL_dostring(lua, szLua);
}