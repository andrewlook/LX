package heronarts.lx.blend;

import java.util.List;

import heronarts.lx.LX;
import heronarts.lx.buffer.Uint8TensorAddBlend;
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
import static heronarts.lx.buffer.TensorConverters.intArrayToUint8Tensor2D;
import static heronarts.lx.buffer.TensorConverters.uint8Tensor2DToIntArray;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class Uint8TensorAddBlendTest {
  private final LXModel mockModel = new LXModel(List.of(TEST_POINTS));
  private final LX mockLX = new LX(mockModel);

  private final Uint8TensorAddBlend add = new Uint8TensorAddBlend();

  private INDArray src;
  private INDArray dst;
  private INDArray out;

  @BeforeEach
  void setUp() {
    src = intArrayToUint8Tensor2D(TEST_SOURCE);
    dst = intArrayToUint8Tensor2D(TEST_DEST);
    out = intArrayToUint8Tensor2D(TEST_BLACK);
  }

  // ============= Range (start, num) Indexing =====================

  @Test
  void testRangeAddBlendFullAlpha() {
    add.blend(dst, src, 1.0, out, 0, mockModel.size);
    assertArrayEquals(EXPECTED_FULL_ALPHA, uint8Tensor2DToIntArray(out));
  }

  @Test
  void testRangeBlendSameDestAndOutput() {
    add.blend(out, src, 1.0, out, 0, mockModel.size);
    assertArrayEquals(EXPECTED_SAME_DEST_AND_OUTPUT, uint8Tensor2DToIntArray(out));
  }

  @Test
  void testRangeAddBlendHalfAlpha() {
    add.blend(dst, src, 0.5, out, 0, mockModel.size);
    assertArrayEquals(EXPECTED_HALF_ALPHA, uint8Tensor2DToIntArray(out));
  }

  // ============= Model Points Indexing =====================

  @Test
  void testModelAddBlendFullAlpha() {
    add.blend(dst, src, 1.0, out, mockModel);
    assertArrayEquals(EXPECTED_FULL_ALPHA, uint8Tensor2DToIntArray(out));
  }

  @Test
  void testModelBlendSameDestAndOutput() {
    add.blend(out, src, 1.0, out, mockModel);
    assertArrayEquals(EXPECTED_SAME_DEST_AND_OUTPUT, uint8Tensor2DToIntArray(out));
  }

  @Test
  void testModelAddBlendHalfAlpha() {
    add.blend(dst, src, 0.5, out, mockModel);
//    debugTensor(out, "ACTUAL (HALF ALPHA)");
//    debugTensor(intArrayToUint8Tensor2D(EXPECTED_HALF_ALPHA), "EXPECTED (HALF ALPHA)");
    assertArrayEquals(EXPECTED_HALF_ALPHA, uint8Tensor2DToIntArray(out));
  }
}