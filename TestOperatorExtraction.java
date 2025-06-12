import heronarts.lx.structure.expression.LXFExpression;
import heronarts.lx.structure.expression.TreeNode;
import heronarts.lx.structure.expression.Label;

public class TestOperatorExtraction {
    public static void main(String[] args) {
        try {
            System.out.println("=== Testing operator extraction ===");
            TreeNode ast = LXFExpression.parse("3+4");
            TreeNode ternary = ast.get(Label.TERNARY);
            TreeNode logicalOr = ternary.get(Label.LOGICAL_OR);
            TreeNode logicalAnd = logicalOr.get(Label.LOGICAL_AND);
            TreeNode comparison = logicalAnd.get(Label.COMPARISON);
            TreeNode additive = comparison.get(Label.ADDITIVE);
            
            System.out.println("ADDITIVE: \"" + additive.text + "\"");
            TreeNode plusFour = additive.elements.get(1);
            System.out.println("Element[1]: \"" + plusFour.text + "\"");
            
            // Check if ADDITIVE_OP label exists
            TreeNode additiveOp = plusFour.get(Label.ADDITIVE_OP);
            if (additiveOp != null) {
                System.out.println("Found ADDITIVE_OP: \"" + additiveOp.text + "\"");
            } else {
                System.out.println("ADDITIVE_OP is null");
            }
            
            // Check for MULTIPLICATIVE in the +4 element  
            TreeNode multiplicative = plusFour.get(Label.MULTIPLICATIVE);
            if (multiplicative != null) {
                System.out.println("Found MULTIPLICATIVE in +4: \"" + multiplicative.text + "\"");
            } else {
                System.out.println("MULTIPLICATIVE in +4 is null");
            }
            
            // Print all labels in the +4 element
            System.out.println("All labels in +4 element:");
            for (Label label : Label.values()) {
                TreeNode labeled = plusFour.get(label);
                if (labeled != null) {
                    System.out.println("  " + label + " -> \"" + labeled.text + "\"");
                }
            }
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}