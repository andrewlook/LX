package heronarts.lx.buffer;

import java.util.Arrays;

public interface BufferUtils {

  static void bufferFill(int[] arr, int color) {
    Arrays.fill(arr, color);
  }

  static int bufferLength(int[] arr) {
    return arr.length;
  }

  static void bufferSet(int[] arr, int idx, int color) {
    arr[idx] = color;
  }

  static void bufferFill(LXBuffer<?> buf, int color) {
    throw new RuntimeException("not implemented");
  }

  static int bufferLength(LXBuffer<?> buf) {
    throw new RuntimeException("not implemented");
  }
}
