package heronarts.lx.structure;

import heronarts.lx.structure.expression.LXFExpression;
import heronarts.lx.structure.expression.TreeNode;
import heronarts.lx.structure.expression.Label;
import heronarts.lx.structure.expression.ParseError;

/**
 * Debug utility to understand the AST structure produced by LXFExpression parser.
 */
public class ASTDebugger {
    
    public static void debugExpression(String expression) {
        try {
            System.out.println("\n=== Debugging expression: \"" + expression + "\" ===");
            TreeNode ast = LXFExpression.parse(expression);
            printNode(ast, 0);
        } catch (ParseError e) {
            System.out.println("Parse error: " + e.getMessage());
        }
    }
    
    private static void printNode(TreeNode node, int depth) {
        String indent = "  ".repeat(depth);
        System.out.println(indent + "Node: text=\"" + node.text + "\", offset=" + node.offset);
        
        // Print labeled elements
        for (Label label : Label.values()) {
            TreeNode labeled = node.get(label);
            if (labeled != null) {
                System.out.println(indent + "  " + label + " -> \"" + labeled.text + "\"");
            }
        }
        
        // Print child elements
        if (!node.elements.isEmpty()) {
            System.out.println(indent + "  Elements (" + node.elements.size() + "):");
            for (int i = 0; i < node.elements.size(); i++) {
                System.out.println(indent + "    [" + i + "]:");
                printNode(node.elements.get(i), depth + 3);
            }
        }
    }
    
    public static void main(String[] args) {
        debugExpression("3 + 4");
    }
}