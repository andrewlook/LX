package heronarts.lx.buffer;

import heronarts.lx.LX;
import heronarts.lx.model.LXModel;

// Buffer for a single frame, which was rendered with
// a particular model state, has a main view along with
// a cue and auxiliary view, as well as cue/aux view state
public class LXIntArrayFrame implements LXIntArrayBuffer {
  private LXModel model;
  public int[] main = null;
  public int[] cue = null;
  public int[] aux = null;
  public boolean cueOn = false;
  public boolean auxOn = false;

  public LXIntArrayFrame(LX lx) {
    setModel(lx.getModel());
  }

  public void setModel(LXModel model) {
    this.model = model;
    if ((this.main == null) || (this.main.length != model.size)) {
      this.main = new int[model.size];
      this.cue = new int[model.size];
      this.aux = new int[model.size];
    }
  }

  public void setCueOn(boolean cueOn) {
    this.cueOn = cueOn;
  }

  public void setAuxOn(boolean auxOn) {
    this.auxOn = auxOn;
  }

  public void copyFrom(LXIntArrayFrame that) {
    setModel(that.model);
    this.cueOn = that.cueOn;
    this.auxOn = that.auxOn;
    System.arraycopy(that.main, 0, this.main, 0, this.main.length);
    System.arraycopy(that.cue, 0, this.cue, 0, this.cue.length);
    System.arraycopy(that.aux, 0, this.aux, 0, this.aux.length);
  }

  public int[] getColors(boolean aux) {
    return aux ? getAuxColors() : getColors();
  }

  public int[] getColors() {
    return this.cueOn ? this.cue : this.main;
  }

  public int[] getAuxColors() {
    return this.auxOn ? this.aux : this.main;
  }

  public LXModel getModel() {
    return this.model;
  }

  @Override
  public int[] getArray() {
    return this.main;
  }

  public int[] getMain() {
    return this.main;
  }

  public int[] getCue() {
    return this.cue;
  }

  public int[] getAux() {
    return this.aux;
  }
}
