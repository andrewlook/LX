package heronarts.lx.buffer;

import java.util.Arrays;

import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import org.junit.jupiter.api.BeforeEach;

class LXTensorBufferTest {

  static final int[] TEST_DEST = new int[]{
      LXColor.BLACK,
      LXColor.WHITE,
      LXColor.GREEN,
      LXColor.BLACK,
  };
  static final int[] TEST_SOURCE = new int[]{
      LXColor.WHITE,
      LXColor.BLACK,
      LXColor.RED,
      LXColor.GREEN
  };
  static final LXPoint[] TEST_POINTS = new LXPoint[]{
      new LXPoint(0f, 1f),
      new LXPoint(0f, 2f),
      new LXPoint(0f, 3f),
      new LXPoint(0f, 4f)
  };

  private TensorBufferPrototype src;
  private TensorBufferPrototype dst;

  @BeforeEach
  void setUp() {
    src = new TensorBufferPrototype(TEST_POINTS.length, 0);
    src.setFromIntArray(TEST_SOURCE);

    dst = new TensorBufferPrototype(TEST_POINTS.length, 0);
    dst.setFromIntArray(TEST_DEST);
  }

  private void bprint(int x) {
    System.out.printf("0x%02X%n", x);
  }

  private void bprint(int[] out) {
    System.out.println("---------");
    Arrays.stream(out).forEach(this::bprint);
  }
}