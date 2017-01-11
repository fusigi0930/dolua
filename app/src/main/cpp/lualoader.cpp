//
// Created by coder on 12/28/16.
//

#include "lualoader.h"
#include "luareplace.h"
#include <string>
#include <sstream>
#include <stdio.h>

FILE *g_shell=NULL;

struct SLuaConst {
	char *name;
	int value;
};

static const struct luaL_Reg override_func[] = {
		{ "print", NLuaFunc::luaPrint },
		{ "send_event", NLuaFunc::luaSendEvent },

		{ NULL, NULL }
};

static const SLuaConst const_data[] = {
		{ "CONST_EVENT_KEY",		0 },
		{ "CONST_EVENT_MOUES",		1 },
		{ "CONST_EVENT_TOUCH", 		2 },
		{ "CONST_EVENT_CUSTOM",		3 },

		{ "CONST_EVENT_ACTION_DOWN",	0 },
		{ "CONST_EVENT_ACTION_UP",		1 },
		{ "CONST_EVENT_ACTION_CLICK",	2 },
		{ "CONST_EVENT_ACTION_RDOWN",	3 },
		{ "CONST_EVENT_ACTION_RUP",		4 },
		{ "CONST_EVENT_ACTION_RCLICK",	5 },
		{ "CONST_EVENT_ACTION_MOVE",	6 },
		{ "CONST_EVENT_ACTION_TOUCH",	7 },
		{ "CONST_EVENT_ACTION_SWIPE",	8 },

		{ NULL, NULL }
};

static void registerFunc(lua_State *L) {
	if (NULL == L) return;

	for (int i=0; NULL != override_func[i].func; i++) {
		lua_pushcfunction(L, override_func[i].func);
		lua_setglobal(L, override_func[i].name);
	}
}

static void registerConst(lua_State *L) {
	if (NULL == L) return;

	for(int i=0; NULL != const_data[i].name; i++) {
		lua_pushnumber(L, const_data[i].value);
		lua_setglobal(L, const_data[i].name);
	}
}

long NLuaLoader::initLua() {
	lua_State *lua=luaL_newstate();
	if (NULL == lua) {
		return 0;
	}

	luaL_openlibs(lua);
	registerFunc(lua);
	registerConst(lua);

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

static void adjustLuaPath(lua_State *L, char *file) {
	if (NULL == L) return;

	std::string szPath=file;
	int pos=szPath.rfind('/');
	if (pos != std::string::npos) {
		std::stringstream s;
		lua_getglobal(L, "package");
		lua_getfield(L, -1, "path");
		s << lua_tostring(L, -1) << ";";
		s << szPath.substr(0, pos) << "/?.lua;";
		lua_pop(L, 1);
		lua_pushstring(L, s.str().c_str());
		lua_setfield(L, -2, "path");
		lua_pop(L, 1);

	}
}

void NLuaLoader::runFile(long lua_state, char *file) {
	lua_State *lua=reinterpret_cast<lua_State*>(lua_state);
	if (NULL == lua) return;
	if (NULL == file) return;

	adjustLuaPath(lua, file);

	if (luaL_loadfile(lua, file)) {
		ALOG("load %s failed", file);
		return;
	}

	if (lua_pcall(lua, 0, LUA_MULTRET, 0)) {
		ALOG("run lua failed");
		return;
	}
}
