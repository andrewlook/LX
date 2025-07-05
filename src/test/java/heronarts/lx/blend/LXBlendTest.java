package heronarts.lx.blend;

import heronarts.lx.LX;
import heronarts.lx.ModelBuffer;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class LXBlendTest {

  static final int[] TEST_SOURCE = new int[]{
      LXColor.WHITE,
      LXColor.BLACK,
      LXColor.RED,
      LXColor.GREEN
  };
  static final int[] TEST_DEST = new int[]{
      LXColor.BLACK,
      LXColor.WHITE,
      LXColor.GREEN,
      LXColor.BLACK,
  };
  static final LXPoint[] TEST_POINTS = new LXPoint[]{
      new LXPoint(0f, 1f),
      new LXPoint(0f, 2f),
      new LXPoint(0f, 3f),
      new LXPoint(0f, 4f)
  };

  private final LXModel mockModel = new LXModel(List.of(TEST_POINTS));
  private final LX mockLX = new LX(mockModel);
  private final LXBlend add = new AddBlend(mockLX);

  private ModelBuffer src;
  private ModelBuffer dst;
  private ModelBuffer out;

  @BeforeEach
  void setUp() {
    src = ModelBuffer.fromArray(mockLX, TEST_SOURCE);
    dst = ModelBuffer.fromArray(mockLX, TEST_DEST);
    out = ModelBuffer.fromArray(mockLX, new int[]{
        LXColor.BLACK,
        LXColor.BLACK,
        LXColor.BLACK,
        LXColor.BLACK,
    });
  }

  @Test
  void testAddBlendFullAlpha() {
    int[] expected = new int[]{
        0xFFFFFFFF, // WHITE + BLACK
        0xFFFFFFFF, // BLACK + WHITE
        0xFFFFFF00, // RED   + GREEN
        0xFF00FF00, // BLACK + GREEN
    };
    add.blend(src, dst, 1.0, out, mockModel);
    assertArrayEquals(expected, out.getArray());
  }

  @Test
  void testAddBlendHalfAlpha() {
    int[] expected = new int[]{
        0xFFFFFFFF, // WHITE + (0.5 * BLACK)
        0xFF7F7F7F, // BLACK + (0.5 * WHITE)
        0xFFFF7F00, // RED   + (0.5 * GREEN)
        0xFF00FF00, // BLACK + (0.5 * GREEN)
    };
    add.blend(src, dst, 0.5, out, mockModel);
    bprint(expected);
    bprint(out.getArray());
    assertArrayEquals(expected, out.getArray());
  }

  private void bprint(int x) {
    System.out.printf("0x%02X%n", x);
  }

  private void bprint(int[] out) {
    System.out.println("---------");
    Arrays.stream(out).forEach(this::bprint);
  }
}