package heronarts.lx.buffer;

import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.ops.transforms.Transforms;

public class TensorAddBlend {

  /**
   * Blends the src buffer onto the destination buffer at the specified alpha amount.
   *
   * @param dst Destination buffer (lower layer)
   * @param src Source buffer (top layer)
   * @param alpha Alpha blend, from 0-1
   * @param output Output buffer, which may be the same as src or dst
   * @param start Starting index to blend
   * @param num Number of pixels to blend
   */
  public void blend(INDArray dst, INDArray src, double alpha, INDArray output, int start, int num) {
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

    // Calculate effective source alpha
    double alphaScale = alpha * 256.0;
    INDArray effectiveAlpha = srcAlpha.mul(alphaScale).div(256.0);

    // Add rounding
    INDArray roundingMask = effectiveAlpha.gte(127.5);
    effectiveAlpha.addi(roundingMask.castTo(DataType.FLOAT));

    // Blend in-place: out = dst + (src * effectiveAlpha / 256)
    outR.addi(srcR.mul(effectiveAlpha).div(256.0));
    outG.addi(srcG.mul(effectiveAlpha).div(256.0));
    outB.addi(srcB.mul(effectiveAlpha).div(256.0));
    outAlpha.addi(effectiveAlpha);

    // Clip to valid range - using simple assign approach
    outR.assign(Transforms.min(outR, 255.0));
    outG.assign(Transforms.min(outG, 255.0));
    outB.assign(Transforms.min(outB, 255.0));
    outAlpha.assign(Transforms.min(outAlpha, 255.0));

//    // Clip to valid range using putWhere (most efficient)
//    INDArray maxVal = Nd4j.scalar(255.0);
//    outR.putWhere(outR.gt(255.0), maxVal);
//    outG.putWhere(outG.gt(255.0), maxVal);
//    outB.putWhere(outB.gt(255.0), maxVal);
//    outAlpha.putWhere(outAlpha.gt(255.0), maxVal);
  }
}
