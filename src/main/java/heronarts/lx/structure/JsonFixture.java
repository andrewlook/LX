/**
 * Copyright 2019- Mark C. Slee, Heron Arts LLC
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

package heronarts.lx.structure;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonObject;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import heronarts.lx.LX;
import heronarts.lx.LXSerializable;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.output.ArtSyncDatagram;
import heronarts.lx.output.KinetDatagram;
import heronarts.lx.output.LXBufferOutput;
import heronarts.lx.output.LXDatagram;
import heronarts.lx.output.LXOutput;
import heronarts.lx.output.LXOutput.GammaTable;
import heronarts.lx.output.StreamingACNDatagram;
import heronarts.lx.parameter.AggregateParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.IEnumParameter;
import heronarts.lx.parameter.LXListenableParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.MutableParameter;
import heronarts.lx.parameter.ObjectParameter;
import heronarts.lx.parameter.StringParameter;
import heronarts.lx.transform.LXMatrix;
import heronarts.lx.transform.LXVector;
import heronarts.lx.utils.LXUtils;
import okio.Buffer;
import okio.BufferedSource;
import okio.Okio;

public class JsonFixture extends LXFixture {

  public static final String PATH_SEPARATOR = "/";
  public static final char PATH_SEPARATOR_CHAR = '/';

  // Label
  private static final String KEY_LABEL = "label";

  // Model tags
  private static final String KEY_TAG = "tag";
  private static final String KEY_TAGS = "tags";
  private static final String KEY_MODEL_KEY = "modelKey";   // deprecated, backwards-compatible
  private static final String KEY_MODEL_KEYS = "modelKeys"; // deprecated, backwards-compatible

  // Geometry
  private static final String KEY_X = "x";
  private static final String KEY_Y = "y";
  private static final String KEY_Z = "z";
  private static final String KEY_YAW = "yaw";
  private static final String KEY_PITCH = "pitch";
  private static final String KEY_ROLL = "roll";
  private static final String KEY_ROTATE_X = "rotateX";
  private static final String KEY_ROTATE_Y = "rotateY";
  private static final String KEY_ROTATE_Z = "rotateZ";
  private static final String KEY_SCALE_X = "scaleX";
  private static final String KEY_SCALE_Y = "scaleY";
  private static final String KEY_SCALE_Z = "scaleZ";
  private static final String KEY_SCALE = "scale";
  private static final String KEY_POINT_SIZE = "pointSize";
  private static final String KEY_DIRECTION = "direction";
  private static final String KEY_NORMAL = "normal";
  private static final String KEY_END = "end";
  private static final String KEY_TRANSFORMS = "transforms";
  private static final String KEY_BRIGHTNESS = "brightness";

  // Points
  private static final String KEY_POINTS = "points";
  private static final String KEY_COORDINATES = "coords";

  // Strips
  private static final String KEY_STRIPS = "strips";
  private static final String KEY_NUM_POINTS = "numPoints";
  private static final String KEY_SPACING = "spacing";

  // Arcs
  private static final String KEY_ARCS = "arcs";
  private static final String KEY_RADIUS = "radius";
  private static final String KEY_DEGREES = "degrees";
  private static final String KEY_ARC_MODE = "mode";
  private static final String VALUE_ARC_MODE_ORIGIN = "origin";
  private static final String VALUE_ARC_MODE_CENTER = "center";

  // Children
  private static final String KEY_COMPONENTS = "components";
  private static final String KEY_CHILDREN = "children";
  private static final String KEY_TYPE = "type";
  private static final String KEY_ID = "id";
  private static final String KEY_INSTANCES = "instances";
  private static final String KEY_INSTANCE = "instance";
  private static final int MAX_INSTANCES = 4096;

  private static final String TYPE_POINT = "point";
  private static final String TYPE_POINTS = "points";
  private static final String TYPE_STRIP = "strip";
  private static final String TYPE_ARC = "arc";
  private static final String TYPE_CLASS = "class";

  // Parameters
  private static final String KEY_PARAMETERS = "parameters";
  private static final String KEY_PARAMETER_LABEL = "label";
  private static final String KEY_PARAMETER_DESCRIPTION = "description";
  private static final String KEY_PARAMETER_TYPE = "type";
  private static final String KEY_PARAMETER_DEFAULT = "default";
  private static final String KEY_PARAMETER_MIN = "min";
  private static final String KEY_PARAMETER_MAX = "max";
  private static final String KEY_PARAMETER_OPTIONS = "options";

  // Outputs
  private static final String KEY_OUTPUT = "output";
  private static final String KEY_OUTPUTS = "outputs";
  private static final String KEY_ENABLED = "enabled";
  private static final String KEY_FPS = "fps";
  private static final String KEY_PROTOCOL = "protocol";
  private static final String KEY_TRANSPORT = "transport";
  private static final String KEY_HOST = "host";
  private static final String KEY_PORT = "port";
  private static final String KEY_BYTE_ORDER = "byteOrder";
  private static final String KEY_UNIVERSE = "universe";
  private static final String KEY_DDP_DATA_OFFSET = "dataOffset";
  private static final String KEY_KINET_PORT = "kinetPort";
  private static final String KEY_KINET_VERSION = "kinetVersion";
  private static final String KEY_OPC_CHANNEL = "channel";
  private static final String KEY_CHANNEL = "channel";
  private static final String KEY_PRIORITY = "priority";
  private static final String KEY_SEQUENCE_ENABLED = "sequenceEnabled";
  private static final String KEY_OFFSET = "offset";
  private static final String KEY_START = "start";
  private static final String KEY_COMPONENT_INDEX = "componentIndex";
  private static final String KEY_COMPONENT_ID = "componentId";
  private static final String KEY_NUM = "num";
  private static final String KEY_STRIDE = "stride";
  private static final String KEY_OUTPUT_STRIDE = "outputStride";
  private static final String KEY_PAD_PRE = "padPre";
  private static final String KEY_PAD_POST = "padPost";
  private static final String KEY_REPEAT = "repeat";
  private static final String KEY_DUPLICATE = "duplicate";
  private static final String KEY_REVERSE = "reverse";
  private static final String KEY_HEADER_BYTES = "headerBytes";
  private static final String KEY_FOOTER_BYTES = "footerBytes";
  private static final String KEY_SEGMENTS = "segments";

  // Metadata
  private static final String KEY_META = "meta";

  // UI
  private static final String KEY_UI = "ui";
  private static final String KEY_MESH = "mesh";
  private static final String KEY_MESHES = "meshes";
  private static final String KEY_MESH_COLOR = "color";
  private static final String KEY_MESH_TEXTURE = "texture";
  private static final String KEY_MESH_FILE = "file";
  private static final String KEY_MESH_VERTICES = "vertices";
  private static final String KEY_MESH_INVERT_NORMALS = "invertNormals";
  private static final String KEY_MESH_RECT_WIDTH = "width";
  private static final String KEY_MESH_RECT_HEIGHT = "height";
  private static final String KEY_MESH_RECT_DEPTH = "depth";
  private static final String KEY_MESH_RECT_AXIS = "axis";

  private static final String KEY_MESH_LIGHTING = "lighting";
  private static final String KEY_MESH_LIGHTING_COLOR = "color";
  private static final String KEY_MESH_LIGHTING_DIRECTION = "direction";
  private static final String KEY_MESH_LIGHTING_AMBIENT = "ambient";
  private static final String KEY_MESH_LIGHTING_DIFFUSE = "diffuse";
  private static final String KEY_MESH_LIGHTING_SPECULAR = "specular";
  private static final String KEY_MESH_LIGHTING_SHININESS = "shininess";

  private static final String MESH_TYPE_UNIFORM_FILL = "uniformFill";
  private static final String MESH_TYPE_TEXTURE_2D = "texture2d";
  private static final String MESH_TYPE_PHONG = "phong";

  private static final String LABEL_PLACEHOLDER = "UNKNOWN";

  private enum JsonProtocolDefinition {
    ARTNET(LXProtocolFixture.Protocol.ARTNET, KEY_UNIVERSE, KEY_CHANNEL, "artnet", "artdmx"),
    ARTSYNC(LXProtocolFixture.Protocol.ARTNET, null, null, "artsync"),
    SACN(LXProtocolFixture.Protocol.SACN, KEY_UNIVERSE, KEY_CHANNEL, "sacn", "e131"),
    DDP(LXProtocolFixture.Protocol.DDP, KEY_DDP_DATA_OFFSET, null, "ddp"),
    OPC(LXProtocolFixture.Protocol.OPC, KEY_OPC_CHANNEL, KEY_OFFSET, "opc"),
    KINET(LXProtocolFixture.Protocol.KINET, KEY_KINET_PORT, KEY_CHANNEL, "kinet");

    private final LXProtocolFixture.Protocol protocol;
    private final String universeKey;
    private final String channelKey;
    private final String[] protocolKeys;

    private JsonProtocolDefinition(LXProtocolFixture.Protocol protocol, String universeKey, String channelKey, String ... protocolKeys) {
      this.protocol = protocol;
      this.universeKey = universeKey;
      this.channelKey = channelKey;
      this.protocolKeys = protocolKeys;
    }

    public boolean requiresExplicitPort() {
      return (this == OPC);
    }

    private static JsonProtocolDefinition get(String key) {
      for (JsonProtocolDefinition protocol : values()) {
        for (String protocolKey : protocol.protocolKeys) {
          if (protocolKey.equals(key)) {
            return protocol;
          }
        }
      }
      return null;
    }
  }

  private enum JsonTransportDefinition {
    UDP(LXProtocolFixture.Transport.UDP, "udp"),
    TCP(LXProtocolFixture.Transport.TCP, "tcp");

    private final LXProtocolFixture.Transport transport;
    private final String transportKey;

    private JsonTransportDefinition(LXProtocolFixture.Transport transport, String transportKey) {
      this.transport = transport;
      this.transportKey = transportKey;
    }

    private static JsonTransportDefinition get(String key) {
      for (JsonTransportDefinition protocol : values()) {
        if (protocol.transportKey.equals(key)) {
          return protocol;
        }
      }
      return null;
    }

  }

  // A bit superfluous, but avoiding using the LXBufferOutput stuff
  // directly as JSON-loading is a separate namespace and want the code
  // to clearly reflect that, in case the two diverge in the future
  private static class JsonByteEncoderDefinition {

    private static final JsonByteEncoderDefinition RGB = new JsonByteEncoderDefinition(LXBufferOutput.ByteOrder.RGB);

    private static final Map<String, JsonByteEncoderDefinition> instances = new HashMap<String, JsonByteEncoderDefinition>();

    private final LXBufferOutput.ByteEncoder byteEncoder;

    private JsonByteEncoderDefinition(LXBufferOutput.ByteEncoder byteEncoder) {
      this.byteEncoder = byteEncoder;
    }

    private JsonByteEncoderDefinition(LX lx, final String className) throws NoSuchMethodException, ClassNotFoundException {
      final Class<?> cls = lx.instantiateStatic(className.replace('/', '$'));
      final Method getNumBytes = cls.getMethod("getNumBytes");
      final Method writeBytes = cls.getMethod("writeBytes", int.class, GammaTable.Curve.class, byte[].class, int.class);
      this.byteEncoder = new LXBufferOutput.ByteEncoder() {

        @Override
        public int getNumBytes() {
          try {
            return (int) getNumBytes.invoke(null);
          } catch (Throwable t) {
            LX.error("ByteEncoderClass " + cls  + " error on getNumBytes: " + t.getMessage());
            return 0;
          }
        }

        @Override
        public void writeBytes(int argb, GammaTable.Curve gamma, byte[] output, int offset) {
          try {
            writeBytes.invoke(null, argb, gamma, output, offset);
          } catch (Throwable t) {
            LX.error("ByteEncoderClass " + cls  + " error on writeBytes: " + t.getMessage());
          }
        }

      };
    }

    private static JsonByteEncoderDefinition get(LX lx, String order) {
      // Did we already look this encoder up? Re-use it!
      JsonByteEncoderDefinition instance = instances.get(order);
      if (instance != null) {
        return instance;
      }


      // Check for the basic byte order enum types
      for (LXBufferOutput.ByteOrder byteOrder : LXBufferOutput.ByteOrder.values()) {
        if (order.equalsIgnoreCase(byteOrder.name())) {
          instance = new JsonByteEncoderDefinition(byteOrder);
          instances.put(order, instance);
          return instance;
        }
      }

      // Try to make a new dynamic encoder based upon class-name
      try {
        final Class<?> cls = lx.instantiateStatic(order.replace('/', '$'));
        final Method getNumBytes = cls.getMethod("getNumBytes");
        final Method writeBytes = cls.getMethod("writeBytes", int.class, GammaTable.Curve.class, byte[].class, int.class);

        // Create a new dynamic encoder which uses reflection to call static methods
        // on the provided class
        instance = new JsonByteEncoderDefinition(new LXBufferOutput.ByteEncoder() {
          @Override
          public int getNumBytes() {
            try {
              return (int) getNumBytes.invoke(null);
            } catch (Throwable t) {
              LX.error("JsonByteEncoder " + cls  + " error on getNumBytes: " + t.getMessage());
              return 0;
            }
          }

          @Override
          public void writeBytes(int argb, GammaTable.Curve gamma, byte[] output, int offset) {
            try {
              writeBytes.invoke(null, argb, gamma, output, offset);
            } catch (Throwable t) {
              LX.error("JsonByteEncoder " + cls  + " error on writeBytes: " + t.getMessage());
            }
          }
        });
        instances.put(order, instance);
        return instance;
      } catch (Throwable x) {
        LX.error(x, "Could not instantiate JsonByteEncoder " + order + ": " + x.getMessage());
      }
      return null;
    }
  }

  public enum ChildType {
    POINT,
    POINTS,
    STRIP,
    ARC,
    JSON,
    CLASS
  };

  public enum ParameterType {
    STRING("string"),
    INT("int"),
    FLOAT("float"),
    BOOLEAN("boolean"),
    STRING_SELECT(null);

    private final String key;

    private ParameterType(String key) {
      this.key = key;
    }

    private static ParameterType get(String str) {
      for (ParameterType type : values()) {
        if (str.toLowerCase().equals(type.key)) {
          return type;
        }
      }
      return null;
    }
  }

  public class ParameterDefinition implements LXParameterListener {

    public final String name;
    public final String label;
    public final String description;
    public final ParameterType type;
    public final LXListenableParameter parameter;
    public final DiscreteParameter intParameter;
    public final BoundedParameter floatParameter;
    public final StringParameter stringParameter;
    public final BooleanParameter booleanParameter;
    public final ObjectParameter<String> stringSelectParameter;

    private boolean isReferenced = false;

    @SuppressWarnings("unchecked")
    private ParameterDefinition(String name, String label, String description, ParameterType type, LXListenableParameter parameter) {
      this.name = name;
      this.label = label;
      this.description = description;
      this.type = type;
      this.parameter = parameter;
      if (description != null) {
        parameter.setDescription(description);
      }
      switch (type) {
      case STRING:
        this.stringParameter = (StringParameter) parameter;
        this.intParameter = null;
        this.floatParameter = null;
        this.booleanParameter = null;
        this.stringSelectParameter = null;
        break;
      case INT:
        this.stringParameter = null;
        this.intParameter = (DiscreteParameter) parameter;
        this.floatParameter = null;
        this.booleanParameter = null;
        this.stringSelectParameter = null;
        break;
      case FLOAT:
        this.stringParameter = null;
        this.intParameter = null;
        this.floatParameter = (BoundedParameter) parameter;
        this.booleanParameter = null;
        this.stringSelectParameter = null;
        break;
      case BOOLEAN:
        this.stringParameter = null;
        this.intParameter = null;
        this.floatParameter = null;
        this.booleanParameter = (BooleanParameter) parameter;
        this.stringSelectParameter = null;
        break;
      case STRING_SELECT:
        this.stringSelectParameter = (ObjectParameter<String>) parameter;
        this.stringParameter = null;
        this.intParameter = null;
        this.floatParameter = null;
        this.booleanParameter = null;
        break;
      default:
        throw new IllegalStateException("Unknown ParameterType: " + type);
      }
      parameter.addListener(this);
    }

    private ParameterDefinition(String name, String label, String description, String value, String defaultStr) {
      this(name, label, description, ParameterType.STRING,
        new StringParameter(label, defaultStr)
        .setValue(value)
      );
    }

    private ParameterDefinition(String name, String label, String description, int value, int defaultInt, int minInt, int maxInt) {
      this(name, label, description, ParameterType.INT,
        (DiscreteParameter) new DiscreteParameter(label, defaultInt, minInt, maxInt + 1)
        .setValue(value)
      );
    }

    private ParameterDefinition(String name, String label, String description, float value, float defaultFloat, float minFloat, float maxFloat) {
      this(name, label, description, ParameterType.FLOAT,
        (BoundedParameter) new BoundedParameter(label, defaultFloat, minFloat, maxFloat)
        .setFormatter(LXParameter.Formatter.DECIMAL_2_TO_8_PLACES)
        .setValue(value)
      );
    }

    private ParameterDefinition(String name, String label, String description, boolean value, boolean defaultBoolean) {
      this(name, label, description, ParameterType.BOOLEAN,
        new BooleanParameter(label, defaultBoolean)
        .setValue(value)
      );
    }

    private ParameterDefinition(String name, String label, String description, String value, String defaultStr, List<String> stringOptions) {
      this(name, label, description, ParameterType.STRING_SELECT,
        new ObjectParameter<String>(label, stringOptions.toArray(new String[0]), defaultStr)
        .setValue(value)
      );
    }

    private void dispose() {
      this.parameter.removeListener(this);
      this.parameter.dispose();
    }

    @Override
    public void onParameterChanged(LXParameter p) {
      if (this.isReferenced) {
        reload(false);
      }
    }

    public String getValueAsString() {
      switch (this.type) {
      case BOOLEAN:
        return String.valueOf(this.booleanParameter.isOn());
      case FLOAT:
        return String.valueOf(this.floatParameter.getValue());
      case INT:
        return String.valueOf(this.intParameter.getValuei());
      case STRING:
        return this.stringParameter.getString();
      case STRING_SELECT:
        return this.stringSelectParameter.getObject();
      default:
        return "";
      }
    }

  }

  private class JsonOutputDefinition {

    private static final int ALL_POINTS = -1;
    private static final int DEFAULT_PORT = -1;

    private final LXFixture fixture;
    private final JsonProtocolDefinition protocol;
    private final JsonTransportDefinition transport;
    private final JsonByteEncoderDefinition byteEncoder;
    private final InetAddress address;
    private final int port;
    private final int universe;
    private final int channel;
    private final int priority;
    private final boolean sequenceEnabled;
    private final KinetDatagram.Version kinetVersion;
    private final float fps;
    private final List<JsonSegmentDefinition> segments;

    private JsonOutputDefinition(LXFixture fixture, JsonProtocolDefinition protocol, JsonTransportDefinition transport, JsonByteEncoderDefinition byteOrder, InetAddress address, int port, int universe, int channel, int priority, boolean sequenceEnabled, KinetDatagram.Version kinetVersion, float fps, List<JsonSegmentDefinition> segments) {
      this.fixture = fixture;
      this.protocol = protocol;
      this.transport = transport;
      this.byteEncoder = byteOrder;
      this.address = address;
      this.port = port;
      this.universe = universe;
      this.channel = channel;
      this.priority = priority;
      this.sequenceEnabled = sequenceEnabled;
      this.kinetVersion = kinetVersion;
      this.fps = fps;
      this.segments = segments;
    }

  }

  private class JsonSegmentDefinition {
    private final int start;
    private final int num;
    private final int stride;
    private final int repeat;
    private final int padPre;
    private final int padPost;
    private final byte[] headerBytes;
    private final byte[] footerBytes;
    private final boolean reverse;
    private final int outputStride;

    // May or may not be specified, if null then the parent output definition is used
    private final JsonByteEncoderDefinition byteEncoder;

    private JsonSegmentDefinition(int start, int num, int stride, int repeat, int padPre, int padPost, boolean reverse, JsonByteEncoderDefinition byteEncoder, byte[] headerBytes, byte[] footerBytes, int outputStride) {
      this.start = start;
      this.num = num;
      this.stride = stride;
      this.repeat = repeat;
      this.reverse = reverse;
      this.padPre = padPre;
      this.padPost = padPost;
      this.byteEncoder = byteEncoder;
      this.headerBytes = headerBytes;
      this.footerBytes = footerBytes;
      this.outputStride = outputStride;
    }
  }

  /**
   * Fixture type parameter stores the file name, without the .lxf suffix
   */
  private final StringParameter fixtureType =
    new StringParameter("Fixture File")
    .setDescription("Fixture definition path");

  public final BooleanParameter error =
    new BooleanParameter("Error", false)
    .setDescription("Whether there was an error loading this fixture");

  public final StringParameter errorMessage =
    new StringParameter("Error Message", "")
    .setDescription("Message describing the error from loading");

  public final BooleanParameter warning =
    new BooleanParameter("Warning", false)
    .setDescription("Whether there are warnings from the loading of the JSON file");

  public final MutableParameter parametersDisposed =
    new MutableParameter("Dispose")
    .setDescription("Monitor for when fixture parameters are disposed");

  public final MutableParameter parametersReloaded =
    new MutableParameter("Reload")
    .setDescription("Monitor for when fixture parameters are reloaded");

  public final List<String> warnings = new CopyOnWriteArrayList<String>();

  private final List<JsonOutputDefinition> definedOutputs = new ArrayList<JsonOutputDefinition>();

  private final List<LXModel.Mesh> mutableMeshes = new ArrayList<>();
  public final List<LXModel.Mesh> meshes = Collections.unmodifiableList(this.mutableMeshes);

  private final Map<String, List<LXFixture>> componentsById = new HashMap<String, List<LXFixture>>();
  private final List<List<LXFixture>> componentsByIndex = new ArrayList<List<LXFixture>>();

  private final LinkedHashMap<String, ParameterDefinition> definedParameters = new LinkedHashMap<String, ParameterDefinition>();
  private final LinkedHashMap<String, ParameterDefinition> reloadParameterValues = new LinkedHashMap<String, ParameterDefinition>();

  // Context in which parameter values are looked up. Typically this is from the fixture itself, but in the case of
  // a sub-fixture, parameter values may come from our parent
  private final JsonFixture jsonParameterContext;

  // Flag to indicate if this is a subfixture of a parent JSON fixture
  private final boolean isJsonSubfixture;

  // Dictionary of values for local parameters (not the parent)
  private Map<String, Object> jsonParameterValues = new HashMap<>();

  private int currentNumInstances = -1;
  private int currentChildInstance = -1;

  public JsonFixture(LX lx) {
    this(lx, null);
  }

  public JsonFixture(LX lx, String fixtureType) {
    super(lx, LABEL_PLACEHOLDER);
    this.isJsonSubfixture = false;
    this.jsonParameterContext = this;
    addParameter("fixtureType", this.fixtureType);
    if (fixtureType != null) {
      this.fixtureType.setValue(fixtureType);
    }
  }

  private JsonFixture(LX lx, JsonFixture parentFixture, Map<String, Object> subFixture, String fixtureType) {
    super(lx, LABEL_PLACEHOLDER);
    this.jsonParameterContext = parentFixture;
    this.jsonParameterValues = subFixture;
    this.isJsonSubfixture = true;
    addParameter("fixtureType", this.fixtureType);
    this.fixtureType.setValue(fixtureType);
  }

  @Override
  public void onParameterChanged(LXParameter p) {
    if (p == this.fixtureType) {
      Map<String, Object> obj = resolveFixtureFile(this.fixtureType.getString());
      if (obj != null) {
        loadFixture(obj, true);
      }
    } else if (p == this.enabled) {
      // JSON fixture enabled cascades down children...
      for (LXFixture child : this.children) {
        child.enabled.setValue(this.enabled.isOn());
      }
    }
    super.onParameterChanged(p);
  }

  public String getFixturePath() {
    String name = this.fixtureType.getString();
    if (!LXUtils.isEmpty(name)) {
      return name + ".lxf";
    }
    return "";
  }

  private void addJsonParameter(ParameterDefinition parameter) {
    if (this.definedParameters.containsKey(parameter.name)) {
      addWarning("Cannot define two parameters of same name: " + parameter.name);
      return;
    }
    this.definedParameters.put(parameter.name, parameter);
  }

  public Collection<ParameterDefinition> getJsonParameters() {
    return Collections.unmodifiableCollection(this.definedParameters.values());
  }

  private void removeJsonParameters() {
    this.parametersDisposed.bang();

    // We're done with these...
    for (ParameterDefinition parameter : this.definedParameters.values()) {
      parameter.dispose();
    }
    // Clear them!
    this.definedParameters.clear();
  }

  private boolean isLoaded = false;

  public void reload() {
    this.reloadParameterValues.clear();
    for (Map.Entry<String, ParameterDefinition> entry : this.definedParameters.entrySet()) {
      this.reloadParameterValues.put(entry.getKey(), entry.getValue());
    }
    reload(true);
    this.reloadParameterValues.clear();
  }

  private void reload(boolean reloadParameters) {
    if (reloadParameters) {
      removeJsonParameters();
    }

    this.warnings.clear();
    this.warning.setValue(false);
    this.errorMessage.setValue("");
    this.error.setValue(false);

    this.definedOutputs.clear();

    // Clear metadata
    this.metaData.clear();

    // Clear the children
    for (LXFixture child : this.children) {
      LX.dispose(child);
    }
    this.mutableChildren.clear();
    this.componentsById.clear();
    this.componentsByIndex.clear();

    // Clear UI meshes
    this.mutableMeshes.clear();

    // Clear transforms
    clearTransforms();

    this.isLoaded = false;
    Map<String, Object> obj = resolveFixtureFile(this.fixtureType.getString());
      if (obj != null) {
        loadFixture(obj, reloadParameters);
      }
    regenerate();
  }

  private File _getMeshFile(String meshType) {
    return this.lx.getMediaFile(LX.Media.FIXTURES, meshType.replace(PATH_SEPARATOR, File.separator), false);
  }

  private File getMeshFile(String meshType) {
    final String prefix = getChildPrefix();
    if (prefix != null) {
      String prefixedType = prefix + PATH_SEPARATOR + meshType;
      File meshFile = _getMeshFile(prefixedType);
      if (meshFile.exists()) {
        return meshFile;
      }
    }
    return _getMeshFile(meshType);
  }

  private File getFixtureFile(String fixtureType) {
    return this.lx.getMediaFile(LX.Media.FIXTURES, fixtureType.replace(PATH_SEPARATOR, File.separator) + ".lxf", false);
  }

  private Map<String, Object> resolveFixtureFile(String fixtureType) {
    if (this.isLoaded) {
      LX.error(new Exception(), "Trying to load JsonFixture twice, why?");
      return null;
    }
    this.isLoaded = true;
    final File fixtureFile = getFixtureFile(fixtureType);
    if (!fixtureFile.exists()) {
      setError("Invalid fixture type, could not find file: " + fixtureFile);
      return null;
    } else if (!fixtureFile.isFile()) {
      setError("Invalid fixture type, not a normal file: " + fixtureFile);
      return null;
    }

    String content = null;
    try {
      content = Files.readString(fixtureFile.toPath());
    } catch (IOException ioe) {
      setError(ioe, "Error loading fixture from " + fixtureFile.getName() + ": " + ioe.getLocalizedMessage());
      setErrorLabel(fixtureType);
      return null;
    }

    Moshi moshi = new Moshi.Builder().build();
    JsonAdapter<Map<String, Object>> adapter = moshi.adapter(
        Types.newParameterizedType(Map.class, String.class, Object.class)
    );
    BufferedSource bufferedSource = Okio.buffer(Okio.source(new ByteArrayInputStream(content.getBytes())));
    JsonReader reader = JsonReader.of(bufferedSource);
    reader.setLenient(true);

    try {
      Map<String, Object> jsonMap = adapter.fromJson(reader);
      return jsonMap;
    } catch (IOException e) {
      setError(e, "Error loading fixture from " + fixtureFile.getName() + ": " + e.getLocalizedMessage());
      setErrorLabel(fixtureType);
      return null;
    }
  }

  void loadFixture(Map<String, Object> obj, boolean loadParameters) {
    try {
      if (loadParameters) {
        loadLabel(obj);
        loadTags(this, obj, true, false);
        loadParameters(obj);
        this.parametersReloaded.bang();
      }

      // Load transforms
      loadTransforms(this, obj);

      // Keeping around for legacy support, but these should all now be a part of
      // the components loading flow
      loadLegacyPoints(obj);
      loadLegacyStrips(obj);
      loadLegacyArcs(obj);
      loadLegacyChildren(obj);

      // Load children of all dynamic types!
      loadComponents(obj);

      // Metadata for this entire fixture
      loadMetaData(obj, this.metaData);

      // Top level outputs on the entire fixture
      loadOutputs(this, obj);

      // Load UI constructs
      loadUI(obj);
    } catch (Exception x) {
      setError(x, "Error loading fixture from " + this.fixtureType.getString() + ": " + x.getLocalizedMessage());
      setErrorLabel(this.fixtureType.getString());
    }
  }

  private void setError(String error) {
    setError(null, error);
  }

  private void setError(Exception x, String error) {
    this.errorMessage.setValue(error);
    this.error.setValue(true);
    LX.error(x, "Fixture " + this.fixtureType.getString() + ".lxf: " + error);
  }

  private void addWarning(String warning) {
    if (warning == null) {
      return;
    }
    this.warnings.add(warning);
    if (this.warning.isOn()) {
      this.warning.bang();
    } else {
      this.warning.setValue(true);
    }
    LX.error("Fixture " + this.fixtureType.getString() + ".lxf: " + warning);
  }

  private void warnDuplicateKeys(Map<String, Object> obj, String ... keys) {
    String found = null;
    for (String key : keys) {
      if (obj.containsKey(key)) {
        if (found != null) {
          addWarning("Should use only one of " + found + " or " + key + " - " + found + " will be ignored.");
        }
        found = key;
      }
    }
  }

  private static final Pattern parameterPattern = Pattern.compile("\\$\\{?([a-zA-Z0-9]+)\\}?");

  private String replaceVariables(String key, String expression, ParameterType returnType) {
    StringBuilder result = new StringBuilder();
    int index = 0;
    Matcher matcher = parameterPattern.matcher(expression);
    while (matcher.find()) {
      String parameterName = matcher.group(1);
      String parameterValue = "";

      if (KEY_INSTANCE.equals(parameterName)) {
        if (returnType == ParameterType.BOOLEAN) {
          addWarning("Cannot load non-boolean parameter $" + parameterName + " into a boolean type: " + key);
          return null;
        }
        if (this.currentChildInstance < 0) {
          addWarning("Cannot reference variable $" + parameterName + " when \"" + KEY_INSTANCES + "\" has not been declared");
          return null;
        }
        parameterValue = String.valueOf(this.currentChildInstance);
      } else if (KEY_INSTANCES.equals(parameterName)) {
        parameterValue = String.valueOf(this.currentNumInstances);
        if (returnType == ParameterType.BOOLEAN) {
          addWarning("Cannot load non-boolean parameter $" + parameterName + " into a boolean type: " + key);
          return null;
        }
      } else {
        ParameterDefinition parameter = this.definedParameters.get(parameterName);
        if (parameter == null) {
          addWarning("Illegal reference in " + key + ", there is no parameter: " + parameterName);
          return null;
        }
        parameter.isReferenced = true;

        switch (returnType) {
        case FLOAT -> {
          switch (parameter.type) {
          case FLOAT, INT -> parameterValue = String.valueOf(parameter.parameter.getValue());
          case BOOLEAN -> parameterValue = String.valueOf(parameter.booleanParameter.isOn());
          default -> {
            addWarning("Cannot load non-numeric parameter $" + parameterName + " into a float type: " + key);
            return null;
          }
          }
        }
        case INT -> {
          switch (parameter.type) {
          case INT -> parameterValue = String.valueOf(parameter.intParameter.getValuei());
          case FLOAT -> parameterValue = String.valueOf(parameter.floatParameter.getValue());
          case BOOLEAN -> parameterValue = String.valueOf(parameter.booleanParameter.isOn());
          default -> {
            addWarning("Cannot load non-numeric parameter $" + parameterName + " into an integer type: " + key);
            return null;
          }
          }
        }
        case STRING, STRING_SELECT -> {
          parameterValue = parameter.getValueAsString();
        }
        case BOOLEAN -> {
          if (parameter.type == ParameterType.BOOLEAN) {
            parameterValue = String.valueOf(parameter.booleanParameter.isOn());
          } else {
            addWarning("Cannot load non-boolean parameter $" + parameterName + " into a boolean type: " + key);
            return null;
          }
        }
        }
      }
      result.append(expression, index, matcher.start());
      result.append(parameterValue);
      index = matcher.end();
    }
    if (index < expression.length()) {
      result.append(expression, index, expression.length());
    }
    return result.toString();
  }

  private float evaluateVariableExpression(Map<String, Object> obj, String key, String expression, ParameterType type) {
    String substitutedExpression = replaceVariables(key, expression, type);
    if (substitutedExpression == null) {
      return 0;
    }
    try {
      float value = _evaluateNumericExpression(substitutedExpression.replaceAll("\\s", ""));
      if (Float.isNaN(value)) {
        addWarning("Variable expression produces NaN: " + expression);
        return 0;
      }
      if (Float.isInfinite(value)) {
        addWarning("Variable expression produces infinite value: " + expression);
        return 0;
      }
      return value;
    } catch (Exception nfx) {
      addWarning("Bad formatting in variable expression: " + expression);
      nfx.printStackTrace();
      return 0;
    }
  }

  private enum SimpleFunction {
    sin(f -> { return (float) Math.sin(Math.toRadians(f)); }),
    cos(f -> { return (float) Math.cos(Math.toRadians(f)); }),
    tan(f -> { return (float) Math.tan(Math.toRadians(f)); }),
    asin(f -> { return (float) Math.toDegrees(Math.asin(f)); }),
    acos(f -> { return (float) Math.toDegrees(Math.acos(f)); }),
    atan(f -> { return (float) Math.toDegrees(Math.atan(f)); }),
    deg(f -> { return (float) Math.toDegrees(f); }),
    rad(f -> { return (float) Math.toRadians(f); }),
    abs(f -> { return Math.abs(f); }),
    sqrt(f -> { return (float) Math.sqrt(f); }),
    floor(f -> { return (float) Math.floor(f); }),
    ceil(f -> { return (float) Math.ceil(f); }),
    round(f -> { return Math.round(f); });

    private interface Compute {
      public float compute(float f);
    }

    private final Compute compute;

    private SimpleFunction(Compute compute) {
      this.compute = compute;
    }

  }

  private static final String OPERATOR_CHARS = "^*/+-%<>=!&|";

  private static boolean isUnaryMinus(char[] chars, int index) {
    // Check it's actually a minus
    if (chars[index] != '-') {
      return false;
    }

    // If at the very front of the thing, it's unary!
    if (index == 0) {
      return true;
    }

    // Check if preceded by another simple operator, e.g. 4+-4
    if (OPERATOR_CHARS.indexOf(chars[index-1]) >= 0) {
      return true;
    }
    // Check if preceded by a simple function token, which will no longer have
    // parentheses, e.g. sin(-4) will have become sin-4 after parenthetical resolution
    for (SimpleFunction function : SimpleFunction.values()) {
      final String name = function.name();
      final int len = name.length();
      if ((index >= len) && new String(chars, index-len, len).equals(name)) {
        return true;
      }
    }
    return false;
  }

  // Super-trivial hacked up implementation of *very* basic math expressions, which has now
  // got some functions tacked on. If this slippery slope keeps sliding will need to get a
  // real expression parsing + evaluation library involved at some point...
  static float _evaluateNumericExpression(String expression) {
    if (_evaluateExpression(expression) instanceof ExpressionResult.Numeric numeric) {
      return numeric.number;
    }
    throw new IllegalArgumentException("Expected expression to be numeric: " + expression);
  }

  private boolean evaluateBooleanExpression(Map<String, Object> obj, String key, String expression) {
    String substitutedExpression = replaceVariables(key, expression, ParameterType.BOOLEAN);
    if (substitutedExpression == null) {
      return false;
    }
    try {
      return _evaluateBooleanExpression(substitutedExpression.replaceAll("\\s", ""));
    } catch (Exception x) {
      addWarning("Bad formatting in boolean expression: " + expression);
      x.printStackTrace();
      return false;
    }
  }

  static boolean _evaluateBooleanExpression(String expression) {
    if (_evaluateExpression(expression) instanceof ExpressionResult.Boolean bool) {
      return bool.bool;
    }
    throw new IllegalArgumentException("Expected expression to be boolean: " + expression);
  }

  static abstract class ExpressionResult {

    private static class Numeric extends ExpressionResult {
      private final float number;
      private Numeric(float number) {
        this.number = number;
      }

      @Override
      public String toString() {
        return String.valueOf(this.number);
      }
    }

    private static class Boolean extends ExpressionResult {

      private static final Boolean TRUE = new Boolean(true);
      private static final Boolean FALSE = new Boolean(false);

      private final boolean bool;
      private Boolean(boolean bool) {
        this.bool = bool;
      }

      @Override
      public String toString() {
        return String.valueOf(this.bool);
      }
    }
  }

  private static final String[][] EXPRESSION_OPERATORS = {
    { "||", "|" }, // Both forms are logical, not bitwise
    { "&&", "&" }, // Both forms are logical, not bitwise
    { "<=", ">=", "<", ">" },
    { "==", "!=" },
    { "+", "-" },
    { "*", "/", "%" },
    { "^" }
  };

  static int _getOperatorIndex(String expression, char[] chars, String operator) {
    if ("-".equals(operator)) {
      for (int index = chars.length - 1; index > 0; --index) {
        // Skip over the tricky unary minus operator! If preceded by another operator,
        // then it's actually just a negative sign which will be handled later. Do not
        // process it as a subtraction.
        if ((chars[index] == '-') && !isUnaryMinus(chars, index)) {
          return index;
        }
      }
      return -1;
    }
    return expression.lastIndexOf(operator);
  }

  /**
   * Expressions can have ambiguous types when nested with parentheses! This is getting
   * out of control and I really should have just used a proper expression parsing library
   * of some sort (-mcslee, June 2025, and yet bound to continue bolting onto this...)
   *
   * @param expression Portion of expression to evaluate
   * @return ExpressionResult, which may be boolean or numeric
   */
  static ExpressionResult _evaluateExpression(String expression) {
    char[] chars = expression.toCharArray();

    // Parentheses pass
    int openParen = -1;
    for (int i = 0; i < chars.length; ++i) {
      if (chars[i] == '(') {
        openParen = i;
      } else if (chars[i] == ')') {
        if (openParen < 0) {
          throw new IllegalArgumentException("Mismatched parentheses in expression: " + expression);
        }

        // Whenever we find a closed paren, evaluate just this one parenthetical.
        // This will naturally work from in->out on nesting, since every closed-paren
        // catches the open-paren that was closest to it.
        ExpressionResult result = _evaluateExpression(expression.substring(openParen+1, i));
        if ((openParen == 0) && (i == chars.length-1)) {
          // Whole thing in parentheses? Just return!
          return result;
        }

        // Evaluate expression recursively with this parenthetical removed
        return _evaluateExpression(
          expression.substring(0, openParen) +
          result.toString() +
          expression.substring(i + 1)
        );
      }
    }

    // Ternary conditional, lowest precedence, right->left associative
    final int condition = expression.indexOf('?');
    if (condition > 0) {
      final int end = expression.lastIndexOf(':');
      if (end <= condition) {
        throw new IllegalArgumentException("Mismatched ternary conditional ?: in expression: " + expression);
      }
      return _evaluateBooleanExpression(expression.substring(0, condition)) ?
        _evaluateExpression(expression.substring(condition+1, end)) :
        _evaluateExpression(expression.substring(end+1));
    }

    // Left->right associative operators, working up the precedence ladder
    for (String[] operators : EXPRESSION_OPERATORS) {
      int lastIndex = -1;
      String operator = null;
      for (String candidate : operators) {
        int candidateIndex = _getOperatorIndex(expression, chars, candidate);
        if (candidateIndex > lastIndex) {
          operator = candidate;
          lastIndex = candidateIndex;
        }
      }
      if (operator != null) {
        String left = expression.substring(0, lastIndex);
        String right = expression.substring(lastIndex + operator.length());
        return switch (operator) {
          case "&&", "&" -> new ExpressionResult.Boolean(
            _evaluateBooleanExpression(left) &&
            _evaluateBooleanExpression(right)
          );
          case "||", "|" -> new ExpressionResult.Boolean(
            _evaluateBooleanExpression(left) ||
            _evaluateBooleanExpression(right)
          );
          case "<=" -> new ExpressionResult.Boolean(
            _evaluateNumericExpression(left) <=
            _evaluateNumericExpression(right)
          );
          case "<" -> new ExpressionResult.Boolean(
            _evaluateNumericExpression(left) <
            _evaluateNumericExpression(right)
          );
          case ">=" -> new ExpressionResult.Boolean(
            _evaluateNumericExpression(left) >=
            _evaluateNumericExpression(right)
          );
          case ">" -> new ExpressionResult.Boolean(
            _evaluateNumericExpression(left) >
            _evaluateNumericExpression(right)
          );
          case "==" -> new ExpressionResult.Boolean(
            _evaluateNumericExpression(left) ==
            _evaluateNumericExpression(right)
          );
          case "!=" -> new ExpressionResult.Boolean(
            _evaluateNumericExpression(left) !=
            _evaluateNumericExpression(right)
          );
          case "+" -> new ExpressionResult.Numeric(
            _evaluateNumericExpression(left) +
            _evaluateNumericExpression(right)
          );
          case "-" -> new ExpressionResult.Numeric(
            _evaluateNumericExpression(left) -
            _evaluateNumericExpression(right)
          );
          case "*" -> new ExpressionResult.Numeric(
            _evaluateNumericExpression(left) *
            _evaluateNumericExpression(right)
          );
          case "/" -> new ExpressionResult.Numeric(
            _evaluateNumericExpression(left) /
            _evaluateNumericExpression(right)
          );
          case "%" -> new ExpressionResult.Numeric(
            _evaluateNumericExpression(left) %
            _evaluateNumericExpression(right)
          );
          case "^" -> new ExpressionResult.Numeric((float) Math.pow(
            _evaluateNumericExpression(left),
            _evaluateNumericExpression(right)
          ));

          default -> throw new IllegalStateException("Unrecognized operator: " + operator);
        };
      }
    }

    // Dreaded nasty unary operators!
    String trimmed = expression.trim();
    if (!trimmed.isEmpty()) {
      final char unary = trimmed.charAt(0);
      if (unary == '-') {
        // Float.parseFloat() would handle one of these fine, but it won't handle
        // them potentially stacking up at the front, e.g. if multiple expression
        // resolutions have resulted in something like ---4, so do the negations
        // manually one by one
        return new ExpressionResult.Numeric(-_evaluateNumericExpression(expression.substring(1)));
      } else if (unary == '!') {
        return new ExpressionResult.Boolean(!_evaluateBooleanExpression(expression.substring(1)));
      }

      // Check for simple function operators
      for (SimpleFunction function : SimpleFunction.values()) {
        final String name = function.name();
        if (trimmed.startsWith(name)) {
          float argument = _evaluateNumericExpression(expression.substring(name.length()));
          return new ExpressionResult.Numeric(function.compute.compute(argument));
        }
      }
    }

    // Sort out what we got here
    return switch (trimmed.toLowerCase()) {
      case "" -> throw new IllegalArgumentException("Cannot evaluate empty expression: " + expression);
      case "true" -> ExpressionResult.Boolean.TRUE;
      case "false" -> ExpressionResult.Boolean.FALSE;
      default -> new ExpressionResult.Numeric(Float.parseFloat(trimmed));
    };
  }


  private float loadFloat(Map<String, Object> obj, String key, boolean variablesAllowed) {
    return loadFloat(obj, key, variablesAllowed, key + " should be primitive float value");
  }

  private float loadFloat(Map<String, Object> obj, String key, boolean variablesAllowed, String warning) {
    if (obj.containsKey(key)) {
      Object floatElem = obj.get(key);
      if (floatElem instanceof Float) {
        if (variablesAllowed && floatElem instanceof String) {
          return evaluateVariableExpression(obj, key, (String) floatElem, ParameterType.FLOAT);
        }
        return (Float) floatElem;
      }
      addWarning(warning);
    }
    return 0f;
  }

  private boolean loadBoolean(Map<String, Object> obj, String key, boolean variablesAllowed, String warning) {
    if (obj.containsKey(key)) {
      Object boolElem = obj.get(key);
      if (boolElem instanceof Boolean) {
        if (variablesAllowed && boolElem instanceof String) {
          return evaluateBooleanExpression(obj, key, (String) boolElem);
        }
        return (Boolean) boolElem;
      }
      addWarning(warning);
    }
    return false;
  }

  private int loadInt(Map<String, Object> obj, String key, boolean variablesAllowed, String warning) {
    if (obj.containsKey(key)) {
      Object intElem = obj.get(key);
      if (intElem instanceof Integer) {
        if (variablesAllowed && intElem instanceof String) {
          return (int) evaluateVariableExpression(obj, key, (String) intElem, ParameterType.INT);
        }
        return (Integer) intElem;
      }
      addWarning(warning);
    }
    return 0;
  }

  private int loadColor(Map<String, Object> obj, String key) {
    Object colorElem = obj.get(key);
    if (colorElem instanceof String && ((String) colorElem).toLowerCase().startsWith("0x")) {
      return Integer.parseUnsignedInt((String) colorElem, 16);
    } else {
      return (Integer) colorElem;
    }
  }

  private LXVector loadVector(Map<String, Object> obj, String warning) {
    if (!obj.containsKey(KEY_X) && !obj.containsKey(KEY_Y) && !obj.containsKey(KEY_Z)) {
      addWarning(warning);
    }
    return new LXVector(
      loadFloat(obj, KEY_X, true),
      loadFloat(obj, KEY_Y, true),
      loadFloat(obj, KEY_Z, true)
    );
  }

  private String loadString(Map<String, Object> obj, String key, boolean variablesAllowed, String warning) {
    if (obj.containsKey(key)) {
      Object stringElem = obj.get(key);
      if (stringElem instanceof String) {
        if (variablesAllowed) {
          return replaceVariables(key, (String) stringElem, ParameterType.STRING);
        } else {
          return (String) stringElem;
        }
      }
      addWarning(warning);
    }
    return null;
  }

  private List<Object> loadArray(Map<String, Object> obj, String key) {
    return loadArray(obj, key, key + " must be a JSON array");
  }

  private List<Object> loadArray(Map<String, Object> obj, String key, String warning) {
    if (obj.containsKey(key)) {
      Object arrayElem = obj.get(key);
      if (arrayElem instanceof List) {
        return (List<Object>) arrayElem;
      }
      addWarning(warning);
    }
    return null;
  }

  private Map<String, Object> loadObject(Map<String, Object> obj, String key, String warning) {
    if (obj.containsKey(key)) {
      Object objElem = obj.get(key);
      if (objElem instanceof Map) {
        return (Map<String, Object>) objElem;
      }
      addWarning(warning);
    }
    return null;
  }

  private void loadGeometry(LXFixture fixture, Map<String, Object> obj) {
    loadTransforms(fixture, obj);
    if (obj.containsKey(KEY_X)) {
      fixture.x.setValue(loadFloat(obj, KEY_X, true));
    }
    if (obj.containsKey(KEY_Y)) {
      fixture.y.setValue(loadFloat(obj, KEY_Y, true));
    }
    if (obj.containsKey(KEY_Z)) {
      fixture.z.setValue(loadFloat(obj, KEY_Z, true));
    }
    if (obj.containsKey(KEY_YAW)) {
      fixture.yaw.setValue(loadFloat(obj, KEY_YAW, true));
    }
    if (obj.containsKey(KEY_PITCH)) {
      fixture.pitch.setValue(loadFloat(obj, KEY_PITCH, true));
    }
    if (obj.containsKey(KEY_ROLL)) {
      fixture.roll.setValue(loadFloat(obj, KEY_ROLL, true));
    }
    if (obj.containsKey(KEY_SCALE)) {
      fixture.scale.setValue(loadFloat(obj, KEY_SCALE, true));
    }
    if (obj.containsKey(KEY_POINT_SIZE)) {
      fixture.hasCustomPointSize.setValue(true);
      fixture.pointSize.setValue(loadFloat(obj, KEY_POINT_SIZE, true));
    }
  }

  private void loadTransforms(LXFixture fixture, Map<String, Object> obj) {
    final List<Object> transformsArr = loadArray(obj, KEY_TRANSFORMS, KEY_TRANSFORMS + " must be an array");
    if (transformsArr == null) {
      return;
    }
    for (Object transformElem : transformsArr) {
      if (transformElem instanceof Map) {
        loadTransform(fixture, (Map<String, Object>) transformElem);
      } else if (transformElem != null) {
        addWarning(KEY_TRANSFORMS + " should only contain transform elements in JSON object format, found invalid: " + transformElem);
      }
    }
  }

  private static final String[] TRANSFORM_TRANSLATE = { KEY_X, KEY_Y, KEY_Z };
  private static final String[] TRANSFORM_ROTATE = { KEY_YAW, KEY_PITCH, KEY_ROLL, KEY_ROTATE_X, KEY_ROTATE_Y, KEY_ROTATE_Z };
  private static final String[] TRANSFORM_SCALE = { KEY_SCALE, KEY_SCALE_X, KEY_SCALE_Y, KEY_SCALE_Z };

  private static final boolean isTransform(Map<String, Object> obj, String[] keys) {
    for (String key : keys) {
      if (obj.containsKey(key)) {
        return true;
      }
    }
    return false;
  }

  private void loadTransform(LXFixture fixture, Map<String, Object> obj) {
    if (obj.containsKey(KEY_ENABLED)) {
      boolean enabled = loadBoolean(obj, KEY_ENABLED, true, "Transform must specify boolean expression for " + KEY_ENABLED);
      if (!enabled) {
        return;
      }
    }

    // Check there are not multiple rotations specified
    int rotateCount = 0;
    for (String key : TRANSFORM_ROTATE) {
      if (obj.containsKey(key)) {
        ++rotateCount;
      }
    }
    if (rotateCount > 1) {
      addWarning("Transform may not contain multiple rotations: " + obj);
      return;
    }

    final boolean isTranslate = isTransform(obj, TRANSFORM_TRANSLATE);
    final boolean isScale = isTransform(obj, TRANSFORM_SCALE);
    final boolean isRotate = rotateCount > 0;

    if (isTranslate) {
      if (isRotate) {
        addWarning("Transform may not contain both translation and rotation: " + obj);
        return;
      }
      if (isScale) {
        addWarning("Transform may not contain both translation and scaling: " + obj);
        return;
      }
    } else if (isRotate) {
      if (isScale) {
        addWarning("Transform may not contain both rotation and scaling: " + obj);
        return;
      }
    }

    // All clear at this point, read the values
    if (obj.containsKey(KEY_X)) {
      fixture.addTransform(new Transform(Transform.Type.TRANSLATE_X, loadFloat(obj, KEY_X, true)));
    }
    if (obj.containsKey(KEY_Y)) {
      fixture.addTransform(new Transform(Transform.Type.TRANSLATE_Y, loadFloat(obj, KEY_Y, true)));
    }
    if (obj.containsKey(KEY_Z)) {
      fixture.addTransform(new Transform(Transform.Type.TRANSLATE_Z, loadFloat(obj, KEY_Z, true)));
    }

    if (obj.containsKey(KEY_YAW)) {
      fixture.addTransform(new Transform(Transform.Type.ROTATE_Y, loadFloat(obj, KEY_YAW, true)));
    }
    if (obj.containsKey(KEY_PITCH)) {
      fixture.addTransform(new Transform(Transform.Type.ROTATE_X, loadFloat(obj, KEY_PITCH, true)));
    }
    if (obj.containsKey(KEY_ROLL)) {
      fixture.addTransform(new Transform(Transform.Type.ROTATE_Z, loadFloat(obj, KEY_ROLL, true)));
    }
    if (obj.containsKey(KEY_ROTATE_X)) {
      fixture.addTransform(new Transform(Transform.Type.ROTATE_X, loadFloat(obj, KEY_ROTATE_X, true)));
    }
    if (obj.containsKey(KEY_ROTATE_Y)) {
      fixture.addTransform(new Transform(Transform.Type.ROTATE_Y, loadFloat(obj, KEY_ROTATE_Y, true)));
    }
    if (obj.containsKey(KEY_ROTATE_Z)) {
      fixture.addTransform(new Transform(Transform.Type.ROTATE_Z, loadFloat(obj, KEY_ROTATE_Z, true)));
    }

    if (obj.containsKey(KEY_SCALE)) {
      Object scaleElem = obj.get(KEY_SCALE);
      if (scaleElem instanceof Map) {
        Map<String, Object> scale = (Map<String, Object>) scaleElem;
        if (scale.containsKey(KEY_X)) {
          fixture.addTransform(new Transform(Transform.Type.SCALE_X, loadFloat(scale, KEY_X, true)));
        }
        if (scale.containsKey(KEY_Y)) {
          fixture.addTransform(new Transform(Transform.Type.SCALE_Y, loadFloat(scale, KEY_Y, true)));
        }
        if (scale.containsKey(KEY_Z)) {
          fixture.addTransform(new Transform(Transform.Type.SCALE_Z, loadFloat(scale, KEY_Z, true)));
        }
      } else if (scaleElem instanceof Float || scaleElem instanceof String) {
        final float scale = loadFloat(obj, KEY_SCALE, true);
        fixture.addTransform(new Transform(Transform.Type.SCALE, scale));
      } else {
        addWarning("Transform element " + KEY_SCALE + " must be a float or JSON object: " + scaleElem);
        return;
      }
    }
    if (obj.containsKey(KEY_SCALE_X)) {
      fixture.addTransform(new Transform(Transform.Type.SCALE_X, loadFloat(obj, KEY_SCALE_X, true)));
    }
    if (obj.containsKey(KEY_SCALE_Y)) {
      fixture.addTransform(new Transform(Transform.Type.SCALE_Y, loadFloat(obj, KEY_SCALE_Y, true)));
    }
    if (obj.containsKey(KEY_SCALE_Z)) {
      fixture.addTransform(new Transform(Transform.Type.SCALE_Z, loadFloat(obj, KEY_SCALE_Z, true)));
    }
  }

  private void loadLabel(Map<String, Object> obj) {
    // Don't reload this if the user has renamed it
    if (!this.label.getString().equals(LABEL_PLACEHOLDER)) {
      return;
    }
    String validLabel = this.fixtureType.getString();
    String testLabel = loadString(obj, KEY_LABEL, false, KEY_LABEL + " should contain a string");
    if (testLabel != null) {
      testLabel = testLabel.trim();
      if (testLabel.isEmpty()) {
        addWarning(KEY_LABEL + " should contain a non-empty string");
      } else {
        validLabel = testLabel;
      }
    }
    this.label.setValue(validLabel);
  }

  private void setErrorLabel(String fixtureType) {
    if (this.label.getString().equals(LABEL_PLACEHOLDER)) {
      int lastSeparator = fixtureType.lastIndexOf(PATH_SEPARATOR);
      if (lastSeparator >= 0) {
        fixtureType = fixtureType.substring(lastSeparator+1);
      }
      this.label.setValue(fixtureType);
    }
  }

  private void loadTags(LXFixture fixture, Map<String, Object> obj, boolean includeParent, boolean replaceVariables) {
    List<String> validTags = _loadTags(obj, replaceVariables, this);
    if (includeParent) {
      for (String tag : _loadTags(this.jsonParameterValues, true, this.jsonParameterContext)) {
        if (validTags.contains(tag)) {
          addWarning("Parent JSON fixture redundantly specifies tag: " + tag);
        } else {
          validTags.add(tag);
        }
      }
    }
    fixture.setTags(validTags);
  }

  private List<String> _loadTags(Map<String, Object> obj, boolean replaceVariables, JsonFixture variableContext) {
    warnDuplicateKeys(obj, KEY_MODEL_KEY, KEY_MODEL_KEYS, KEY_TAG, KEY_TAGS);
    String keyTags = obj.containsKey(KEY_TAGS) ? KEY_TAGS : KEY_MODEL_KEYS;
    String keyTag = obj.containsKey(KEY_TAG) ? KEY_TAG : KEY_MODEL_KEY;
    List<String> validTags = new ArrayList<String>();

    if (obj.containsKey(KEY_MODEL_KEY) || obj.containsKey(KEY_MODEL_KEYS)) {
      addWarning(KEY_MODEL_KEY + "/" + KEY_MODEL_KEYS + " are deprecated, please update to " + KEY_TAG + "/" + KEY_TAGS);
    }

    if (obj.containsKey(keyTags)) {
      List<Object> tagsArr = loadArray(obj, keyTags);
      for (Object tagElem : tagsArr) {
        if (!(tagElem instanceof String)) {
          addWarning(keyTags + " may only contain strings");
        } else {
          String tag = ((String) tagElem).trim();
          if (replaceVariables) {
            tag = variableContext.replaceVariables(keyTags, tag, ParameterType.STRING);
          }
          if (tag == null || tag.isEmpty()) {
            addWarning(keyTags + " should not contain empty string values");
          } else if (!LXModel.Tag.isValid(tag)) {
            addWarning("Ignoring invalid tag, should only contain [A-Za-z0-9_.-]: " + tag);
          } else {
            validTags.add(tag);
          }
        }
      }
    } else if (obj.containsKey(keyTag)) {
      String tag = loadString(obj, keyTag, false, keyTag + " should contain a single string value");
      if (tag != null) {
        tag = tag.trim();
        if (replaceVariables) {
          tag = variableContext.replaceVariables(keyTag, tag, ParameterType.STRING);
        }
        if (tag == null || tag.isEmpty()) {
          addWarning(keyTag + " must contain a non-empty string value");
        } else if (!LXModel.Tag.isValid(tag)) {
          addWarning("Ignoring invalid tag, should only contain [A-Za-z0-9_.-]: " + tag);
        } else {
          validTags.add(tag);
        }
      }
    }

    return validTags;
  }

  private void loadParameters(Map<String, Object> obj) {
    Map<String, Object> parametersObj = loadObject(obj, KEY_PARAMETERS, KEY_PARAMETERS + " must be a JSON object");
    if (parametersObj == null) {
      return;
    }
    for (String parameterName : parametersObj.keySet()) {
      if (!parameterName.matches("^[a-zA-Z0-9]+$")) {
        addWarning("Invalid parameter name, must be non-empty only containing ASCII alphanumerics: " + parameterName);
        continue;
      }
      if (parameterName.equals(KEY_INSTANCES) || parameterName.equals(KEY_INSTANCE)) {
        addWarning("Invalid parameter name, keyword is reserved: " + parameterName);
        continue;
      }
      String parameterLabel = parameterName;
      if (this.definedParameters.containsKey(parameterName)) {
        addWarning("Parameter cannot be defined twice: " + parameterName);
        continue;
      }
      Object parameterElem = parametersObj.get(parameterName);
      if (!(parameterElem instanceof Map)) {
        addWarning("Definition for parameter " + parameterName + " must be a JSON object specifying " + KEY_PARAMETER_TYPE + " and " + KEY_PARAMETER_DEFAULT);
        continue;
      }
      Map<String, Object> parameterObj = (Map<String, Object>) parameterElem;
      if (parameterObj.containsKey(KEY_PARAMETER_LABEL)) {
        String rawLabel = loadString(parameterObj, KEY_PARAMETER_LABEL, false, "Parameter " + KEY_PARAMETER_LABEL + " must be valid String");
        if (!rawLabel.matches("^[a-zA-Z0-9 _-]+$")) {
          addWarning("Invalid parameter label, must be non-empty only containing ASCII alphanumerics: " + rawLabel);
        } else {
          parameterLabel = rawLabel;
        }
      }

      String parameterDescription = loadString(parameterObj, KEY_PARAMETER_DESCRIPTION, false, "Parameter " + KEY_PARAMETER_DESCRIPTION + " must be strign value.");

      // Ensure default value is present
      if (!parameterObj.containsKey(KEY_PARAMETER_DEFAULT)) {
        addWarning("Parameter " + parameterName + " must specify " + KEY_PARAMETER_DEFAULT);
        continue;
      }
      Object defaultElem = parameterObj.get(KEY_PARAMETER_DEFAULT);
      if (!(defaultElem instanceof Float || defaultElem instanceof Integer || defaultElem instanceof Boolean || defaultElem instanceof String)) {
        addWarning("Parameter " + parameterName + " must specify primitive value for " + KEY_PARAMETER_DEFAULT);
        continue;
      }

      String typeStr = loadString(parameterObj, KEY_PARAMETER_TYPE, false, "Parameter " + parameterName + " must specify valid type string");;
      ParameterType type = ParameterType.get(typeStr);
      if (type == null) {
        addWarning("Parameter " + parameterName + " must specify valid type string");
        continue;
      }
      if (type == ParameterType.STRING) {
        // Check for the string select type when options are present
        if (parameterObj.containsKey(KEY_PARAMETER_OPTIONS)) {
          type = ParameterType.STRING_SELECT;
        }
      }

      ParameterDefinition reloadDefinition = this.reloadParameterValues.get(parameterName);
      if ((reloadDefinition != null) && (reloadDefinition.type != type)) {
        reloadDefinition = null;
      }

      switch (type) {
      case FLOAT:
        final float defaultFloat = defaultElem instanceof Float ? (Float) defaultElem : Float.parseFloat(defaultElem.toString());
        float floatValue = defaultFloat;
        if (this.jsonParameterValues.containsKey(parameterName)) {
          floatValue = this.jsonParameterContext.loadFloat(this.jsonParameterValues, parameterName, true);
        } else if (reloadDefinition != null) {
          floatValue = reloadDefinition.floatParameter.getValuef();
        }
        float minFloat = -Float.MAX_VALUE;
        float maxFloat = Float.MAX_VALUE;
        if (parameterObj.containsKey(KEY_PARAMETER_MIN)) {
          minFloat = loadFloat (parameterObj, KEY_PARAMETER_MIN, false, "Parameter min value must be a float");
        }
        if (parameterObj.containsKey(KEY_PARAMETER_MAX)) {
          maxFloat = loadFloat(parameterObj, KEY_PARAMETER_MAX, false, "Parameter min value must be a float");
        }
        if (minFloat > maxFloat) {
          addWarning("Parameter minimum may not be greater than maximum: " + minFloat + ">" + maxFloat);
          break;
        }
        addJsonParameter(new ParameterDefinition(parameterName, parameterLabel, parameterDescription, floatValue, defaultFloat, minFloat, maxFloat));
        break;
      case INT:
        int minInt = 0;
        int maxInt = 1 << 16;
        if (parameterObj.containsKey(KEY_PARAMETER_MIN)) {
          minInt = loadInt(parameterObj, KEY_PARAMETER_MIN, false, "Parameter min value must be an integer");
        }
        if (parameterObj.containsKey(KEY_PARAMETER_MAX)) {
          maxInt = loadInt(parameterObj, KEY_PARAMETER_MAX, false, "Parameter min value must be an integer");
        }
        final int defaultInt = (Integer) defaultElem;
        int intValue = defaultInt;
        if (minInt > maxInt) {
          addWarning("Parameter minimum may not be greater than maximum: " + minInt + ">" + maxInt);
          break;
        }
        if (this.jsonParameterValues.containsKey(parameterName)) {
          intValue = this.jsonParameterContext.loadInt(this.jsonParameterValues, parameterName, true, "Child parameter should be an int: " + parameterName);
        } else if (reloadDefinition != null) {
          intValue = LXUtils.constrain(reloadDefinition.intParameter.getValuei(), minInt, maxInt);
        }
        addJsonParameter(new ParameterDefinition(parameterName, parameterLabel, parameterDescription, intValue, defaultInt, minInt, maxInt));
        break;
      case STRING:
        final String defaultString = defaultElem instanceof String ? (String) defaultElem : defaultElem.toString();
        String stringValue = defaultString;
        if (this.jsonParameterValues.containsKey(parameterName)) {
          stringValue = this.jsonParameterContext.loadString(this.jsonParameterValues, parameterName, true, "Child parameter should be an string: " + parameterName);
        } else if (reloadDefinition != null) {
          stringValue = reloadDefinition.stringParameter.getString();
        }
        addJsonParameter(new ParameterDefinition(parameterName, parameterLabel, parameterDescription, stringValue, defaultString));
        break;
      case STRING_SELECT:
        final String defaultStringSelect = defaultElem instanceof String ? (String) defaultElem : defaultElem.toString();
        String stringSelectValue = defaultStringSelect;
        if (this.jsonParameterValues.containsKey(parameterName)) {
          stringSelectValue = this.jsonParameterContext.loadString(this.jsonParameterValues, parameterName, true, "Child parameter should be an string: " + parameterName);
        } else if (reloadDefinition != null) {
          stringSelectValue = reloadDefinition.stringSelectParameter.getObject();
        }
        List<String> stringOptions = new ArrayList<String>();
        List<Object> optionsArray = loadArray(parameterObj, KEY_PARAMETER_OPTIONS);
        for (Object optionElem : optionsArray) {
          if (optionElem instanceof String) {
            stringOptions.add((String) optionElem);
          } else {
            addWarning(KEY_PARAMETER_OPTIONS + " should only string options");
          }
        }
        if (stringOptions.isEmpty()) {
          addWarning(KEY_PARAMETER_OPTIONS + " must not be empty");
          break;
        } else if (!stringOptions.contains(stringSelectValue)) {
          addWarning(KEY_PARAMETER_OPTIONS + " must contain default value " + stringSelectValue);
          stringValue = stringOptions.get(0);
        }
        addJsonParameter(new ParameterDefinition(parameterName, parameterLabel, parameterDescription, stringSelectValue, defaultStringSelect, stringOptions));
        break;
      case BOOLEAN:
        final boolean defaultBoolean = defaultElem instanceof Boolean ? (Boolean) defaultElem : Boolean.parseBoolean(defaultElem.toString());
        boolean booleanValue = defaultBoolean;
        if (this.jsonParameterValues.containsKey(parameterName)) {
          booleanValue = this.jsonParameterContext.loadBoolean(this.jsonParameterValues, parameterName, true, "Child parameter should be a boolean: " + parameterName);
        } else if (reloadDefinition != null) {
          booleanValue = reloadDefinition.booleanParameter.isOn();
        }
        addJsonParameter(new ParameterDefinition(parameterName, parameterLabel, parameterDescription, booleanValue, defaultBoolean));
        break;
      }

    }
  }

  @Deprecated
  private void loadLegacyPoints(Map<String, Object> obj) {
    List<Object> pointsArr = loadArray(obj, KEY_POINTS);
    if (pointsArr == null) {
      return;
    }
    addWarning(KEY_POINTS + " is deprecated. Define an element of type " + TYPE_POINTS + " in the " + KEY_COMPONENTS + " array");
    for (Object pointElem : pointsArr) {
      if (pointElem instanceof Map) {
        loadChild((Map<String, Object>) pointElem, ChildType.POINT, null);
      } else if (pointElem != null) {
        addWarning(KEY_POINTS + " should only contain point elements in JSON object format, found invalid: " + pointElem);
      }
    }
  }

  @Deprecated
  private void loadLegacyStrips(Map<String, Object> obj) {
    List<Object> stripsArr = loadArray(obj, KEY_STRIPS);
    if (stripsArr == null) {
      return;
    }
    addWarning(KEY_STRIPS + " is deprecated. Define elements of type " + TYPE_STRIP +" in the " + KEY_COMPONENTS + " array");
    for (Object stripElem : stripsArr) {
      if (stripElem instanceof Map) {
        loadChild((Map<String, Object>) stripElem, ChildType.STRIP, null);
      } else if (stripElem != null) {
        addWarning(KEY_STRIPS + " should only contain strip elements in JSON object format, found invalid: " + stripElem);
      }
    }
  }

  @Deprecated
  private void loadLegacyArcs(Map<String, Object> obj) {
    List<Object> arcsArr = loadArray(obj, KEY_ARCS);
    if (arcsArr == null) {
      return;
    }
    addWarning(KEY_ARCS + " is deprecated. Define elements of type " + TYPE_ARC + " in the " + KEY_COMPONENTS + " array");
    for (Object arcElem : arcsArr) {
      if (arcElem instanceof Map) {
        loadChild((Map<String, Object>) arcElem, ChildType.ARC, null);
      } else if (arcElem != null) {
        addWarning(KEY_ARCS + " should only contain arc elements in JSON object format, found invalid: " + arcElem);
      }
    }
  }

  @Deprecated
  private void loadLegacyChildren(Map<String, Object> obj) {
    List<Object> childrenArr = loadArray(obj, KEY_CHILDREN);
    if (childrenArr == null) {
      return;
    }
    addWarning(KEY_CHILDREN + " is deprecated. Define elements of specific type in the " + KEY_COMPONENTS + " array");
    for (Object childElem : childrenArr) {
      if (childElem instanceof Map) {
        loadChild((Map<String, Object>) childElem);
      } else if (childElem != null) {
        addWarning(KEY_CHILDREN + " should only contain child elements in JSON object format, found invalid: " + childElem);
      }
    }
  }

  private PointListFixture loadPoints(Map<String, Object> pointsObj) {
    List<Object> coordsArr = loadArray(pointsObj, KEY_COORDINATES);
    if (coordsArr == null) {
      addWarning("Points must specify " + KEY_COORDINATES);
      return null;
    }
    List<LXVector> coords = new ArrayList<LXVector>();
    for (Object coordElem : coordsArr) {
      if (coordElem instanceof Map) {
        coords.add(loadVector((Map<String, Object>) coordElem, "Coordinate should specify at least one x/y/z value"));
      } else if (coordElem != null) {
        addWarning(KEY_COORDINATES + " should only contain point elements in JSON object format, found invalid: " + coordElem);
      }
    }
    if (coords.isEmpty()) {
      addWarning("Points must specify non-empty array of " + KEY_COORDINATES);
      return null;
    }

    return new PointListFixture(this.lx, coords);
  }

  private StripFixture loadStrip(Map<String, Object> stripObj) {
    if (!stripObj.containsKey(KEY_NUM_POINTS)) {
      addWarning("Strip must specify " + KEY_NUM_POINTS);
      return null;
    }
    int numPoints = loadInt(stripObj, KEY_NUM_POINTS, true, "Strip must specify a positive integer for " + KEY_NUM_POINTS);
    if (numPoints <= 0) {
      addWarning("Strip must specify positive integer value for " + KEY_NUM_POINTS);
      return null;
    }
    if (numPoints > StripFixture.MAX_POINTS) {
      addWarning("Single strip may not define more than " + StripFixture.MAX_POINTS + " points, tried to define " + numPoints);
      return null;
    }

    // Make strip with number of points
    StripFixture strip = new StripFixture(this.lx);
    strip.numPoints.setValue(numPoints);

    // Strip spacing default to 1
    float spacing = 1f;

    // Load the strip direction if specified (which implies a spacing, but spacing can be overridden explicitly)
    if (stripObj.containsKey(KEY_DIRECTION)) {
      if (stripObj.containsKey(KEY_END)) {
        addWarning("Strip object should not specify both " + KEY_DIRECTION + " and " + KEY_END + ", ignoring " + KEY_DIRECTION);
      } else if (stripObj.containsKey(KEY_YAW) || stripObj.containsKey(KEY_PITCH) || stripObj.containsKey(KEY_ROLL)) {
        addWarning("Strip object should not specify both " + KEY_DIRECTION + " and yaw/pitch/roll, ignoring " + KEY_DIRECTION);
      } else {
        Map<String, Object> directionObj = loadObject(stripObj, KEY_DIRECTION, "Strip direction should be a vector object");
        if (directionObj != null) {
          LXVector direction = loadVector(directionObj, "Strip direction should specify at least one x/y/z value");
          if (direction.isZero()) {
            addWarning("Strip direction vector should not be all 0");
          } else {
            spacing = direction.mag();
            strip.yaw.setValue(Math.toDegrees(Math.atan2(-direction.z, direction.x)));
            strip.roll.setValue(Math.toDegrees(Math.asin(direction.y / spacing)));
            strip.pitch.setValue(0);
          }
        }
      }
    }

    // Explicit spacing specified?
    if (stripObj.containsKey(KEY_SPACING)) {
      if (stripObj.containsKey(KEY_END)) {
        addWarning("Strip object should not specify both " + KEY_SPACING + " and " + KEY_END + ", ignoring " + KEY_SPACING);
      } else {
        float testSpacing = loadFloat(stripObj, KEY_SPACING, true, "Strip must specify a positive " + KEY_SPACING);
        if (testSpacing >= 0) {
          spacing = testSpacing;
        } else {
          addWarning("Strip may not specify a negative spacing");
        }
      }
    }

    // End-point specified
    if (stripObj.containsKey(KEY_END)) {
      if (stripObj.containsKey(KEY_YAW) || stripObj.containsKey(KEY_PITCH) || stripObj.containsKey(KEY_ROLL)) {
        addWarning("Strip object should not specify both " + KEY_END + " and yaw/pitch/roll, ignoring " + KEY_END);
      } else {
        Map<String, Object> endObj = loadObject(stripObj, KEY_END, "Strip " + KEY_END + " should be a vector object");
        if (endObj != null) {
          LXVector direction =
            loadVector(endObj, "Strip " + KEY_END + " should specify at least one x/y/z value")
            .sub(loadVector(stripObj, null));
          if (direction.isZero()) {
            addWarning("Strip " + KEY_END + " vector cannot match strip origin");
          } else {
            float magnitude = direction.mag();
            if (numPoints > 1) {
              spacing = magnitude / (numPoints-1);
            }
            strip.yaw.setValue(Math.toDegrees(Math.atan2(-direction.z, direction.x)));
            strip.roll.setValue(Math.toDegrees(Math.asin(direction.y / magnitude)));
            strip.pitch.setValue(0);
          }
        }
      }
    }

    strip.spacing.setValue(spacing);

    return strip;
  }

  private ArcFixture loadArc(Map<String, Object> arcObj) {
    if (!arcObj.containsKey(KEY_NUM_POINTS)) {
      addWarning("Arc must specify " + KEY_NUM_POINTS + ", key was not found");
      return null;
    }
    int numPoints = loadInt(arcObj, KEY_NUM_POINTS, true, "Arc must specify a positive integer for " + KEY_NUM_POINTS);
    if (numPoints <= 0) {
      addWarning("Arc must specify positive integer value for " + KEY_NUM_POINTS);
      return null;
    }
    if (numPoints > ArcFixture.MAX_POINTS) {
      addWarning("Single arc may not define more than " + ArcFixture.MAX_POINTS + " points");
      return null;
    }

    float radius = loadFloat(arcObj, KEY_RADIUS, true, "Arc must specify radius");
    if (radius <= 0) {
      addWarning("Arc must specify positive value for " + KEY_RADIUS);
      return null;
    }

    float degrees = loadFloat(arcObj, KEY_DEGREES, true, "Arc must specify number of degrees to cover");
    if (degrees <= 0) {
      addWarning("Arc must specify positive value for " + KEY_DEGREES);
      return null;
    }

    ArcFixture arc = new ArcFixture(this.lx);
    arc.numPoints.setValue(numPoints);
    arc.radius.setValue(radius);
    arc.degrees.setValue(degrees);

    // Arc position mode
    ArcFixture.PositionMode positionMode = ArcFixture.PositionMode.ORIGIN;
    if (arcObj.containsKey(KEY_ARC_MODE)) {
      String arcMode = loadString(arcObj, KEY_ARC_MODE, true, "Arc " + KEY_ARC_MODE + " must be a string");
      if (VALUE_ARC_MODE_CENTER.equals(arcMode)) {
        positionMode = ArcFixture.PositionMode.CENTER;
      } else if (VALUE_ARC_MODE_ORIGIN.equals(arcMode)) {
        positionMode = ArcFixture.PositionMode.ORIGIN;
      } else if (arcMode != null) {
        addWarning("Arc " + KEY_ARC_MODE + " must be one of " + VALUE_ARC_MODE_CENTER + " or " + VALUE_ARC_MODE_ORIGIN + " - invalid value " + arcMode);
      }
    }
    arc.positionMode.setValue(positionMode);

    // Load the strip direction, one of two ways
    if (arcObj.containsKey(KEY_NORMAL)) {
      if (arcObj.containsKey(KEY_DIRECTION) || arcObj.containsKey(KEY_YAW) || arcObj.containsKey(KEY_PITCH)) {
        addWarning("Arc object should not specify both " + KEY_NORMAL + " and direction/yaw/pitch, only using " + KEY_NORMAL);
      }
      Map<String, Object> normalObj = loadObject(arcObj, KEY_NORMAL, "Arc normal should be a vector object");
      if (normalObj != null) {
        LXVector normal = loadVector(normalObj, "Arc normal should specify at least one x/y/z value");
        if (normal.isZero()) {
          addWarning("Arc normal vector should not be all 0");
        } else {
          arc.yaw.setValue(Math.toDegrees(Math.atan2(normal.x, normal.z))); // yaw
          arc.pitch.setValue(Math.toDegrees(Math.asin(normal.y / normal.mag()))); // pitch
          arc.roll.setValue(Math.toRadians(loadFloat(arcObj, KEY_ROLL, true)));
        }
      }
    } else if (arcObj.containsKey(KEY_DIRECTION)) {
      if (arcObj.containsKey(KEY_YAW) || arcObj.containsKey(KEY_ROLL) || arcObj.containsKey(KEY_NORMAL)) {
        addWarning("Arc object should not specify both " + KEY_DIRECTION + " and yaw/roll/normal, only using " + KEY_DIRECTION);
      }
      Map<String, Object> directionObj = loadObject(arcObj, KEY_DIRECTION, "Arc direction should be a vector object");
      if (directionObj != null) {
        LXVector direction = loadVector(directionObj, "Arc direction should specify at least one x/y/z value");
        if (direction.isZero()) {
          addWarning("Arc direction vector should not be all 0");
        } else {
          arc.yaw.setValue(Math.toDegrees(Math.atan2(-direction.z, direction.x))); // yaw
          arc.pitch.setValue(Math.toDegrees(Math.toRadians(loadFloat(arcObj, KEY_PITCH, true)))); // pitch
          arc.roll.setValue(Math.toDegrees(Math.asin(direction.y / direction.mag()))); // roll
        }
      }
    }

    return arc;
  }

  private LXFixture loadNative(Map<String, Object> nativeObj) {
    String className = loadString(nativeObj, KEY_CLASS, true, "Fixture type must specify " + KEY_CLASS);
    if (LXUtils.isEmpty(className)) {
      addWarning("Fixture type must specify " + KEY_CLASS);
      return null;
    }

    try {
      LXFixture fixture = this.lx.instantiateFixture(className);
      if (nativeObj.containsKey(KEY_PARAMETERS)) {
        Map<String, Object> paramsObj = (Map<String, Object>) nativeObj.get(KEY_PARAMETERS);
        fixture.isLoading = true;
        for (LXParameter parameter : fixture.getParameters()) {
          if (parameter instanceof AggregateParameter) {
            // Let this store/restore from the underlying parameter values
            continue;
          }
          final String path = parameter.getPath();

          // Substitute enum/name parameter paths
          if (parameter instanceof IEnumParameter<?>) {
            String enumPath = LXSerializable.Utils.getEnumNamePath(path);
            if (paramsObj.containsKey(enumPath)) {
              Object enumParam = paramsObj.get(enumPath);
              if (enumParam instanceof String) {
                paramsObj.put(enumPath, replaceVariables(enumPath, (String) enumParam, ParameterType.STRING));
              }
            }
          }

          // Substitute variable expressions in string/int/float/boolean values
          if (paramsObj.containsKey(path)) {
            Object param = paramsObj.get(path);
            if (param instanceof String) {
              String primitive = (String) param;
              if (parameter instanceof StringParameter) {
                paramsObj.put(path, replaceVariables(path, primitive, ParameterType.STRING));
              } else if (parameter instanceof BooleanParameter) {
                boolean boolVal = evaluateBooleanExpression(paramsObj, path, primitive);
                paramsObj.put(path, boolVal);
              } else if (parameter instanceof DiscreteParameter) {
                int intVal = (int) evaluateVariableExpression(paramsObj, path, primitive, ParameterType.INT);
                paramsObj.put(path, intVal);
              } else {
                int floatVal = (int) evaluateVariableExpression(paramsObj, path, primitive, ParameterType.FLOAT);
                paramsObj.put(path, floatVal);
              }
            }
            LXSerializable.Utils.loadParameter(parameter, (Map<String, Object>) paramsObj, path);
          }

        }
        fixture.isLoading = false;
      }
      return fixture;
    } catch (Exception x) {
      addWarning("Failed to load native fixture class " + className + ": " + x.getMessage());
    }
    return null;
  }

  private void loadComponents(Map<String, Object> obj) {
    List<Object> componentsArr = loadArray(obj, KEY_COMPONENTS);
    if (componentsArr == null) {
      return;
    }
    for (Object componentElem : componentsArr) {
      if (componentElem instanceof Map) {
        // Push an element onto the index array
        this.componentsByIndex.add(null);
        loadChild((Map<String, Object>) componentElem);
      } else if (componentElem != null) {
        addWarning(KEY_COMPONENTS + " should only contain child elements in JSON object format, found invalid: " + componentElem);
      }
    }
  }

  private void loadChild(Map<String, Object> childObj) {
    if (!childObj.containsKey(KEY_TYPE)) {
      addWarning("Child object must specify type");
      return;
    }
    String type = loadString(childObj, KEY_TYPE, true, "Child object must specify string type");
    if (type == null || type.isEmpty()) {
      addWarning("Child object must specify valid non-empty type: " + type);
      return;
    }

    if (childObj.containsKey(KEY_ENABLED)) {
      boolean enabled = loadBoolean(childObj, KEY_ENABLED, true, "Child object must specify boolean expression for " + KEY_ENABLED);
      if (!enabled) {
        return;
      }
    }

    if (childObj.containsKey(KEY_INSTANCES)) {

      int numInstances = loadInt(childObj, KEY_INSTANCES, true, "Child object must specify positive number of instances");
      if (numInstances <= 0) {
        addWarning("Child object specifies illegal number of instances: " + numInstances);
        return;
      }
      if (numInstances >= MAX_INSTANCES) {
        addWarning("Child object specifies too many instances: " + numInstances + " >= " + MAX_INSTANCES);
        return;
      }

      // Load this child N times with an instance variable set
      this.currentNumInstances = numInstances;
      for (int i = 0; i < numInstances; ++i) {
        this.currentChildInstance = i;
        Map<String, Object> instanceObj = LXSerializable.Utils.deepCopy(childObj);
        instanceObj.remove(KEY_INSTANCES);
        loadChild(instanceObj);
      }
      this.currentNumInstances = -1;
      this.currentChildInstance = -1;

    } else {

      if (TYPE_POINT.equals(type)) {
        loadChild(childObj, ChildType.POINT, null);
      } else if (TYPE_POINTS.equals(type)) {
        loadChild(childObj, ChildType.POINTS, null);
      } else if (TYPE_STRIP.equals(type)) {
        loadChild(childObj, ChildType.STRIP, null);
      } else if (TYPE_ARC.equals(type)) {
        loadChild(childObj, ChildType.ARC, null);
      } else if (TYPE_CLASS.equals(type)) {
        loadChild(childObj, ChildType.CLASS, null);
      } else {
        loadChild(childObj, ChildType.JSON, type);
      }
    }
  }

  private String getChildPrefix() {
    String fixtureType = this.fixtureType.getString();
    if (fixtureType == null || fixtureType.isEmpty()) {
      return null;
    }
    int pathIndex = fixtureType.lastIndexOf(PATH_SEPARATOR_CHAR);
    if (pathIndex > 0) {
      return fixtureType.substring(0, pathIndex);
    }
    return null;
  }

  private void loadChild(Map<String, Object> childObj, ChildType type, String jsonType) {
    LXFixture child = null;
    switch (type) {
    case POINT:
      child = new PointFixture(this.lx);
      break;
    case POINTS:
      child = loadPoints(childObj);
      break;
    case STRIP:
      child = loadStrip(childObj);
      break;
    case ARC:
      child = loadArc(childObj);
      break;
    case CLASS:
      child = loadNative(childObj);
      break;
    case JSON:
      if ((jsonType == null) || jsonType.isEmpty() || jsonType.equals(PATH_SEPARATOR)) {
        throw new IllegalArgumentException("May not create JsonFixture with null or empty type");
      }
      if (jsonType.charAt(0) == PATH_SEPARATOR_CHAR) {
        // If the provided fixture type is absolute, always go absolute
        jsonType = jsonType.substring(0);
      } else {
        // Otherwise, check first relative to the folder that we are currently in
        final String prefix = getChildPrefix();
        if (prefix != null) {
          String prefixedType = prefix + PATH_SEPARATOR + jsonType;
          File file = getFixtureFile(prefixedType);
          if (file.exists()) {
            jsonType = prefixedType;
          }
        }
      }
      JsonFixture jsonChild = new JsonFixture(this.lx, this, childObj, jsonType);
      child = jsonChild;
      if (jsonChild.error.isOn()) {
        setError(jsonChild.errorMessage.getString());
        return;
      }
      if (jsonChild.warning.isOn() ) {
        this.warnings.addAll(jsonChild.warnings);
        if (this.warning.isOn()) {
          this.warning.bang();
        } else {
          this.warning.setValue(true);
        }
      }
      break;
    }

    if (child != null) {
      // Do this for all child types
      loadGeometry(child, childObj);

      // Load child brightness
      loadBrightness(child, childObj);

      // Load tags for non-JSON child types
      if (type != ChildType.JSON) {
        loadTags(child, childObj, false, true);
      }

      // Load meta-data fields for the child
      loadMetaData(childObj, child.metaData);

      // Load outputs for the child
      loadOutputs(child, childObj);

      // Set child enabled
      child.enabled.setValue(this.enabled.isOn());

      // Check for an ID by which outputs can reference this
      String childId = loadString(childObj, KEY_ID, true, "Component ID must be a valid string");
      if (childId != null) {
        boolean duplicated = false;
        if (this.currentChildInstance <= 0) {
          if (this.componentsById.containsKey(childId)) {
            addWarning("Cannot duplicate component ID already in use: " + childId);
            duplicated = true;
          } else {
            // Non-instanced children are tracked here, as well as master instance 0
            List<LXFixture> children = new ArrayList<LXFixture>();
            children.add(child);
            this.componentsById.put(childId, children);
          }
        }
        // This is an instanced child, add to the list of children
        if (!duplicated && this.currentChildInstance >= 0) {
          // NB: instance 0 was added above to prime the list, only add later instances here
          if (this.currentChildInstance > 0) {
            this.componentsById.get(childId).add(child);
          }
          // Also add tracking to individual child instances
          this.componentsById.putIfAbsent(childId + "[" + this.currentChildInstance + "]", Arrays.asList(new LXFixture[] { child }));
        }
      }

      // Keep track of fixture in component list
      List<LXFixture> list = this.componentsByIndex.get(this.componentsByIndex.size() - 1);
      if (list == null) {
        list = new ArrayList<LXFixture>();
        this.componentsByIndex.set(this.componentsByIndex.size() - 1, list);
      }
      list.add(child);

      // Add child to the tree
      addChild(child, true);
    }
  }

  private void loadOutputs(LXFixture fixture, Map<String, Object> obj) {
    if (obj.containsKey(KEY_OUTPUT) && obj.containsKey(KEY_OUTPUTS)) {
      addWarning("Should not have both " + KEY_OUTPUT + " and " + KEY_OUTPUTS);
    }
    Map<String, Object> outputObj = loadObject(obj, KEY_OUTPUT, KEY_OUTPUT + " must be an output object");
    if (outputObj != null) {
      loadOutput(fixture, outputObj);
    }
    List<Object> outputsArr = loadArray(obj, KEY_OUTPUTS, KEY_OUTPUTS + " must be an array of outputs");
    if (outputsArr != null) {
      for (Object outputElem : outputsArr) {
        if (outputElem instanceof Map) {
          loadOutput(fixture, (Map<String, Object>) outputElem);
        } else if (outputElem != null) {
          addWarning(KEY_OUTPUTS + " should only contain output elements in JSON object format, found invalid: " + outputElem);
        }
      }
    }
  }

  private void loadOutput(LXFixture fixture, Map<String, Object> outputObj) {
    if (outputObj.containsKey(KEY_ENABLED)) {
      boolean enabled = loadBoolean(outputObj, KEY_ENABLED, true, "Output field '" + KEY_ENABLED + "' must be a valid boolean expression");
      if (!enabled) {
        return;
      }
    }

    float fps = OutputDefinition.FPS_UNSPECIFIED;
    if (outputObj.containsKey(KEY_FPS)) {
      fps = loadFloat(outputObj, KEY_FPS, true, "Output should specify valid FPS limit");
      if (fps < 0 || fps > LXOutput.MAX_FRAMES_PER_SECOND) {
        addWarning("Output FPS must be between 0-" + LXOutput.MAX_FRAMES_PER_SECOND);
        fps = OutputDefinition.FPS_UNSPECIFIED;
      }
    }

    final String protocolStr = loadString(outputObj, KEY_PROTOCOL, true, "Output must specify a valid " + KEY_PROTOCOL);
    JsonProtocolDefinition protocol = JsonProtocolDefinition.get(protocolStr);
    if (protocol == null) {
      addWarning("Output definition must define a valid protocol, not recognized: " + protocolStr);
      return;
    }

    JsonTransportDefinition transport = JsonTransportDefinition.UDP;
    if (outputObj.containsKey(KEY_TRANSPORT)) {
      if (protocol == JsonProtocolDefinition.OPC) {
        transport = JsonTransportDefinition.get(loadString(outputObj, KEY_TRANSPORT, true, "Output must specify valid transport"));
        if (transport == null) {
          transport = JsonTransportDefinition.UDP;
          addWarning("Output should define a valid transport");
        }
      } else {
        addWarning("Output transport may only be defined for OPC protocol, not " + protocol);
      }
    }

    String host = loadString(outputObj, KEY_HOST, true, "Output must specify a valid host");
    if ((host == null) || host.isEmpty()) {
      addWarning("Output must define a valid, non-empty " + KEY_HOST);
      return;
    }
    InetAddress address;
    try {
      address = InetAddress.getByName(host);
    } catch (UnknownHostException uhx) {
      addWarning("Cannot send output to invalid host: " + host);
      return;
    }

    int port = JsonOutputDefinition.DEFAULT_PORT;
    if (outputObj.containsKey(KEY_PORT)) {
      port = loadInt(outputObj, KEY_PORT, true, "Output must specify a valid host");
      if (port <= 0) {
        addWarning("Output port number must be positive: " + port);
        return;
      }
    } else if (protocol.requiresExplicitPort()) {
      addWarning("Protcol " + protocol + " requires an expicit port number to be specified");
      return;
    }

    String universeKey = protocol.universeKey;
    int universe = loadInt(outputObj, universeKey, true, "Output " + universeKey + " must be a valid integer");
    if (universe < 0) {
      addWarning("Output " + universeKey + " may not be negative");
      return;
    }

    String channelKey = protocol.channelKey;
    int channel = loadInt(outputObj, channelKey, true, "Output " + channelKey + " must be a valid integer");
    if (channel < 0) {
      addWarning("Output " + channelKey + " may not be negative");
      return;
    } else if (channel >= protocol.protocol.maxChannels) {
      addWarning("Output " + channelKey + " may not be greater than " + protocol.protocol + " limit " + channel + " > " + protocol.protocol.maxChannels);
      return;
    }

    int priority = StreamingACNDatagram.DEFAULT_PRIORITY;
    if (outputObj.containsKey(KEY_PRIORITY)) {
      final int jsonPriority = loadInt(outputObj, KEY_PRIORITY, true, "Output " + KEY_PRIORITY + " must be a valid integer");
      if (jsonPriority < 0 || jsonPriority > StreamingACNDatagram.MAX_PRIORITY) {
        addWarning("Output " + KEY_PRIORITY + " must be within range [0-200], ignoring value: " + jsonPriority);
      } else {
        priority = jsonPriority;
      }
    }

    KinetDatagram.Version kinetVersion = KinetDatagram.Version.PORTOUT;
    if (protocol == JsonProtocolDefinition.KINET) {
      if (outputObj.containsKey(KEY_KINET_VERSION)) {
        String key = loadString(outputObj, KEY_KINET_VERSION, true, "Output must specify valid KiNET version of PORTOUT or DMXOUT");
        if (key != null) {
          try {
            kinetVersion = KinetDatagram.Version.valueOf(key.toUpperCase());
          } catch (Exception x) {
            addWarning("Output specifies an invalid KiNET version: " + key);
          }
        }
      }
    }

    final boolean sequenceEnabled = loadBoolean(outputObj, KEY_SEQUENCE_ENABLED, true, "Output " + KEY_SEQUENCE_ENABLED + " must be a valid boolean");

    // Top level output byte-order
    JsonByteEncoderDefinition byteOrder = loadByteOrder(outputObj, JsonByteEncoderDefinition.RGB);

    // Load up the segment definitions
    List<JsonSegmentDefinition> segments = new ArrayList<JsonSegmentDefinition>();
    loadSegments(fixture, segments, outputObj, byteOrder);

    this.definedOutputs.add(new JsonOutputDefinition(fixture, protocol, transport, byteOrder, address, port, universe, channel, priority, sequenceEnabled, kinetVersion, fps, segments));
  }

  private void loadBrightness(LXFixture child, Map<String, Object> childObj) {
    if (childObj.containsKey(KEY_BRIGHTNESS)) {
      float brightness = loadFloat(childObj, KEY_BRIGHTNESS, true);
      if (brightness < 0f || brightness > 1f) {
        addWarning("Component " + KEY_BRIGHTNESS + " must be in the range 0-1, invalid: " + brightness);
      } else {
        child.brightness.setValue(brightness);
      }
    }
  }

  private void loadMetaData(Map<String, Object> obj, Map<String, String> metaData) {
    Map<String, Object> metaDataObj = loadObject(obj, KEY_META, KEY_META + " must be a JSON object");
    if (metaDataObj != null) {
      for (Map.Entry<String, Object> entry : metaDataObj.entrySet()) {
        String key = entry.getKey();
        Object value = entry.getValue();
        if (!(value instanceof String)) {
          addWarning("Meta data values must be primtives, key has invalid type: " + key);
        } else {
          metaData.put(key, replaceVariables(key, (String) value, ParameterType.STRING));
        }
      }
    }
  }

  private static final String[] SEGMENT_KEYS = { KEY_NUM, KEY_START, KEY_COMPONENT_INDEX, KEY_COMPONENT_ID, KEY_STRIDE, KEY_REVERSE, KEY_PAD_PRE, KEY_PAD_POST, KEY_HEADER_BYTES, KEY_FOOTER_BYTES };

  private void loadSegments(LXFixture fixture, List<JsonSegmentDefinition> segments, Map<String, Object> outputObj, JsonByteEncoderDefinition defaultByteOrder) {
    if (outputObj.containsKey(KEY_SEGMENTS)) {
      // Specifying an array of segments, keys should not be there by default
      for (String segmentKey : SEGMENT_KEYS) {
        if (outputObj.containsKey(segmentKey)) {
          addWarning(KEY_OUTPUT + " specifies " + KEY_SEGMENTS + ", may not also specify " + segmentKey + ", will be ignored");
        }
      }

      List<Object> segmentsArr = loadArray(outputObj, KEY_SEGMENTS, KEY_SEGMENTS + " must be an array of segments");
      if (segmentsArr != null) {
        for (Object segmentElem : segmentsArr) {
          if (segmentElem instanceof Map) {
            loadSegment(fixture, segments, (Map<String, Object>) segmentElem, defaultByteOrder, false);
          } else if (segmentElem != null) {
            addWarning(KEY_SEGMENTS + " should only contain segment elements in JSON object format, found invalid: " + segmentElem);
          }
        }
      }
    } else {
      // Just specifying one, no need for segments key, defined directly in output
      loadSegment(fixture, segments, outputObj, defaultByteOrder, true);
    }
  }

  private void loadSegment(LXFixture fixture, List<JsonSegmentDefinition> segments, Map<String, Object> segmentObj, JsonByteEncoderDefinition outputByteOrder, boolean isOutput) {
    int num = JsonOutputDefinition.ALL_POINTS;

    int start = loadInt(segmentObj, KEY_START, true, "Output " + KEY_START + " must be a valid integer");
    if (start < 0) {
      addWarning("Output " + KEY_START + " may not be negative");
      return;
    }

    if (segmentObj.containsKey(KEY_COMPONENT_ID)) {
      if (!(fixture instanceof JsonFixture)) {
        addWarning("Output " + KEY_COMPONENT_ID + " may only be used on custom fixtures");
        return;
      }
      String componentId = loadString(segmentObj, KEY_COMPONENT_ID, true, "Output " + KEY_COMPONENT_ID + " must be a valid string");
      if (componentId == null) {
        addWarning("Output " + KEY_COMPONENT_ID + " may not be empty");
        return;
      }
      List<LXFixture> childComponents = ((JsonFixture) fixture).componentsById.get(componentId);
      if (childComponents == null) {
        addWarning("Output " + KEY_COMPONENT_ID + " does not exist: " + componentId);
        return;
      }

      final int offset = start;
      start = ((JsonFixture) fixture).getFixtureOffset(childComponents.get(0));
      num = 0;
      for (LXFixture childFixture : childComponents) {
        num += childFixture.totalSize();
      }
      if (offset >= num) {
        addWarning("Output " + KEY_COMPONENT_ID + "=" + componentId + " start value " + offset + " exceeds size " + num);
        return;
      }
      start += offset;
      num -= offset;

    } else if (segmentObj.containsKey(KEY_COMPONENT_INDEX)) {
      if (!(fixture instanceof JsonFixture)) {
        addWarning("Output " + KEY_COMPONENT_INDEX + " may only be used on custom fixtures");
        return;
      }
      int componentIndex = loadInt(segmentObj, KEY_COMPONENT_INDEX, true, "Output " + KEY_COMPONENT_INDEX + " must be a valid integer");
      if (componentIndex < 0) {
        addWarning("Output " + KEY_COMPONENT_INDEX + " may not be negative (" + componentIndex + ")");
        return;
      }
      if (componentIndex >= ((JsonFixture) fixture).componentsByIndex.size()) {
        addWarning("Output " + KEY_COMPONENT_INDEX + " is out of fixture range (" + componentIndex + ")");
        return;
      }
      List<LXFixture> childComponents = ((JsonFixture) fixture).componentsByIndex.get(componentIndex);
      if (childComponents == null) {
        addWarning("Output " + KEY_COMPONENT_INDEX + " in invalid or disabled (" + componentIndex + ")");
        return;
      }
      final int offset = start;
      start = ((JsonFixture) fixture).getFixtureOffset(childComponents.get(0));
      num = 0;
      for (LXFixture childFixture : childComponents) {
        num += childFixture.totalSize();
      }
      if (offset >= num) {
        addWarning("Output " + KEY_COMPONENT_INDEX + "=" + componentIndex + " start value " + offset + " exceeds size " + num);
        return;
      }
      start += offset;
      num -= offset;
    }

    if (segmentObj.containsKey(KEY_NUM)) {
      num = loadInt(segmentObj, KEY_NUM, true, "Output " + KEY_NUM + " must be a valid integer");
      if (num < 0) {
        addWarning("Output " + KEY_NUM + " may not be negative");
        return;
      }
    }

    int stride = DEFAULT_OUTPUT_STRIDE;
    if (segmentObj.containsKey(KEY_STRIDE)) {
      stride = loadInt(segmentObj, KEY_STRIDE, true, "Output " + KEY_STRIDE + " must be a valid integer");
      if (stride <= 0) {
        addWarning("Output stride must be a positive value, use 'reverse: true' to invert pixel order");
        return;
      }
    }

    int repeat = DEFAULT_OUTPUT_REPEAT;
    if (segmentObj.containsKey(KEY_REPEAT)) {
      repeat = loadInt(segmentObj, KEY_REPEAT, true, "Output " + KEY_REPEAT + " must be a valid integer");
      if (repeat <= 0) {
        addWarning("Output repeat must be a positive value");
        return;
      }
    }

    int padPre = 0, padPost = 0;
    if (segmentObj.containsKey(KEY_PAD_PRE)) {
      padPre = loadInt(segmentObj, KEY_PAD_PRE, true, "Output " + KEY_PAD_PRE + " must be a valid integer");
    }
    if (segmentObj.containsKey(KEY_PAD_POST)) {
      padPost = loadInt(segmentObj, KEY_PAD_POST, true, "Output " + KEY_PAD_POST+ " must be a valid integer");
    }
    if (padPre < 0 || padPost < 0) {
      addWarning("Output padding must be a non-negative value");
      return;
    }

    boolean reverse = loadBoolean(segmentObj, KEY_REVERSE, true, "Output " + KEY_REVERSE + " must be a valid boolean");

    JsonByteEncoderDefinition segmentByteOrder = null;
    if (!isOutput) {
      segmentByteOrder = loadByteOrder(segmentObj, null);
    }

    JsonByteEncoderDefinition byteEncoder = (segmentByteOrder != null) ? segmentByteOrder : outputByteOrder;
    int outputStride = byteEncoder.byteEncoder.getNumBytes();
    if (segmentObj.containsKey(KEY_OUTPUT_STRIDE)) {
      int customStride = loadInt(segmentObj, KEY_OUTPUT_STRIDE, true, "Output " + KEY_OUTPUT_STRIDE + " must be a valid integer");
      if (customStride < outputStride) {
        addWarning("Output stride may not be less than byte order size: " + customStride + " < " + outputStride);
      } else {
        outputStride = customStride;
      }
    }

    // The entire segment may be repeated - note that this is different from the basic
    // repeat option which repeats every *pixel* inline. The segment repeat option duplicates
    // output of the entire segment N times.
    int duplicate = 1;
    if (segmentObj.containsKey(KEY_DUPLICATE)) {
      duplicate = loadInt(segmentObj, KEY_DUPLICATE, true, "Output " + KEY_DUPLICATE + " must be a valid integer");
      if (duplicate <= 0) {
        addWarning("Output duplicate must be a positive value");
        return;
      }
    }

    // Static header / footer bytes
    byte[] headerBytes = loadStaticBytes(segmentObj, KEY_HEADER_BYTES);
    byte[] footerBytes = loadStaticBytes(segmentObj, KEY_FOOTER_BYTES);

    // Duplicate the definition N times (typically 1)
    final JsonSegmentDefinition segment = new JsonSegmentDefinition(start, num, stride, repeat, padPre, padPost, reverse, segmentByteOrder, headerBytes, footerBytes, outputStride);
    for (int i = 0; i < duplicate; ++i) {
      segments.add(segment);
    }
  }

  private byte[] loadStaticBytes(Map<String, Object> segmentObj, String key) {
    if (segmentObj.containsKey(key)) {
      final Object elem = segmentObj.get(key);

      if (elem instanceof String) {
        // Static bytes supplied as a hex string
        final String hex = (String) elem;
        if (hex.isEmpty() || (hex.length() % 2) != 0) {
          addWarning("Byte hex string " + key + " must have positive, even length");
          return null;
        }
        final byte[] bytes = new byte[hex.length() / 2];
        try {
          int b = 0;
          for (int offset = 0; offset < hex.length(); offset += 2) {
            bytes[b++] = (byte) Integer.parseInt(hex.substring(offset, offset+2), 16);
          }
        } catch (NumberFormatException nfx) {
          addWarning("Bad hex byte string " + key + ": " + hex);
          return null;
        }
        return bytes;
      } else if (elem instanceof List) {
        // Static bytes as an array of numeric values
        final List<Object> arr = (List<Object>) elem;
        if (arr.size() == 0) {
          addWarning("Byte array for " + key + " is empty");
          return null;
        }
        final byte[] bytes = new byte[arr.size()];
        for (int i = 0; i < arr.size(); ++i) {
          final Object byteElem = arr.get(i);
          try {
            // GSON doesn't handle 0x prefixed hex numbers natively, they come back as strings
            if (byteElem instanceof String && ((String) byteElem).toLowerCase().startsWith("0x")) {
              bytes[i] = (byte) Integer.parseInt(((String) byteElem).substring(2), 16);
            } else {
              bytes[i] = (byte) ((Number) byteElem).intValue();
            }
          } catch (Exception x) {
            addWarning("Bad byte value " + key + "[" + i + "] = " + byteElem);
          }
        }
        return bytes;
      } else {
        addWarning("Bad static byte data for " + key + ": " + elem);
      }
    }
    return null;
  }

  private JsonByteEncoderDefinition loadByteOrder(Map<String, Object> obj, JsonByteEncoderDefinition defaultByteOrder) {
    JsonByteEncoderDefinition byteOrder = defaultByteOrder;
    String byteOrderStr = loadString(obj, KEY_BYTE_ORDER, true, "Output must specify a valid string " + KEY_BYTE_ORDER);
    if (byteOrderStr != null) {
      if (byteOrderStr.isEmpty()) {
        addWarning("Output must specify non-empty string value for " + KEY_BYTE_ORDER);
      } else {
        JsonByteEncoderDefinition definedByteOrder = JsonByteEncoderDefinition.get(this.lx, byteOrderStr);
        if (definedByteOrder == null) {
          addWarning("Unrecognized byte order type: " + byteOrderStr);
        } else {
          byteOrder = definedByteOrder;
        }
      }
    }
    return byteOrder;
  }

  private void loadUI(Map<String, Object> obj) {
    if (!obj.containsKey(KEY_UI)) {
      return;
    }
    Map<String, Object> uiObj = (Map<String, Object>) obj.get(KEY_UI);
    if (uiObj.containsKey(KEY_MESH) && uiObj.containsKey(KEY_MESHES)) {
      addWarning("Should not have both " + KEY_MESH + " and " + KEY_MESHES);
    }

    if (uiObj.containsKey(KEY_MESH)) {
      loadUIMesh((Map<String, Object>) uiObj.get(KEY_MESH));
    }
    if (uiObj.containsKey(KEY_MESHES)) {
      List<Object> meshArray = (List<Object>) uiObj.get(KEY_MESHES);
      for (Object meshElem : meshArray) {
        loadUIMesh((Map<String, Object>) meshElem);
      }
    }
  }

  private void loadUIMesh(Map<String, Object> meshObj) {
    boolean meshEnabled = true;
    if (meshObj.containsKey(KEY_ENABLED)) {
      meshEnabled = loadBoolean(meshObj, KEY_ENABLED, true, KEY_MESH + " property " + KEY_ENABLED + " must be a valid boolean");
    }
    if (!meshEnabled) {
      return;
    }
    if (!meshObj.containsKey(KEY_TYPE)) {
      addWarning("UI mesh must specify " + KEY_TYPE);
      return;
    }
    LXModel.Mesh.Type meshType = null;
    String meshTypeStr = (String) meshObj.get(KEY_TYPE);
    if (MESH_TYPE_UNIFORM_FILL.equals(meshTypeStr)) {
      meshType = LXModel.Mesh.Type.UNIFORM_FILL;
    } else if (MESH_TYPE_TEXTURE_2D.equals(meshTypeStr)) {
      meshType = LXModel.Mesh.Type.TEXTURE_2D;
    } else if (MESH_TYPE_PHONG.equals(meshTypeStr)) {
      meshType = LXModel.Mesh.Type.PHONG;
    }
    if (meshType == null) {
      addWarning("Unknown mesh type: " + meshTypeStr);
      return;
    }

    int meshColor = 0xffffffff;
    if (meshObj.containsKey(KEY_MESH_COLOR)) {
      meshColor = loadColor(meshObj, KEY_MESH_COLOR);
    }

    LXModel.Mesh.Lighting meshLighting = LXModel.Mesh.Lighting.DEFAULT;
    int meshLightColor = 0xffffffff;
    LXModel.Mesh.Vertex meshLightDirection = new LXModel.Mesh.Vertex(0, 0, 1);
    if (meshObj.containsKey(KEY_MESH_LIGHTING)) {
      final Map<String, Object> meshLightingObj = (Map<String, Object>) meshObj.get(KEY_MESH_LIGHTING);
      meshLighting = new LXModel.Mesh.Lighting(
        loadFloat(meshLightingObj, KEY_MESH_LIGHTING_AMBIENT, true),
        loadFloat(meshLightingObj, KEY_MESH_LIGHTING_DIFFUSE, true),
        loadFloat(meshLightingObj, KEY_MESH_LIGHTING_SPECULAR, true),
        loadFloat(meshLightingObj, KEY_MESH_LIGHTING_SHININESS, true)
      );
      if (meshLightingObj.containsKey(KEY_MESH_LIGHTING_COLOR)) {
        meshLightColor = loadColor(meshLightingObj, KEY_MESH_LIGHTING_COLOR);
      }
      if (meshLightingObj.containsKey(KEY_MESH_LIGHTING_DIRECTION)) {
        Map<String, Object> meshLightingDirectionObj = (Map<String, Object>) meshLightingObj.get(KEY_MESH_LIGHTING_DIRECTION);
        float x = loadFloat(meshLightingDirectionObj, KEY_X, true);
        float y = loadFloat(meshLightingDirectionObj, KEY_Y, true);
        float z = loadFloat(meshLightingDirectionObj, KEY_Z, true);
        meshLightDirection = new LXModel.Mesh.Vertex(x, y, z);
      }
    }

    File meshTexture = null;
    if (meshObj.containsKey(KEY_MESH_TEXTURE)) {
      meshTexture = getMeshFile((String) meshObj.get(KEY_MESH_TEXTURE));
    }

    if (meshObj.containsKey(KEY_MESH_VERTICES) && meshObj.containsKey(KEY_MESH_FILE)) {
      addWarning("UI mesh may not specify both " + KEY_MESH_VERTICES + " and " + KEY_MESH_FILE);
      return;
    }

    if (!meshObj.containsKey(KEY_MESH_VERTICES) && !meshObj.containsKey(KEY_MESH_FILE)) {
      addWarning("UI mesh must specify " + KEY_MESH_VERTICES + " or " + KEY_MESH_FILE);
      return;
    }

    LXModel.Mesh mesh = null;

    if (meshObj.containsKey(KEY_MESH_VERTICES)) {
      LXModel.Mesh.VertexList vertices = new LXModel.Mesh.VertexList();
      List<Object> verticesArr = (List<Object>) meshObj.get(KEY_MESH_VERTICES);
      for (Object vertexElem : verticesArr) {
        Map<String, Object> vertexObj = (Map<String, Object>) vertexElem;
        if (vertexObj.containsKey(KEY_INSTANCES)) {
          int numInstances = loadInt(vertexObj, KEY_INSTANCES, true, "Vertex object must specify positive number of instances");
          if (numInstances <= 0) {
            addWarning("Vertex specifies illegal number of instances: " + numInstances);
            return;
          }
          if (numInstances >= MAX_INSTANCES) {
            addWarning("Vertex specifies too many instances: " + numInstances + " >= " + MAX_INSTANCES);
            return;
          }

          // Load this child N times with an instance variable set
          this.currentNumInstances = numInstances;
          for (int i = 0; i < numInstances; ++i) {
            this.currentChildInstance = i;
            Map<String, Object> instanceObj = LXSerializable.Utils.deepCopy(vertexObj);
            instanceObj.remove(KEY_INSTANCES);
            loadUIVertex(instanceObj, vertices);
          }
          this.currentNumInstances = -1;
          this.currentChildInstance = -1;
        } else {
          loadUIVertex(vertexObj, vertices);
        }
      }
      if (vertices.isEmpty()) {
        addWarning("UI mesh object must specify non-empty " + KEY_MESH_VERTICES);
        return;
      }
      mesh = new LXModel.Mesh(meshType, vertices, meshColor, meshTexture);
    } else if (meshObj.containsKey(KEY_MESH_FILE)) {
      final String meshFileStr = (String) meshObj.get(KEY_MESH_FILE);
      final File meshFile = getMeshFile(meshFileStr);
      if (!meshFile.exists()) {
        addWarning("Cannot find UI mesh file: " + meshFileStr);
      } else {
        mesh = new LXModel.Mesh(meshType, meshFile, meshColor);
      }
    }

    if (mesh != null) {
      mesh.setLighting(meshLighting);
      mesh.setLightColor(meshLightColor);
      mesh.setLightDirection(meshLightDirection);
      mesh.invertNormals = loadBoolean(meshObj, KEY_MESH_INVERT_NORMALS, true, "Mesh must specify valid boolean for " + KEY_MESH_INVERT_NORMALS);
      this.mutableMeshes.add(mesh);
    }
  }

  private enum MeshVertexType {
    VERTEX,
    RECT,
    CUBOID;

    public static MeshVertexType find(String str) {
      str = str.toUpperCase();
      for (MeshVertexType candidate : values()) {
        if (candidate.name().equals(str)) {
          return candidate;
        }
      }
      return null;
    }
  }

  private void loadUIVertex(Map<String, Object> vertexObj, LXModel.Mesh.VertexList vertices) {
    LXVector vector = loadVector(vertexObj, "Mesh vertex must specify at least one of x/y/z");
    final float u = loadFloat(vertexObj, "u", true);
    final float v = loadFloat(vertexObj, "v", true);

    MeshVertexType vertexType = MeshVertexType.VERTEX;
    if (vertexObj.containsKey(KEY_TYPE)) {
      String typeStr = (String) vertexObj.get(KEY_TYPE);
      vertexType = MeshVertexType.find(typeStr);
      if (vertexType == null) {
        addWarning("Unknown mesh vertex type: " + typeStr);
        return;
      }
    }
    switch (vertexType) {
      case VERTEX -> vertices.add(new LXModel.Mesh.Vertex(vector.x, vector.y, vector.z, u, v));
      case RECT -> loadUIVertexRect(vertexObj, vector, vertices);
      case CUBOID -> loadUIVertexCuboid(vertexObj, vector, vertices);
    };
  }

  private enum MeshRectAxis {
    XY,
    XZ,
    YX,
    YZ,
    ZX,
    ZY;

    public static MeshRectAxis find(String str) {
      str = str.toUpperCase();
      for (MeshRectAxis candidate : values()) {
        if (candidate.name().equals(str)) {
          return candidate;
        }
      }
      return null;
    }
  }

  private void loadUIVertexRect(Map<String, Object> vertexObj, LXVector vertex, LXModel.Mesh.VertexList vertices) {
    final float width = loadFloat(vertexObj, KEY_MESH_RECT_WIDTH, true);
    final float height = loadFloat(vertexObj, KEY_MESH_RECT_HEIGHT, true);
    if ((width == 0) || (height == 0)) {
      addWarning("Mesh vertex type \"rect\" must provide non-zero width/height");
      return;
    }

    MeshRectAxis rectAxis = MeshRectAxis.XY;
    if (vertexObj.containsKey(KEY_MESH_RECT_AXIS)) {
      String axisStr = (String) vertexObj.get(KEY_MESH_RECT_AXIS);
      rectAxis = MeshRectAxis.find(axisStr);
      if (rectAxis == null) {
        addWarning("Unknown mesh rect axis: " + axisStr);
        return;
      }
    }
    _loadUIVertexRect(vertices, vertex, width, height, rectAxis);
  }

  private void _loadUIVertexRect(LXModel.Mesh.VertexList vertices, LXVector vertex, float width, float height, MeshRectAxis rectAxis) {
    switch (rectAxis) {
      case XY -> {
        vertices.add(vertex, 0, 1);
        vertices.add(vertex.copy().add(width, 0), 1, 1);
        vertices.add(vertex.copy().add(0, height), 0, 0);
        vertices.add(vertex.copy().add(0, height), 0, 0);
        vertices.add(vertex.copy().add(width, 0), 1, 1);
        vertices.add(vertex.copy().add(width, height), 1, 0);
      }
      case XZ -> {
        vertices.add(vertex, 0, 1);
        vertices.add(vertex.copy().add(width, 0, 0), 1, 1);
        vertices.add(vertex.copy().add(0, 0, height), 0, 0);
        vertices.add(vertex.copy().add(0, 0, height), 0, 0);
        vertices.add(vertex.copy().add(width, 0, 0), 1, 1);
        vertices.add(vertex.copy().add(width, 0, height), 1, 0);
      }
      case YX -> {
        vertices.add(vertex, 0, 1);
        vertices.add(vertex.copy().add(0, width, 0), 1, 1);
        vertices.add(vertex.copy().add(height, 0, 0), 0, 0);
        vertices.add(vertex.copy().add(height, 0, 0), 0, 0);
        vertices.add(vertex.copy().add(0, width, 0), 1, 1);
        vertices.add(vertex.copy().add(height, width, 0), 1, 0);
      }
      case YZ -> {
        vertices.add(vertex, 0, 1);
        vertices.add(vertex.copy().add(0, width, 0), 1, 1);
        vertices.add(vertex.copy().add(0, 0, height), 0, 0);
        vertices.add(vertex.copy().add(0, 0, height), 0, 0);
        vertices.add(vertex.copy().add(0, width, 0), 1, 1);
        vertices.add(vertex.copy().add(0, width, height), 1, 0);
      }
      case ZX -> {
        vertices.add(vertex, 0, 1);
        vertices.add(vertex.copy().add(0, 0, width), 1, 1);
        vertices.add(vertex.copy().add(height, 0, 0), 0, 0);
        vertices.add(vertex.copy().add(height, 0, 0), 0, 0);
        vertices.add(vertex.copy().add(0, 0, width), 1, 1);
        vertices.add(vertex.copy().add(height, 0, width), 1, 0);
      }
      case ZY -> {
        vertices.add(vertex, 0, 1);
        vertices.add(vertex.copy().add(0, 0, width), 1, 1);
        vertices.add(vertex.copy().add(0, height, 0), 0, 0);
        vertices.add(vertex.copy().add(0, height, 0), 0, 0);
        vertices.add(vertex.copy().add(0, 0, width), 1, 1);
        vertices.add(vertex.copy().add(0, height, width), 1, 0);
      }
    }
  }

  private void loadUIVertexCuboid(Map<String, Object> vertexObj, LXVector vertex, LXModel.Mesh.VertexList vertices) {
    final float width = loadFloat(vertexObj, KEY_MESH_RECT_WIDTH, true);
    final float height = loadFloat(vertexObj, KEY_MESH_RECT_HEIGHT, true);
    final float depth = loadFloat(vertexObj, KEY_MESH_RECT_DEPTH, true);
    if ((width == 0) || (height == 0)) {
      addWarning("Mesh vertex type \"cuboid\" must provide non-zero width/height/depth");
      return;
    }
    _loadUIVertexRect(vertices, vertex, width, height, MeshRectAxis.XY);
    _loadUIVertexRect(vertices, vertex.copy().add(0, height, 0), width, depth, MeshRectAxis.XZ);
    _loadUIVertexRect(vertices, vertex.copy().add(0, height, depth), width, -height, MeshRectAxis.XY);
    _loadUIVertexRect(vertices, vertex.copy().add(0, 0, depth), width, -depth, MeshRectAxis.XZ);
    _loadUIVertexRect(vertices, vertex.copy().add(0, 0, depth), -depth, height, MeshRectAxis.ZY);
    _loadUIVertexRect(vertices, vertex.copy().add(width, 0, 0), depth, height, MeshRectAxis.ZY);
  }

  @Override
  protected void buildOutputs() {
    for (JsonOutputDefinition output : this.definedOutputs) {
      buildOutput(output);
    }
  }

  private int getFixtureOffset(LXFixture child) {
    int offset = size();
    for (LXFixture fixture : this.children) {
      if (child == fixture) {
        return offset;
      }
      offset += fixture.totalSize();
    }
    return 0;
  }

  private void buildOutput(JsonOutputDefinition output) {
    // Special case! Add an art-sync datagram directly
    if (output.protocol == JsonProtocolDefinition.ARTSYNC) {
      buildArtSyncDatagram(output);
      return;
    }

    // Use this helper, as firstPointIndex values may not necessarily set at this point, if we are in an
    // initial file load and the outputs are being regenerated by a parameter change. Later the
    // LXStructure will rebuild the model and set indices appropriately
    int fixtureOffset = getFixtureOffset(output.fixture);
    int fixtureSize = output.fixture.totalSize();

    List<Segment> segments = new ArrayList<Segment>();
    for (JsonSegmentDefinition segment : output.segments) {

      if (segment.start < 0 || (segment.start >= fixtureSize)) {
        addWarning("Output specifies invalid start position: " + segment.start + " should be between [0, " + (fixtureSize-1) + "]");
        return;
      }
      int num = (segment.num == JsonOutputDefinition.ALL_POINTS) ? fixtureSize : segment.num;

      if (segment.start + segment.stride * (num-1) >= fixtureSize) {
        addWarning("Output specifies excessive size beyond fixture limits: start=" + segment.start + " num=" + num + " stride=" + segment.stride + " fixtureSize=" + fixtureSize);
        return;
      }

      segments.add(new Segment(
        segment.start + fixtureOffset,
        num,
        segment.stride,
        segment.repeat,
        segment.padPre,
        segment.padPost,
        segment.reverse,
        (segment.byteEncoder != null) ? segment.byteEncoder.byteEncoder : output.byteEncoder.byteEncoder,
        segment.headerBytes,
        segment.footerBytes,
        segment.outputStride
       ));
    }

    // Add an output definition!
    addOutputDefinition(new OutputDefinition(
      output.protocol.protocol,
      output.transport.transport,
      output.address,
      (output.port == JsonOutputDefinition.DEFAULT_PORT) ? output.protocol.protocol.defaultPort : output.port,
      output.universe,
      output.channel,
      output.priority,
      output.sequenceEnabled,
      output.kinetVersion,
      output.fps,
      segments.toArray(new Segment[0])
    ));

  }

  private void buildArtSyncDatagram(JsonOutputDefinition output) {
    LXDatagram artSync = new ArtSyncDatagram(this.lx);
    if (output.port != JsonOutputDefinition.DEFAULT_PORT) {
      artSync.setPort(output.port);
    }
    artSync.setAddress(output.address);
    artSync.framesPerSecond.setValue(output.fps);
    addOutputDirect(artSync);
  }

  @Override
  protected List<LXModel.Mesh> getModelMeshes() {
    return this.meshes;
  }

  @Override
  protected int size() {
    // No points of our own, all points are managed by children
    return 0;
  }

  @Override
  protected void computePointGeometry(LXMatrix matrix, List<LXPoint> points) {
    // Nothing needs doing here, all points are held in children
  }

  @Override
  protected void addModelMetaData(Map<String, String> metaData) {
    for (ParameterDefinition parameter : this.definedParameters.values()) {
      metaData.put(parameter.name, parameter.getValueAsString());
    }
  }

  private static final String KEY_FIXTURE_TYPE = "jsonFixtureType";
  private static final String KEY_JSON_PARAMETERS = "jsonParameters";

  // TODO(look): implement adapter shim for JsonObject?
  @Override
  public void load(LX lx, JsonObject obj) {
    System.out.println(obj.toString());
    // if (this.isJsonSubfixture) {
    //   throw new IllegalStateException("Should never be loading/saving a child JsonSubfixture");
    // }

    // // This has been saved, let's call up the values for the JSON parameters we customized
    // this.jsonParameterValues =
    //   obj.has(KEY_JSON_PARAMETERS) ? (Map<String, Object>) obj.get(KEY_JSON_PARAMETERS) : new HashMap<String, Object>();

    // // Now do the normal LXFixture loading, which will trigger regeneration if
    // // we're part of a hierarchy
    // super.load(lx, obj);

    // // Now get rid of this, don't want to interfere with manual reload()
    // this.jsonParameterValues = new Map<String, Object>();
  }

  @Override
  public void save(LX lx, JsonObject obj) {
    // if (this.isJsonSubfixture) {
    //   throw new IllegalStateException("Should never be loading/saving a child JsonSubfixture");
    // }
    // obj.addProperty(KEY_FIXTURE_TYPE, this.fixtureType.getString());
    // Map<String, Object> jsonParameters = new Map<String, Object>();
    // for (ParameterDefinition parameter : this.definedParameters.values()) {
    //   switch (parameter.type) {
    //   case FLOAT:
    //     jsonParameters.addProperty(parameter.name, parameter.floatParameter.getValue());
    //     break;
    //   case INT:
    //     jsonParameters.addProperty(parameter.name, parameter.intParameter.getValuei());
    //     break;
    //   case STRING:
    //     jsonParameters.addProperty(parameter.name, parameter.stringParameter.getString());
    //     break;
    //   case STRING_SELECT:
    //     jsonParameters.addProperty(parameter.name, parameter.stringSelectParameter.getObject());
    //     break;
    //   case BOOLEAN:
    //     jsonParameters.addProperty(parameter.name, parameter.booleanParameter.isOn());
    //     break;
    //   }
    // }
    // obj.add(KEY_JSON_PARAMETERS, jsonParameters);
    // super.save(lx, obj);
  }

}
