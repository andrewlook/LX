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

public interface LXIntArrayBuffer {
  public int[] getArray();

  public default LXIntArrayBuffer copyTo(LXIntArrayBuffer that) {
    final int[] array = getArray();
    System.arraycopy(array, 0, that.getArray(), 0, array.length);
    return this;
  }

  public default LXIntArrayBuffer copyFrom(LXIntArrayBuffer that) {
    final int[] array = getArray();
    System.arraycopy(that.getArray(), 0, array, 0, array.length);
    return this;
  }
}
