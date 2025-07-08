package heronarts.lx.buffer;

import org.apache.commons.lang3.NotImplementedException;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.ops.transforms.Transforms;

public class Uint8TensorAddBlend extends BaseTensorBlend {

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
    validateInputs(DataType.UINT8, dst, src, output);

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

  @Override
  public void blend(INDArray dst, INDArray src, double alpha, INDArray output, INDArray mask) {
    validateInputs(DataType.UINT8, dst, src, output);
    validateMask(mask, dst.size(0));

    if (output != dst) {
      output.assign(dst);
    }

    // Ensure mask is the right shape for broadcasting - should be [N] not [N, 1]
    INDArray broadcastMask = mask;
    if (mask.rank() == 2) {
      broadcastMask = mask.getColumn(0); // Convert [N, 1] to [N]
    }

    // Calculate the blend for all pixels
    INDArray srcAlpha = src.getColumn(0).castTo(DataType.FLOAT);
    INDArray srcR = src.getColumn(1).castTo(DataType.FLOAT);
    INDArray srcG = src.getColumn(2).castTo(DataType.FLOAT);
    INDArray srcB = src.getColumn(3).castTo(DataType.FLOAT);

    // Calculate effective source alpha using the original integer formula
    double alphaScale = alpha * 256.0;
    INDArray effectiveAlpha = srcAlpha.mul(alphaScale).div(256.0);

    // Add rounding: srcAlpha = a + (a >= 127.5 ? 1 : 0)
    INDArray roundingMask = effectiveAlpha.gte(127.5);
    effectiveAlpha.addi(roundingMask.castTo(DataType.FLOAT));

    // Calculate deltas (what to add to dst)
    INDArray deltaR = srcR.mul(effectiveAlpha).div(256.0);
    INDArray deltaG = srcG.mul(effectiveAlpha).div(256.0);
    INDArray deltaB = srcB.mul(effectiveAlpha).div(256.0);
    INDArray deltaAlpha = effectiveAlpha;

    // Apply mask: only add deltas where mask is true
    // Use .mul() instead of .muli() to avoid in-place shape issues
    deltaR = deltaR.mul(broadcastMask);
    deltaG = deltaG.mul(broadcastMask);
    deltaB = deltaB.mul(broadcastMask);
    deltaAlpha = deltaAlpha.mul(broadcastMask);

    // Apply to output (dst values + masked deltas)
    INDArray outAlpha = output.getColumn(0).castTo(DataType.FLOAT);
    INDArray outR = output.getColumn(1).castTo(DataType.FLOAT);
    INDArray outG = output.getColumn(2).castTo(DataType.FLOAT);
    INDArray outB = output.getColumn(3).castTo(DataType.FLOAT);

    // Add deltas and clip
    outAlpha.addi(deltaAlpha);
    outAlpha.assign(Transforms.min(outAlpha, 255.0));

    outR.addi(deltaR);
    outR.assign(Transforms.min(outR, 255.0));

    outG.addi(deltaG);
    outG.assign(Transforms.min(outG, 255.0));

    outB.addi(deltaB);
    outB.assign(Transforms.min(outB, 255.0));

    // Convert back to UINT8 and write to output
    output.getColumn(0).assign(outAlpha.castTo(DataType.UINT8));
    output.getColumn(1).assign(outR.castTo(DataType.UINT8));
    output.getColumn(2).assign(outG.castTo(DataType.UINT8));
    output.getColumn(3).assign(outB.castTo(DataType.UINT8));
  }
}
