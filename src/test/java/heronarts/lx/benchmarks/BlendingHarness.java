package heronarts.lx.benchmarks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import heronarts.lx.LX;
import heronarts.lx.blend.AddBlend;
import heronarts.lx.blend.LXBlend;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import static heronarts.lx.blend.LXBlend.BlendTarget.target;

@State(Scope.Benchmark)
public class BlendingHarness {
  public LX lx;
  public LXModel model;

  // Per-trial state
  public int numChannels;
  public int numPointsPerChannel;
  public int[][] destinations;
  public int[][] sources;
  public int[][] actualOutputs;
  public int[][] expectedOutputs;

  // Current limitations: only testing one blend at a time, with one alpha value, scoped to whole model (not views).
  // TODO: consider separating this logic out - e.g. to make an array of blends to test, or array of BlendTargets, etc
  public double alpha;
  public LXBlend.BlendTarget blendTarget;
  public LXBlend blendToTest;

  // Per-invocation state
  int index;
  int[] dst;
  int[] src;
  int[] expected;
  int[] actual;

  public void setupTrialBase(int numChannels, int numPointsPerChannel) {
    this.numChannels = numChannels;
    this.numPointsPerChannel = numPointsPerChannel;

    model = fakeModelWithNumPoints(numPointsPerChannel);
    lx = new LX(model);

    destinations = generateColorArrays(numChannels, numPointsPerChannel);
    sources = generateColorArrays(numChannels, numPointsPerChannel);
    actualOutputs = new int[numChannels][numPointsPerChannel];
    // zero out the arrays we'll use to store our outputs
    for (int i = 0; i < numChannels; i++) {
      Arrays.fill(actualOutputs[i], LXColor.BLACK);
    }

    blendToTest = new AddBlend(lx);
    blendTarget = target(model);
    alpha = 0.9;
    expectedOutputs = blendResult(blendToTest, destinations, sources, alpha, blendTarget);
  }

  @Setup(Level.Invocation)
  public void setupInvocationBase() {
    dst = destinations[index];
    src = sources[index];
    expected = expectedOutputs[index];
    actual = actualOutputs[index];
  }

  @TearDown(Level.Invocation)
  public void verifyInvocationBase() {
    assert Arrays.equals(expected, actual);
    this.index = (this.index + 1) % this.numChannels;
  }

  public static int[][] generateColorArrays(int numTests, int numPointsPerChannel) {
    Random rand = new Random();
    int[][] result = new int[numTests][numPointsPerChannel];
    for (int i = 0; i < numTests; i++) {
      for (int j = 0; j < numPointsPerChannel; j++) {
        int r = rand.nextInt(256);
        int g = rand.nextInt(256);
        int b = rand.nextInt(256);
        int a = rand.nextInt(256);
        result[i][j] = LXColor.rgba(r, g, b, a);
      }
    }
    return result;
  }

  public static LXModel fakeModelWithNumPoints(int numPointsPerChannel) {
    List<LXPoint> points = new ArrayList<>();
    for (int j = 0; j < numPointsPerChannel; j++) {
      points.add(new LXPoint((float) j, (float) j + 1));
    }
    return new LXModel(points);
  }

  public static int[][] blendResult(LXBlend blend, int[][] left, int[][] right, double alpha, LXBlend.BlendTarget blendTarget) {
    int numTests = left.length;
    int numPointsPerChannel = left[0].length;
    if (right.length != numTests) {
      throw new RuntimeException("mismatched num tests");
    } else if (right[0].length != numPointsPerChannel) {
      throw new RuntimeException("mismatches num points per test");
    }
    int[][] blendResult = new int[numTests][numPointsPerChannel];
    for (int i = 0; i < numTests; i++) {
      int[] out = blendResult[i];
      int[] dst = left[i];
      int[] src = right[i];

      blend.blend(dst, src, alpha, out, blendTarget);
    }
    return blendResult;
  }
}
