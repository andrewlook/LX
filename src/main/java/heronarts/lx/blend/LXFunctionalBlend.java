package heronarts.lx.blend;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;

public class LXFunctionalBlend extends LXBlend {
  /**
   * Functional interface for a static blending function
   */
  public interface BlendFunction {
    /**
     * Blend function to combine two colors
     *
     * @param dst   Background color
     * @param src   Overlay color
     * @param alpha Secondary alpha mask (from 0x00 - 0x100)
     * @return Blended color
     */
    public int apply(int dst, int src, int alpha);
  }

  private final BlendFunction function;

  public LXFunctionalBlend(LX lx, BlendFunction function) {
    super(lx);
    this.function = function;
  }

  @Override
  public void blend(int[] dst, int[] src, double alpha, int[] output, BlendTarget target) {
    int alphaMask = (int) (alpha * LXColor.BLEND_ALPHA_FULL);

    if (target.model != null) {
      for (LXPoint p : target.model.points) {
        output[p.index] = this.function.apply(dst[p.index], src[p.index], alphaMask);
      }
    } else {
      for (int i = target.start; i < target.start + target.num; ++i) {
        output[i] = this.function.apply(dst[i], src[i], alphaMask);
      }
    }
  }
}

