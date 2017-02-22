#include <opencv2/opencv.hpp>
#include "common.h"
#include "luna_four_code.h"

class COpenCV {
private:
	lua_State *mLua;

public:
	static char className[];
	static Luna<COpenCV>::FunctionType methods[];
	static Luna<COpenCV>::PropertyType properties[];

	COpenCV(lua_State *lua);
	virtual ~COpenCV();
};