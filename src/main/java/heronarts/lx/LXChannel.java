/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
 *
 * This file is part of the LX Studio software library. By using
 * LX, you agree to the terms of the LX Studio Software License
 * and Distribution Agreement, available at: http://lx.studio/license
 *
 * Please note that the LX license is not open-source. The license
 * allows for free, non-commercial use.
 *
 * HERON ARTS MAKES NO WARRANTY, EXPRESS, IMPLIED, STATUTORY, OR
 * OTHERWISE, AND SPECIFICALLY DISCLAIMS ANY WARRANTY OF
 * MERCHANTABILITY, NON-INFRINGEMENT, OR FITNESS FOR A PARTICULAR
 * PURPOSE, WITH RESPECT TO THE SOFTWARE.
 *
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.lx;

import heronarts.lx.blend.LXBlend;
import heronarts.lx.clip.LXChannelClip;
import heronarts.lx.clip.LXClip;
import heronarts.lx.midi.LXMidiEngine;
import heronarts.lx.midi.LXShortMessage;
import heronarts.lx.model.LXModel;
import heronarts.lx.osc.OscMessage;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.MutableParameter;
import heronarts.lx.parameter.ObjectParameter;
import heronarts.lx.parameter.BooleanParameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * A channel is a single component of the engine that has a set of patterns from
 * which it plays and rotates. It also has a fader to control how this channel
 * is blended with the channels before it.
 */
public class LXChannel extends LXChannelBus {

  /**
   * Listener interface for objects which want to be notified when the internal
   * channel state is modified.
   */
  public interface Listener extends LXChannelBus.Listener {
    public void groupChanged(LXChannel channel, LXGroup group);
    public void patternAdded(LXChannel channel, LXPattern pattern);
    public void patternRemoved(LXChannel channel, LXPattern pattern);
    public void patternMoved(LXChannel channel, LXPattern pattern);
    public void patternWillChange(LXChannel channel, LXPattern pattern, LXPattern nextPattern);
    public void patternDidChange(LXChannel channel, LXPattern pattern);
  }

  public interface MidiListener {
    public void midiReceived(LXChannel channel, LXShortMessage message);
  }

  /**
   * Utility class to extend in cases where only some methods need overriding.
   */
  public abstract static class AbstractListener implements Listener {

    @Override
    public void indexChanged(LXChannelBus channel) {
    }

    @Override
    public void groupChanged(LXChannel channel, LXGroup group) {
    }

    @Override
    public void effectAdded(LXBus channel, LXEffect effect) {
    }

    @Override
    public void effectRemoved(LXBus channel, LXEffect effect) {
    }

    @Override
    public void effectMoved(LXBus channel, LXEffect effect) {
    }

    @Override
    public void patternAdded(LXChannel channel, LXPattern pattern) {
    }

    @Override
    public void patternRemoved(LXChannel channel, LXPattern pattern) {
    }

    @Override
    public void patternMoved(LXChannel channel, LXPattern pattern) {
    }

    @Override
    public void patternWillChange(LXChannel channel, LXPattern pattern,
        LXPattern nextPattern) {
    }

    @Override
    public void patternDidChange(LXChannel channel, LXPattern pattern) {
    }
  }

  private final List<Listener> listeners = new ArrayList<Listener>();
  private final List<Listener> listenerSnapshot = new ArrayList<Listener>();
  private final List<MidiListener> midiListeners = new ArrayList<MidiListener>();

  public enum AutoCycleMode {
    NEXT,
    RANDOM;

    @Override
    public String toString() {
      switch (this) {
      case NEXT:
        return "Next";
      default:
      case RANDOM:
        return "Random";
      }
    }
  };

  /**
   * Which pattern is focused in the channel
   */
  public final DiscreteParameter focusedPattern;

  /**
   * Whether the channel control UI is expanded
   */
  public final BooleanParameter controlsExpanded =
    new BooleanParameter("Expanded", true)
    .setDescription("Whether the control elements for the channel device are expanded");

