package heronarts.lx.blend;

import java.util.List;

import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static heronarts.lx.blend.BlendTestHelpers.EXPECTED_FULL_ALPHA;
import static heronarts.lx.blend.BlendTestHelpers.EXPECTED_HALF_ALPHA;
import static heronarts.lx.blend.BlendTestHelpers.EXPECTED_SAME_DEST_AND_OUTPUT;
import static heronarts.lx.blend.BlendTestHelpers.TEST_BLACK;
import static heronarts.lx.blend.BlendTestHelpers.TEST_DEST;
import static heronarts.lx.blend.BlendTestHelpers.TEST_POINTS;
import static heronarts.lx.blend.BlendTestHelpers.TEST_SOURCE;
import static heronarts.lx.blend.BlendTestHelpers.bprint;
import static heronarts.lx.blend.BlendTestHelpers.copyOf;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class LXFunctionalAddBlendTest {
  private final LXModel mockModel = new LXModel(List.of(TEST_POINTS));
  private final LX mockLX = new LX(mockModel);
  private final LXBlend add = new AddBlend(mockLX);

  private int[] src;
  private int[] dst;
  private int[] out;

  @BeforeEach
  void setUp() {
    src = copyOf(TEST_SOURCE);
    dst = copyOf(TEST_DEST);
    out = copyOf(TEST_BLACK);
  }

  @Test
  void testAddBlendFullAlpha() {
    add.blend(dst, src, 1.0, out, mockModel);
    assertArrayEquals(EXPECTED_FULL_ALPHA, out);
  }

  @Test
  void testBlendSameDestAndOutput() {
    add.blend(out, src, 1.0, out, mockModel);
    bprint(EXPECTED_SAME_DEST_AND_OUTPUT);
    bprint(out);
    assertArrayEquals(EXPECTED_SAME_DEST_AND_OUTPUT, out);
  }

  @Test
  void testAddBlendHalfAlpha() {
    add.blend(dst, src, 0.5, out, mockModel);
    bprint(EXPECTED_HALF_ALPHA);
    bprint(out);
    assertArrayEquals(EXPECTED_HALF_ALPHA, out);
  }

}