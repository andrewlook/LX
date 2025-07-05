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

public interface LXIntArrayBuffer extends LXBuffer<LXIntArrayBuffer> {
  int[] getArray();

  default LXIntArrayBuffer copyTo(LXBuffer<?> that) {
    if (that instanceof LXIntArrayBuffer) {
      final int[] array = getArray();
      System.arraycopy(array, 0, ((LXIntArrayBuffer) that).getArray(), 0, array.length);
    }
    return self();
  }

  default LXIntArrayBuffer copyFrom(LXBuffer<?> that) {
    if (that instanceof LXIntArrayBuffer) {
      final int[] array = getArray();
      System.arraycopy(((LXIntArrayBuffer) that).getArray(), 0, array, 0, array.length);
    }
    return self();
  }

  @Override
  default int[] toIntArray() {
    return getArray();
  }

  @Override
  default LXIntArrayBuffer self() {
    return this;
  }
}
