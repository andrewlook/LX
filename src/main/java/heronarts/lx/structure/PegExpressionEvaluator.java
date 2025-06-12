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
            if (hasLogicalOrOperation(logicalOr)) {
                return evaluateLogicalOrOperation(logicalOr);
            }
            return evaluateASTNode(logicalOr);
        }
        
        // LOGICAL_AND level
        TreeNode logicalAnd = node.get(Label.LOGICAL_AND);
        if (logicalAnd != null) {
            if (hasLogicalAndOperation(logicalAnd)) {
                return evaluateLogicalAndOperation(logicalAnd);
            }
            return evaluateASTNode(logicalAnd);
        }
        
        // COMPARISON level
        TreeNode comparison = node.get(Label.COMPARISON);
        if (comparison != null) {
            if (hasComparisonOperation(comparison)) {
                return evaluateComparisonOperation(comparison);
            }
            return evaluateASTNode(comparison);
        }
        
        // ADDITIVE level
        TreeNode additive = node.get(Label.ADDITIVE);
        if (additive != null) {
            if (hasAdditiveOperation(additive)) {
                return evaluateAdditiveOperation(additive);
            }
            return evaluateASTNode(additive);
        }
        
        // MULTIPLICATIVE level
        TreeNode multiplicative = node.get(Label.MULTIPLICATIVE);
        if (multiplicative != null) {
            if (hasMultiplicativeOperation(multiplicative)) {
                return evaluateMultiplicativeOperation(multiplicative);
            }
            return evaluateASTNode(multiplicative);
        }
        
        // POWER level
        TreeNode power = node.get(Label.POWER);
        if (power != null) {
            if (hasPowerOperation(power)) {
                return evaluatePowerOperation(power);
            }
            return evaluateASTNode(power);
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

    // Helper methods to detect operations at each level
    
    private boolean hasLogicalOrOperation(TreeNode node) {
        if (node.elements.size() < 2) return false;
        
        TreeNode secondElement = node.elements.get(1);
        return secondElement != null && !secondElement.text.trim().isEmpty() && 
               (secondElement.text.contains("||") || secondElement.text.contains("|"));
    }
    
    private boolean hasLogicalAndOperation(TreeNode node) {
        if (node.elements.size() < 2) return false;
        
        TreeNode secondElement = node.elements.get(1);
        return secondElement != null && !secondElement.text.trim().isEmpty() && 
               (secondElement.text.contains("&&") || secondElement.text.contains("&"));
    }
    
    private boolean hasComparisonOperation(TreeNode node) {
        if (node.elements.size() < 2) return false;
        
        TreeNode secondElement = node.elements.get(1);
        return secondElement != null && !secondElement.text.trim().isEmpty() && 
               (secondElement.text.contains("<=") || secondElement.text.contains(">=") || 
                secondElement.text.contains("<") || secondElement.text.contains(">") || 
                secondElement.text.contains("==") || secondElement.text.contains("!="));
    }
    
    private boolean hasAdditiveOperation(TreeNode node) {
        // For additive operations, look for the second element starting with + or -
        if (node.elements.size() < 2) return false;
        
        TreeNode secondElement = node.elements.get(1);
        return secondElement != null && !secondElement.text.trim().isEmpty() && 
               (secondElement.text.startsWith("+") || secondElement.text.startsWith("-"));
    }
    
    private boolean hasMultiplicativeOperation(TreeNode node) {
        if (node.elements.size() < 2) return false;
        
        TreeNode secondElement = node.elements.get(1);
        return secondElement != null && !secondElement.text.trim().isEmpty() && 
               (secondElement.text.startsWith("*") || secondElement.text.startsWith("/") || 
                secondElement.text.startsWith("%"));
    }
    
    private boolean hasPowerOperation(TreeNode node) {
        if (node.elements.size() < 2) return false;
        
        TreeNode secondElement = node.elements.get(1);
        return secondElement != null && !secondElement.text.trim().isEmpty() && 
               secondElement.text.startsWith("^");
    }

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
     * Generic binary operation evaluator that handles left-associative operators.
     * Works directly with AST nodes instead of re-parsing text.
     */
    private ExpressionResult evaluateBinaryOperation(TreeNode node, String... operators) {
        // With the new grammar, binary operations have a cleaner structure:
        // elements[0] = left operand (as AST node)
        // elements[1] = operator + right operand structure
        
        if (node.elements.size() < 2) {
            // No operation, just evaluate the single element
            if (node.elements.size() == 1) {
                return evaluateASTNode(node.elements.get(0));
            }
            throw new IllegalArgumentException("No operands found in binary operation: " + node.text);
        }
        
        // Left operand is the first element
        ExpressionResult left = evaluateASTNode(node.elements.get(0));
        
        // Process all operator + operand pairs (for left-associativity)
        ExpressionResult result = left;
        
        for (int i = 1; i < node.elements.size(); i++) {
            TreeNode operatorElement = node.elements.get(i);
            
            // Find the operator and right operand in this element
            String operator = null;
            TreeNode rightOperand = null;
            
            // Look through the elements to find operator and operand
            // The structure is: [SPC, operator, SPC, operand] or similar
            for (TreeNode subElement : operatorElement.elements) {
                String text = subElement.text.trim();
                if (!text.isEmpty()) {
                    boolean isOperator = false;
                    for (String op : operators) {
                        if (text.equals(op)) {
                            operator = op;
                            isOperator = true;
                            break;
                        }
                    }
                    
                    if (!isOperator && rightOperand == null && !text.matches("\\s+")) {
                        // This is the right operand - could be a simple text or a complex node
                        rightOperand = subElement;
                    }
                }
            }
            
            // If we didn't find a direct text operator, the structure might be nested
            if (operator == null || rightOperand == null) {
                // Look for nested structures that contain the operator and operand
                for (TreeNode subElement : operatorElement.elements) {
                    if (subElement.elements.size() > 0) {
                        // Try to parse this as a nested structure
                        for (TreeNode nestedElement : subElement.elements) {
                            String text = nestedElement.text.trim();
                            if (!text.isEmpty()) {
                                for (String op : operators) {
                                    if (text.equals(op)) {
                                        operator = op;
                                        break;
                                    }
                                }
                                
                                // Look for the operand node (has labels like POWER, UNARY, etc.)
                                if (rightOperand == null && hasAnyLabel(nestedElement)) {
                                    rightOperand = nestedElement;
                                }
                            }
                        }
                    }
                }
            }
            
            if (operator == null || rightOperand == null) {
                throw new IllegalArgumentException("Could not find operator and operand in: " + operatorElement.text);
            }
            
            ExpressionResult right = evaluateASTNode(rightOperand);
            result = applyBinaryOperator(operator, result, right);
        }
        
        return result;
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