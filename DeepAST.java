import heronarts.lx.structure.expression.LXFExpression;
import heronarts.lx.structure.expression.TreeNode;
import heronarts.lx.structure.expression.Label;

public class DeepAST {
    public static void main(String[] args) {
        String[] expressions = {"3+4", "abs(-5)"};
        
        for (String expr : expressions) {
            try {
                System.out.println("\n=== Expression: \"" + expr + "\" ===");
                TreeNode ast = LXFExpression.parse(expr);
                TreeNode ternary = ast.get(Label.TERNARY);
                System.out.println("TERNARY: \"" + ternary.text + "\"");
                
                // Go deeper into the LOGICAL_OR level
                TreeNode logicalOr = ternary.get(Label.LOGICAL_OR);
                if (logicalOr != null) {
                    System.out.println("  LOGICAL_OR: \"" + logicalOr.text + "\"");
                    
                    TreeNode logicalAnd = logicalOr.get(Label.LOGICAL_AND);
                    if (logicalAnd != null) {
                        System.out.println("    LOGICAL_AND: \"" + logicalAnd.text + "\"");
                        
                        TreeNode comparison = logicalAnd.get(Label.COMPARISON);
                        if (comparison != null) {
                            System.out.println("      COMPARISON: \"" + comparison.text + "\"");
                            
                            TreeNode additive = comparison.get(Label.ADDITIVE);
                            if (additive != null) {
                                System.out.println("        ADDITIVE: \"" + additive.text + "\"");
                                System.out.println("        ADDITIVE elements: " + additive.elements.size());
                                for (int i = 0; i < additive.elements.size(); i++) {
                                    TreeNode elem = additive.elements.get(i);
                                    System.out.println("          [" + i + "]: \"" + elem.text + "\"");
                                    // Check what labels this element has
                                    for (Label label : Label.values()) {
                                        TreeNode labeled = elem.get(label);
                                        if (labeled != null) {
                                            System.out.println("            " + label + " -> \"" + labeled.text + "\"");
                                        }
                                    }
                                }
                                
                                TreeNode multiplicative = additive.get(Label.MULTIPLICATIVE);
                                if (multiplicative != null) {
                                    System.out.println("          MULTIPLICATIVE: \"" + multiplicative.text + "\"");
                                    
                                    TreeNode power = multiplicative.get(Label.POWER);
                                    if (power != null) {
                                        System.out.println("            POWER: \"" + power.text + "\"");
                                        
                                        TreeNode unary = power.get(Label.UNARY);
                                        if (unary != null) {
                                            System.out.println("              UNARY: \"" + unary.text + "\"");
                                            
                                            TreeNode primary = unary.get(Label.PRIMARY);
                                            if (primary != null) {
                                                System.out.println("                PRIMARY: \"" + primary.text + "\"");
                                                
                                                // Check for FUNCTION vs NUMBER
                                                TreeNode function = primary.get(Label.FUNCTION);
                                                TreeNode number = primary.get(Label.NUMBER);
                                                if (function != null) {
                                                    System.out.println("                  FUNCTION: \"" + function.text + "\"");
                                                    TreeNode funcName = function.get(Label.FUNCTION_NAME);
                                                    TreeNode expression = function.get(Label.EXPRESSION);
                                                    if (funcName != null) System.out.println("                    FUNCTION_NAME: \"" + funcName.text + "\"");
                                                    if (expression != null) System.out.println("                    EXPRESSION: \"" + expression.text + "\"");
                                                } else if (number != null) {
                                                    System.out.println("                  NUMBER: \"" + number.text + "\"");
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Error parsing: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}