  /**
   * Whether this channel should listen to MIDI events
   */
  public final BooleanParameter midiMonitor =
    new BooleanParameter("MIDI Monitor", false)
    .setDescription("Enables or disables monitoring of live MIDI input on this channel");

  /**
   * Which channel MIDI messages this channel observes
   */
  public final EnumParameter<LXMidiEngine.Channel> midiChannel =
    new EnumParameter<LXMidiEngine.Channel>("MIDI Channel", LXMidiEngine.Channel.OMNI)
    .setDescription("Determines which MIDI channel is responded to");

  /**
   * Whether auto pattern transition is enabled on this channel
   */
  public final BooleanParameter autoCycleEnabled =
    new BooleanParameter("Auto-Cycle", false)
    .setDescription("When enabled, this channel will automatically cycle between its patterns");

  /**
   * Auto-cycle to a random pattern, not the next one
   */
  public final EnumParameter<AutoCycleMode> autoCycleMode =
    new EnumParameter<AutoCycleMode>("Auto-Cycle Mode", AutoCycleMode.NEXT)
    .setDescription("Mode of auto cycling");

  /**
   * Time in milliseconds after which transition thru the pattern set is automatically initiated.
   */
  public final BoundedParameter autoCycleTimeSecs = (BoundedParameter)
    new BoundedParameter("Cycle Time", 60, .1, 60*60*4)
    .setDescription("Sets the number of seconds after which the channel cycles to the next pattern")
    .setUnits(LXParameter.Units.SECONDS);

  public final BoundedParameter transitionTimeSecs = (BoundedParameter)
    new BoundedParameter("Transition Time", 5, .1, 180)
    .setDescription("Sets the duration of blending transitions between patterns")
    .setUnits(LXParameter.Units.SECONDS);

  public final BooleanParameter transitionEnabled =
    new BooleanParameter("Transitions", false)
    .setDescription("When enabled, transitions between patterns use a blend");

  public final ObjectParameter<LXBlend> transitionBlendMode;

  private final List<LXPattern> mutablePatterns = new ArrayList<LXPattern>();
  public final List<LXPattern> patterns = Collections.unmodifiableList(mutablePatterns);

  public final MutableParameter controlSurfaceFocusIndex = (MutableParameter)
    new MutableParameter("SurfaceFocusIndex", 0)
    .setDescription("Control surface focus index");

  public final MutableParameter controlSurfaceFocusLength = (MutableParameter)
    new MutableParameter("SurfaceFocusLength", 0)
    .setDescription("Control surface focus length");

  private double autoCycleProgress = 0;
  private double transitionProgress = 0;
  private int activePatternIndex = -1;
  private int nextPatternIndex = -1;

  /**
   * Group that this channel belongs to
   */
  private LXGroup group = null;

  /**
   * Transition that we are in the middle of executing
   */
  private LXBlend transition = null;

  private long transitionMillis = 0;

  LXChannel(LX lx, int index, LXPattern[] patterns) {
    super(lx, index, "Channel-" + (index+1));

    this.focusedPattern =
      new DiscreteParameter("Focused Pattern", 0, Math.max(1, patterns.length))
      .setDescription("Which pattern has focus in the UI");

    this.transitionBlendMode = new ObjectParameter<LXBlend>("Transition Blend", new LXBlend[1])
      .setDescription("Specifies the blending function used for transitions between patterns on the channel");
    updateTransitionBlendOptions();

    this.transitionMillis = lx.engine.nowMillis;

    _updatePatterns(patterns);

    // Initialize colors array
    LXPattern pattern = getActivePattern();
    this.colors = (pattern != null) ? pattern.getColors() : this.blendBuffer.getArray();

    addArray("pattern", this.patterns);

    addInternalParameter("controlsExpanded", this.controlsExpanded);
    addParameter("midiMonitor", this.midiMonitor);
    addParameter("midiChannel", this.midiChannel);
    addParameter("autoCycleEnabled", this.autoCycleEnabled);
    addParameter("autoCycleMode", this.autoCycleMode);
    addParameter("autoCycleTimeSecs", this.autoCycleTimeSecs);
    addParameter("transitionEnabled", this.transitionEnabled);
    addParameter("transitionTimeSecs", this.transitionTimeSecs);
    addParameter("transitionBlendMode", this.transitionBlendMode);
  }

