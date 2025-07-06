package heronarts.lx.blend;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;

import static heronarts.lx.blend.LXBlend.BlendTarget.target;

public class BlendingHarness {

  final int[][] destinations;
  final int[][] sources;
  final int[][] expectedOutputs;
  final int[][] actualOutputs;

  final LX lx;
  final LXModel lxModel;

  final double alpha;
  final LXBlend.BlendTarget blendTarget;
  final LXBlend blendToTest;

  public static BlendingHarness generate(int numTests, int numPointsPerTest) {
    int[][] destinations = generateColorArrays(numTests, numPointsPerTest);
    int[][] sources = generateColorArrays(numTests, numPointsPerTest);

    LXModel model = fakeModelWithNumPoints(numPointsPerTest);
    LX lx = new LX(model);

    // Current limitations: only testing one blend at a time, with one alpha value, scoped to whole model (not views)
    LXBlend blendToTest = new AddBlend(lx);
    LXBlend.BlendTarget blendTarget = target(model);
    double alpha = 0.9;

    int[][] expectedOutputs = blendResult(blendToTest, destinations, sources, alpha, blendTarget);
    int[][] actualOutputs = new int[numTests][numPointsPerTest];
    for (int i = 0; i < numTests; i++) {
      Arrays.fill(actualOutputs[i], LXColor.BLACK);
    }

    return new BlendingHarness(
        destinations,
        sources,
        expectedOutputs,
        actualOutputs,
        lx,
        model,
        alpha,
        blendTarget,
        blendToTest
    );
  }

  BlendingHarness(
      int[][] destinations,
      int[][] sources,
      int[][] expectedOutputs,
      int[][] actualOutputs,
      LX lx,
      LXModel lxModel,
      double alpha,
      LXBlend.BlendTarget blendTarget,
      LXBlend blendToTest
  ) {
    this.destinations = destinations;
    this.sources = sources;
    this.expectedOutputs = expectedOutputs;
    this.actualOutputs = actualOutputs;
    this.lx = lx;
    this.lxModel = lxModel;
    this.alpha = alpha;
    this.blendTarget = blendTarget;
    this.blendToTest = blendToTest;
  }

  public static int[][] generateColorArrays(int numTests, int numPointsPerTest) {
    Random rand = new Random();
    int[][] result = new int[numTests][numPointsPerTest];
    for (int i = 0; i < numTests; i++) {
      for (int j = 0; j < numPointsPerTest; j++) {
        int r = rand.nextInt(256);
        int g = rand.nextInt(256);
        int b = rand.nextInt(256);
        int a = rand.nextInt(256);
        result[i][j] = LXColor.rgba(r, g, b, a);
      }
    }
    return result;
  }

  public static LXModel fakeModelWithNumPoints(int numPointsPerTest) {
    List<LXPoint> points = new ArrayList<>();
    for (int j = 0; j < numPointsPerTest; j++) {
      points.add(new LXPoint((float) j, (float) j + 1));
    }
    return new LXModel(points);
  }

  public static int[][] blendResult(LXBlend blend, int[][] left, int[][] right, double alpha, LXBlend.BlendTarget blendTarget) {
    int numTests = left.length;
    int numPointsPerTest = left[0].length;
    if (right.length != numTests) {
      throw new RuntimeException("mismatched num tests");
    } else if (right[0].length != numPointsPerTest) {
      throw new RuntimeException("mismatches num points per test");
    }
    int[][] blendResult = new int[numTests][numPointsPerTest];
    for (int i = 0; i < numTests; i++) {
      int[] out = blendResult[i];
      int[] dst = left[i];
      int[] src = right[i];

      blend.blend(dst, src, alpha, out, blendTarget);
    }
    return blendResult;
  }
}
