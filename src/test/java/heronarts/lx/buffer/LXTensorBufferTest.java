package heronarts.lx.buffer;

import org.junit.jupiter.api.BeforeEach;

import static heronarts.lx.blend.BlendTestHelpers.TEST_DEST;
import static heronarts.lx.blend.BlendTestHelpers.TEST_POINTS;
import static heronarts.lx.blend.BlendTestHelpers.TEST_SOURCE;

class LXTensorBufferTest {

  private TensorBufferPrototype src;
  private TensorBufferPrototype dst;

  @BeforeEach
  void setUp() {
    src = new TensorBufferPrototype(TEST_POINTS.length, 0);
    src.setFromIntArray(TEST_SOURCE);

    dst = new TensorBufferPrototype(TEST_POINTS.length, 0);
    dst.setFromIntArray(TEST_DEST);
  }
}