  void updateTransitionBlendOptions() {
    for (LXBlend blend : this.transitionBlendMode.getObjects()) {
      if (blend != null) {
        blend.dispose();
      }
    }
    this.transitionBlendMode.setObjects(this.lx.instantiateTransitionBlends());
  }

  @Override
  public void onParameterChanged(LXParameter p) {
    super.onParameterChanged(p);
    if (p == this.autoCycleEnabled) {
      if (this.transition == null) {
        this.transitionMillis = this.lx.engine.nowMillis;
      }
    }
  }

  @Override
  protected void onModelChanged(LXModel model) {
    super.onModelChanged(model);
    for (LXPattern pattern : this.mutablePatterns) {
      pattern.setModel(model);
    }
  }

  public final void addListener(Listener listener) {
    super.addListener(listener);
    this.listeners.add(listener);
  }

  public final void removeListener(Listener listener) {
    super.removeListener(listener);
    this.listeners.remove(listener);
  }

  public LXChannel addMidiListener(MidiListener listener) {
    this.midiListeners.add(listener);
    return this;
  }

  public LXChannel removeMidiListener(MidiListener listener) {
    this.midiListeners.remove(listener);
    return this;
  }

  public static final String PATH_PATTERN = "pattern";
  public static final String PATH_ACTIVE = "active";
  public static final String PATH_ACTIVE_PATTERN = "activePattern";
  public static final String PATH_NEXT_PATTERN = "nextPattern";

  @Override
  public boolean handleOscMessage(OscMessage message, String[] parts, int index) {
    String path = parts[index];
    if (path.equals(PATH_PATTERN)) {
      String patternId = parts[index+1];
      LXPattern pattern = null;
      if (patternId.equals(PATH_ACTIVE)) {
        pattern = getActivePattern();
      } else if (patternId.matches("\\d+")) {
        pattern = this.patterns.get(Integer.parseInt(patternId) - 1);
      } else {
        for (LXPattern p : this.patterns) {
          if (p.getOscLabel().equals(patternId)) {
            pattern = p;
            break;
          }
        }
      }
      if (pattern == null) {
        System.err.println("[OSC] Channel " + getLabel() + " has no pattern at path: " + patternId);
        return false;
      } else {
        return pattern.handleOscMessage(message, parts, index + 2);
      }
    } else if (path.equals(PATH_ACTIVE_PATTERN) || path.equals(PATH_NEXT_PATTERN)) {
      goIndex(message.getInt());
      return true;
    }
    return super.handleOscMessage(message, parts, index);
  }

  public void midiMessage(LXShortMessage message) {
    for (MidiListener listener : this.midiListeners) {
      listener.midiReceived(this, message);
    }
    midiDispatch(message);
  }

  public void midiDispatch(LXShortMessage message) {
    LXPattern activePattern = getActivePattern();
    message.dispatch(activePattern);
    LXPattern nextPattern = getNextPattern();
    if (nextPattern != null && nextPattern != activePattern) {
      message.dispatch(nextPattern);
    }
  }

  LXChannel setGroup(LXGroup group) {
    if (this.group != group) {
      this.group = group;
      for (Listener listener : this.listeners) {
        listener.groupChanged(this, group);
      }
    }
    return this;
  }

  @Override
  public LXGroup getGroup() {
    return this.group;
  }

  @Override
  protected LXClip constructClip(int index) {
    return new LXChannelClip(this.lx, this, index);
  }

  public final List<LXPattern> getPatterns() {
    return this.patterns;
  }

