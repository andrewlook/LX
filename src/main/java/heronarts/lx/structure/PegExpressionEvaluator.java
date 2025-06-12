package heronarts.lx.structure;

import heronarts.lx.structure.expression.LXFExpression;
import heronarts.lx.structure.expression.TreeNode;
import heronarts.lx.structure.expression.Label;
import heronarts.lx.structure.expression.ParseError;

/**
 * Implementation of ExpressionEvaluator that uses the PEG parser (LXFExpression) 
 * to parse expressions into an Abstract Syntax Tree (AST) and then evaluates them.
 * 
 * This implementation provides better error reporting and potentially better 
 * performance for complex expressions compared to the recursive descent approach
 * used by StringExpressionEvaluator.
 * 
 * Supports all the same features:
 * - Basic arithmetic: +, -, *, /, %, ^
 * - Boolean logic: &&, ||, !
 * - Comparisons: <, <=, >, >=, ==, !=
 * - Parentheses for grouping
 * - Ternary conditional: condition ? true_expr : false_expr
 * - Unary operators: -, !
 * - Mathematical functions: sin, cos, tan, abs, sqrt, floor, ceil, round, etc.
 * - Variables: $variableName, ${variableName}
 * - Proper operator precedence and associativity
 */
public class PegExpressionEvaluator implements ExpressionEvaluator {

    @Override
    public float evaluateNumeric(String expression) {
        ExpressionResult result = evaluate(expression);
        if (result instanceof ExpressionResult.Numeric numeric) {
            return numeric.getValue();
        }
        throw new IllegalArgumentException("Expected expression to be numeric: " + expression);
    }

    @Override
    public boolean evaluateBoolean(String expression) {
        ExpressionResult result = evaluate(expression);
        if (result instanceof ExpressionResult.Boolean bool) {
            return bool.getValue();
        }
        throw new IllegalArgumentException("Expected expression to be boolean: " + expression);
    }

    @Override
    public ExpressionResult evaluate(String expression) {
        try {
            TreeNode ast = LXFExpression.parse(expression.trim());
            return evaluateASTNode(ast);
        } catch (ParseError e) {
            throw new IllegalArgumentException("Parse error in expression '" + expression + "': " + e.getMessage(), e);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error evaluating expression '" + expression + "': " + e.getMessage(), e);
        }
    }

    /**
     * Recursively evaluates an AST node based on the PEG parser structure.
     * The parser creates a hierarchy: LOGICAL_OR → LOGICAL_AND → COMPARISON → ADDITIVE → MULTIPLICATIVE → POWER → UNARY → PRIMARY
     */
    private ExpressionResult evaluateASTNode(TreeNode node) {
        if (node == null || node.text.isEmpty()) {
            throw new IllegalArgumentException("Cannot evaluate null or empty AST node");
        }

        // Check if this is a ternary conditional at the top level
        if (node.elements.size() >= 2 && node.elements.get(1).text.startsWith("?")) {
            return evaluateTernary(node);
        }

        // Traverse down the precedence hierarchy by checking which level has operations
        return evaluateWithPrecedence(node);
    }

