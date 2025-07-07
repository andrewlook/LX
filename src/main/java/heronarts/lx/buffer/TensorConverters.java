package heronarts.lx.buffer;

import java.util.Arrays;

import heronarts.lx.color.LXColor;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import static heronarts.lx.buffer.TensorDebugUtils.debugTensor;
import static heronarts.lx.buffer.TensorDebugUtils.printAsBinary;

public class TensorConverters {
  static final INDArray ALPHA_MASK = Nd4j.scalar(LXColor.ALPHA_MASK);
  static final INDArray ALPHA_SHIFT = Nd4j.scalar(LXColor.ALPHA_SHIFT);
  static final INDArray R_MASK = Nd4j.scalar(LXColor.R_MASK);
  static final INDArray R_SHIFT = Nd4j.scalar(LXColor.R_SHIFT);
  static final INDArray G_MASK = Nd4j.scalar(LXColor.G_MASK);
  static final INDArray G_SHIFT = Nd4j.scalar(LXColor.G_SHIFT);
  static final INDArray B_MASK = Nd4j.scalar(LXColor.B_MASK);

  public static INDArray intArrayToUint8Tensor2D(int[] arr) {
    INDArray tensor = Nd4j.createFromArray(arr);
    DataType dtype = tensor.dataType();

    INDArray alpha = Nd4j.bitwise.rightShift(
        Nd4j.bitwise.and(tensor, ALPHA_MASK.castTo(dtype)),
        ALPHA_SHIFT.castTo(dtype)
    ).castTo(DataType.UINT8);
    INDArray r = Nd4j.bitwise.rightShift(
        Nd4j.bitwise.and(tensor, R_MASK.castTo(dtype)),
        R_SHIFT.castTo(dtype)
    ).castTo(DataType.UINT8);
    INDArray g = Nd4j.bitwise.rightShift(
        Nd4j.bitwise.and(tensor, G_MASK.castTo(dtype)),
        G_SHIFT.castTo(dtype)
    ).castTo(DataType.UINT8);
    INDArray b = Nd4j.bitwise.and(tensor, B_MASK.castTo(dtype)).castTo(DataType.UINT8);

    // stack along dimension 1, creating a new tensor with shape (arr.length, 4)
    return Nd4j.stack(1, alpha, r, g, b);
  }

  public static INDArray intArrayToFloatTensor2D(int[] arr) {
    INDArray floatArr = intArrayToUint8Tensor2D(arr).castTo(DataType.FLOAT);
    floatArr.divi(255.0); // divide in-place
    return floatArr;
  }

  public static int[] uint8Tensor2DToIntArray(INDArray uint8Tensor) {
    // Extract channels and convert to INT32 for bitwise ops
    INDArray alpha = uint8Tensor.getColumn(0).castTo(DataType.INT32);
    INDArray r = uint8Tensor.getColumn(1).castTo(DataType.INT32);
    INDArray g = uint8Tensor.getColumn(2).castTo(DataType.INT32);
    INDArray b = uint8Tensor.getColumn(3).castTo(DataType.INT32);

    // Shift and combine in one expression
    INDArray combined = Nd4j.bitwise().or(
        Nd4j.bitwise().or(
            Nd4j.bitwise().leftShift(alpha, ALPHA_SHIFT),
            Nd4j.bitwise().leftShift(r, R_SHIFT)
        ),
        Nd4j.bitwise().or(
            Nd4j.bitwise().leftShift(g, G_SHIFT),
            b
        )
    );

    // More efficient conversion to int array
    return combined.data().asInt();
  }

  public static int[] floatTensor2DToIntArray(INDArray floatTensor) {
    INDArray uint8Tensor = floatTensor.mul(255.0).castTo(DataType.UINT8);
    return uint8Tensor2DToIntArray(uint8Tensor);
  }
}
