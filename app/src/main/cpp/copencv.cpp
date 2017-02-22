#include "copencv.h"

char COpenCV::className[] = "opencv";

Luna<COpenCV>::FunctionType COpenCV::methods[] = {

};

Luna<COpenCV>::PropertyType COpenCV::properties[] = {

};

COpenCV::COpenCV(lua_State *lua) {
	mLua=lua;
}

COpenCV::~COpenCV() {

}