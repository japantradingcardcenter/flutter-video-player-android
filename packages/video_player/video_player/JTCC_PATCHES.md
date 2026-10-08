# JTCC video_player patch

Based on official `video_player` 2.11.1, with the upstream monorepo layout retained.

The creation-completion signal now completes even when audio setup or native player creation throws. This allows `dispose()` to release the Dart controller and lifecycle observer without waiting indefinitely. An uninitialized native player ID is never passed to the platform's `dispose()` method. Initialization errors still propagate to the caller; successful player disposal continues to await platform cleanup.

Use this package through a Git dependency pinned to a tested full commit SHA, with `path: packages/video_player/video_player`. The Android implementation is maintained separately in the sibling `video_player_android` directory. Both patches are covered by the repository's Video player CI workflow.
