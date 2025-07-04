package heronarts.lx.structure;

/**
 * Implementation of ExpressionEvaluator that evaluates string-based mathematical 
 * and boolean expressions.
 * 
 * This class supports:
 * - Basic arithmetic: +, -, *, /, %, ^
 * - Boolean logic: &&, ||, !
 * - Comparisons: <, <=, >, >=, ==, !=
 * - Parentheses for grouping
 * - Ternary conditional: condition ? true_expr : false_expr
 * - Unary operators: -, !
 * - Mathematical functions: sin, cos, tan, abs, sqrt, floor, ceil, round, etc.
 * - Proper operator precedence and associativity
 */
public class StringExpressionEvaluator implements ExpressionEvaluator {

    private static final String[][] EXPRESSION_OPERATORS = {
        { "||", "|" }, // Both forms are logical, not bitwise
        { "&&", "&" }, // Both forms are logical, not bitwise
        { "<=", ">=", "<", ">" },
        { "==", "!=" },
        { "+", "-" },
        { "*", "/", "%" },
        { "^" }
    };

    private enum SimpleFunction {
        sin(f -> { return (float) Math.sin(Math.toRadians(f)); }),
        cos(f -> { return (float) Math.cos(Math.toRadians(f)); }),
        tan(f -> { return (float) Math.tan(Math.toRadians(f)); }),
        asin(f -> { return (float) Math.toDegrees(Math.asin(f)); }),
        acos(f -> { return (float) Math.toDegrees(Math.acos(f)); }),
        atan(f -> { return (float) Math.toDegrees(Math.atan(f)); }),
        deg(f -> { return (float) Math.toDegrees(f); }),
        rad(f -> { return (float) Math.toRadians(f); }),
        abs(f -> { return Math.abs(f); }),
        sqrt(f -> { return (float) Math.sqrt(f); }),
        floor(f -> { return (float) Math.floor(f); }),
        ceil(f -> { return (float) Math.ceil(f); }),
        round(f -> { return Math.round(f); });

        private interface Compute {
            public float compute(float f);
        }
        private final Compute compute;
        private SimpleFunction(Compute compute) {
            this.compute = compute;
        }
    }

    private static final String OPERATOR_CHARS = "^*/+-%<>=!&|";

    @Override
    public float evaluateNumeric(String expression) {
        if (evaluate(expression) instanceof ExpressionResult.Numeric numeric) {
            return numeric.getValue();
        }
        throw new IllegalArgumentException("Expected expression to be numeric: " + expression);
    }

    @Override
    public boolean evaluateBoolean(String expression) {
        if (evaluate(expression) instanceof ExpressionResult.Boolean bool) {
            return bool.getValue();
        }
        throw new IllegalArgumentException("Expected expression to be boolean: " + expression);
    }

