/**
 * Copyright 2016- Mark C. Slee, Heron Arts LLC
 * <p>
 * This file is part of the LX Studio software library. By using
 * LX, you agree to the terms of the LX Studio Software License
 * and Distribution Agreement, available at: http://lx.studio/license
 * <p>
 * Please note that the LX license is not open-source. The license
 * allows for free, non-commercial use.
 * <p>
 * HERON ARTS MAKES NO WARRANTY, EXPRESS, IMPLIED, STATUTORY, OR
 * OTHERWISE, AND SPECIFICALLY DISCLAIMS ANY WARRANTY OF
 * MERCHANTABILITY, NON-INFRINGEMENT, OR FITNESS FOR A PARTICULAR
 * PURPOSE, WITH RESPECT TO THE SOFTWARE.
 *
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.lx.blend;

import heronarts.lx.LX;
import heronarts.lx.LXBuffer;
import heronarts.lx.LXComponent;
import heronarts.lx.LXModulatorComponent;
import heronarts.lx.model.LXModel;

/**
 * An LXBlend is a loop-based implementation of a compositing algorithm.
 * Two color buffers are blended together using some logic, typically
 * a standard alpha-compositing technique. However, more complex blend
 * modes may be authored, taking into account position information from
 * the model, for instance.
 */
public abstract class LXBlend extends LXModulatorComponent {

  private String name;

  protected LXBlend(LX lx) {
    super(lx);
    this.name = LXComponent.getComponentName(this, "Blend");
  }

  public LXBlend setBlendContext(LXComponent parent) {
    super.setParent(parent);
    return this;
  }

  /**
   * Sets name of this blend mode
   *
   * @param name UI name of blend
   * @return this
   */
  public LXBlend setName(String name) {
    this.name = name;
    return this;
  }

  /**
   * Returns the name of this blend, to be shown in UI
   *
   * @return Blend name
   */
  public String getName() {
    return this.name;
  }

  /**
   * Gets the name of this blend.
   */
  @Override
  public String getLabel() {
    return getName();
  }

  /**
   * Name of the blend
   */
  @Override
  public String toString() {
    return getName();
  }

//    public void blend(LXBuffer dst, LXBuffer src, double alpha, LXBuffer buffer, LXModel model) {
//        blend(dst, src, alpha, buffer.getArray(), model);
//    }

  /**
   * Blends the src buffer onto the destination buffer at the specified alpha amount.
   *
   * @param dst    Destination buffer (lower layer)
   * @param src    Source buffer (top layer)
   * @param alpha  Alpha blend, from 0-1
   * @param output Output buffer, which may be the same as src or dst
   * @param model  A model which indicates the set of points to blend
   */
  public abstract void blend(LXBuffer dst, LXBuffer src, double alpha, LXBuffer output, LXModel model);

  /**
   * Blends the src buffer onto the destination buffer at the specified alpha amount.
   *
   * @param dst    Destination buffer (lower layer)
   * @param src    Source buffer (top layer)
   * @param alpha  Alpha blend, from 0-1
   * @param output Output buffer, which may be the same as src or dst
   * @param start  Starting index to blend
   * @param num    Number of pixels to blend
   */
  public abstract void blend(LXBuffer dst, LXBuffer src, double alpha, LXBuffer output, int start, int num);

  /**
   * Transitions from one buffer to another. By default, this is used by first
   * blending from-to with alpha 0-1, then blending to-from with
   * alpha 1-0. Blends which are asymmetrical may override this method for
   * custom functionality. This method is used by pattern transitions on
   * channels as well as the crossfader.
   *
   * @param from   First buffer
   * @param to     Second buffer
   * @param amt    Interpolation from-to (0-1)
   * @param output Output buffer, which may be the same as from or to
   * @param model  The model with points that should be blended
   */
  public void lerp(LXBuffer from, LXBuffer to, double amt, LXBuffer output, LXModel model) {
    LXBuffer dst, src;
    double alpha;
    if (amt <= 0.5) {
      dst = from;
      src = to;
      alpha = amt * 2.;
    } else {
      dst = to;
      src = from;
      alpha = (1 - amt) * 2.;
    }
    blend(dst, src, alpha, output, model);
  }

  /**
   * Subclasses may override this method. It will be invoked when the blend is
   * about to become active for a transition. Blends may take care of any
   * initialization needed or reset parameters if desired. Note that a blend used on
   * a channel fader or crossfader will only receive this message once.
   */
  public /* abstract */ void onActive() {
  }

  /**
   * Subclasses may override this method. It will be invoked when the transition is
   * no longer active. Resources may be freed if desired. Note that this method will
   * only be received once blends used on channel faders or crossfaders.
   */
  public /* abstract */ void onInactive() {
  }

}
