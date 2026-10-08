#!/usr/bin/env bash
# Copyright 2013 The Flutter Authors
# Use of this source code is governed by a BSD-style license that can be
# found in the LICENSE file.

set -euo pipefail

# A minimal host avoids app signing/configuration requirements. Robolectric SDK 36 needs Java 21.
plugin_root="$(cd "$(dirname "$0")/.." && pwd -P)"
cd "$plugin_root"
flutter pub get
flutter test --no-pub

host_dir="$(mktemp -d "${TMPDIR:-/tmp}/jtcc-video-player-test.XXXXXX")"
host_dir="$(cd "$host_dir" && pwd -P)"
trap 'rm -rf "$host_dir"' EXIT

flutter create --platforms=android --org com.jtcc.test --project-name video_player_test_host --no-pub "$host_dir"
cat > "$host_dir/pubspec.yaml" <<YAML
name: video_player_test_host
publish_to: none
environment:
  sdk: ^3.13.0
dependencies:
  flutter:
    sdk: flutter
  video_player_android:
    path: "$plugin_root"
YAML
cd "$host_dir"
flutter pub get
# Resolve Flutter's debug embedding for the plugin before its JVM tests.
flutter build apk --debug --target-platform android-arm64
cd android
./gradlew :video_player_android:testDebugUnitTest
