# JNI symbols in libquran_soundtouch.so are bound by class + method name.
-keepclasseswithmembernames class com.quranengine.core.audioplayer.SoundTouch {
    native <methods>;
}
