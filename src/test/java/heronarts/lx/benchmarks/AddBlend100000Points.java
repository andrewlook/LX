package heronarts.lx.benchmarks;

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
import org.openjdk.jmh.annotations.Timeout;

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
@State(Scope.Benchmark)
@Fork(value = 1, warmups = 1)
@Timeout(time = 10, timeUnit = TimeUnit.SECONDS)
public class AddBlend100000Points extends BlendingHarness {
  static final int NUM_CHANNELS = 16;
  static final int NUM_POINTS_PER_CHANNEL = 100_000;

  @Setup(Level.Trial)
  public void setupWholeTrial() {
    setupTrialBase(NUM_CHANNELS, NUM_POINTS_PER_CHANNEL);
  }

  @Benchmark
  public void measureLXBlend() {
    blendToTest.blend(dst, src, alpha, actual, blendTarget);
  }
}
