package heronarts.lx.buffer;

import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.ops.transforms.Transforms;

public class FloatTensorAddBlend extends BaseTensorBlend {

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
  @Override
  public void blend(INDArray dst, INDArray src, double alpha, INDArray output, int start, int num) {
    // Validate inputs
    validateInputs(DataType.FLOAT, dst, src, output);

    // Work on slices
    INDArray dstSlice = dst.get(NDArrayIndex.interval(start, start + num), NDArrayIndex.all());
    INDArray srcSlice = src.get(NDArrayIndex.interval(start, start + num), NDArrayIndex.all());
    INDArray outSlice = output.get(NDArrayIndex.interval(start, start + num), NDArrayIndex.all());

    // Copy dst to output if they're different objects
    if (output != dst) {
      outSlice.assign(dstSlice);
    }

    // Extract channels
    INDArray outAlpha = outSlice.getColumn(0);
    INDArray outR = outSlice.getColumn(1);
    INDArray outG = outSlice.getColumn(2);
    INDArray outB = outSlice.getColumn(3);

    INDArray srcAlpha = srcSlice.getColumn(0);
    INDArray srcR = srcSlice.getColumn(1);
    INDArray srcG = srcSlice.getColumn(2);
    INDArray srcB = srcSlice.getColumn(3);

    // Convert to integer space for exact calculation, then back to float
    // This ensures we match the UINT8 implementation exactly

    // Scale [0,1] to [0,255] for integer math
    INDArray srcAlpha255 = srcAlpha.mul(255.0);
    INDArray srcR255 = srcR.mul(255.0);
    INDArray srcG255 = srcG.mul(255.0);
    INDArray srcB255 = srcB.mul(255.0);

    // Apply the exact integer formula
    double alphaScale = alpha * 256.0;
    INDArray effectiveAlpha = srcAlpha255.mul(alphaScale).div(256.0);

    // Add rounding: effectiveAlpha += (effectiveAlpha >= 127.5 ? 1 : 0)
    INDArray roundingMask = effectiveAlpha.gte(127.5);
    effectiveAlpha.addi(roundingMask.castTo(DataType.FLOAT));

    // Additive blend in 255 space: out = dst + (src * effectiveAlpha / 256)
    outR.muli(255.0).addi(srcR255.mul(effectiveAlpha).div(256.0));
    outG.muli(255.0).addi(srcG255.mul(effectiveAlpha).div(256.0));
    outB.muli(255.0).addi(srcB255.mul(effectiveAlpha).div(256.0));
    outAlpha.muli(255.0).addi(effectiveAlpha);

    // Clip to [0, 255]
    outR.assign(Transforms.min(outR, 255.0));
    outG.assign(Transforms.min(outG, 255.0));
    outB.assign(Transforms.min(outB, 255.0));
    outAlpha.assign(Transforms.min(outAlpha, 255.0));

    // Convert back to [0,1] range
    outR.divi(255.0);
    outG.divi(255.0);
    outB.divi(255.0);
    outAlpha.divi(255.0);

//    // Clip to valid range using putWhere (most efficient)
//    INDArray maxVal = Nd4j.scalar(255.0);
//    outR.putWhere(outR.gt(255.0), maxVal);
//    outG.putWhere(outG.gt(255.0), maxVal);
//    outB.putWhere(outB.gt(255.0), maxVal);
//    outAlpha.putWhere(outAlpha.gt(255.0), maxVal);
  }

  /**
   * @param mask Boolean tensor [N] or [N, 1] - true where blending should occur
   */
  @Override
  public void blend(INDArray dst, INDArray src, double alpha, INDArray output, INDArray mask) {
    validateInputs(DataType.FLOAT, dst, src, output);
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
    INDArray srcAlpha = src.getColumn(0);
    INDArray srcR = src.getColumn(1);
    INDArray srcG = src.getColumn(2);
    INDArray srcB = src.getColumn(3);

    // Apply exact integer formula (same as before)
    INDArray srcAlpha255 = srcAlpha.mul(255.0);
    INDArray srcR255 = srcR.mul(255.0);
    INDArray srcG255 = srcG.mul(255.0);
    INDArray srcB255 = srcB.mul(255.0);

    double alphaScale = alpha * 256.0;
    INDArray effectiveAlpha = srcAlpha255.mul(alphaScale).div(256.0);
    INDArray roundingMask = effectiveAlpha.gte(127.5);
    effectiveAlpha.addi(roundingMask.castTo(DataType.FLOAT));

    // Calculate deltas (what to add to dst)
    INDArray deltaR = srcR255.mul(effectiveAlpha).div(256.0);
    INDArray deltaG = srcG255.mul(effectiveAlpha).div(256.0);
    INDArray deltaB = srcB255.mul(effectiveAlpha).div(256.0);
    INDArray deltaAlpha = effectiveAlpha;

    // Apply mask: only add deltas where mask is true
    // Use .mul() instead of .muli() to avoid in-place shape issues
    deltaR = deltaR.mul(broadcastMask);
    deltaG = deltaG.mul(broadcastMask);
    deltaB = deltaB.mul(broadcastMask);
    deltaAlpha = deltaAlpha.mul(broadcastMask);

    // Apply to output (convert to 255 space, add deltas, clip, convert back)
    INDArray outAlpha = output.getColumn(0);
    INDArray outR = output.getColumn(1);
    INDArray outG = output.getColumn(2);
    INDArray outB = output.getColumn(3);

    // Scale to 255, add deltas, clip, scale back
    outAlpha.muli(255.0).addi(deltaAlpha);
    outAlpha.assign(Transforms.min(outAlpha, 255.0));
    outAlpha.divi(255.0);

    outR.muli(255.0).addi(deltaR);
    outR.assign(Transforms.min(outR, 255.0));
    outR.divi(255.0);

    outG.muli(255.0).addi(deltaG);
    outG.assign(Transforms.min(outG, 255.0));
    outG.divi(255.0);

    outB.muli(255.0).addi(deltaB);
    outB.assign(Transforms.min(outB, 255.0));
    outB.divi(255.0);
  }
}
