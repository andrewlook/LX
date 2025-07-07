package heronarts.lx.buffer;

import heronarts.lx.model.LXModel;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.ndarray.INDArray;

public abstract class BaseTensorBlend {

  // ============= Range (start, num) Indexing =====================

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

  /**
   * Blends the src buffer onto the destination buffer at the specified alpha amount.
   * All tensors must be 2D with shape [N, 4] with values in range [0, 1].
   *
   * @param dst Destination buffer (lower layer) - tensor [N, 4] with values [0, 1]
   * @param src Source buffer (top layer) - tensor [N, 4] with values [0, 1]
   * @param alpha Alpha blend, from 0-1
   * @param output Output buffer - tensor [N, 4], may be the same as src or dst
   * @param start Starting index to blend
   * @param num Number of pixels to blend
   */
  public abstract void blend(INDArray dst, INDArray src, double alpha, INDArray output, int start, int num);

  // ============= Model Points Indexing =====================

  public void blend(INDArray dst, INDArray src, double alpha, INDArray output, LXModel model) {
    blend(dst, src, alpha, output, model.getIndicesMask());
  }

  /**
   * @param mask Boolean tensor [N] or [N, 1] - true where blending should occur
   */
  public abstract void blend(INDArray dst, INDArray src, double alpha, INDArray output, INDArray mask);

  protected void validateMask(INDArray mask, long expectedSize) {
    if (mask.rank() > 2) {
      throw new IllegalArgumentException("Mask must be 1D or 2D, got rank: " + mask.rank());
    }
    if (mask.length() != expectedSize) {
      throw new IllegalArgumentException("Mask length must match tensor size: " + expectedSize + ", got: " + mask.length());
    }
  }

  protected void validateInputs(DataType dtype, INDArray dst, INDArray src, INDArray output) {
    // Check data types
    if (dst.dataType() != dtype) {
      throw new IllegalArgumentException("dst must be "+dtype.toString()+", got: " + dst.dataType());
    }
    if (src.dataType() != dtype) {
      throw new IllegalArgumentException("src must be "+dtype.toString()+", got: " + dst.dataType());
    }
    if (output.dataType() != dtype) {
      throw new IllegalArgumentException("output must be "+dtype.toString()+", got: " + dst.dataType());
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
