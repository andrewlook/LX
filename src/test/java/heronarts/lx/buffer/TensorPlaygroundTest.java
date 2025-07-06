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

  static INDArray intArrayTo2DTensorUint8(int[] arr) {
    System.out.println("About to convert: " + Arrays.toString(arr));

    INDArray tensor = Nd4j.createFromArray(arr);
    debugTensor(tensor);
    printAsBinary(tensor, "INT32 TENSOR");
    DataType dtype = tensor.dataType();

    INDArray ALPHA_MASK = Nd4j.scalar(LXColor.ALPHA_MASK).castTo(dtype);
    INDArray ALPHA_SHIFT = Nd4j.scalar(LXColor.ALPHA_SHIFT).castTo(dtype);

    INDArray R_MASK = Nd4j.scalar(LXColor.R_MASK).castTo(dtype);
    INDArray R_SHIFT = Nd4j.scalar(LXColor.R_SHIFT).castTo(dtype);

    INDArray G_MASK = Nd4j.scalar(LXColor.G_MASK).castTo(dtype);
    INDArray G_SHIFT = Nd4j.scalar(LXColor.G_SHIFT).castTo(dtype);

    INDArray B_MASK = Nd4j.scalar(LXColor.B_MASK).castTo(dtype);

    INDArray alpha = Nd4j.bitwise.rightShift(
        Nd4j.bitwise.and(tensor, ALPHA_MASK),
        ALPHA_SHIFT
    ).castTo(DataType.UINT8);
    INDArray r = Nd4j.bitwise.rightShift(
        Nd4j.bitwise.and(tensor, R_MASK),
        R_SHIFT
    ).castTo(DataType.UINT8);
    INDArray g = Nd4j.bitwise.rightShift(
        Nd4j.bitwise.and(tensor, G_MASK),
        G_SHIFT
    ).castTo(DataType.UINT8);
    INDArray b = Nd4j.bitwise.and(tensor, B_MASK).castTo(DataType.UINT8);

    // stack along dimension 1, creating a new tensor with shape (arr.length, 4)
    return Nd4j.stack(1, alpha, r, g, b);
  }

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