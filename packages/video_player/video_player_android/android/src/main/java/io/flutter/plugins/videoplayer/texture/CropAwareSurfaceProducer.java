// Copyright 2013 The Flutter Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.
// Copyright 2026 Japan Trading Card Center. All rights reserved.

package io.flutter.plugins.videoplayer.texture;

import android.graphics.SurfaceTexture;
import android.view.Surface;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.flutter.view.TextureRegistry;
import java.util.function.Function;

/**
 * Uses Flutter's registered SurfaceTexture so its transform includes the decoder crop rectangle.
 *
 * <p>The ImageReader path in Flutter 3.47.1 paints the full HardwareBuffer (for example 1088 pixels
 * for a 1080-pixel AVC video). SurfaceTexture preserves the visible crop without changing the app's
 * renderer or guessing codec padding. The surface is retained until player disposal, including
 * while paused/backgrounded, matching Flutter's manual SurfaceTexture lifecycle.
 */
public final class CropAwareSurfaceProducer implements TextureRegistry.SurfaceProducer {
  private final TextureRegistry.SurfaceTextureEntry texture;
  private final Function<SurfaceTexture, Surface> surfaceFactory;
  @Nullable private Surface surface;
  private int width;
  private int height;
  private boolean released;

  public CropAwareSurfaceProducer(@NonNull TextureRegistry.SurfaceTextureEntry texture) {
    this(texture, Surface::new);
  }

  @VisibleForTesting
  CropAwareSurfaceProducer(
      @NonNull TextureRegistry.SurfaceTextureEntry texture,
      @NonNull Function<SurfaceTexture, Surface> surfaceFactory) {
    this.texture = texture;
    this.surfaceFactory = surfaceFactory;
  }

  @Override
  public long id() {
    return texture.id();
  }

  @Override
  public void setSize(int width, int height) {
    checkNotReleased();
    texture.surfaceTexture().setDefaultBufferSize(width, height);
    this.width = width;
    this.height = height;
  }

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public int getHeight() {
    return height;
  }

  @Override
  @NonNull
  public Surface getSurface() {
    checkNotReleased();
    if (surface == null) {
      surface = surfaceFactory.apply(texture.surfaceTexture());
    }
    return surface;
  }

  @Override
  @NonNull
  public Surface getForcedNewSurface() {
    checkNotReleased();
    if (surface != null) {
      surface.release();
      surface = null;
    }
    return getSurface();
  }

  @Override
  public void setCallback(@Nullable Callback callback) {
    // This manual surface is not destroyed by ImageReader background cleanup. No callbacks needed.
  }

  @Override
  public void scheduleFrame() {
    // createSurfaceTexture registers Flutter's frame listener; new decoder frames schedule
    // painting.
  }

  @Override
  public boolean handlesCropAndRotation() {
    return true;
  }

  @Override
  public void release() {
    if (released) {
      return;
    }
    released = true;
    if (surface != null) {
      surface.release();
      surface = null;
    }
    texture.release();
  }

  private void checkNotReleased() {
    if (released) {
      throw new IllegalStateException("Video surface has been released");
    }
  }
}
