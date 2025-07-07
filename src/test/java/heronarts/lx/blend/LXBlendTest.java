package heronarts.lx.blend;

import java.util.Arrays;
import java.util.List;

import heronarts.lx.LX;
import heronarts.lx.buffer.ModelBuffer;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static heronarts.lx.blend.LXBlend.BlendTarget.target;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class LXBlendTest {

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

  private final LXModel mockModel = new LXModel(List.of(TEST_POINTS));
  private final LX mockLX = new LX(mockModel);
  private final LXBlend add = new AddBlend(mockLX);

  private int[] src;
  private int[] dst;
  private int[] out;

  @BeforeEach
  void setUp() {
    src = TEST_SOURCE;

    dst = TEST_DEST;

    out = new int[]{
        LXColor.BLACK,
        LXColor.BLACK,
        LXColor.BLACK,
        LXColor.BLACK,
    };
  }

  @Test
  void testAddBlendFullAlpha() {
    int[] expected = new int[]{
        0xFFFFFFFF, // WHITE + BLACK
        0xFFFFFFFF, // BLACK + WHITE
        0xFFFFFF00, // RED   + GREEN
        0xFF00FF00, // BLACK + GREEN
    };
    add.blend(dst, src, 1.0, out, target(mockModel));
    assertArrayEquals(expected, out);
  }

  @Test
  void testBlendSameDestAndOutput() {
    int[] expected = new int[]{
        0xFFFFFFFF,
        0xFF000000,
        0xFFFF0000,
        0xFF00FF00
    };
    add.blend(out, src, 1.0, out, target(mockModel));
    bprint(expected);
    bprint(out);
    assertArrayEquals(expected, out);
  }

  @Test
  void testBlendToDest() {
    int[] expected = new int[]{
        0xFFFFFFFF,
        0xFF000000,
        0xFFFF0000,
        0xFF00FF00
    };
    add.blendToDest(out, src, 1.0, target(mockModel));
    bprint(expected);
    bprint(out);
    assertArrayEquals(expected, out);
  }

  @Test
  void testAddBlendHalfAlpha() {
    int[] expected = new int[]{
        0xFF7F7F7F, // BLACK + (0.5 * WHITE)
        0xFFFFFFFF, // WHITE + (0.5 * BLACK)
        0xFF7FFF00, // GREEN + (0.5 * RED)
        0xFF007F00, // BLACK + (0.5 * GREEN)
    };
    add.blend(dst, src, 0.5, out, target(mockModel));
    bprint(expected);
    bprint(out);
    assertArrayEquals(expected, out);
  }

  private void bprint(int x) {
    System.out.printf("0x%02X%n", x);
  }

  private void bprint(int[] out) {
    System.out.println("---------");
    Arrays.stream(out).forEach(this::bprint);
  }
}