  public final LXPattern getPattern(int index) {
    return this.patterns.get(index);
  }

  public final LXPattern getPattern(String label) {
    for (LXPattern pattern : this.patterns) {
      if (pattern.getLabel().equals(label)) {
        return pattern;
      }
    }
    return null;
  }

  public final LXPattern getPatternByClassName(String className) {
    for (LXPattern pattern : this.patterns) {
      if (pattern.getClass().getName().equals(className)) {
        return pattern;
      }
    }
    return null;
  }

  public final LXChannel setPatterns(LXPattern[] patterns) {
    LXPattern active;

    // Clean up any existing transition or running pattern
    if (this.transition != null) {
      finishTransition();
    } else {
      active = getActivePattern();
      if (active != null) {
        active.onInactive();
      }
    }
    _updatePatterns(patterns);
    this.activePatternIndex = this.nextPatternIndex = (this.patterns.isEmpty()) ? -1 : 0;
    this.transition = null;

    // Is there an active pattern? Notify it
    active = getActivePattern();
    if (active != null) {
      active.onActive();
    }
    return this;
  }

  public final LXChannel addPattern(LXPattern pattern) {
    return addPattern(pattern, -1);
  }

  public final LXChannel addPattern(LXPattern pattern, int index) {
    if (index > this.mutablePatterns.size()) {
      throw new IllegalArgumentException("Invalid pattern index: " + index);
    }
    pattern.setChannel(this);
    pattern.setModel(this.model);

    // Make sure focused pattern doesn't change
    LXPattern focusedPattern = getFocusedPattern();

    if (index < 0) {
      pattern.setIndex(this.mutablePatterns.size());
      this.mutablePatterns.add(pattern);
    } else {
      pattern.setIndex(index);

      LXPattern activePattern = getActivePattern();
      LXPattern nextPattern = getNextPattern();

      this.mutablePatterns.add(index, pattern);
      for (int i = index + 1; i < this.mutablePatterns.size(); ++i) {
        this.mutablePatterns.get(i).setIndex(i);
      }

      // Keep active/next as they were
      if (activePattern != null) {
        this.activePatternIndex = activePattern.getIndex();
      }
      if (nextPattern != null) {
        this.nextPatternIndex = nextPattern.getIndex();
      }
    }

    this.focusedPattern.setRange(Math.max(1, this.mutablePatterns.size()));
    if (focusedPattern != null) {
      // Retain the focused pattern index if it has been shifted
      this.focusedPattern.setValue(focusedPattern.getIndex());
    } else {
      // Otherwise send a bang - newly added pattern is focused
      this.focusedPattern.bang();
    }

    this.listenerSnapshot.clear();
    this.listenerSnapshot.addAll(this.listeners);
    for (Listener listener : this.listenerSnapshot) {
      listener.patternAdded(this, pattern);
    }

    // If this was the first pattern, focusedPattern has "changed" going from 0 -> 0
    if (this.mutablePatterns.size() == 1) {
      this.activePatternIndex = this.nextPatternIndex = 0;
      this.focusedPattern.bang();
      LXPattern activePattern = getActivePattern();
      activePattern.onActive();
      for (Listener listener : this.listeners) {
        listener.patternDidChange(this, activePattern);
      }
    }
    return this;
  }

