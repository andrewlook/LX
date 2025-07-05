/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
 * <p>
 * This file is part of the LX Studio software library. By using
 * LX, you agree to the terms of the LX Studio Software License
 * and Distribution Agreement, available at: http://lx.studio/license
 * <p>
 * Please note that the LX license is not open-source. The license
 * allows for free, non-commercial use.
 * <p>
 * HERON ARTS MAKES NO WARRANTY, EXPRESS, IMPLIED, STATUTORY, OR
 * OTHERWISE, AND SPECIFICALLY DISCLAIMS ANY WARRANTY OF
 * MERCHANTABILITY, NON-INFRINGEMENT, OR FITNESS FOR A PARTICULAR
 * PURPOSE, WITH RESPECT TO THE SOFTWARE.
 *
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.lx;

import java.util.Arrays;

import heronarts.lx.model.LXModel;

public class ModelBuffer implements LXBuffer {

  private final LX lx;
  private int[] array;
  private final int defaultColor;

  private final LX.Listener modelListener = new LX.Listener() {
    @Override
    public void modelChanged(LX lx, LXModel model) {
//      synchronized (array) {
//        if (array.length != model.size) {
//          initArray(model.size);
//        }
//      }
    }
  };

  public ModelBuffer(LX lx) {
    this(lx, 0);
  }

  public ModelBuffer(LX lx, int defaultColor) {
    this.lx = lx;
    this.defaultColor = defaultColor;
    initArray(lx.model.size);
    lx.addListener(this.modelListener);
  }

  /**
   * Package-protected version for unit testing, to make it easier to set up
   * specific array values.
   */
  public static ModelBuffer fromArray(LX lx, int[] arr) {
    ModelBuffer buf = new ModelBuffer(lx);

    int size = buf.getArray().length;
    if (arr.length != size) {
      throw new RuntimeException("Mismatched buffer size vs. num model points");
    }
    System.arraycopy(arr, 0, buf.array, 0, size);
    return buf;
  }

  public static ModelBuffer copyOf(LX lx, ModelBuffer that) {
    ModelBuffer buf = new ModelBuffer(lx, that.defaultColor);
    if (buf.getArray().length > 0) {
      return (ModelBuffer) buf.copyFrom(that);
    }
    return buf;
  }

  private void initArray(int size) {
    this.array = new int[size];
    Arrays.fill(this.array, this.defaultColor);
  }

  public int[] getArray() {
    return this.array;
  }

  public void dispose() {
    this.lx.removeListener(this.modelListener);
  }

}
