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
public abstract class ModelBuffer implements LXArrayBuffer {

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
  public ModelBuffer(LX lx) {
    this.lx = lx;
    lx.addListener(this.modelListener);
  }

  public void dispose() {
    this.lx.removeListener(this.modelListener);
  }

  public static LXArrayBuffer shimModelBuffer(LX lx) {
    return shimModelBuffer(lx, 0);
  }

  public static ModelBuffer shimModelBuffer(LX lx, int defaultColor) {
    if (SHIM_MODEL_DELEGATE) {
      return new ModelDelegateBuffer(lx, defaultColor);
    } else {
      return new ModelArrayBuffer(lx, defaultColor);
    }
  }
}
