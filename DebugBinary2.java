import heronarts.lx.structure.expression.LXFExpression;
import heronarts.lx.structure.expression.TreeNode;
import heronarts.lx.structure.expression.Label;

public class DebugBinary2 {
    public static void main(String[] args) {
        try {
            System.out.println("=== Analyzing '+4' element structure ===");
            TreeNode ast = LXFExpression.parse("3+4");
            TreeNode ternary = ast.get(Label.TERNARY);
            TreeNode logicalOr = ternary.get(Label.LOGICAL_OR);
            TreeNode logicalAnd = logicalOr.get(Label.LOGICAL_AND);
            TreeNode comparison = logicalAnd.get(Label.COMPARISON);
            TreeNode additive = comparison.get(Label.ADDITIVE);
            
            // Get the "+4" element
            TreeNode plusFour = additive.elements.get(1);
            System.out.println("plusFour text: \"" + plusFour.text + "\"");
            System.out.println("plusFour elements: " + plusFour.elements.size());
            
            if (plusFour.elements.size() > 0) {
                TreeNode subElem = plusFour.elements.get(0);
                System.out.println("  sub-element: \"" + subElem.text + "\"");
                System.out.println("  sub-element children: " + subElem.elements.size());
                
                // Check labels on sub-element
                for (Label label : Label.values()) {
                    TreeNode labeled = subElem.get(label);
                    if (labeled != null) {
                        System.out.println("    " + label + " -> \"" + labeled.text + "\"");
                    }
                }
                
                // Maybe the operand is in a deeper structure?
                for (int i = 0; i < subElem.elements.size(); i++) {
                    TreeNode deeper = subElem.elements.get(i);
                    System.out.println("    deeper[" + i + "]: \"" + deeper.text + "\"");
                }
            }
            
            System.out.println("\n=== Let's try parsing just '4' ===");
            TreeNode four = LXFExpression.parse("4");
            TreeNode fourTernary = four.get(Label.TERNARY);
            TreeNode fourLogicalOr = fourTernary.get(Label.LOGICAL_OR);
            TreeNode fourLogicalAnd = fourLogicalOr.get(Label.LOGICAL_AND);
            TreeNode fourComparison = fourLogicalAnd.get(Label.COMPARISON);
            TreeNode fourAdditive = fourComparison.get(Label.ADDITIVE);
            TreeNode fourMultiplicative = fourAdditive.get(Label.MULTIPLICATIVE);
            TreeNode fourPower = fourMultiplicative.get(Label.POWER);
            TreeNode fourUnary = fourPower.get(Label.UNARY);
            TreeNode fourPrimary = fourUnary.get(Label.PRIMARY);
            TreeNode fourNumber = fourPrimary.get(Label.NUMBER);
            
            System.out.println("Just '4' parsed as NUMBER: \"" + fourNumber.text + "\"");
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}