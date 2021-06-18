# 设置cmake最低版本
cmake_minimum_required(VERSION 3.4.1)

set(jnilibs "${CMAKE_SOURCE_DIR}/../jniLibs")

# # 查找当前目录下的所有源文件
## 并将名称保存到 SOURCE 变量
aux_source_directory(. SOURCE)

add_library(
        native-lib
        SHARED
        ${SOURCE} )

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
        )