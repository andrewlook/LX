package heronarts.lx.buffer;

import heronarts.lx.color.LXColor;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.cpu.nativecpu.NDArray;
import org.nd4j.linalg.factory.Nd4j;

public class TensorBufferPrototype {

  private INDArray array;
  private final int defaultColor;

  public TensorBufferPrototype(int numPoints) {
    this(numPoints, 0);
  }

  public TensorBufferPrototype(int numPoints, int defaultColor) {
    this.defaultColor = defaultColor;
    initArray(numPoints);
  }

  public void initArray(int numPoints) {
    final long[] shape = new long[]{numPoints, 4};
    final long[] stride = Nd4j.getStrides(shape);
    final long offset = 0L;
    char ordering = 'c'; // column order?

    this.array = new NDArray(org.nd4j.linalg.api.buffer.DataType.UINT8, stride, shape, offset, ordering);
  }

  public int length() {
    return (int) this.array.shape()[0];
  }

  /**
   * Equivalent of getArray(): for read-only use cases, we could swizzle back into an int[] array.
   * For use cases that need direct modification, we'd need to find those and replace the direct access
   * somehow.
   */
  public int[] readOnlyArray() {
    return new int[0];
  }

  public int[] writableArray() {
    return new int[0];
  }

  /**
   * Adapter for loading up int[]
   */
  public TensorBufferPrototype setFromIntArray(int[] arr) {
    for (int i = 0; i < arr.length; i++) {
      final int color = arr[i];
      final byte red = LXColor.red(color);
      final byte blue = LXColor.blue(color);
      final byte green = LXColor.green(color);
      final byte alpha = LXColor.alpha(color);
      // DO something...
    }
    return null;
  }
}
