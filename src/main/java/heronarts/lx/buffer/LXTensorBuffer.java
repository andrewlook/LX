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

public interface LXTensorBuffer<T extends LXBuffer<T>> extends LXBuffer<T> {

  default T copyTo(LXBuffer<?> that) {
    if (that instanceof LXTensorBuffer) {
      throw new RuntimeException("not implemented");
    } else if (that instanceof LXArrayBuffer<?>) {
      System.arraycopy(this.readOnlyArray(), 0, that.writableArray(), 0, this.length());
    } else {
      throw new RuntimeException("unrecognized type");
    }
    return self();
  }

  default T copyFrom(LXBuffer<?> that) {
    if (that instanceof LXTensorBuffer) {
      throw new RuntimeException("not implemented");
    } else if (that instanceof LXArrayBuffer) {
      System.arraycopy(that.readOnlyArray(), 0, this.writableArray(), 0, this.length());
    } else {
      throw new RuntimeException("unrecognized type");
    }
    return self();
  }

  @Override
  default T self() {
    return (T) this;
  }
}
