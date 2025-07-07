package heronarts.lx.buffer;

import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.ops.transforms.Transforms;

public class FloatTensorAddBlend {

  /**
   * Blends the src buffer onto the destination buffer at the specified alpha amount.
   * All tensors must be 2D with shape [N, 4] and FLOAT data type with values in range [0, 1].
   *
   * @param dst Destination buffer (lower layer) - FLOAT tensor [N, 4] with values [0, 1]
   * @param src Source buffer (top layer) - FLOAT tensor [N, 4] with values [0, 1]
   * @param alpha Alpha blend, from 0-1
   * @param output Output buffer - FLOAT tensor [N, 4], may be the same as src or dst
   * @param start Starting index to blend
   * @param num Number of pixels to blend
   */
  public void blend(INDArray dst, INDArray src, double alpha, INDArray output, int start, int num) {
    // Validate inputs
    validateInputs(dst, src, output);

    // Work on slices
    INDArray dstSlice = dst.get(NDArrayIndex.interval(start, start + num), NDArrayIndex.all());
    INDArray srcSlice = src.get(NDArrayIndex.interval(start, start + num), NDArrayIndex.all());
    INDArray outSlice = output.get(NDArrayIndex.interval(start, start + num), NDArrayIndex.all());

    // Copy dst to output if they're different objects
    if (output != dst) {
      outSlice.assign(dstSlice);
    }

    // Extract channels (already FLOAT)
    INDArray outAlpha = outSlice.getColumn(0);
    INDArray outR = outSlice.getColumn(1);
    INDArray outG = outSlice.getColumn(2);
    INDArray outB = outSlice.getColumn(3);

    INDArray srcAlpha = srcSlice.getColumn(0);
    INDArray srcR = srcSlice.getColumn(1);
    INDArray srcG = srcSlice.getColumn(2);
    INDArray srcB = srcSlice.getColumn(3);

    // For float [0,1] values, adapt the formula from integer version
    // Original integer: effectiveAlpha = (srcAlpha * alpha * 256) / 256 + rounding
    // Float equivalent: effectiveAlpha = srcAlpha * alpha + rounding_adjustment
    INDArray effectiveAlpha = srcAlpha.mul(alpha);

    // Add rounding equivalent for [0,1] range
    // Original checked >= 127.5/255 ≈ 0.5, so we check >= 0.5
    // Original added 1/255, so we add equivalent in [0,1] space
    INDArray roundingMask = effectiveAlpha.gte(0.5);
    effectiveAlpha.addi(roundingMask.castTo(DataType.FLOAT).div(255.0)); // Add 1/255

    // Additive blend in [0,1] space: out = dst + (src * effectiveAlpha)
    outR.addi(srcR.mul(effectiveAlpha));
    outG.addi(srcG.mul(effectiveAlpha));
    outB.addi(srcB.mul(effectiveAlpha));
    outAlpha.addi(effectiveAlpha);

    // Clip to valid range [0, 1] for float values
    outR.assign(Transforms.min(outR, 1.0));
    outG.assign(Transforms.min(outG, 1.0));
    outB.assign(Transforms.min(outB, 1.0));
    outAlpha.assign(Transforms.min(outAlpha, 1.0));

//    // Clip to valid range using putWhere (most efficient)
//    INDArray maxVal = Nd4j.scalar(255.0);
//    outR.putWhere(outR.gt(255.0), maxVal);
//    outG.putWhere(outG.gt(255.0), maxVal);
//    outB.putWhere(outB.gt(255.0), maxVal);
//    outAlpha.putWhere(outAlpha.gt(255.0), maxVal);
  }

  /**
   * Convenience method to blend entire tensors
   *
   * @param dst Destination buffer (lower layer) - FLOAT tensor [N, 4] with values [0, 1]
   * @param src Source buffer (top layer) - FLOAT tensor [N, 4] with values [0, 1]
   * @param alpha Alpha blend, from 0-1
   * @param output Output buffer - FLOAT tensor [N, 4], may be the same as src or dst
   */
  public void blend(INDArray dst, INDArray src, double alpha, INDArray output) {
    blend(dst, src, alpha, output, 0, (int) dst.size(0));
  }

  private void validateInputs(INDArray dst, INDArray src, INDArray output) {
    // Check data types
    if (dst.dataType() != DataType.FLOAT) {
      throw new IllegalArgumentException("dst must be FLOAT, got: " + dst.dataType());
    }
    if (src.dataType() != DataType.FLOAT) {
      throw new IllegalArgumentException("src must be FLOAT, got: " + src.dataType());
    }
    if (output.dataType() != DataType.FLOAT) {
      throw new IllegalArgumentException("output must be FLOAT, got: " + output.dataType());
    }

    // Check shapes
    if (dst.rank() != 2 || dst.size(1) != 4) {
      throw new IllegalArgumentException("dst must be 2D with shape [N, 4], got: " + java.util.Arrays.toString(dst.shape()));
    }
    if (src.rank() != 2 || src.size(1) != 4) {
      throw new IllegalArgumentException("src must be 2D with shape [N, 4], got: " + java.util.Arrays.toString(src.shape()));
    }
    if (output.rank() != 2 || output.size(1) != 4) {
      throw new IllegalArgumentException("output must be 2D with shape [N, 4], got: " + java.util.Arrays.toString(output.shape()));
    }

    // Check compatible sizes
    if (dst.size(0) != src.size(0) || dst.size(0) != output.size(0)) {
      throw new IllegalArgumentException("All tensors must have same number of rows");
    }

    // Validate alpha range bounds (optional - could be expensive for large tensors)
    // Uncomment if you want strict validation:
    /*
    if (dst.minNumber().doubleValue() < 0.0 || dst.maxNumber().doubleValue() > 1.0) {
      throw new IllegalArgumentException("dst values must be in range [0, 1]");
    }
    if (src.minNumber().doubleValue() < 0.0 || src.maxNumber().doubleValue() > 1.0) {
      throw new IllegalArgumentException("src values must be in range [0, 1]");
    }
    */
  }
}