  public final LXChannel removePattern(LXPattern pattern) {
    int index = this.mutablePatterns.indexOf(pattern);
    if (index < 0) {
      return this;
    }
    boolean wasActive = (this.activePatternIndex == index);
    boolean wasNext = (this.transition != null) && (this.nextPatternIndex == index);
    boolean activateNext = false;
    int focusedPatternIndex = this.focusedPattern.getValuei();
    if (this.transition != null) {
      if (wasNext) {
        cancelTransition();
      } else if (wasActive) {
        finishTransition();
      }
    } else if (wasActive) {
      pattern.onInactive();
      activateNext = true;
    }
    this.mutablePatterns.remove(index);
    for (int i = index; i < this.mutablePatterns.size(); ++i) {
      this.mutablePatterns.get(i).setIndex(i);
    }
    if (this.activePatternIndex > index) {
      --this.activePatternIndex;
    } else if (this.activePatternIndex >= this.mutablePatterns.size()) {
      this.activePatternIndex = this.mutablePatterns.size() - 1;
    }
    if (this.nextPatternIndex > index) {
      --this.nextPatternIndex;
    } else if (this.nextPatternIndex >= this.mutablePatterns.size()) {
      this.nextPatternIndex = this.mutablePatterns.size() - 1;
    }
    if (focusedPatternIndex > index) {
      --focusedPatternIndex;
    } else if (focusedPatternIndex >= this.mutablePatterns.size()) {
      focusedPatternIndex = this.mutablePatterns.size() - 1;
    }
    if ((focusedPatternIndex >= 0) && (this.focusedPattern.getValuei() != focusedPatternIndex)) {
      this.focusedPattern.setValue(focusedPatternIndex);
    } else {
      // Either the value wasn't changed, or we removed the last one
      this.focusedPattern.bang();
    }
    this.focusedPattern.setRange(Math.max(1, this.mutablePatterns.size()));
    this.listenerSnapshot.clear();
    this.listenerSnapshot.addAll(this.listeners);
    for (Listener listener : this.listenerSnapshot) {
      listener.patternRemoved(this, pattern);
    }
    if (activateNext && !this.patterns.isEmpty()) {
      LXPattern newActive = getActivePattern();
      newActive.onActive();
      for (Listener listener : this.listeners) {
        listener.patternDidChange(this, newActive);
      }
      this.lx.engine.osc.sendMessage(getOscAddress() + "/" + PATH_ACTIVE_PATTERN, newActive.getIndex());
    }
    pattern.dispose();
    return this;
  }

  private void _updatePatterns(LXPattern[] patterns) {
    if (patterns == null) {
      throw new IllegalArgumentException("May not set null pattern array");
    }
    // Remove all existing patterns
    for (int i = this.mutablePatterns.size() - 1; i >= 0; --i) {
      removePattern(this.mutablePatterns.get(i));
    }
    // Add new patterns
    for (LXPattern pattern : patterns) {
      if (pattern == null) {
        throw new IllegalArgumentException("Pattern array may not include null elements");
      }
      addPattern(pattern);
    }
  }

  public LXChannel movePattern(LXPattern pattern, int index) {
    LXPattern focusedPattern = getFocusedPattern();
    LXPattern activePattern = getActivePattern();
    LXPattern nextPattern = getNextPattern();
    this.mutablePatterns.remove(pattern);
    this.mutablePatterns.add(index, pattern);
    int i = 0;
    for (LXPattern p : this.mutablePatterns) {
       p.setIndex(i++);
    }
    this.activePatternIndex = activePattern.getIndex();
    this.nextPatternIndex = nextPattern.getIndex();
    for (Listener listener : this.listeners) {
      listener.patternMoved(this, pattern);
    }
    if (pattern == focusedPattern) {
      this.focusedPattern.setValue(pattern.getIndex());
    }
    return this;
  }

  public LXChannel reloadPattern(LXPattern pattern) {
    if (!this.patterns.contains(pattern)) {
      throw new IllegalStateException("Cannot reload pattern not on a channel");
    }
    boolean active = (pattern == getActivePattern());
    boolean focused = (pattern == getFocusedPattern());

    int index = pattern.getIndex();
    JsonObject patternObj = new JsonObject();
    pattern.save(getLX(), patternObj);
    removePattern(pattern);
    LXPattern newPattern = loadPattern(patternObj, index);
    if (focused) {
      this.focusedPattern.setValue(newPattern.getIndex());
    }
    if (active) {
      goPattern(newPattern);
    }
    return this;
  }

  public final int getFocusedPatternIndex() {
    return this.focusedPattern.getValuei();
  }

