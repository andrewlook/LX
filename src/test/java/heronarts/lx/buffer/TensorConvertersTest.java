package heronarts.lx.buffer;

import org.junit.jupiter.api.Test;
import org.nd4j.linalg.api.ndarray.INDArray;

import static heronarts.lx.blend.BlendTestHelpers.TEST_DEST;
import static heronarts.lx.buffer.TensorConverters.floatTensor2DToIntArray;
import static heronarts.lx.buffer.TensorConverters.intArrayToFloatTensor2D;
import static heronarts.lx.buffer.TensorConverters.intArrayToUint8Tensor2D;
import static heronarts.lx.buffer.TensorConverters.uint8Tensor2DToIntArray;
import static heronarts.lx.buffer.TensorDebugUtils.debugTensor;
import static heronarts.lx.buffer.TensorDebugUtils.printAsBinaryUint8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class TensorConvertersTest {

  @Test
  public void testConvertUint8() {
    INDArray uint8Tensor = intArrayToUint8Tensor2D(TEST_DEST);
    debugTensor(uint8Tensor);
    printAsBinaryUint8(uint8Tensor, "UINT8");
    int[] roundtrip = uint8Tensor2DToIntArray(uint8Tensor);
    assertArrayEquals(TEST_DEST, roundtrip);
  }

  @Test
  public void testConvertFloat() {
    INDArray floatTensor = intArrayToFloatTensor2D(TEST_DEST);
    debugTensor(floatTensor);
    printAsBinaryUint8(floatTensor, "FLOAT");
    int[] roundtrip = floatTensor2DToIntArray(floatTensor);
    assertArrayEquals(TEST_DEST, roundtrip);
  }
}