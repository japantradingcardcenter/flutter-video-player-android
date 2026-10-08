# video\_player\_android

The Android implementation of [`video_player`][1].

## JTCC fork

This fork applies texture crop fixes to the official `video_player_android 2.9.6` release. The upstream repository structure and history are preserved. The maintenance branch is `jtcc/video-player-android-2.9.6`; `main` tracks the upstream repository.

Use the Git URL below and pin `ref` to a tested commit on the maintenance branch:

```yaml
dependency_overrides:
  video_player_android:
    git:
      url: https://github.com/japantradingcardcenter/flutter-video-player-android
      ref: <tested-commit-sha>
      path: packages/video_player/video_player_android
```

See [patch notes and maintenance](JTCC_PATCHES.md) for the changes and device checks. Run `flutter analyze --no-pub` and `bash tool/test.sh` from this package directory with Flutter 3.47.1, Java 21, and the Android SDK.

## Upstream usage

This package is [endorsed][2], which means you can simply use `video_player`
normally. This package will be automatically included in your app when you do,
so you do not need to add it to your `pubspec.yaml`.

However, if you `import` this package to use any of its APIs directly, you
should add it to your `pubspec.yaml` as usual.

## Known issues

Using `VideoViewType.platformView` is not currently recommended on Android due to a known [issue][3] affecting platform views on Android.

[1]: https://pub.dev/packages/video_player
[2]: https://flutter.dev/to/endorsed-federated-plugin
[3]: https://github.com/flutter/flutter/issues/164899