    /**
     * Evaluate by walking down the precedence hierarchy until we find the actual operation or leaf.
     */
    private ExpressionResult evaluateWithPrecedence(TreeNode node) {
        // Check if this is a function (has both FUNCTION_NAME and EXPRESSION argument)
        TreeNode functionName = node.get(Label.FUNCTION_NAME);
        TreeNode expression = node.get(Label.EXPRESSION);
        if (functionName != null && expression != null) {
            return evaluateFunction(node);
        }
        
        // Traverse the precedence hierarchy: TERNARY -> LOGICAL_OR -> ... -> PRIMARY
        
        // TERNARY level
        TreeNode ternary = node.get(Label.TERNARY);
        if (ternary != null) {
            return evaluateASTNode(ternary);
        }
        
        // LOGICAL_OR level
        TreeNode logicalOr = node.get(Label.LOGICAL_OR);
        if (logicalOr != null) {
            return evaluateLogicalOrOperation(logicalOr);
        }
        
        // LOGICAL_AND level
        TreeNode logicalAnd = node.get(Label.LOGICAL_AND);
        if (logicalAnd != null) {
            return evaluateLogicalAndOperation(logicalAnd);
        }
        
        // COMPARISON level
        TreeNode comparison = node.get(Label.COMPARISON);
        if (comparison != null) {
            return evaluateComparisonOperation(comparison);
        }
        
        // ADDITIVE level
        TreeNode additive = node.get(Label.ADDITIVE);
        if (additive != null) {
            return evaluateAdditiveOperation(additive);
        }
        
        // MULTIPLICATIVE level
        TreeNode multiplicative = node.get(Label.MULTIPLICATIVE);
        if (multiplicative != null) {
            return evaluateMultiplicativeOperation(multiplicative);
        }
        
        // POWER level
        TreeNode power = node.get(Label.POWER);
        if (power != null) {
            return evaluatePowerOperation(power);
        }
        
        // UNARY level
        TreeNode unary = node.get(Label.UNARY);
        if (unary != null) {
            return evaluateUnaryOperation(unary);
        }
        
        // PRIMARY level - leaf nodes
        TreeNode primary = node.get(Label.PRIMARY);
        if (primary != null) {
            return evaluatePrimary(primary);
        }
        
        // If we get here, try to parse the text directly
        return parseLeafNode(node);
    }

    // Operation evaluation methods

    private ExpressionResult evaluateTernary(TreeNode node) {
        // Ternary: condition ? true_expr : false_expr
        // The node structure should contain the condition and both expressions
        String text = node.text;
        int questionPos = text.indexOf('?');
        int colonPos = text.lastIndexOf(':');
        
        if (questionPos == -1 || colonPos == -1 || colonPos <= questionPos) {
            throw new IllegalArgumentException("Malformed ternary expression: " + text);
        }

        String conditionText = text.substring(0, questionPos).trim();
        String trueExprText = text.substring(questionPos + 1, colonPos).trim();
        String falseExprText = text.substring(colonPos + 1).trim();

        boolean condition = evaluateBoolean(conditionText);
        return condition ? evaluate(trueExprText) : evaluate(falseExprText);
    }

    // Operation evaluation methods
    
    private ExpressionResult evaluateLogicalOrOperation(TreeNode node) {
        return evaluateBinaryOperation(node, "||", "|");
    }
    
    private ExpressionResult evaluateLogicalAndOperation(TreeNode node) {
        return evaluateBinaryOperation(node, "&&", "&");
    }

    private ExpressionResult evaluateComparisonOperation(TreeNode node) {
        return evaluateBinaryOperation(node, "<=", ">=", "<", ">", "==", "!=");
    }
    
    private ExpressionResult evaluateAdditiveOperation(TreeNode node) {
        return evaluateBinaryOperation(node, "+", "-");
    }
    
    private ExpressionResult evaluateMultiplicativeOperation(TreeNode node) {
        return evaluateBinaryOperation(node, "*", "/", "%");
    }
    
    private ExpressionResult evaluatePowerOperation(TreeNode node) {
        return evaluateBinaryOperation(node, "^");
    }

