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

  public static INDArray intArrayTo2DTensorUint8(int[] arr) {
    System.out.println("About to convert: " + Arrays.toString(arr));

    INDArray tensor = Nd4j.createFromArray(arr);
    debugTensor(tensor);
    printAsBinary(tensor, "INT32 TENSOR");
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
}
