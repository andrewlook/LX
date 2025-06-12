import heronarts.lx.structure.expression.LXFExpression;
import heronarts.lx.structure.expression.TreeNode;
import heronarts.lx.structure.PegExpressionEvaluator;

public class SimpleParseTest {
    public static void main(String[] args) {
        try {
            System.out.println("Testing simple parse...");
            TreeNode ast = LXFExpression.parse("3");
            System.out.println("Simple parse successful: " + ast.text);
            
            System.out.println("\nTesting addition parse...");
            ast = LXFExpression.parse("3+4");
            System.out.println("Addition parse successful: " + ast.text);
            
            System.out.println("\nTesting PegExpressionEvaluator...");
            PegExpressionEvaluator evaluator = new PegExpressionEvaluator();
            float result1 = evaluator.evaluateNumeric("3+4");
            System.out.println("3+4 = " + result1);
            float result2 = evaluator.evaluateNumeric("3-4");
            System.out.println("3-4 = " + result2);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}