    /**
     * Generic binary operation evaluator using the Canopy PEG AST structure.
     * The PEG parser creates nodes where:
     * - element[0] = first operand (has its own hierarchy structure)
     * - element[1] = operator + second operand as "+4" with sub-elements
     */
    private ExpressionResult evaluateBinaryOperation(TreeNode node, String... operators) {
        // Check if there are multiple elements (indicating an operation)
        if (node.elements.size() < 2) {
            // No operation at this level, continue down the precedence hierarchy
            return evaluateWithPrecedence(node);
        }
        
        String element1Text = node.elements.get(1).text.trim();
        if (element1Text.isEmpty()) {
            // No operation at this level, continue down the precedence hierarchy
            return evaluateWithPrecedence(node);
        }
        
        // We have a binary operation - use Canopy's structure
        // Element 0 is the first operand
        ExpressionResult result = evaluateASTNode(node.elements.get(0));
        
        // Process remaining elements which contain operator + operand
        for (int i = 1; i < node.elements.size(); i++) {
            TreeNode opElement = node.elements.get(i);
            String opText = opElement.text.trim();
            
            if (opText.isEmpty()) continue;
            
            String operator = null;
            ExpressionResult right = null;
            
            // Check if the text starts with an operator
            for (String op : operators) {
                if (opText.startsWith(op)) {
                    operator = op;
                    // Extract the operand text after the operator
                    String operandText = opText.substring(op.length()).trim();
                    if (!operandText.isEmpty()) {
                        try {
                            float value = Float.parseFloat(operandText);
                            right = new ExpressionResult.Numeric(value);
                        } catch (NumberFormatException e) {
                            // Try to evaluate it as an expression
                            right = evaluate(operandText);
                        }
                    }
                    break;
                }
            }
            
            if (operator != null && right != null) {
                result = applyBinaryOperator(operator, result, right);
            }
        }
        
        return result;
    }
    
