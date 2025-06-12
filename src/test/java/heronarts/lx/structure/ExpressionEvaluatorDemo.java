package heronarts.lx.structure;

/**
 * Demo program to test JsonFixture expression evaluation functionality.
 * This class contains a copy of the expression evaluation logic and manual tests.
 * 
 * The test covers expressions like those found in LXF files:
 * - "($offset+(($row-1)/2))*$pointSpacing" 
 * - "($row-1)*$rowSpacing"
 * - "$flipBacking ? -2 : 2"
 */
public class ExpressionEvaluatorDemo {

    // Copy of the expression evaluation logic for testing
    private static abstract class ExpressionResult {
        private static class Numeric extends ExpressionResult {
            private final float number;
            private Numeric(float number) {
                this.number = number;
            }
            @Override
            public String toString() {
                return String.valueOf(this.number);
            }
        }

        private static class Boolean extends ExpressionResult {
            private static final Boolean TRUE = new Boolean(true);
            private static final Boolean FALSE = new Boolean(false);
            private final boolean bool;
            private Boolean(boolean bool) {
                this.bool = bool;
            }
            @Override
            public String toString() {
                return String.valueOf(this.bool);
            }
        }
    }

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

    private static boolean isUnaryMinus(char[] chars, int index) {
        if (chars[index] != '-') {
            return false;
        }
        if (index == 0) {
            return true;
        }
        if (OPERATOR_CHARS.indexOf(chars[index-1]) >= 0) {
            return true;
        }
        for (SimpleFunction function : SimpleFunction.values()) {
            final String name = function.name();
            final int len = name.length();
            if ((index >= len) && new String(chars, index-len, len).equals(name)) {
                return true;
            }
        }
        if (chars[index-1] == '(' || chars[index-1] == '?' || chars[index-1] == ':') {
            return true;
        }
        return false;
    }

    private static int getOperatorIndex(String expression, char[] chars, String operator) {
        if ("-".equals(operator)) {
            for (int index = chars.length - 1; index > 0; --index) {
                if (chars[index] == '-' && !isUnaryMinus(chars, index)) {
                    return index;
                }
            }
            return -1;
        } else {
            return expression.lastIndexOf(operator);
        }
    }

