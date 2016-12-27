# Sets the minimum version of CMake required to build the native
# library. You should either keep the default value or only pass a
# value of 3.4.0 or lower.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds it for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
             native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             # Associated headers in the same location as their source
             # file are automatically included.
             src/main/cpp/native-lib.cpp )

add_library(
			lua

			SHARED

			src/main/cpp/lua-5.3.2/src/lapi.c
			src/main/cpp/lua-5.3.2/src/lauxlib.c
			src/main/cpp/lua-5.3.2/src/lbaselib.c
			src/main/cpp/lua-5.3.2/src/lbitlib.c
			src/main/cpp/lua-5.3.2/src/lcode.c
			src/main/cpp/lua-5.3.2/src/lcorolib.c
			src/main/cpp/lua-5.3.2/src/lctype.c
			src/main/cpp/lua-5.3.2/src/ldblib.c
			src/main/cpp/lua-5.3.2/src/ldebug.c
			src/main/cpp/lua-5.3.2/src/ldo.c
			src/main/cpp/lua-5.3.2/src/ldump.c
			src/main/cpp/lua-5.3.2/src/lfunc.c
			src/main/cpp/lua-5.3.2/src/lgc.c
			src/main/cpp/lua-5.3.2/src/linit.c
			src/main/cpp/lua-5.3.2/src/liolib.c
			src/main/cpp/lua-5.3.2/src/llex.c
			src/main/cpp/lua-5.3.2/src/lmathlib.c
			src/main/cpp/lua-5.3.2/src/lmem.c
			src/main/cpp/lua-5.3.2/src/loadlib.c
			src/main/cpp/lua-5.3.2/src/lobject.c
			src/main/cpp/lua-5.3.2/src/lopcodes.c
			src/main/cpp/lua-5.3.2/src/loslib.c
			src/main/cpp/lua-5.3.2/src/lparser.c
			src/main/cpp/lua-5.3.2/src/lstate.c
			src/main/cpp/lua-5.3.2/src/lstring.c
			src/main/cpp/lua-5.3.2/src/lstrlib.c
			src/main/cpp/lua-5.3.2/src/ltable.c
			src/main/cpp/lua-5.3.2/src/ltablib.c
			src/main/cpp/lua-5.3.2/src/ltm.c
			src/main/cpp/lua-5.3.2/src/lundump.c
			src/main/cpp/lua-5.3.2/src/lutf8lib.c
			src/main/cpp/lua-5.3.2/src/lvm.c
			src/main/cpp/lua-5.3.2/src/lzio.c
)

target_compile_definitions(
            lua
            PUBLIC
            ANDROID_LUA=1
            LUA_USE_C89=1
)


# Searches for a specified prebuilt library and stores the path as a
# variable. Because system libraries are included in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in the
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       native-lib

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )
