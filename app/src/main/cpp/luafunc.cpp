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

int NLuaFunc::luaSentEvent(lua_State *L) {
	// argument 0: event type
	// argument 1: event value
	// argument 2: event action
	// argument 3: event duration or repeat times

	return 0;
}