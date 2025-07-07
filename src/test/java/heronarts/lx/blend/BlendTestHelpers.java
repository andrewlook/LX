package heronarts.lx.blend;

import java.util.Arrays;

import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;

public class BlendTestHelpers {

  public static final int[] TEST_DEST = new int[]{
      LXColor.BLACK,
      LXColor.WHITE,
      LXColor.GREEN,
      LXColor.BLACK,
  };
  public static final int[] TEST_SOURCE = new int[]{
      LXColor.WHITE,
      LXColor.BLACK,
      LXColor.RED,
      LXColor.GREEN
  };
  public static final int[] TEST_BLACK = new int[]{
      LXColor.BLACK,
      LXColor.BLACK,
      LXColor.BLACK,
      LXColor.BLACK,
  };

  // result of blend(dst, src, 1.0, out)
  public static int[] EXPECTED_FULL_ALPHA = new int[]{
      0xFFFFFFFF, // WHITE + BLACK
      0xFFFFFFFF, // BLACK + WHITE
      0xFFFFFF00, // RED   + GREEN
      0xFF00FF00, // BLACK + GREEN
  };
  // result of blend(dst, src, 0.5, out)
  public static final int[] EXPECTED_HALF_ALPHA = new int[]{
      0xFF7F7F7F, // BLACK + (0.5 * WHITE)
      0xFFFFFFFF, // WHITE + (0.5 * BLACK)
      0xFF7FFF00, // GREEN + (0.5 * RED)
      0xFF007F00, // BLACK + (0.5 * GREEN)
  };
  // result of blend(dst, src, 1.0, dst)
  public static int[] EXPECTED_SAME_DEST_AND_OUTPUT = new int[]{
      0xFFFFFFFF,
      0xFF000000,
      0xFFFF0000,
      0xFF00FF00
  };

  public static final LXPoint[] TEST_POINTS = new LXPoint[]{
      new LXPoint(0f, 1f),
      new LXPoint(0f, 2f),
      new LXPoint(0f, 3f),
      new LXPoint(0f, 4f)
  };

  public static int[] copyOf(int[] arr) {
    int[] copy = new int[arr.length];
    System.arraycopy(arr, 0, copy, 0, arr.length);
    return copy;
  }

  public static void bprint(int x) {
    System.out.printf("0x%02X%n", x);
  }

  public static void bprint(int[] out) {
    System.out.println("---------");
    Arrays.stream(out).forEach(BlendTestHelpers::bprint);
  }

}
