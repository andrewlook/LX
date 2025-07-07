package heronarts.lx.buffer;

import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.ops.transforms.Transforms;

public class Uint8TensorAddBlend {

  /**
   * Blends the src buffer onto the destination buffer at the specified alpha amount.
   * All tensors must be 2D with shape [N, 4] and UINT8 data type.
   *
   * @param dst Destination buffer (lower layer) - UINT8 tensor [N, 4]
   * @param src Source buffer (top layer) - UINT8 tensor [N, 4]
   * @param alpha Alpha blend, from 0-1
   * @param output Output buffer - UINT8 tensor [N, 4], may be the same as src or dst
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

    // Extract channels and convert to FLOAT for calculations
    INDArray outAlpha = outSlice.getColumn(0).castTo(DataType.FLOAT);
    INDArray outR = outSlice.getColumn(1).castTo(DataType.FLOAT);
    INDArray outG = outSlice.getColumn(2).castTo(DataType.FLOAT);
    INDArray outB = outSlice.getColumn(3).castTo(DataType.FLOAT);

    INDArray srcAlpha = srcSlice.getColumn(0).castTo(DataType.FLOAT);
    INDArray srcR = srcSlice.getColumn(1).castTo(DataType.FLOAT);
    INDArray srcG = srcSlice.getColumn(2).castTo(DataType.FLOAT);
    INDArray srcB = srcSlice.getColumn(3).castTo(DataType.FLOAT);

    // Calculate effective source alpha using the original integer formula
    // effectiveAlpha = (srcAlpha * alpha * 256) / 256
    double alphaScale = alpha * 256.0;
    INDArray effectiveAlpha = srcAlpha.mul(alphaScale).div(256.0);

    // Add rounding: srcAlpha = a + (a >= 127.5 ? 1 : 0)
    INDArray roundingMask = effectiveAlpha.gte(127.5);
    effectiveAlpha.addi(roundingMask.castTo(DataType.FLOAT));

    // Additive blend: out = dst + (src * effectiveAlpha / 256)
    outR.addi(srcR.mul(effectiveAlpha).div(256.0));
    outG.addi(srcG.mul(effectiveAlpha).div(256.0));
    outB.addi(srcB.mul(effectiveAlpha).div(256.0));
    outAlpha.addi(effectiveAlpha);

    // Clip to valid UINT8 range [0, 255]
    outR.assign(Transforms.min(outR, 255.0));
    outG.assign(Transforms.min(outG, 255.0));
    outB.assign(Transforms.min(outB, 255.0));
    outAlpha.assign(Transforms.min(outAlpha, 255.0));

    // Convert back to UINT8 and write to output
    outSlice.getColumn(0).assign(outAlpha.castTo(DataType.UINT8));
    outSlice.getColumn(1).assign(outR.castTo(DataType.UINT8));
    outSlice.getColumn(2).assign(outG.castTo(DataType.UINT8));
    outSlice.getColumn(3).assign(outB.castTo(DataType.UINT8));
  }

  /**
   * Convenience method to blend entire tensors
   */
  public void blend(INDArray dst, INDArray src, double alpha, INDArray output) {
    blend(dst, src, alpha, output, 0, (int)dst.size(0));
  }

  private void validateInputs(INDArray dst, INDArray src, INDArray output) {
    // Check data types
    if (dst.dataType() != DataType.UINT8) {
      throw new IllegalArgumentException("dst must be UINT8, got: " + dst.dataType());
    }
    if (src.dataType() != DataType.UINT8) {
      throw new IllegalArgumentException("src must be UINT8, got: " + src.dataType());
    }
    if (output.dataType() != DataType.UINT8) {
      throw new IllegalArgumentException("output must be UINT8, got: " + output.dataType());
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
  }
}
