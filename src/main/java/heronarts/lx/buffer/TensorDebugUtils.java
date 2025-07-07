package heronarts.lx.buffer;

import java.util.Arrays;

import org.nd4j.linalg.api.ndarray.INDArray;

public class TensorDebugUtils {

  public static void debugTensor(INDArray arr) {
    System.out.println("[[[[[[[[[[[[[[");
    System.out.println("Data type: " + arr.dataType());
    System.out.println("Shape: " + Arrays.toString(arr.shape()));
    System.out.println("Values: \n" + arr);
    System.out.println("]]]]]]]]]]]]]]");
  }

  public static void printAsBinary(INDArray array, String label) {
    printAsBinary(array, label, false);
  }

  public static void printAsBinary(INDArray array, String label, boolean includeIntVals) {
    System.out.println(label + ":");
    for (int i = 0; i < array.length(); i++) {
      int value = array.getInt(i);
      String binary = String.format("%32s", Integer.toBinaryString(value)).replace(' ', '0');
      // Add spacing every 4 bits for readability
      String formatted = binary.replaceAll("(.{8})", "$1 ").trim();
      if (includeIntVals) {
        System.out.printf("  [%d]: %10d = %s%n", i, value, formatted);
      } else {
        System.out.printf("  [%d]: %s%n", i, formatted);
      }
    }
  }

  public static void printAsBinaryUint8(INDArray array, String label) {
    printAsBinaryUint8(array, label, false);
  }

  public static void printAsBinaryUint8(INDArray array, String label, boolean includeIntVals) {
    System.out.println(label + ":");
    System.out.println("Shape: " + Arrays.toString(array.shape()));

    if (array.rank() == 1) {
      // Handle 1D arrays
      for (int i = 0; i < array.length(); i++) {
        int value = array.getInt(i) & 0xFF;
        String binary = String.format("%8s", Integer.toBinaryString(value)).replace(' ', '0');
        if (includeIntVals) {
          System.out.printf("  [%d]: %3d = %s%n", i, value, binary);
        } else {
          System.out.printf("  [%d]: %s%n", i, binary);
        }
      }
    } else if (array.rank() == 2) {
      // Handle 2D arrays with column headers
      long rows = array.size(0);
      long cols = array.size(1);

      // Print column headers
      System.out.print("      ");
      for (int col = 0; col < cols; col++) {
        System.out.printf(" Col%d    ", col);
      }
      System.out.println();

      // Print rows
      for (int row = 0; row < rows; row++) {
        System.out.printf("Row%2d: ", row);
        for (int col = 0; col < cols; col++) {
          int value = array.getInt(row, col) & 0xFF;
          String binary = String.format("%8s", Integer.toBinaryString(value)).replace(' ', '0');
          if (includeIntVals) {
            System.out.printf("%3d=%s ", value, binary);
          } else {
            System.out.printf("%s ", binary);
          }
        }
        System.out.println();
      }
    } else {
      System.out.println("  (Arrays with rank > 2 not supported)");
    }
  }
}
