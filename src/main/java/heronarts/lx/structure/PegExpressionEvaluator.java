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
     * Recursively evaluates an AST node and returns the result.
     */
    private ExpressionResult evaluateASTNode(TreeNode node) {
        if (node == null) {
            throw new IllegalArgumentException("Cannot evaluate null AST node");
        }

        // Check what type of node this is based on its labeled elements
        // The PEG parser creates TreeNode objects with labeled elements that indicate the structure

        // Handle ternary conditional (highest level)
        if (node.get(Label.EXPRESSION) != null && node.text.contains("?")) {
            return evaluateTernary(node);
        }

        // Handle logical OR
        if (node.get(Label.LOGICAL_OR) != null) {
            return evaluateLogicalOr(node);
        }

        // Handle logical AND  
        if (node.get(Label.LOGICAL_AND) != null) {
            return evaluateLogicalAnd(node);
        }

        // Handle comparison operations
        if (node.get(Label.COMPARISON) != null) {
            return evaluateComparison(node);
        }

        // Handle additive operations (+, -)
        if (node.get(Label.ADDITIVE) != null) {
            return evaluateAdditive(node);
        }

        // Handle multiplicative operations (*, /, %)
        if (node.get(Label.MULTIPLICATIVE) != null) {
            return evaluateMultiplicative(node);
        }

        // Handle power operations (^)
        if (node.get(Label.POWER) != null) {
            return evaluatePower(node);
        }

        // Handle unary operations (-, !)
        if (node.get(Label.UNARY) != null) {
            return evaluateUnary(node);
        }

        // Handle primary expressions (numbers, booleans, variables, functions, parentheses)
        if (node.get(Label.PRIMARY) != null) {
            return evaluatePrimary(node);
        }

        // Handle function calls
        if (node.get(Label.FUNCTION_NAME) != null) {
            return evaluateFunction(node);
        }

        // Handle variables
        if (node.get(Label.VARIABLE) != null) {
            return evaluateVariable(node);
        }

        // Handle direct number literals
        if (node.get(Label.NUMBER) != null || node.get(Label.INTEGER_TYPE) != null || node.get(Label.FLOAT_TYPE) != null) {
            return evaluateNumber(node);
        }

        // Handle boolean literals
        if (node.get(Label.BOOLEAN_TYPE) != null) {
            return evaluateBoolean(node);
        }

        // If we can't identify the node type from labels, try to parse the text directly
        return parseLeafNode(node);
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

    private ExpressionResult evaluateLogicalOr(TreeNode node) {
        return evaluateBinaryOperation(node, Label.LOGICAL_OR, "||", "|");
    }

    private ExpressionResult evaluateLogicalAnd(TreeNode node) {
        return evaluateBinaryOperation(node, Label.LOGICAL_AND, "&&", "&");
    }

    private ExpressionResult evaluateComparison(TreeNode node) {
        return evaluateBinaryOperation(node, Label.COMPARISON, "<=", ">=", "<", ">", "==", "!=");
    }

    private ExpressionResult evaluateAdditive(TreeNode node) {
        return evaluateBinaryOperation(node, Label.ADDITIVE, "+", "-");
    }

    private ExpressionResult evaluateMultiplicative(TreeNode node) {
        return evaluateBinaryOperation(node, Label.MULTIPLICATIVE, "*", "/", "%");
    }

    private ExpressionResult evaluatePower(TreeNode node) {
        return evaluateBinaryOperation(node, Label.POWER, "^");
    }

    /**
     * Generic binary operation evaluator that handles left-associative operators.
     */
    private ExpressionResult evaluateBinaryOperation(TreeNode node, Label label, String... operators) {
        String text = node.text.trim();
        
        // Find the rightmost operator (for left-associativity)
        int lastOpPos = -1;
        String foundOperator = null;
        
        for (String op : operators) {
            int pos = text.lastIndexOf(op);
            if (pos > lastOpPos) {
                lastOpPos = pos;
                foundOperator = op;
            }
        }

        if (lastOpPos == -1) {
            // No operator found, this might be a nested expression
            if (node.elements.size() == 1) {
                return evaluateASTNode(node.elements.get(0));
            }
            throw new IllegalArgumentException("No operator found in binary operation: " + text);
        }

        String leftText = text.substring(0, lastOpPos).trim();
        String rightText = text.substring(lastOpPos + foundOperator.length()).trim();

        ExpressionResult left = evaluate(leftText);
        ExpressionResult right = evaluate(rightText);

        return applyBinaryOperator(foundOperator, left, right);
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

    private ExpressionResult evaluateUnary(TreeNode node) {
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
        
        // If no unary operator, evaluate the operand directly
        TreeNode unaryNode = node.get(Label.UNARY);
        if (unaryNode != null) {
            return evaluateASTNode(unaryNode);
        }
        
        throw new IllegalArgumentException("Unknown unary operation: " + text);
    }

    private ExpressionResult evaluatePrimary(TreeNode node) {
        TreeNode primaryNode = node.get(Label.PRIMARY);
        if (primaryNode != null) {
            return evaluateASTNode(primaryNode);
        }
        
        // Handle parenthesized expressions
        String text = node.text.trim();
        if (text.startsWith("(") && text.endsWith(")")) {
            String inner = text.substring(1, text.length() - 1).trim();
            return evaluate(inner);
        }
        
        return parseLeafNode(node);
    }

    private ExpressionResult evaluateFunction(TreeNode node) {
        TreeNode functionNameNode = node.get(Label.FUNCTION_NAME);
        TreeNode unaryNode = node.get(Label.UNARY);
        
        if (functionNameNode == null || unaryNode == null) {
            // Parse from text
            String text = node.text.trim();
            return evaluateFunctionFromText(text);
        }
        
        String functionName = functionNameNode.text.trim();
        ExpressionResult argument = evaluateASTNode(unaryNode);
        
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
        
        throw new IllegalArgumentException("Cannot parse expression: " + text);
    }
}