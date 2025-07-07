package heronarts.lx.blend;

import java.util.List;

import heronarts.lx.LX;
import heronarts.lx.buffer.FloatTensorAddBlend;
import heronarts.lx.model.LXModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.nd4j.linalg.api.ndarray.INDArray;

import static heronarts.lx.blend.BlendTestHelpers.EXPECTED_FULL_ALPHA;
import static heronarts.lx.blend.BlendTestHelpers.EXPECTED_HALF_ALPHA;
import static heronarts.lx.blend.BlendTestHelpers.EXPECTED_SAME_DEST_AND_OUTPUT;
import static heronarts.lx.blend.BlendTestHelpers.TEST_BLACK;
import static heronarts.lx.blend.BlendTestHelpers.TEST_DEST;
import static heronarts.lx.blend.BlendTestHelpers.TEST_POINTS;
import static heronarts.lx.blend.BlendTestHelpers.TEST_SOURCE;
import static heronarts.lx.buffer.TensorConverters.floatTensor2DToIntArray;
import static heronarts.lx.buffer.TensorConverters.intArrayToFloatTensor2D;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class FloatTensorAddBlendTest {
  private final LXModel mockModel = new LXModel(List.of(TEST_POINTS));
  private final LX mockLX = new LX(mockModel);

  private final FloatTensorAddBlend add = new FloatTensorAddBlend();

  private INDArray src;
  private INDArray dst;
  private INDArray out;

  @BeforeEach
  void setUp() {
    src = intArrayToFloatTensor2D(TEST_SOURCE);
    dst = intArrayToFloatTensor2D(TEST_DEST);
    out = intArrayToFloatTensor2D(TEST_BLACK);
  }

  // ============= Range (start, num) Indexing =====================

  @Test
  void testRangeAddBlendFullAlpha() {
    add.blend(dst, src, 1.0, out, 0, mockModel.size);
    assertArrayEquals(EXPECTED_FULL_ALPHA, floatTensor2DToIntArray(out));
  }

  @Test
  void testRangeBlendSameDestAndOutput() {
    add.blend(out, src, 1.0, out, 0, mockModel.size);
    assertArrayEquals(EXPECTED_SAME_DEST_AND_OUTPUT, floatTensor2DToIntArray(out));
  }

  @Test
  void testRangeAddBlendHalfAlpha() {
    add.blend(dst, src, 0.5, out, 0, mockModel.size);
    assertArrayEquals(EXPECTED_HALF_ALPHA, floatTensor2DToIntArray(out));
  }

  // ============= Model Points Indexing =====================

  @Test
  void testModelAddBlendFullAlpha() {
    add.blend(dst, src, 1.0, out, mockModel);
    assertArrayEquals(EXPECTED_FULL_ALPHA, floatTensor2DToIntArray(out));
  }

  @Test
  void testModelBlendSameDestAndOutput() {
    add.blend(out, src, 1.0, out, mockModel);
    assertArrayEquals(EXPECTED_SAME_DEST_AND_OUTPUT, floatTensor2DToIntArray(out));
  }

  @Test
  void testModelAddBlendHalfAlpha() {
    add.blend(dst, src, 0.5, out, mockModel);
//    debugTensor(out, "ACTUAL (HALF ALPHA)");
//    debugTensor(intArrayToFloatTensor2D(EXPECTED_HALF_ALPHA), "EXPECTED (HALF ALPHA)");
    assertArrayEquals(EXPECTED_HALF_ALPHA, floatTensor2DToIntArray(out));
  }
}