package heronarts.lx.buffer;

import heronarts.lx.blend.LXBlend;

public interface BlendStack {

  boolean SHIM_BLEND_STACK = false;

  int[] getDestination();

  int[] getOutput();

  void initialize(int[] destination, int[] output);

  void blend(LXBlend blend, BlendStack that, double alpha, LXBlend.BlendTarget target);

  void blend(LXBlend blend, int[] src, double alpha, LXBlend.BlendTarget target);

//  void blend(LXBlend blend, int[] src, double alpha, int start, int num);

  void transition(LXBlend blend, int[] src, double lerp, LXBlend.BlendTarget target);

  void copyFrom(BlendStack that);

  static BlendStack shim() {
    if (SHIM_BLEND_STACK) {
      throw new RuntimeException("not implemented");
    } else {
      return new BlendStackArrayImpl();
    }
  }
}