    private static ExpressionResult evaluateExpression(String expression) {
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
                ExpressionResult result = evaluateExpression(expression.substring(openParen+1, i));
                if ((openParen == 0) && (i == chars.length-1)) {
                    return result;
                }
                return evaluateExpression(
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
            return evaluateBooleanExpression(expression.substring(0, condition)) ?
                evaluateExpression(expression.substring(condition+1, end)) :
                evaluateExpression(expression.substring(end+1));
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
                        evaluateBooleanExpression(left) &&
                        evaluateBooleanExpression(right)
                    );
                    case "||", "|" -> new ExpressionResult.Boolean(
                        evaluateBooleanExpression(left) ||
                        evaluateBooleanExpression(right)
                    );
                    case "<=" -> new ExpressionResult.Boolean(
                        evaluateNumericExpression(left) <=
                        evaluateNumericExpression(right)
                    );
                    case "<" -> new ExpressionResult.Boolean(
                        evaluateNumericExpression(left) <
                        evaluateNumericExpression(right)
                    );
                    case ">=" -> new ExpressionResult.Boolean(
                        evaluateNumericExpression(left) >=
                        evaluateNumericExpression(right)
                    );
                    case ">" -> new ExpressionResult.Boolean(
                        evaluateNumericExpression(left) >
                        evaluateNumericExpression(right)
                    );
                    case "==" -> new ExpressionResult.Boolean(
                        evaluateNumericExpression(left) ==
                        evaluateNumericExpression(right)
                    );
                    case "!=" -> new ExpressionResult.Boolean(
                        evaluateNumericExpression(left) !=
                        evaluateNumericExpression(right)
                    );
                    case "+" -> new ExpressionResult.Numeric(
                        evaluateNumericExpression(left) +
                        evaluateNumericExpression(right)
                    );
                    case "-" -> new ExpressionResult.Numeric(
                        evaluateNumericExpression(left) -
                        evaluateNumericExpression(right)
                    );
                    case "*" -> new ExpressionResult.Numeric(
                        evaluateNumericExpression(left) *
                        evaluateNumericExpression(right)
                    );
                    case "/" -> new ExpressionResult.Numeric(
                        evaluateNumericExpression(left) /
                        evaluateNumericExpression(right)
                    );
                    case "%" -> new ExpressionResult.Numeric(
                        evaluateNumericExpression(left) %
                        evaluateNumericExpression(right)
                    );
                    case "^" -> new ExpressionResult.Numeric((float) Math.pow(
                        evaluateNumericExpression(left),
                        evaluateNumericExpression(right)
                    ));
                    default -> throw new IllegalStateException("Unrecognized operator: " + operator);
                };
            }
        }

        // Unary operators
        String trimmed = expression.trim();
        if (!trimmed.isEmpty()) {
            final char unary = trimmed.charAt(0);
            if (unary == '-') {
                return new ExpressionResult.Numeric(-evaluateNumericExpression(expression.substring(1)));
            } else if (unary == '!') {
                return new ExpressionResult.Boolean(!evaluateBooleanExpression(expression.substring(1)));
            }

            // Check for simple function operators
            for (SimpleFunction function : SimpleFunction.values()) {
                final String name = function.name();
                if (trimmed.startsWith(name)) {
                    float argument = evaluateNumericExpression(expression.substring(name.length()));
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

    private static float evaluateNumericExpression(String expression) {
        if (evaluateExpression(expression) instanceof ExpressionResult.Numeric numeric) {
            return numeric.number;
        }
        throw new IllegalArgumentException("Expected expression to be numeric: " + expression);
    }

    private static boolean evaluateBooleanExpression(String expression) {
        if (evaluateExpression(expression) instanceof ExpressionResult.Boolean bool) {
            return bool.bool;
        }
        throw new IllegalArgumentException("Expected expression to be boolean: " + expression);
    }

    public static void test(String expression, float expected) {
        try {
            float result = evaluateNumericExpression(expression);
            boolean passed = Math.abs(result - expected) < 0.0001f;
            System.out.printf("Test: %-40s Expected: %10.5f Got: %10.5f %s%n", 
                "\"" + expression + "\"", expected, result, passed ? "PASS" : "FAIL");
        } catch (Exception e) {
            System.out.printf("Test: %-40s Expected: %10.5f Got: ERROR: %s%n", 
                "\"" + expression + "\"", expected, e.getMessage());
        }
    }

    public static void test(String expression, boolean expected) {
        try {
            boolean result = evaluateBooleanExpression(expression);
            boolean passed = result == expected;
            System.out.printf("Test: %-40s Expected: %10s Got: %10s %s%n", 
                "\"" + expression + "\"", expected, result, passed ? "PASS" : "FAIL");
        } catch (Exception e) {
            System.out.printf("Test: %-40s Expected: %10s Got: ERROR: %s%n", 
                "\"" + expression + "\"", expected, e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("JsonFixture Expression Evaluator Test Suite");
        System.out.println("============================================\n");

        System.out.println("Basic Arithmetic:");
        test("3+4", 7.0f);
        test("3-4", -1.0f);
        test("3*4", 12.0f);
        test("3/4", 0.75f);
        test("3%2", 1.0f);
        test("3^2", 9.0f);

        System.out.println("\nOperator Precedence:");
        test("2+3*4", 14.0f);
        test("(2+3)*4", 20.0f);
        test("3+2^3", 11.0f);
        test("(3+2)^3", 125.0f);

        System.out.println("\nParentheses:");
        test("((((42))))", 42.0f);
        test("2*(3+(4*5))", 50.0f);
        test("2*((3+4)*2-3)", 22.0f);

        System.out.println("\nComparisons:");
        test("5>3", true);
        test("3>5", false);
        test("5>=5", true);
        test("3<5", true);
        test("5==5", true);
        test("5!=3", true);

        System.out.println("\nLogical Operations:");
        test("true&&true", true);
        test("true&&false", false);
        test("true||false", true);
        test("false||false", false);

        System.out.println("\nUnary Operators:");
        test("-5", -5.0f);
        test("--5", 5.0f);
        test("---5", -5.0f);
        test("!true", false);
        test("!false", true);

        System.out.println("\nSimple Functions:");
        test("sin0", 0.0f);
        test("cos0", 1.0f);
        test("abs-5", 5.0f);
        test("sqrt9", 3.0f);
        test("floor2.7", 2.0f);
        test("ceil2.3", 3.0f);
        test("round2.7", 3.0f);

        System.out.println("\nTernary Conditional:");
        test("true?10:20", 10.0f);
        test("false?10:20", 20.0f);
        test("3>2?5:7", 5.0f);
        test("2>3?5:7", 7.0f);

        System.out.println("\nLXF-Like Expressions:");
        // Test expression like: ($offset+(($row-1)/2))*$pointSpacing
        // Simulate: offset=0, row=2, pointSpacing=1.9685039
        test("(0+((2-1)/2))*1.9685039", 0.98425195f);
        
        // Simulate: offset=1, row=3, pointSpacing=1.9685039
        test("(1+((3-1)/2))*1.9685039", 3.9370078f);
        
        // Test expression like: ($row-1)*$rowSpacing
        // Simulate: row=2, rowSpacing=1.7047743848
        test("(2-1)*1.7047743848", 1.7047743848f);
        
        // Test expression like: ($offset+(($row-1)/2)-0.5)*$pointSpacing
        // Simulate: offset=0, row=2, pointSpacing=1.9685039
        test("(0+((2-1)/2)-0.5)*1.9685039", 0.0f);
        
        // Test ternary expression like: $flipBacking ? -2 : 2
        test("true?-2:2", -2.0f);
        test("false?-2:2", 2.0f);

        System.out.println("\nWhitespace Handling:");
        test("3 + 4", 7.0f);
        test("  3  *  4  ", 12.0f);
        test(" true && true ", true);

        System.out.println("\nError Cases:");
        try {
            evaluateExpression("");
            System.out.println("Test: \"\" - Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            System.out.println("Test: \"\" - Correctly threw: " + e.getMessage());
        }

        try {
            evaluateExpression("(2+3");
            System.out.println("Test: \"(2+3\" - Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            System.out.println("Test: \"(2+3\" - Correctly threw: " + e.getMessage());
        }

        try {
            evaluateExpression("abc");
            System.out.println("Test: \"abc\" - Should have thrown NumberFormatException");
        } catch (NumberFormatException e) {
            System.out.println("Test: \"abc\" - Correctly threw: " + e.getMessage());
        }

        System.out.println("\nTest suite completed!");
    }
}