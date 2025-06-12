import heronarts.lx.structure.expression.LXFExpression;
import heronarts.lx.structure.expression.TreeNode;
import heronarts.lx.structure.expression.Label;

public class InvestigateAST {
    public static void main(String[] args) {
        String[] expressions = {"3", "3+4", "3-4", "abs(-5)", "sin(0)"};
        
        for (String expr : expressions) {
            try {
                System.out.println("\n=== Expression: \"" + expr + "\" ===");
                TreeNode ast = LXFExpression.parse(expr);
                printDetailed(ast, 0);
            } catch (Exception e) {
                System.out.println("Error parsing: " + e.getMessage());
            }
        }
    }
    
    private static void printDetailed(TreeNode node, int depth) {
        String indent = "  ".repeat(depth);
        System.out.println(indent + "Node: text=\"" + node.text + "\", offset=" + node.offset);
        
        // Print all labeled elements using reflection
        for (Label label : Label.values()) {
            TreeNode labeled = node.get(label);
            if (labeled != null) {
                System.out.println(indent + "  " + label + " -> \"" + labeled.text + "\"");
                if (labeled.elements.size() > 0) {
                    System.out.println(indent + "    (contains " + labeled.elements.size() + " elements)");
                }
            }
        }
        
        // Print raw elements
        if (!node.elements.isEmpty()) {
            System.out.println(indent + "  Raw Elements (" + node.elements.size() + "):");
            for (int i = 0; i < node.elements.size(); i++) {
                TreeNode element = node.elements.get(i);
                System.out.println(indent + "    [" + i + "]: \"" + element.text + "\"" + 
                    (element.elements.size() > 0 ? " (+" + element.elements.size() + " sub)" : ""));
            }
        }
    }
}