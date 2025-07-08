package heronarts.lx.buffer;

import heronarts.lx.LX;
import heronarts.lx.blend.LXBlend;

public class BlendStackArrayImpl implements BlendStack {

  public int[] destination;
  public int[] output;

  public void initialize(int[] destination, int[] output) {
    this.destination = destination;
    this.output = output;

    if (this.destination == this.output) {
      LX.error(new Exception("BlendStack initialized with the same destination/output"));
    } else {
      // We need to splat the output array right away. Channels may have views applied
      // which mean blend calls might not touch all the pixels. So we've got to get them
      // all re-initted upfront.
      System.arraycopy(this.destination, 0, this.output, 0, this.destination.length);
      this.destination = this.output;
    }
  }

  public int[] getDestination() {
    return this.destination;
  }

  public int[] getOutput() {
    return this.output;
  }

  public void blend(LXBlend blend, BlendStack that, double alpha, LXBlend.BlendTarget target) {
    this.blend(blend, that.getDestination(), alpha, target);
  }

  public void blend(LXBlend blend, int[] src, double alpha, LXBlend.BlendTarget target) {
    blend.blend(this.destination, src, alpha, this.output, target);
    this.destination = this.output;
  }
//
//  public void blend(LXBlend blend, int[] src, double alpha, int start, int num) {
//    blend.blend(this.destination, src, alpha, this.output, start, num);
//    this.destination = this.output;
//  }

  public void transition(LXBlend blend, int[] src, double lerp, LXBlend.BlendTarget target) {
    blend.lerp(this.destination, src, lerp, this.output, target);
    this.destination = this.output;
  }

  public void copyFrom(BlendStack that) {
    System.arraycopy(that.getDestination(), 0, this.output, 0, that.getDestination().length);
    this.destination = this.output;
  }
}