  public final LXPattern getFocusedPattern() {
    if (this.patterns.isEmpty()) {
      return null;
    }
    return this.patterns.get(this.focusedPattern.getValuei());
  }

  public final int getActivePatternIndex() {
    return this.activePatternIndex;
  }

  public final LXPattern getActivePattern() {
    return (this.activePatternIndex >= 0) ? this.mutablePatterns.get(this.activePatternIndex) : null;
  }

  public final LXPattern getTargetPattern() {
    return (this.transition != null) ? getNextPattern() : getActivePattern();
  }

  public final int getNextPatternIndex() {
    return this.nextPatternIndex;
  }

  public final LXPattern getNextPattern() {
    return (this.nextPatternIndex >= 0) ? this.mutablePatterns.get(this.nextPatternIndex) : null;
  }

  public final LXChannel goPrev() {
    if (this.transition != null) {
      return this;
    }
    if (this.patterns.size() <= 1) {
      return this;
    }

    this.nextPatternIndex = this.activePatternIndex - 1;
    if (this.nextPatternIndex < 0) {
      this.nextPatternIndex = this.mutablePatterns.size() - 1;
    }
    startTransition();
    return this;
  }

  public final LXChannel goNext() {
    if (this.transition != null) {
      return this;
    }
    if (this.patterns.size() <= 1) {
      return this;
    }
    this.nextPatternIndex = this.activePatternIndex;
    do {
      this.nextPatternIndex = (this.nextPatternIndex + 1) % this.patterns.size();
    } while ((this.nextPatternIndex != this.activePatternIndex)
        && !getNextPattern().isAutoCycleEligible());
    if (this.nextPatternIndex != this.activePatternIndex) {
      startTransition();
    }
    return this;
  }

  public final LXChannel goPattern(LXPattern pattern) {
    int index = this.patterns.indexOf(pattern);
    if (index >= 0) {
      goIndex(index);
    }
    return this;
  }

  private final List<LXPattern> randomEligible = new ArrayList<LXPattern>();

  public final LXChannel goRandom() {
    if (this.transition != null) {
      return this;
    }
    if (this.patterns.size() <= 1) {
      return this;
    }
    LXPattern activePattern = getActivePattern();
    this.randomEligible.clear();
    for (LXPattern pattern : this.patterns) {
      if (pattern != activePattern && pattern.isAutoCycleEligible()) {
        this.randomEligible.add(pattern);
      }
    }
    int numEligible = this.randomEligible.size();
    if (numEligible > 0) {
      return goPattern(this.randomEligible.get(LXUtils.constrain((int) LXUtils.random(0, numEligible), 0, numEligible - 1)));
    }
    return this;
  }

  public final LXChannel goIndex(int i) {
    if (i < 0 || i >= this.patterns.size()) {
      return this;
    }
    if (this.transition != null) {
      finishTransition();
    }
    this.nextPatternIndex = i;
    startTransition();
    return this;
  }

  public LXBus disableAutoCycle() {
    this.autoCycleEnabled.setValue(false);
    return this;
  }

  /**
   * Enable automatic transition from pattern to pattern on this channel
   *
   * @param autoCycleThreshold time in seconds
   * @return this
   */
  public LXBus enableAutoCycle(double autoCycleThreshold) {
    this.autoCycleTimeSecs.setValue(autoCycleThreshold);
    this.autoCycleEnabled.setValue(true);
    return this;
  }

  /**
   * Return progress towards making a cycle
   *
   * @return amount of progress towards the next cycle
   */
  public double getAutoCycleProgress() {
    return this.autoCycleProgress;
  }

  /**
   * Return progress through a transition
   *
   * @return amount of progress thru current transition
   */
  public double getTransitionProgress() {
    return this.transitionProgress;
  }

