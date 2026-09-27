cmake_minimum_required(VERSION 3.22.1)
project(quran_soundtouch CXX)

set(CMAKE_CXX_STANDARD 17)

# SoundTouch stays its own shared library (LGPL-2.1): it is linked dynamically
# and can be swapped without rebuilding the rest of the engine.
add_library(soundtouch SHARED
    soundtouch/source/AAFilter.cpp
    soundtouch/source/FIFOSampleBuffer.cpp
    soundtouch/source/FIRFilter.cpp
    soundtouch/source/InterpolateCubic.cpp
    soundtouch/source/InterpolateLinear.cpp
    soundtouch/source/InterpolateShannon.cpp
    soundtouch/source/RateTransposer.cpp
    soundtouch/source/SoundTouch.cpp
    soundtouch/source/TDStretch.cpp
    soundtouch/source/cpu_detect_x86.cpp
    soundtouch/source/sse_optimized.cpp
)
target_include_directories(soundtouch PUBLIC soundtouch/include)
target_compile_options(soundtouch PRIVATE -O2 -ffast-math -fexceptions)

add_library(quran_soundtouch SHARED soundtouch_jni.cpp)
target_link_libraries(quran_soundtouch soundtouch)
target_compile_options(quran_soundtouch PRIVATE -O2 -fexceptions)

# Google Play requires 16 KB page alignment for native libraries.
foreach(lib soundtouch quran_soundtouch)
    target_link_options(${lib} PRIVATE "-Wl,-z,max-page-size=16384")
endforeach()
