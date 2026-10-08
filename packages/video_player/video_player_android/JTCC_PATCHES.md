# Patch notes and maintenance

Based on `video_player_android 2.9.6` at upstream commit `0bceb6429cb5f46cafee519f51e924c1b53d3199`. The upstream license, authors, tests, and example app are preserved.

## Texture crop fix

Texture-based players use `CropAwareSurfaceProducer`, which wraps a registered Flutter `SurfaceTexture`, instead of the ImageReader-based surface producer.

This addresses extra padding when an AVC video's display width differs from the decoder buffer width. The SurfaceTexture transform matrix handles the visible region without a fixed pixel crop.

The implementation changes are limited to `VideoPlayerPlugin.java` and `texture/CropAwareSurfaceProducer.java`. Two corresponding Android test files cover texture selection and surface reuse, replacement, and disposal.

The Dart API, Pigeon messages, ExoPlayer version, quality and buffering settings, and PlatformView path are unchanged. Surfaces remain available while playback is stopped or the app is in the background, and are released when the player is disposed.

## Updating the package

1. Check whether upstream has resolved the crop issue. If so, remove the app's dependency override and return to the official package.
2. Otherwise, review the upstream changes and apply only the patches that are still needed.
3. Run `flutter analyze` and `bash tool/test.sh`.
4. Verify aspect ratio, rotation, color inversion, pause/resume, and background recovery on a device. Pay particular attention to Flutter Texture API changes.
5. Update the commit SHA pinned by the app and run its regression tests.

Recheck compatibility and whether the patch is still needed when updating Flutter or the upstream package. Unit tests do not replace device checks for rendering quality and performance.