  private void startTransition() {
    LXPattern activePattern = getActivePattern();
    LXPattern nextPattern = getNextPattern();
    if (activePattern == nextPattern) {
      return;
    }
    nextPattern.onActive();
    for (Listener listener : this.listeners) {
      listener.patternWillChange(this, activePattern, nextPattern);
    }
    this.lx.engine.osc.sendMessage(getOscAddress() + "/" + PATH_NEXT_PATTERN, nextPattern.getIndex());
    if (this.transitionEnabled.isOn()) {
      this.transition = this.transitionBlendMode.getObject();
      this.transition.onActive();
      nextPattern.onTransitionStart();
      this.transitionMillis = this.lx.engine.nowMillis;
    } else {
      finishTransition();
    }
  }

  private void cancelTransition() {
    if (this.transition != null) {
      LXPattern nextPattern = getNextPattern();
      nextPattern.onTransitionEnd();
      nextPattern.onInactive();
      this.transition.onInactive();
      this.transition = null;
      this.transitionMillis = this.lx.engine.nowMillis;
      LXPattern activePattern = getActivePattern();
      for (Listener listener : listeners) {
        listener.patternDidChange(this, activePattern);
      }
      this.lx.engine.osc.sendMessage(getOscAddress() + "/" + PATH_ACTIVE_PATTERN, activePattern.getIndex());
      this.lx.engine.osc.sendMessage(getOscAddress() + "/" + PATH_NEXT_PATTERN, -1);
    }
  }

  private void finishTransition() {
    getActivePattern().onInactive();
    this.activePatternIndex = this.nextPatternIndex;
    LXPattern activePattern = getActivePattern();
    if (this.transition != null) {
      activePattern.onTransitionEnd();
      this.transition.onInactive();
    }
    this.transition = null;
    this.transitionMillis = this.lx.engine.nowMillis;
    for (Listener listener : listeners) {
      listener.patternDidChange(this, activePattern);
    }
    this.lx.engine.osc.sendMessage(getOscAddress() + "/" + PATH_ACTIVE_PATTERN, activePattern.getIndex());
    this.lx.engine.osc.sendMessage(getOscAddress() + "/" + PATH_NEXT_PATTERN, -1);
    if (this.lx.flags.focusActivePattern) {
      this.focusedPattern.setValue(this.activePatternIndex);
    }
  }

  @Override
  public void loop(double deltaMs) {
    long loopStart = System.nanoTime();

    // Delegate to LXChannelBus loop method
    super.loop(deltaMs);

    // LXChannelBus will have figured out if we need to run everything.
    // If not, then we're done here and skip the rest.
    if (!this.isAnimating) {
      this.timer.loopNanos = System.nanoTime() - loopStart;
      return;
    }

    // Check for transition completion
    if (this.transition != null) {
      double transitionMs = this.lx.engine.nowMillis - this.transitionMillis;
      double transitionDone = 1000 * this.transitionTimeSecs.getValue();
      if (transitionMs >= transitionDone) {
        finishTransition();
      }
    }

    // Auto-cycle if appropriate
    if (this.transition == null) {
      this.autoCycleProgress = (this.lx.engine.nowMillis - this.transitionMillis) / (1000 * this.autoCycleTimeSecs.getValue());
      if (this.autoCycleProgress >= 1) {
        this.autoCycleProgress = 1;
        if (this.autoCycleEnabled.isOn()) {
          switch (this.autoCycleMode.getEnum()) {
          case NEXT:
            goNext();
            break;
          case RANDOM:
            goRandom();
            break;
          }
        }
      }
    }

    // Initialize colors...
    int[] colors = this.blendBuffer.getArray();

    // Run active pattern
    LXPattern activePattern = getActivePattern();
    if (activePattern != null) {
      activePattern.loop(deltaMs);
      colors = activePattern.getColors();
    }

    // Run transition!
    if (this.transition != null) {
      this.autoCycleProgress = 1.;
      this.transitionProgress = (this.lx.engine.nowMillis - this.transitionMillis) / (1000 * this.transitionTimeSecs.getValue());
      getNextPattern().loop(deltaMs);
      this.transition.loop(deltaMs);
      colors = this.blendBuffer.getArray();
      this.transition.lerp(
        getActivePattern().getColors(),
        getNextPattern().getColors(),
        this.transitionProgress,
        colors
      );
    } else {
      this.transitionProgress = 0;
    }

    // Apply effects
    long effectStart = System.nanoTime();
    if (this.mutableEffects.size() > 0) {
      int[] array = this.blendBuffer.getArray();
      if (colors != array) {
        System.arraycopy(colors, 0, array, 0, colors.length);
      }
      colors = array;
      for (LXEffect effect : this.mutableEffects) {
        effect.setBuffer(this.blendBuffer);
        effect.loop(deltaMs);
      }
    }
    ((LXBus.Timer) this.timer).effectNanos = System.nanoTime() - effectStart;

    this.colors = colors;
    this.timer.loopNanos = System.nanoTime() - loopStart;
  }

