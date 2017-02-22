//
// Created by coder on 12/28/16.
//

#include "lualoader.h"
#include "luareplace.h"
#include <string>
#include <sstream>
#include "copencv.h"

#define VAR_FLAG_EXIT "__var_flag_exit__"
#define FLAG_EXIT		1001

FILE *g_shell=NULL;

struct SLuaConst {
	char *name;
	int value;
};

static const struct luaL_Reg override_func[] = {
		{ "print", NLuaFunc::luaPrint },
		{ "send_event", NLuaFunc::luaSendEvent },
		{ "wait", NLuaFunc::luaSleep },

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
		{ "CONST_EVENT_ACTION_PRESS",	9 },
		{ "CONST_EVENT_ACTION_LPRESS",	10 },

		{ "CONST_KEYCODE_UNKNOWN", 0 },
		{ "CONST_KEYCODE_SOFT_LEFT", 1 },
		{ "CONST_KEYCODE_SOFT_RIGHT", 2 },
		{ "CONST_KEYCODE_HOME", 3 },
		{ "CONST_KEYCODE_BACK", 4 },
		{ "CONST_KEYCODE_CALL", 5 },
		{ "CONST_KEYCODE_ENDCALL", 6 },
		{ "CONST_KEYCODE_0", 7 },
		{ "CONST_KEYCODE_1", 8 },
		{ "CONST_KEYCODE_2", 9 },
		{ "CONST_KEYCODE_3", 10 },
		{ "CONST_KEYCODE_4", 11 },
		{ "CONST_KEYCODE_5", 12 },
		{ "CONST_KEYCODE_6", 13 },
		{ "CONST_KEYCODE_7", 14 },
		{ "CONST_KEYCODE_8", 15 },
		{ "CONST_KEYCODE_9", 16 },
		{ "CONST_KEYCODE_STAR", 17 },
		{ "CONST_KEYCODE_POUND", 18 },
		{ "CONST_KEYCODE_DPAD_UP", 19 },
		{ "CONST_KEYCODE_DPAD_DOWN", 20 },
		{ "CONST_KEYCODE_DPAD_LEFT", 21 },
		{ "CONST_KEYCODE_DPAD_RIGHT", 22 },
		{ "CONST_KEYCODE_DPAD_CENTER", 23 },
		{ "CONST_KEYCODE_VOLUME_UP", 24 },
		{ "CONST_KEYCODE_VOLUME_DOWN", 25 },
		{ "CONST_KEYCODE_POWER", 26 },
		{ "CONST_KEYCODE_CAMERA", 27 },
		{ "CONST_KEYCODE_CLEAR", 28 },
		{ "CONST_KEYCODE_A", 29 },
		{ "CONST_KEYCODE_B", 30 },
		{ "CONST_KEYCODE_C", 31 },
		{ "CONST_KEYCODE_D", 32 },
		{ "CONST_KEYCODE_E", 33 },
		{ "CONST_KEYCODE_F", 34 },
		{ "CONST_KEYCODE_G", 35 },
		{ "CONST_KEYCODE_H", 36 },
		{ "CONST_KEYCODE_I", 37 },
		{ "CONST_KEYCODE_J", 38 },
		{ "CONST_KEYCODE_K", 39 },
		{ "CONST_KEYCODE_L", 40 },
		{ "CONST_KEYCODE_M", 41 },
		{ "CONST_KEYCODE_N", 42 },
		{ "CONST_KEYCODE_O", 43 },
		{ "CONST_KEYCODE_P", 44 },
		{ "CONST_KEYCODE_Q", 45 },
		{ "CONST_KEYCODE_R", 46 },
		{ "CONST_KEYCODE_S", 47 },
		{ "CONST_KEYCODE_T", 48 },
		{ "CONST_KEYCODE_U", 49 },
		{ "CONST_KEYCODE_V", 50 },
		{ "CONST_KEYCODE_W", 51 },
		{ "CONST_KEYCODE_X", 52 },
		{ "CONST_KEYCODE_Y", 53 },
		{ "CONST_KEYCODE_Z", 54 },
		{ "CONST_KEYCODE_COMMA", 55 },
		{ "CONST_KEYCODE_PERIOD", 56 },
		{ "CONST_KEYCODE_ALT_LEFT", 57 },
		{ "CONST_KEYCODE_ALT_RIGHT", 58 },
		{ "CONST_KEYCODE_SHIFT_LEFT", 59 },
		{ "CONST_KEYCODE_SHIFT_RIGHT", 60 },
		{ "CONST_KEYCODE_TAB", 61 },
		{ "CONST_KEYCODE_SPACE", 62 },
		{ "CONST_KEYCODE_SYM", 63 },
		{ "CONST_KEYCODE_EXPLORER", 64 },
		{ "CONST_KEYCODE_ENVELOPE", 65 },
		{ "CONST_KEYCODE_ENTER", 66 },
		{ "CONST_KEYCODE_DEL", 67 },
		{ "CONST_KEYCODE_GRAVE", 68 },
		{ "CONST_KEYCODE_MINUS", 69 },
		{ "CONST_KEYCODE_EQUALS", 70 },
		{ "CONST_KEYCODE_LEFT_BRACKET", 71 },
		{ "CONST_KEYCODE_RIGHT_BRACKET", 72 },
		{ "CONST_KEYCODE_BACKSLASH", 73 },
		{ "CONST_KEYCODE_SEMICOLON", 74 },
		{ "CONST_KEYCODE_APOSTROPHE", 75 },
		{ "CONST_KEYCODE_SLASH", 76 },
		{ "CONST_KEYCODE_AT", 77 },
		{ "CONST_KEYCODE_NUM", 78 },
		{ "CONST_KEYCODE_HEADSETHOOK", 79 },
		{ "CONST_KEYCODE_FOCUS", 80 },   // *Camera
		{ "CONST_KEYCODE_PLUS", 81 },
		{ "CONST_KEYCODE_MENU", 82 },
		{ "CONST_KEYCODE_NOTIFICATION", 83 },
		{ "CONST_KEYCODE_SEARCH", 84 },
		{ "CONST_KEYCODE_MEDIA_PLAY_PAUSE", 85 },
		{ "CONST_KEYCODE_MEDIA_STOP", 86 },
		{ "CONST_KEYCODE_MEDIA_NEXT", 87 },
		{ "CONST_KEYCODE_MEDIA_PREVIOUS", 88 },
		{ "CONST_KEYCODE_MEDIA_REWIND", 89 },
		{ "CONST_KEYCODE_MEDIA_FAST_FORWARD", 90 },
		{ "CONST_KEYCODE_MUTE", 91 },
		{ "CONST_KEYCODE_PAGE_UP", 92 },
		{ "CONST_KEYCODE_PAGE_DOWN", 93 },
		{ "CONST_KEYCODE_PICTSYMBOLS", 94 },   // switch symbol-sets (Emoji,Kao-moji)
		{ "CONST_KEYCODE_SWITCH_CHARSET", 95 },   // switch char-sets (Kanji,Katakana)
		{ "CONST_KEYCODE_BUTTON_A", 96 },
		{ "CONST_KEYCODE_BUTTON_B", 97 },
		{ "CONST_KEYCODE_BUTTON_C", 98 },
		{ "CONST_KEYCODE_BUTTON_X", 99 },
		{ "CONST_KEYCODE_BUTTON_Y", 100 },
		{ "CONST_KEYCODE_BUTTON_Z", 101 },
		{ "CONST_KEYCODE_BUTTON_L1", 102 },
		{ "CONST_KEYCODE_BUTTON_R1", 103 },
		{ "CONST_KEYCODE_BUTTON_L2", 104 },
		{ "CONST_KEYCODE_BUTTON_R2", 105 },
		{ "CONST_KEYCODE_BUTTON_THUMBL", 106 },
		{ "CONST_KEYCODE_BUTTON_THUMBR", 107 },
		{ "CONST_KEYCODE_BUTTON_START", 108 },
		{ "CONST_KEYCODE_BUTTON_SELECT", 109 },
		{ "CONST_KEYCODE_BUTTON_MODE", 110 },
		{ "CONST_KEYCODE_ESCAPE", 111 },
		{ "CONST_KEYCODE_FORWARD_DEL", 112 },
		{ "CONST_KEYCODE_CTRL_LEFT", 113 },
		{ "CONST_KEYCODE_CTRL_RIGHT", 114 },
		{ "CONST_KEYCODE_CAPS_LOCK", 115 },
		{ "CONST_KEYCODE_SCROLL_LOCK", 116 },
		{ "CONST_KEYCODE_META_LEFT", 117 },
		{ "CONST_KEYCODE_META_RIGHT", 118 },
		{ "CONST_KEYCODE_FUNCTION", 119 },
		{ "CONST_KEYCODE_SYSRQ", 120 },
		{ "CONST_KEYCODE_BREAK", 121 },
		{ "CONST_KEYCODE_MOVE_HOME", 122 },
		{ "CONST_KEYCODE_MOVE_END", 123 },
		{ "CONST_KEYCODE_INSERT", 124 },
		{ "CONST_KEYCODE_FORWARD", 125 },
		{ "CONST_KEYCODE_MEDIA_PLAY", 126 },
		{ "CONST_KEYCODE_MEDIA_PAUSE", 127 },
		{ "CONST_KEYCODE_MEDIA_CLOSE", 128 },
		{ "CONST_KEYCODE_MEDIA_EJECT", 129 },
		{ "CONST_KEYCODE_MEDIA_RECORD", 130 },
		{ "CONST_KEYCODE_F1", 131 },
		{ "CONST_KEYCODE_F2", 132 },
		{ "CONST_KEYCODE_F3", 133 },
		{ "CONST_KEYCODE_F4", 134 },
		{ "CONST_KEYCODE_F5", 135 },
		{ "CONST_KEYCODE_F6", 136 },
		{ "CONST_KEYCODE_F7", 137 },
		{ "CONST_KEYCODE_F8", 138 },
		{ "CONST_KEYCODE_F9", 139 },
		{ "CONST_KEYCODE_F10", 140 },
		{ "CONST_KEYCODE_F11", 141 },
		{ "CONST_KEYCODE_F12", 142 },
		{ "CONST_KEYCODE_NUM_LOCK", 143 },
		{ "CONST_KEYCODE_NUMPAD_0", 144 },
		{ "CONST_KEYCODE_NUMPAD_1", 145 },
		{ "CONST_KEYCODE_NUMPAD_2", 146 },
		{ "CONST_KEYCODE_NUMPAD_3", 147 },
		{ "CONST_KEYCODE_NUMPAD_4", 148 },
		{ "CONST_KEYCODE_NUMPAD_5", 149 },
		{ "CONST_KEYCODE_NUMPAD_6", 150 },
		{ "CONST_KEYCODE_NUMPAD_7", 151 },
		{ "CONST_KEYCODE_NUMPAD_8", 152 },
		{ "CONST_KEYCODE_NUMPAD_9", 153 },
		{ "CONST_KEYCODE_NUMPAD_DIVIDE", 154 },
		{ "CONST_KEYCODE_NUMPAD_MULTIPLY", 155 },
		{ "CONST_KEYCODE_NUMPAD_SUBTRACT", 156 },
		{ "CONST_KEYCODE_NUMPAD_ADD", 157 },
		{ "CONST_KEYCODE_NUMPAD_DOT", 158 },
		{ "CONST_KEYCODE_NUMPAD_COMMA", 159 },
		{ "CONST_KEYCODE_NUMPAD_ENTER", 160 },
		{ "CONST_KEYCODE_NUMPAD_EQUALS", 161 },
		{ "CONST_KEYCODE_NUMPAD_LEFT_PAREN", 162 },
		{ "CONST_KEYCODE_NUMPAD_RIGHT_PAREN", 163 },
		{ "CONST_KEYCODE_VOLUME_MUTE", 164 },
		{ "CONST_KEYCODE_INFO", 165 },
		{ "CONST_KEYCODE_CHANNEL_UP", 166 },
		{ "CONST_KEYCODE_CHANNEL_DOWN", 167 },
		{ "CONST_KEYCODE_ZOOM_IN", 168 },
		{ "CONST_KEYCODE_ZOOM_OUT", 169 },
		{ "CONST_KEYCODE_TV", 170 },
		{ "CONST_KEYCODE_WINDOW", 171 },
		{ "CONST_KEYCODE_GUIDE", 172 },
		{ "CONST_KEYCODE_DVR", 173 },
		{ "CONST_KEYCODE_BOOKMARK", 174 },
		{ "CONST_KEYCODE_CAPTIONS", 175 },
		{ "CONST_KEYCODE_SETTINGS", 176 },
		{ "CONST_KEYCODE_TV_POWER", 177 },
		{ "CONST_KEYCODE_TV_INPUT", 178 },
		{ "CONST_KEYCODE_STB_POWER", 179 },
		{ "CONST_KEYCODE_STB_INPUT", 180 },
		{ "CONST_KEYCODE_AVR_POWER", 181 },
		{ "CONST_KEYCODE_AVR_INPUT", 182 },
		{ "CONST_KEYCODE_PROG_RED", 183 },
		{ "CONST_KEYCODE_PROG_GREEN", 184 },
		{ "CONST_KEYCODE_PROG_YELLOW", 185 },
		{ "CONST_KEYCODE_PROG_BLUE", 186 },
		{ "CONST_KEYCODE_APP_SWITCH", 187 },
		{ "CONST_KEYCODE_BUTTON_1", 188 },
		{ "CONST_KEYCODE_BUTTON_2", 189 },
		{ "CONST_KEYCODE_BUTTON_3", 190 },
		{ "CONST_KEYCODE_BUTTON_4", 191 },
		{ "CONST_KEYCODE_BUTTON_5", 192 },
		{ "CONST_KEYCODE_BUTTON_6", 193 },
		{ "CONST_KEYCODE_BUTTON_7", 194 },
		{ "CONST_KEYCODE_BUTTON_8", 195 },
		{ "CONST_KEYCODE_BUTTON_9", 196 },
		{ "CONST_KEYCODE_BUTTON_10", 197 },
		{ "CONST_KEYCODE_BUTTON_11", 198 },
		{ "CONST_KEYCODE_BUTTON_12", 199 },
		{ "CONST_KEYCODE_BUTTON_13", 200 },
		{ "CONST_KEYCODE_BUTTON_14", 201 },
		{ "CONST_KEYCODE_BUTTON_15", 202 },
		{ "CONST_KEYCODE_BUTTON_16", 203 },
		{ "CONST_KEYCODE_LANGUAGE_SWITCH", 204 },
		{ "CONST_KEYCODE_MANNER_MODE", 205 },
		{ "CONST_KEYCODE_3D_MODE", 206 },
		{ "CONST_KEYCODE_CONTACTS", 207 },
		{ "CONST_KEYCODE_CALENDAR", 208 },
		{ "CONST_KEYCODE_MUSIC", 209 },
		{ "CONST_KEYCODE_CALCULATOR", 210 },
		{ "CONST_KEYCODE_ZENKAKU_HANKAKU", 211 },
		{ "CONST_KEYCODE_EISU", 212 },
		{ "CONST_KEYCODE_MUHENKAN", 213 },
		{ "CONST_KEYCODE_HENKAN", 214 },
		{ "CONST_KEYCODE_KATAKANA_HIRAGANA", 215 },
		{ "CONST_KEYCODE_YEN", 216 },
		{ "CONST_KEYCODE_RO", 217 },
		{ "CONST_KEYCODE_KANA", 218 },
		{ "CONST_KEYCODE_ASSIST", 219 },
		{ "CONST_KEYCODE_BRIGHTNESS_DOWN", 220 },
		{ "CONST_KEYCODE_BRIGHTNESS_UP", 221 },
		{ "CONST_KEYCODE_MEDIA_AUDIO_TRACK", 222 },
		{ "CONST_KEYCODE_SLEEP", 223 },
		{ "CONST_KEYCODE_WAKEUP", 224 },
		{ "CONST_KEYCODE_PAIRING", 225 },
		{ "CONST_KEYCODE_MEDIA_TOP_MENU", 226 },
		{ "CONST_KEYCODE_11", 227 },
		{ "CONST_KEYCODE_12", 228 },
		{ "CONST_KEYCODE_LAST_CHANNEL", 229 },
		{ "CONST_KEYCODE_TV_DATA_SERVICE", 230 },
		{ "CONST_KEYCODE_VOICE_ASSIST", 231 },
		{ "CONST_KEYCODE_TV_RADIO_SERVICE", 232 },
		{ "CONST_KEYCODE_TV_TELETEXT", 233 },
		{ "CONST_KEYCODE_TV_NUMBER_ENTRY", 234 },
		{ "CONST_KEYCODE_TV_TERRESTRIAL_ANALOG", 235 },
		{ "CONST_KEYCODE_TV_TERRESTRIAL_DIGITAL", 236 },
		{ "CONST_KEYCODE_TV_SATELLITE", 237 },
		{ "CONST_KEYCODE_TV_SATELLITE_BS", 238 },
		{ "CONST_KEYCODE_TV_SATELLITE_CS", 239 },
		{ "CONST_KEYCODE_TV_SATELLITE_SERVICE", 240 },
		{ "CONST_KEYCODE_TV_NETWORK", 241 },
		{ "CONST_KEYCODE_TV_ANTENNA_CABLE", 242 },
		{ "CONST_KEYCODE_TV_INPUT_HDMI_1", 243 },
		{ "CONST_KEYCODE_TV_INPUT_HDMI_2", 244 },
		{ "CONST_KEYCODE_TV_INPUT_HDMI_3", 245 },
		{ "CONST_KEYCODE_TV_INPUT_HDMI_4", 246 },
		{ "CONST_KEYCODE_TV_INPUT_COMPOSITE_1", 247 },
		{ "CONST_KEYCODE_TV_INPUT_COMPOSITE_2", 248 },
		{ "CONST_KEYCODE_TV_INPUT_COMPONENT_1", 249 },
		{ "CONST_KEYCODE_TV_INPUT_COMPONENT_2", 250 },
		{ "CONST_KEYCODE_TV_INPUT_VGA_1", 251 },
		{ "CONST_KEYCODE_TV_AUDIO_DESCRIPTION", 252 },
		{ "CONST_KEYCODE_TV_AUDIO_DESCRIPTION_MIX_UP", 253 },
		{ "CONST_KEYCODE_TV_AUDIO_DESCRIPTION_MIX_DOWN", 254 },
		{ "CONST_KEYCODE_TV_ZOOM_MODE", 255 },
		{ "CONST_KEYCODE_TV_CONTENTS_MENU", 256 },
		{ "CONST_KEYCODE_TV_MEDIA_CONTEXT_MENU", 257 },
		{ "CONST_KEYCODE_TV_TIMER_PROGRAMMING", 258 },
		{ "CONST_KEYCODE_HELP", 259 },
		{ "CONST_KEYCODE_NAVIGATE_PREVIOUS", 260 },
		{ "CONST_KEYCODE_NAVIGATE_NEXT", 261 },
		{ "CONST_KEYCODE_NAVIGATE_IN", 262 },
		{ "CONST_KEYCODE_NAVIGATE_OUT", 263 },
		{ "CONST_KEYCODE_STEM_PRIMARY", 264 },
		{ "CONST_KEYCODE_STEM_1", 265 },
		{ "CONST_KEYCODE_STEM_2", 266 },
		{ "CONST_KEYCODE_STEM_3", 267 },
		{ "CONST_KEYCODE_MEDIA_SKIP_FORWARD", 272 },
		{ "CONST_KEYCODE_MEDIA_SKIP_BACKWARD", 273 },
		{ "CONST_KEYCODE_MEDIA_STEP_FORWARD", 274 },
		{ "CONST_KEYCODE_MEDIA_STEP_BACKWARD", 275 },
		{ "CONST_KEYCODE_SOFT_SLEEP", 276 },

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

static void luaHook(lua_State *L, lua_Debug *ar) {
	if (NULL == L) {
		return;
	}

	lua_getglobal(L, VAR_FLAG_EXIT);
	int nIsExit=lua_tointeger(L, -1);

	if (nIsExit) {
		luaL_error(L, "force terminate!");
	}
}

long NLuaLoader::initLua() {
	lua_State *lua=luaL_newstate();
	if (NULL == lua) {
		return 0;
	}

	luaL_openlibs(lua);
	lua_sethook(lua, luaHook, LUA_MASKLINE, 0);
	setFlag(reinterpret_cast<long>(lua), FLAG_EXIT, 0);
	registerFunc(lua);
	registerConst(lua);

	Luna<COpenCV>::Register(lua);

	return reinterpret_cast<long>(lua);
}

void NLuaLoader::closeLua(long lua_state) {
	lua_State *lua=reinterpret_cast<lua_State*>(lua_state);

	if (lua) {
		ALOG("close lua");
		lua_close(lua);
	}

}

void NLuaLoader::runString(long lua_state, char *szLua) {
	lua_State *lua=reinterpret_cast<lua_State*>(lua_state);
	if (NULL == lua) return;
	if (NULL == szLua) return;

	setFlag(reinterpret_cast<long>(lua), FLAG_EXIT, 0);
	luaL_dostring(lua, szLua);
}

void NLuaLoader::setFlag(long lua_state, int nFlag, int nValue) {
	lua_State *lua=reinterpret_cast<lua_State*>(lua_state);
	if (NULL == lua) return;

	switch (nFlag) {
		default:
			return;
		case FLAG_EXIT:
			lua_pushinteger(lua, nValue);
			lua_setglobal(lua, VAR_FLAG_EXIT);
			break;
	}
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
	setFlag(reinterpret_cast<long>(lua), FLAG_EXIT, 0);

	if (luaL_loadfile(lua, file)) {
		ALOG("load %s failed", file);
		return;
	}

	ALOG("running lua");
	if (lua_pcall(lua, 0, LUA_MULTRET, 0)) {
		ALOG("run lua failed");
		return;
	}
	ALOG("finish lua");
}

void NLuaLoader::stopLua(long lua_state) {
	lua_State *lua=reinterpret_cast<lua_State*>(lua_state);

	if (NULL == lua) return;

	luaL_error(lua, "force stop!");
}