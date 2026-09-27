# SoundTouch (vendored)

Time-stretch library used by `SoundTouchAudioProcessor` so faster/slower Quran
recitation keeps its pitch without Media3 Sonic's warble.

- Upstream: https://codeberg.org/soundtouch/soundtouch
- Version: 2.3.3 (commit `e83424d5928ab8513d2d082779c275765dee31b9`)
- License: LGPL-2.1 (see `COPYING.TXT`). Built as its own shared library,
  `libsoundtouch.so`, so it can be replaced independently of the app.
- Only the core time-stretch sources are copied (no BPM detection, SoundStretch,
  or platform samples). Files are unmodified.
