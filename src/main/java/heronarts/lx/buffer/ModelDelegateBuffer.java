/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
 *
 * This file is part of the LX Studio software library. By using
 * LX, you agree to the terms of the LX Studio Software License
 * and Distribution Agreement, available at: http://lx.studio/license
 *
 * Please note that the LX license is not open-source. The license
 * allows for free, non-commercial use.
 *
 * HERON ARTS MAKES NO WARRANTY, EXPRESS, IMPLIED, STATUTORY, OR
 * OTHERWISE, AND SPECIFICALLY DISCLAIMS ANY WARRANTY OF
 * MERCHANTABILITY, NON-INFRINGEMENT, OR FITNESS FOR A PARTICULAR
 * PURPOSE, WITH RESPECT TO THE SOFTWARE.
 *
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.lx.buffer;

import heronarts.lx.LX;

/**
 * Decoupling LX model-listening behavior from storage implementation.
 */
public class ModelDelegateBuffer<T extends LXArrayBuffer<T>> extends ModelBuffer<T> {

  private final LXBuffer<?> buffer;

  /**
   * Provide a generic buffer implementation - if size is already set correctly,
   * our initArray() implementation will check the length first and skip it.
   */
  protected ModelDelegateBuffer(LX lx, LXBuffer<?> buf) {
    super(lx);
    this.buffer = buf;
    this.initArray(lx.getModel().size);
  }

  // Shim constructors - internalize choice of buffer implementation
  protected ModelDelegateBuffer(LX lx, int defaultColor) {
    this(lx, new LXArrayBufferImpl<>(lx.getModel().size, defaultColor));
  }

  public ModelDelegateBuffer(LX lx) {
    this(lx, 0);
  }

  // -----------------------------------------------------------------
  // Delegated methods - use the child buffer for these.
  //
  // Note: length(), toIntArray() are implemented by getArray(), so
  // they don't need to be overridden here.
  // -----------------------------------------------------------------
  @Override
  public int[] getArray() {
    return this.buffer.getArray();
  }

  @Override
  public void initArray(int numPoints) {
    if (buffer.length() != numPoints) {
      this.buffer.initArray(numPoints);
    }
  }

  @Override
  public T setFromIntArray(int[] arr) {
    this.buffer.setFromIntArray(arr);
    return self();
  }
}
