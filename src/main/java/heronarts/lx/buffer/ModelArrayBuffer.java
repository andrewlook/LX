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

import java.util.Arrays;

import heronarts.lx.LX;

public class ModelArrayBuffer extends ModelBuffer {

  private int[] array;
  private final int defaultColor;

  public ModelArrayBuffer(LX lx) {
    this(lx, 0);
  }

  public ModelArrayBuffer(LX lx, int defaultColor) {
    super(lx);
    this.defaultColor = defaultColor;
    initArray(lx.getModel().size);
  }

  @Override
  public void initArray(int numPoints) {
    this.array = new int[numPoints];
    Arrays.fill(this.array, this.defaultColor);
  }

  @Override
  public int[] getArray() {
    return this.array;
  }

  @Override
  public ModelArrayBuffer setFromIntArray(int[] arr) {
    this.array = arr;
    return this;
  }
}
