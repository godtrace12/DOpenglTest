# 设置cmake最低版本
cmake_minimum_required(VERSION 3.4.1)

set(jnilibs "${CMAKE_SOURCE_DIR}/../jniLibs")

# # 查找当前目录下的所有源文件
## 并将名称保存到 SOURCE 变量
aux_source_directory(. SOURCE)

include_directories(
        api
        render
        util
)

# 用了aux_source_directory，还要用file才能把cmakelists下级目录文件加进来编译？？
# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
file(GLOB src-files
        ${CMAKE_SOURCE_DIR}/*.cpp
        ${CMAKE_SOURCE_DIR}/render/*.cpp
        ${CMAKE_SOURCE_DIR}/render/base/*.cpp
        ${CMAKE_SOURCE_DIR}/render/RectangleRender.cpp
        ${CMAKE_SOURCE_DIR}/util/*.cpp
        ${CMAKE_SOURCE_DIR}/api/*.cpp)

add_library(
        native-lib
        SHARED
        ${SOURCE}
        ${src-files}
)

#引入头文件
#include_directories(render/base)

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log )

target_link_libraries( # Specifies the target library.
        native-lib

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}
        EGL
        GLESv3
        OpenSLES
        )