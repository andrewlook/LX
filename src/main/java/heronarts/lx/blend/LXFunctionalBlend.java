package heronarts.lx.blend;

import heronarts.lx.LX;
import heronarts.lx.LXBuffer;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXModel;
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
  public void blend(LXBuffer dst, LXBuffer src, double alpha, LXBuffer output, LXModel model) {
    int alphaMask = (int) (alpha * LXColor.BLEND_ALPHA_FULL);
    int[] out = output.getArray();
    int[] from = dst.getArray();
    int[] to = src.getArray();
    for (LXPoint p : model.points) {
      out[p.index] = this.function.apply(from[p.index], to[p.index], alphaMask);
    }
  }

  @Override
  public void blend(LXBuffer dst, LXBuffer src, double alpha, LXBuffer output, int start, int num) {
    int alphaMask = (int) (alpha * LXColor.BLEND_ALPHA_FULL);
    int[] out = output.getArray();
    int[] from = dst.getArray();
    int[] to = src.getArray();
    for (int i = start; i < start + num; ++i) {
      out[i] = this.function.apply(from[i], to[i], alphaMask);
    }
  }
}

