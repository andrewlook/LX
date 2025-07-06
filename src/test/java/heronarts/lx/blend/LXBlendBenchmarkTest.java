package heronarts.lx.blend;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Timeout;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * Hierarchy:
 *
 *    Trial
 *    ├── Iteration 1
 *      │   ├── Invocation 1
 *      │   ├── Invocation 2
 *      │   └── Invocation N
 *    ├── Iteration 2
 *      │   ├── Invocation 1
 *      │   └── ...
 *      └── Iteration M
 *
 * The rough plan here:
 * - For each "trial", create a bunch of color buffers, so we're not worrying about memory allocation during the test.
 * - Also precompute their correct answers using LX baseline impl, so that for the alternate implementations we can
 *   verify correctness (maybe this belongs here, or maybe a similar harness could be used for randomized testing).
 *
 * - For each "iteration":
 *   - For each "invocation": ("numTests")
 *     - Select a different pair of dest/source arrays, blend them into actual[].
 */
@BenchmarkMode(Mode.All)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Fork(value = 1, warmups = 1)
@Timeout(time = 60, timeUnit = TimeUnit.SECONDS)
public class LXBlendBenchmarkTest {
  int numChannels = 16;
  int numPointsPerTest = 100000;

  // Per-trial state
  int[][] destinations;
  int[][] sources;
  int[][] expectedOutputs;
  int[][] actualOutputs;

  LXBlend.BlendTarget blendTarget;
  double alpha;
  LXBlend blendToTest;

  // Per-invocation state
  int index;
  int[] dst;
  int[] src;
  int[] expected;
  int[] actual;


  @Setup(Level.Trial)
  public void setupWholeTrial() {
    BlendingHarness harness = BlendingHarness.generate(numChannels, numPointsPerTest);

    this.destinations = harness.destinations;
    this.sources = harness.sources;
    this.expectedOutputs = harness.expectedOutputs;
    this.actualOutputs = harness.actualOutputs;

    this.blendTarget = harness.blendTarget;
    this.alpha = harness.alpha;
    this.blendToTest = harness.blendToTest;

    this.index = 0;
  }

  @Setup(Level.Invocation)
  public void setupInvocation() {
    dst = destinations[index];
    src = sources[index];
    expected = expectedOutputs[index];
    actual = actualOutputs[index];
  }

  @TearDown(Level.Invocation)
  public void verifyInvocation() {
    assert Arrays.equals(expected, actual);
    this.index = (this.index + 1) % numChannels;
  }

  @Benchmark
  public void measureLXBlend() {
    blendToTest.blend(dst, src, alpha, actual, blendTarget);
  }

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder()
        .include(LXBlendBenchmarkTest.class.getSimpleName())
        .build();

    new Runner(opt).run();
  }
}