  @Override
  public void dispose() {
    for (LXPattern pattern : this.mutablePatterns) {
      pattern.dispose();
    }
    this.mutablePatterns.clear();
    if (this.thread.hasStarted) {
      this.thread.interrupt();
    }
    super.dispose();
  }

  private static final String KEY_PATTERNS = "patterns";
  private static final String KEY_PATTERN_INDEX = "patternIndex";
  private static final String KEY_GROUP = "group";

  @Override
  public void save(LX lx, JsonObject obj) {
    super.save(lx, obj);
    obj.addProperty(KEY_PATTERN_INDEX, this.activePatternIndex);
    obj.add(KEY_PATTERNS, LXSerializable.Utils.toArray(lx, this.patterns));
    if (this.group != null) {
      obj.addProperty(KEY_GROUP, this.group.getId());
    }
  }

  @Override
  public void load(LX lx, JsonObject obj) {
    // Remove patterns
    for (int i = this.mutablePatterns.size() - 1; i >= 0; --i) {
      removePattern(this.mutablePatterns.get(i));
    }

    // Set appropriate group membership
    if (obj.has(KEY_GROUP)) {
      LXGroup group = (LXGroup) lx.getProjectComponent(obj.get(KEY_GROUP).getAsInt());
      group.addChannel(this);
    }

    // Add patterns
    JsonArray patternsArray = obj.getAsJsonArray(KEY_PATTERNS);
    for (JsonElement patternElement : patternsArray) {
      JsonObject patternObj = (JsonObject) patternElement;
      loadPattern(patternObj, -1);
    }

    // Set the active index instantly, do not transition!
    this.activePatternIndex = this.nextPatternIndex = -1;
    if (obj.has(KEY_PATTERN_INDEX)) {
      int patternIndex = obj.get(KEY_PATTERN_INDEX).getAsInt();
      if (patternIndex < this.patterns.size()) {
        this.activePatternIndex = this.nextPatternIndex = patternIndex;
      }
    }
    LXPattern activePattern = getActivePattern();
    if (activePattern != null) {
      for (Listener listener : listeners) {
        listener.patternDidChange(this, activePattern);
      }
      this.lx.engine.osc.sendMessage(getOscAddress() + "/" + PATH_ACTIVE_PATTERN, activePattern.getIndex());
    }

    // Set the focused pattern to the active one, if it exists
    if (this.activePatternIndex >= 0) {
      this.focusedPattern.setValue(this.activePatternIndex);
    }

    super.load(lx, obj);
  }

  private LXPattern loadPattern(JsonObject patternObj, int index) {
    String patternClass = patternObj.get(KEY_CLASS).getAsString();
    LXPattern pattern;
    try {
      pattern = this.lx.instantiatePattern(patternClass);
    } catch (LX.InstantiationException x) {
      pattern = new LXPattern.Placeholder(lx);
      lx.command.pushError("Pattern class " + patternClass + " could not be loaded, check that content files were not removed?", x);
    }
    pattern.load(lx, patternObj);
    addPattern(pattern, index);
    return pattern;
  }


}
