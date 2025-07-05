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
import heronarts.lx.model.LXModel;

/**
 * Decoupling LX model-listening behavior from storage implementation.
 */
public abstract class ModelBuffer<T extends LXArrayBuffer<T>> implements LXArrayBuffer<T> {

  protected final LX lx;
  private final LX.Listener modelListener = new LX.Listener() {
    @Override
    public void modelChanged(LX lx, LXModel model) {
      initArray(model.size);
    }
  };

  /**
   * Provide a generic buffer implementation - if size is already set correctly,
   * our initArray() implementation will check the length first and skip it.
   */
  protected ModelBuffer(LX lx) {
    this.lx = lx;
    lx.addListener(this.modelListener);
  }

  public void dispose() {
    this.lx.removeListener(this.modelListener);
  }

  public static ModelBuffer shim(LX lx) {
    return shim(lx, 0, SHIM_MODEL_DELEGATE);
  }

  public static ModelBuffer shim(LX lx, int defaultColor) {
    return shim(lx, defaultColor, SHIM_MODEL_DELEGATE);
  }

  public static ModelBuffer shim(LX lx, int defaultColor, boolean useDelegate) {
    if (useDelegate) {
      return new ModelDelegateBuffer<>(lx, defaultColor);
    } else {
      return new ModelArrayBuffer<>(lx, defaultColor);
    }
  }
}