    @Override
    public ExpressionResult evaluate(String expression) {
        char[] chars = expression.toCharArray();

        // Parentheses pass
        int openParen = -1;
        for (int i = 0; i < chars.length; ++i) {
            if (chars[i] == '(') {
                openParen = i;
            } else if (chars[i] == ')') {
                if (openParen < 0) {
                    throw new IllegalArgumentException("Mismatched parentheses in expression: " + expression);
                }

                // Whenever we find a closed paren, evaluate just this one parenthetical.
                // This will naturally work from in->out on nesting, since every closed-paren
                // catches the open-paren that was closest to it.
                ExpressionResult result = evaluate(expression.substring(openParen+1, i));
                if ((openParen == 0) && (i == chars.length-1)) {
                    // Whole thing in parentheses? Just return!
                    return result;
                }

                // Evaluate expression recursively with this parenthetical removed
                return evaluate(
                    expression.substring(0, openParen) +
                    result.toString() +
                    expression.substring(i + 1)
                );
            }
        }

        // Ternary conditional, lowest precedence, right->left associative
        final int condition = expression.indexOf('?');
        if (condition > 0) {
            final int end = expression.lastIndexOf(':');
            if (end <= condition) {
                throw new IllegalArgumentException("Mismatched ternary conditional ?: in expression: " + expression);
            }
            return evaluateBoolean(expression.substring(0, condition)) ?
                evaluate(expression.substring(condition+1, end)) :
                evaluate(expression.substring(end+1));
        }

        // Left->right associative operators, working up the precedence ladder
        for (String[] operators : EXPRESSION_OPERATORS) {
            int lastIndex = -1;
            String operator = null;
            for (String candidate : operators) {
                int candidateIndex = getOperatorIndex(expression, chars, candidate);
                if (candidateIndex > lastIndex) {
                    operator = candidate;
                    lastIndex = candidateIndex;
                }
            }
            if (operator != null) {
                String left = expression.substring(0, lastIndex);
                String right = expression.substring(lastIndex + operator.length());
                return switch (operator) {
                    case "&&", "&" -> new ExpressionResult.Boolean(
                        evaluateBoolean(left) &&
                        evaluateBoolean(right)
                    );
                    case "||", "|" -> new ExpressionResult.Boolean(
                        evaluateBoolean(left) ||
                        evaluateBoolean(right)
                    );
                    case "<=" -> new ExpressionResult.Boolean(
                        evaluateNumeric(left) <=
                        evaluateNumeric(right)
                    );
                    case "<" -> new ExpressionResult.Boolean(
                        evaluateNumeric(left) <
                        evaluateNumeric(right)
                    );
                    case ">=" -> new ExpressionResult.Boolean(
                        evaluateNumeric(left) >=
                        evaluateNumeric(right)
                    );
                    case ">" -> new ExpressionResult.Boolean(
                        evaluateNumeric(left) >
                        evaluateNumeric(right)
                    );
                    case "==" -> new ExpressionResult.Boolean(
                        evaluateNumeric(left) ==
                        evaluateNumeric(right)
                    );
                    case "!=" -> new ExpressionResult.Boolean(
                        evaluateNumeric(left) !=
                        evaluateNumeric(right)
                    );
                    case "+" -> new ExpressionResult.Numeric(
                        evaluateNumeric(left) +
                        evaluateNumeric(right)
                    );
                    case "-" -> new ExpressionResult.Numeric(
                        evaluateNumeric(left) -
                        evaluateNumeric(right)
                    );
                    case "*" -> new ExpressionResult.Numeric(
                        evaluateNumeric(left) *
                        evaluateNumeric(right)
                    );
                    case "/" -> new ExpressionResult.Numeric(
                        evaluateNumeric(left) /
                        evaluateNumeric(right)
                    );
                    case "%" -> new ExpressionResult.Numeric(
                        evaluateNumeric(left) %
                        evaluateNumeric(right)
                    );
                    case "^" -> new ExpressionResult.Numeric((float) Math.pow(
                        evaluateNumeric(left),
                        evaluateNumeric(right)
                    ));

                    default -> throw new IllegalStateException("Unrecognized operator: " + operator);
                };
            }
        }

        // Dreaded nasty unary operators!
        String trimmed = expression.trim();
        if (!trimmed.isEmpty()) {
            final char unary = trimmed.charAt(0);
            if (unary == '-') {
                // Float.parseFloat() would handle one of these fine, but it won't handle
                // them potentially stacking up at the front, e.g. if multiple expression
                // resolutions have resulted in something like ---4, so do the negations
                // manually one by one
                return new ExpressionResult.Numeric(-evaluateNumeric(expression.substring(1)));
            } else if (unary == '!') {
                return new ExpressionResult.Boolean(!evaluateBoolean(expression.substring(1)));
            }

            // Check for simple function operators
            for (SimpleFunction function : SimpleFunction.values()) {
                final String name = function.name();
                if (trimmed.startsWith(name)) {
                    float argument = evaluateNumeric(expression.substring(name.length()));
                    return new ExpressionResult.Numeric(function.compute.compute(argument));
                }
            }
        }

        // Sort out what we got here
        return switch (trimmed.toLowerCase()) {
            case "" -> throw new IllegalArgumentException("Cannot evaluate empty expression: " + expression);
            case "true" -> ExpressionResult.Boolean.TRUE;
            case "false" -> ExpressionResult.Boolean.FALSE;
            default -> new ExpressionResult.Numeric(Float.parseFloat(trimmed));
        };
    }

    private static boolean isUnaryMinus(char[] chars, int index) {
        // Check it's actually a minus
        if (chars[index] != '-') {
            return false;
        }

        // If at the very front of the thing, it's unary!
        if (index == 0) {
            return true;
        }

        // Check if preceded by another simple operator, e.g. 4+-4
        if (OPERATOR_CHARS.indexOf(chars[index-1]) >= 0) {
            return true;
        }
        // Check if preceded by a simple function token, which will no longer have
        // parentheses, e.g. sin(-4) will have become sin-4 after parenthetical resolution
        for (SimpleFunction function : SimpleFunction.values()) {
            final String name = function.name();
            final int len = name.length();
            if ((index >= len) && new String(chars, index-len, len).equals(name)) {
                return true;
            }
        }
        // Check if preceded by a conditional operator
        if (chars[index-1] == '(' || chars[index-1] == '?' || chars[index-1] == ':') {
            return true;
        }
        return false;
    }

    private static int getOperatorIndex(String expression, char[] chars, String operator) {
        if ("-".equals(operator)) {
            for (int index = chars.length - 1; index > 0; --index) {
                // Skip over the tricky unary minus operator! If preceded by another operator,
                // then it's actually just a negative sign which will be handled later. Do not
                // treat it as a binary subtraction operator.
                if (chars[index] == '-' && !isUnaryMinus(chars, index)) {
                    return index;
                }
            }
            return -1;
        } else {
            return expression.lastIndexOf(operator);
        }
    }
}