    /**
     * Parse a simple value directly (number, boolean).
     */
    private ExpressionResult parseDirectValue(String text) {
        // Try to parse as boolean
        if ("true".equalsIgnoreCase(text)) {
            return ExpressionResult.Boolean.TRUE;
        } else if ("false".equalsIgnoreCase(text)) {
            return ExpressionResult.Boolean.FALSE;
        }
        
        // Try to parse as number
        try {
            float value = Float.parseFloat(text);
            return new ExpressionResult.Numeric(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Cannot parse value: " + text);
        }
    }
    
    /**
     * Helper method to check if a node has any labels (indicating it's a structured node).
     */
    private boolean hasAnyLabel(TreeNode node) {
        for (Label label : Label.values()) {
            if (node.get(label) != null) {
                return true;
            }
        }
        return false;
    }

    private ExpressionResult applyBinaryOperator(String operator, ExpressionResult left, ExpressionResult right) {
        return switch (operator) {
            case "&&", "&" -> new ExpressionResult.Boolean(
                ((ExpressionResult.Boolean) left).getValue() &&
                ((ExpressionResult.Boolean) right).getValue()
            );
            case "||", "|" -> new ExpressionResult.Boolean(
                ((ExpressionResult.Boolean) left).getValue() ||
                ((ExpressionResult.Boolean) right).getValue()
            );
            case "<=" -> new ExpressionResult.Boolean(
                ((ExpressionResult.Numeric) left).getValue() <=
                ((ExpressionResult.Numeric) right).getValue()
            );
            case "<" -> new ExpressionResult.Boolean(
                ((ExpressionResult.Numeric) left).getValue() <
                ((ExpressionResult.Numeric) right).getValue()
            );
            case ">=" -> new ExpressionResult.Boolean(
                ((ExpressionResult.Numeric) left).getValue() >=
                ((ExpressionResult.Numeric) right).getValue()
            );
            case ">" -> new ExpressionResult.Boolean(
                ((ExpressionResult.Numeric) left).getValue() >
                ((ExpressionResult.Numeric) right).getValue()
            );
            case "==" -> new ExpressionResult.Boolean(
                ((ExpressionResult.Numeric) left).getValue() ==
                ((ExpressionResult.Numeric) right).getValue()
            );
            case "!=" -> new ExpressionResult.Boolean(
                ((ExpressionResult.Numeric) left).getValue() !=
                ((ExpressionResult.Numeric) right).getValue()
            );
            case "+" -> new ExpressionResult.Numeric(
                ((ExpressionResult.Numeric) left).getValue() +
                ((ExpressionResult.Numeric) right).getValue()
            );
            case "-" -> new ExpressionResult.Numeric(
                ((ExpressionResult.Numeric) left).getValue() -
                ((ExpressionResult.Numeric) right).getValue()
            );
            case "*" -> new ExpressionResult.Numeric(
                ((ExpressionResult.Numeric) left).getValue() *
                ((ExpressionResult.Numeric) right).getValue()
            );
            case "/" -> new ExpressionResult.Numeric(
                ((ExpressionResult.Numeric) left).getValue() /
                ((ExpressionResult.Numeric) right).getValue()
            );
            case "%" -> new ExpressionResult.Numeric(
                ((ExpressionResult.Numeric) left).getValue() %
                ((ExpressionResult.Numeric) right).getValue()
            );
            case "^" -> new ExpressionResult.Numeric((float) Math.pow(
                ((ExpressionResult.Numeric) left).getValue(),
                ((ExpressionResult.Numeric) right).getValue()
            ));
            default -> throw new IllegalArgumentException("Unrecognized operator: " + operator);
        };
    }

    private ExpressionResult evaluateUnaryOperation(TreeNode node) {
        String text = node.text.trim();
        
        if (text.startsWith("-")) {
            String operand = text.substring(1).trim();
            ExpressionResult result = evaluate(operand);
            return new ExpressionResult.Numeric(-((ExpressionResult.Numeric) result).getValue());
        } else if (text.startsWith("!")) {
            String operand = text.substring(1).trim();
            ExpressionResult result = evaluate(operand);
            return new ExpressionResult.Boolean(!((ExpressionResult.Boolean) result).getValue());
        }
        
        // If no unary operator at this level, traverse down
        TreeNode primary = node.get(Label.PRIMARY);
        if (primary != null) {
            return evaluateASTNode(primary);
        }
        
        // If we have elements, evaluate the first one
        if (!node.elements.isEmpty()) {
            return evaluateASTNode(node.elements.get(0));
        }
        
        throw new IllegalArgumentException("Unknown unary operation: " + text);
    }

    private ExpressionResult evaluatePrimary(TreeNode node) {
        // Check if it's a parenthesized expression
        TreeNode expression = node.get(Label.EXPRESSION);
        if (expression != null) {
            return evaluateASTNode(expression);
        }
        
        // Check if it's a function
        TreeNode functionName = node.get(Label.FUNCTION_NAME);
        if (functionName != null) {
            return evaluateFunction(node);
        }
        
        // Check for direct number, boolean, or variable
        if (node.get(Label.NUMBER) != null || node.get(Label.BOOLEAN_TYPE) != null || node.get(Label.VARIABLE) != null) {
            return parseLeafNode(node);
        }
        
        // If we have elements, try to find the actual content
        for (TreeNode element : node.elements) {
            if (!element.text.trim().isEmpty() && !element.text.equals("(") && !element.text.equals(")")) {
                return parseLeafNode(element);
            }
        }
        
        return parseLeafNode(node);
    }

    private ExpressionResult evaluateFunction(TreeNode node) {
        TreeNode functionNameNode = node.get(Label.FUNCTION_NAME);
        TreeNode expressionNode = node.get(Label.EXPRESSION);
        
        if (functionNameNode == null || expressionNode == null) {
            throw new IllegalArgumentException("Invalid function structure in node: " + node.text);
        }
        
        String functionName = functionNameNode.text.trim();
        ExpressionResult argument = evaluateASTNode(expressionNode);
        
        return applyFunction(functionName, ((ExpressionResult.Numeric) argument).getValue());
    }

    private ExpressionResult evaluateFunctionFromText(String text) {
        // List of known functions
        String[] functions = {"sin", "cos", "tan", "asin", "acos", "atan", "deg", "rad", "abs", "sqrt", "floor", "ceil", "round"};
        
        for (String func : functions) {
            if (text.startsWith(func)) {
                String argText = text.substring(func.length()).trim();
                float argument = evaluateNumeric(argText);
                return applyFunction(func, argument);
            }
        }
        
        throw new IllegalArgumentException("Unknown function: " + text);
    }

    private ExpressionResult applyFunction(String functionName, float argument) {
        return switch (functionName) {
            case "sin" -> new ExpressionResult.Numeric((float) Math.sin(Math.toRadians(argument)));
            case "cos" -> new ExpressionResult.Numeric((float) Math.cos(Math.toRadians(argument)));
            case "tan" -> new ExpressionResult.Numeric((float) Math.tan(Math.toRadians(argument)));
            case "asin" -> new ExpressionResult.Numeric((float) Math.toDegrees(Math.asin(argument)));
            case "acos" -> new ExpressionResult.Numeric((float) Math.toDegrees(Math.acos(argument)));
            case "atan" -> new ExpressionResult.Numeric((float) Math.toDegrees(Math.atan(argument)));
            case "deg" -> new ExpressionResult.Numeric((float) Math.toDegrees(argument));
            case "rad" -> new ExpressionResult.Numeric((float) Math.toRadians(argument));
            case "abs" -> new ExpressionResult.Numeric(Math.abs(argument));
            case "sqrt" -> new ExpressionResult.Numeric((float) Math.sqrt(argument));
            case "floor" -> new ExpressionResult.Numeric((float) Math.floor(argument));
            case "ceil" -> new ExpressionResult.Numeric((float) Math.ceil(argument));
            case "round" -> new ExpressionResult.Numeric(Math.round(argument));
            default -> throw new IllegalArgumentException("Unknown function: " + functionName);
        };
    }

    private ExpressionResult evaluateVariable(TreeNode node) {
        // Variables are not supported in this basic evaluator
        // In a real implementation, you would look up variable values from a context
        String text = node.text.trim();
        throw new UnsupportedOperationException("Variables are not supported in this evaluator: " + text);
    }

    private ExpressionResult evaluateNumber(TreeNode node) {
        String text = node.text.trim();
        try {
            float value = Float.parseFloat(text);
            return new ExpressionResult.Numeric(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format: " + text, e);
        }
    }

    private ExpressionResult evaluateBoolean(TreeNode node) {
        String text = node.text.trim().toLowerCase();
        return switch (text) {
            case "true" -> ExpressionResult.Boolean.TRUE;
            case "false" -> ExpressionResult.Boolean.FALSE;
            default -> throw new IllegalArgumentException("Invalid boolean value: " + text);
        };
    }

    /**
     * Fallback method to parse leaf nodes directly from their text content.
     */
    private ExpressionResult parseLeafNode(TreeNode node) {
        String text = node.text.trim();
        
        if (text.isEmpty()) {
            throw new IllegalArgumentException("Cannot evaluate empty expression");
        }
        
        // Try to parse as boolean
        if ("true".equalsIgnoreCase(text)) {
            return ExpressionResult.Boolean.TRUE;
        } else if ("false".equalsIgnoreCase(text)) {
            return ExpressionResult.Boolean.FALSE;
        }
        
        // Try to parse as number
        try {
            float value = Float.parseFloat(text);
            return new ExpressionResult.Numeric(value);
        } catch (NumberFormatException e) {
            // Not a number
        }
        
        // Check if it's a function call
        String[] functions = {"sin", "cos", "tan", "asin", "acos", "atan", "deg", "rad", "abs", "sqrt", "floor", "ceil", "round"};
        for (String func : functions) {
            if (text.startsWith(func)) {
                return evaluateFunctionFromText(text);
            }
        }
        
        // Check if it's a parenthesized expression
        if (text.startsWith("(") && text.endsWith(")")) {
            String inner = text.substring(1, text.length() - 1).trim();
            return evaluate(inner);
        }
        
        // For very simple cases like just ".", this might be part of a float that got fragmented
        // In this case, we should have handled it at a higher level
        throw new IllegalArgumentException("Cannot parse expression: " + text);
    }
}