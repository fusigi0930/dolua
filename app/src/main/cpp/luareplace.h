//
// Created by coder on 12/28/16.
//

#ifndef DO_LUA_LUAREPLACE_H_H
#define DO_LUA_LUAREPLACE_H_H

#include "common.h"

namespace NLuaFunc {
	int luaPrint(lua_State *L);
	int luaSendEvent(lua_State *L);
	int luaSleep(lua_State *L);
};

#endif //DO_LUA_LUAREPLACE_H_H
