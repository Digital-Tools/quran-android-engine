// JNI bridge between SoundTouchAudioProcessor (Kotlin) and the vendored
// SoundTouch library. Converts ExoPlayer's interleaved 16-bit PCM to/from the
// float samples SoundTouch is built for.

#include <jni.h>

#include <algorithm>
#include <cmath>
#include <exception>
#include <vector>

#include "SoundTouch.h"

namespace {

struct Stretcher {
    soundtouch::SoundTouch soundTouch;
    int channels = 0;
    std::vector<float> scratch;
};

Stretcher* fromHandle(jlong handle) {
    return reinterpret_cast<Stretcher*>(handle);
}

void ensureScratch(Stretcher* s, int frames) {
    const size_t needed = static_cast<size_t>(frames) * s->channels;
    if (s->scratch.size() < needed) s->scratch.resize(needed);
}

}  // namespace

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_quranengine_core_audioplayer_SoundTouch_nativeCreate(
        JNIEnv*, jclass, jint sampleRate, jint channels, jfloat tempo) {
    try {
        auto* s = new Stretcher();
        s->channels = channels;
        s->soundTouch.setSampleRate(static_cast<unsigned int>(sampleRate));
        s->soundTouch.setChannels(static_cast<unsigned int>(channels));
        s->soundTouch.setRate(1.0);
        s->soundTouch.setPitch(1.0);
        s->soundTouch.setTempo(tempo);
        return reinterpret_cast<jlong>(s);
    } catch (const std::exception&) {
        return 0;
    }
}

JNIEXPORT void JNICALL
Java_com_quranengine_core_audioplayer_SoundTouch_nativeRelease(JNIEnv*, jclass, jlong handle) {
    delete fromHandle(handle);
}

JNIEXPORT void JNICALL
Java_com_quranengine_core_audioplayer_SoundTouch_nativePutSamples(
        JNIEnv* env, jclass, jlong handle, jshortArray samples, jint frames) {
    Stretcher* s = fromHandle(handle);
    if (frames <= 0) return;
    ensureScratch(s, frames);
    const jsize count = frames * s->channels;
    jshort* in = env->GetShortArrayElements(samples, nullptr);
    for (jsize i = 0; i < count; ++i) s->scratch[i] = in[i] / 32768.0f;
    env->ReleaseShortArrayElements(samples, in, JNI_ABORT);
    s->soundTouch.putSamples(s->scratch.data(), static_cast<unsigned int>(frames));
}

JNIEXPORT jint JNICALL
Java_com_quranengine_core_audioplayer_SoundTouch_nativeReceiveSamples(
        JNIEnv* env, jclass, jlong handle, jshortArray output, jint maxFrames) {
    Stretcher* s = fromHandle(handle);
    if (maxFrames <= 0) return 0;
    ensureScratch(s, maxFrames);
    const unsigned int frames =
            s->soundTouch.receiveSamples(s->scratch.data(), static_cast<unsigned int>(maxFrames));
    if (frames == 0) return 0;
    const jsize count = static_cast<jsize>(frames) * s->channels;
    jshort* out = env->GetShortArrayElements(output, nullptr);
    for (jsize i = 0; i < count; ++i) {
        const float v = std::lround(s->scratch[i] * 32768.0f);
        out[i] = static_cast<jshort>(std::clamp(v, -32768.0f, 32767.0f));
    }
    env->ReleaseShortArrayElements(output, out, 0);
    return static_cast<jint>(frames);
}

JNIEXPORT jint JNICALL
Java_com_quranengine_core_audioplayer_SoundTouch_nativeAvailableFrames(JNIEnv*, jclass, jlong handle) {
    return static_cast<jint>(fromHandle(handle)->soundTouch.numSamples());
}

JNIEXPORT jint JNICALL
Java_com_quranengine_core_audioplayer_SoundTouch_nativeUnprocessedFrames(JNIEnv*, jclass, jlong handle) {
    return static_cast<jint>(fromHandle(handle)->soundTouch.numUnprocessedSamples());
}

JNIEXPORT void JNICALL
Java_com_quranengine_core_audioplayer_SoundTouch_nativeFlush(JNIEnv*, jclass, jlong handle) {
    fromHandle(handle)->soundTouch.flush();
}

JNIEXPORT void JNICALL
Java_com_quranengine_core_audioplayer_SoundTouch_nativeClear(JNIEnv*, jclass, jlong handle) {
    fromHandle(handle)->soundTouch.clear();
}

}  // extern "C"
