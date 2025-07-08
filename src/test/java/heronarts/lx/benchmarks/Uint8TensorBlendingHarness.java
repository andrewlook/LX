package heronarts.lx.benchmarks;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import heronarts.lx.buffer.Uint8TensorAddBlend;
import org.nd4j.linalg.api.ndarray.INDArray;
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

import static heronarts.lx.buffer.TensorConverters.intArrayToUint8Tensor2D;
import static heronarts.lx.buffer.TensorConverters.uint8Tensor2DToIntArray;

@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, warmups = 1)
@Timeout(time = 10, timeUnit = TimeUnit.SECONDS)
public class Uint8TensorBlendingHarness extends BlendingHarness {
  // Per-trial state
  public INDArray[] destinationsND;
  public INDArray[] sourcesND;
  public INDArray[] actualOutputsND;

  // Per-invocation state
  INDArray dst;
  INDArray src;
  INDArray actual;

  final Uint8TensorAddBlend uint8TensorAdd = new Uint8TensorAddBlend();

  public void setupTrialBase(int numChannels, int numPointsPerChannel) {
    super.setupTrialBase(numChannels, numPointsPerChannel);

    destinationsND = new INDArray[destinations.length];
    sourcesND = new INDArray[sources.length];
    actualOutputsND = new INDArray[actualOutputs.length];

    for (int i = 0; i < destinations.length; i++) {
      destinationsND[i] = intArrayToUint8Tensor2D(destinations[i]);
    }
    for (int i = 0; i < sources.length; i++) {
      sourcesND[i] = intArrayToUint8Tensor2D(sources[i]);
    }
    for (int i = 0; i < actualOutputs.length; i++) {
      actualOutputsND[i] = intArrayToUint8Tensor2D(actualOutputs[i]);
    }
  }

  @Override
  @Setup(Level.Invocation)
  public void setupInvocationBase() {
    dst = destinationsND[index];
    src = sourcesND[index];
    actual = actualOutputsND[index];
    // copy the expected as int[], since we'll round-trip convert to verify.
    expected = expectedOutputs[index];
  }

  @TearDown(Level.Invocation)
  public void verifyInvocationBase() {
    assert Arrays.equals(expected, uint8Tensor2DToIntArray(actual));
    this.index = (this.index + 1) % this.numChannels;
  }
}
