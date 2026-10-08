// Copyright 2013 The Flutter Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.videoplayer.texture;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.graphics.SurfaceTexture;
import android.view.Surface;
import io.flutter.view.TextureRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class CropAwareSurfaceProducerTest {
  @Test
  public void usesRegisteredTextureAndPreservesRequestedVideoSize() {
    TextureRegistry.SurfaceTextureEntry entry = mock(TextureRegistry.SurfaceTextureEntry.class);
    SurfaceTexture texture = mock(SurfaceTexture.class);
    when(entry.id()).thenReturn(42L);
    when(entry.surfaceTexture()).thenReturn(texture);
    CropAwareSurfaceProducer producer = new CropAwareSurfaceProducer(entry);
    producer.setSize(1080, 1920);
    assertEquals(42L, producer.id());
    assertEquals(1080, producer.getWidth());
    assertEquals(1920, producer.getHeight());
    verify(texture).setDefaultBufferSize(1080, 1920);
    assertTrue(producer.handlesCropAndRotation());
  }

  @Test
  public void retainsSurfaceUntilDisposedAndReleasesResourcesOnce() {
    TextureRegistry.SurfaceTextureEntry entry = mock(TextureRegistry.SurfaceTextureEntry.class);
    Surface surface = mock(Surface.class);
    CropAwareSurfaceProducer producer = new CropAwareSurfaceProducer(entry, ignored -> surface);
    TextureRegistry.SurfaceProducer.Callback callback =
        mock(TextureRegistry.SurfaceProducer.Callback.class);
    assertSame(surface, producer.getSurface());
    producer.setCallback(callback);
    assertSame(surface, producer.getSurface());
    verifyNoInteractions(callback);
    producer.release();
    producer.release();
    verify(surface).release();
    verify(entry).release();
    assertThrows(IllegalStateException.class, producer::getSurface);
    assertThrows(IllegalStateException.class, () -> producer.setSize(1080, 1920));
  }

  @Test
  public void forcedReplacementReleasesOldSurfaceAndDisposeReleasesReplacement() {
    TextureRegistry.SurfaceTextureEntry entry = mock(TextureRegistry.SurfaceTextureEntry.class);
    Surface first = mock(Surface.class);
    Surface second = mock(Surface.class);
    java.util.ArrayDeque<Surface> surfaces = new java.util.ArrayDeque<>();
    surfaces.add(first);
    surfaces.add(second);
    CropAwareSurfaceProducer producer =
        new CropAwareSurfaceProducer(entry, ignored -> surfaces.remove());
    assertSame(first, producer.getSurface());
    assertSame(second, producer.getForcedNewSurface());
    verify(first).release();
    verify(second, never()).release();
    producer.release();
    verify(second).release();
  }

  @Test
  public void disposeBeforeSurfaceCreationDoesNotAllocateSurface() {
    TextureRegistry.SurfaceTextureEntry entry = mock(TextureRegistry.SurfaceTextureEntry.class);
    CropAwareSurfaceProducer producer =
        new CropAwareSurfaceProducer(
            entry,
            ignored -> {
              throw new AssertionError("Surface should not be allocated during disposal");
            });
    producer.release();
    verify(entry).release();
    assertThrows(IllegalStateException.class, producer::getForcedNewSurface);
  }
}
