// Copyright 2013 The Flutter Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:video_player/video_player.dart';
import 'package:video_player_platform_interface/video_player_platform_interface.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  for (final failAudioSetup in <bool>[true, false]) {
    test(
      'disposes controller after ${failAudioSetup ? 'audio setup' : 'native creation'} failure',
      () async {
        final VideoPlayerPlatform original = VideoPlayerPlatform.instance;
        final platform = _FailingPlatform(failAudioSetup: failAudioSetup);
        VideoPlayerPlatform.instance = platform;
        final controller = VideoPlayerController.networkUrl(
          Uri.parse('https://example.com/video.m3u8'),
          videoPlayerOptions: VideoPlayerOptions(mixWithOthers: true),
        );
        addTearDown(() {
          VideoPlayerPlatform.instance = original;
        });
        final Future<void> initialization = controller.initialize();
        final Future<void> initializationCheck = expectLater(
          initialization,
          throwsA(isA<PlatformException>()),
        );
        await platform.started.future;
        final Future<void> disposal = controller.dispose();
        platform.fail.complete();
        await initializationCheck;
        await disposal.timeout(const Duration(seconds: 5));
        expect(platform.disposedIds, isEmpty);
        // The Dart notifier is disposed too, rather than merely abandoning its Future.
        expect(
          () => controller.addListener(() {}),
          throwsA(isA<FlutterError>()),
        );
        await controller.dispose();
      },
    );
  }
}

class _FailingPlatform extends VideoPlayerPlatform {
  _FailingPlatform({required this.failAudioSetup});

  final bool failAudioSetup;
  final Completer<void> started = Completer<void>();
  final Completer<void> fail = Completer<void>();
  final List<int> disposedIds = <int>[];

  @override
  Future<void> init() async {}

  Future<Never> _fail() async {
    started.complete();
    await fail.future;
    throw PlatformException(code: 'creation_failed');
  }

  @override
  Future<void> setMixWithOthers(bool mixWithOthers) async {
    if (failAudioSetup) {
      await _fail();
    }
  }

  @override
  Future<int?> createWithOptions(VideoCreationOptions options) => _fail();

  @override
  Future<void> dispose(int playerId) async {
    disposedIds.add(playerId);
  }
}
