import heronarts.lx.structure.expression.LXFExpression;
import heronarts.lx.structure.expression.TreeNode;
import heronarts.lx.structure.expression.Label;

public class DebugBinary {
    public static void main(String[] args) {
        try {
            System.out.println("=== Analyzing binary operation structure ===");
            TreeNode ast = LXFExpression.parse("3+4");
            TreeNode ternary = ast.get(Label.TERNARY);
            TreeNode logicalOr = ternary.get(Label.LOGICAL_OR);
            TreeNode logicalAnd = logicalOr.get(Label.LOGICAL_AND);
            TreeNode comparison = logicalAnd.get(Label.COMPARISON);
            TreeNode additive = comparison.get(Label.ADDITIVE);
            
            System.out.println("ADDITIVE node: \"" + additive.text + "\"");
            System.out.println("Elements count: " + additive.elements.size());
            
            for (int i = 0; i < additive.elements.size(); i++) {
                TreeNode elem = additive.elements.get(i);
                System.out.println("\nElement [" + i + "]: \"" + elem.text + "\"");
                System.out.println("  Elements count: " + elem.elements.size());
                
                // Check if this element has its own structure
                for (Label label : Label.values()) {
                    TreeNode labeled = elem.get(label);
                    if (labeled != null) {
                        System.out.println("  " + label + " -> \"" + labeled.text + "\"");
                    }
                }
                
                // If it has sub-elements, print them too
                for (int j = 0; j < elem.elements.size(); j++) {
                    TreeNode subElem = elem.elements.get(j);
                    System.out.println("    [" + j + "]: \"" + subElem.text + "\"");
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}