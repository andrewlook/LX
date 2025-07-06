package heronarts.lx.buffer;

import java.util.Arrays;

import heronarts.lx.color.LXColor;
import org.junit.jupiter.api.Test;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import static heronarts.lx.buffer.PrintTensorUtils.debugTensor;
import static heronarts.lx.buffer.PrintTensorUtils.printAsBinary;
import static heronarts.lx.buffer.PrintTensorUtils.printAsBinaryUint8;
import static heronarts.lx.buffer.TensorConverters.intArrayTo2DTensorUint8;

class TensorPlaygroundTest {

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

  @Test
  public void testConvertUint8() {
    INDArray converted = intArrayTo2DTensorUint8(TEST_DEST);
    debugTensor(converted);
    printAsBinaryUint8(converted, "UINT8");

    INDArray asFloat = converted.castTo(DataType.FLOAT16);

    debugTensor(asFloat);
//    asFloat.data()
  }

  @Test
  public void testSandbox() {
    // Create UINT8 NDArray
    INDArray a = Nd4j.create(DataType.UINT8, 3);
    a.putScalar(0, 255);
    a.putScalar(1, 128);
    a.putScalar(2, 64);

    INDArray b = Nd4j.create(DataType.UINT8, 3);
    b.putScalar(0, 15);
    b.putScalar(1, 31);
    b.putScalar(2, 63);

    System.out.println(a);
    System.out.println(b);

    System.out.println(Nd4j.bitwise.and(a, b));
    System.out.println(Nd4j.bitwise.or(a, b));
  }

  private void bprint(int x) {
    System.out.printf("0x%02X%n", x);
  }

  private void bprint(int[] out) {
    System.out.println("---------");
    Arrays.stream(out).forEach(this::bprint);
  }
}