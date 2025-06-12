package heronarts.lx.structure;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JsonFixture expression evaluation functionality.
 * This class contains a copy of the expression evaluation logic for testing purposes.
 * 
 * The test covers expressions like those found in LXF files:
 * - "($offset+(($row-1)/2))*$pointSpacing" 
 * - "($row-1)*$rowSpacing"
 * - "$flipBacking ? -2 : 2"
 */
public class ExpressionEvaluatorTest {

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

    // Test methods

    @Test
    @DisplayName("Test basic numeric literals")
    void testNumericLiterals() {
        assertEquals(42.0f, evaluateNumericExpression("42"));
        assertEquals(3.14f, evaluateNumericExpression("3.14"));
        assertEquals(-5.0f, evaluateNumericExpression("-5"));
        assertEquals(0.0f, evaluateNumericExpression("0"));
    }

    @Test
    @DisplayName("Test boolean literals")
    void testBooleanLiterals() {
        assertTrue(evaluateBooleanExpression("true"));
        assertFalse(evaluateBooleanExpression("false"));
    }

    @Test
    @DisplayName("Test basic arithmetic operations")
    void testBasicArithmetic() {
        assertEquals(7.0f, evaluateNumericExpression("3+4"));
        assertEquals(-1.0f, evaluateNumericExpression("3-4"));
        assertEquals(12.0f, evaluateNumericExpression("3*4"));
        assertEquals(0.75f, evaluateNumericExpression("3/4"));
        assertEquals(1.0f, evaluateNumericExpression("3%2"));
        assertEquals(9.0f, evaluateNumericExpression("3^2"));
    }

    @Test
    @DisplayName("Test operator precedence")
    void testOperatorPrecedence() {
        assertEquals(14.0f, evaluateNumericExpression("2+3*4"));
        assertEquals(20.0f, evaluateNumericExpression("(2+3)*4"));
        assertEquals(11.0f, evaluateNumericExpression("3+2^3"));
        assertEquals(125.0f, evaluateNumericExpression("(3+2)^3"));
    }

    @Test
    @DisplayName("Test nested parentheses")
    void testNestedParentheses() {
        assertEquals(42.0f, evaluateNumericExpression("((((42))))"));
        assertEquals(50.0f, evaluateNumericExpression("2*(3+(4*5))"));
        assertEquals(22.0f, evaluateNumericExpression("2*((3+4)*2-3)"));
    }

    @Test
    @DisplayName("Test comparison operations")
    void testComparisonOperations() {
        assertTrue(evaluateBooleanExpression("5>3"));
        assertFalse(evaluateBooleanExpression("3>5"));
        assertTrue(evaluateBooleanExpression("5>=5"));
        assertTrue(evaluateBooleanExpression("3<5"));
        assertFalse(evaluateBooleanExpression("5<3"));
        assertTrue(evaluateBooleanExpression("3<=3"));
        assertTrue(evaluateBooleanExpression("5==5"));
        assertFalse(evaluateBooleanExpression("5==3"));
        assertTrue(evaluateBooleanExpression("5!=3"));
        assertFalse(evaluateBooleanExpression("5!=5"));
    }

    @Test
    @DisplayName("Test logical operations")
    void testLogicalOperations() {
        assertTrue(evaluateBooleanExpression("true&&true"));
        assertFalse(evaluateBooleanExpression("true&&false"));
        assertTrue(evaluateBooleanExpression("true||false"));
        assertFalse(evaluateBooleanExpression("false||false"));
        
        // Test both forms of logical operators
        assertTrue(evaluateBooleanExpression("true&true"));
        assertTrue(evaluateBooleanExpression("true|false"));
    }

    @Test
    @DisplayName("Test unary operators")
    void testUnaryOperators() {
        assertEquals(-5.0f, evaluateNumericExpression("-5"));
        assertEquals(5.0f, evaluateNumericExpression("--5"));
        assertEquals(-5.0f, evaluateNumericExpression("---5"));
        
        assertFalse(evaluateBooleanExpression("!true"));
        assertTrue(evaluateBooleanExpression("!false"));
        assertTrue(evaluateBooleanExpression("!!true"));
    }

    @Test
    @DisplayName("Test simple functions")
    void testSimpleFunctions() {
        assertEquals(0.0f, evaluateNumericExpression("sin0"), 0.0001f);
        assertEquals(1.0f, evaluateNumericExpression("cos0"), 0.0001f);
        assertEquals(5.0f, evaluateNumericExpression("abs-5"));
        assertEquals(3.0f, evaluateNumericExpression("sqrt9"));
        assertEquals(2.0f, evaluateNumericExpression("floor2.7"));
        assertEquals(3.0f, evaluateNumericExpression("ceil2.3"));
        assertEquals(3.0f, evaluateNumericExpression("round2.7"));
    }

    @Test
    @DisplayName("Test ternary conditional operator")
    void testTernaryConditional() {
        assertEquals(10.0f, evaluateNumericExpression("true?10:20"));
        assertEquals(20.0f, evaluateNumericExpression("false?10:20"));
        assertEquals(5.0f, evaluateNumericExpression("3>2?5:7"));
        assertEquals(7.0f, evaluateNumericExpression("2>3?5:7"));
        
        // Nested ternary
        assertEquals(1.0f, evaluateNumericExpression("true?true?1:2:3"));
        assertEquals(2.0f, evaluateNumericExpression("true?false?1:2:3"));
        assertEquals(3.0f, evaluateNumericExpression("false?true?1:2:3"));
    }

    @Test
    @DisplayName("Test complex expressions similar to LXF file")
    void testLXFLikeExpressions() {
        // Test expression like: ($offset+(($row-1)/2))*$pointSpacing
        // We'll simulate with actual numbers since variables aren't available in this context
        
        // Simulate: offset=0, row=2, pointSpacing=1.9685039
        // (0+((2-1)/2))*1.9685039 = (0+(1/2))*1.9685039 = 0.5*1.9685039 = 0.98425195
        assertEquals(0.98425195f, evaluateNumericExpression("(0+((2-1)/2))*1.9685039"), 0.0001f);
        
        // Simulate: offset=1, row=3, pointSpacing=1.9685039
        // (1+((3-1)/2))*1.9685039 = (1+1)*1.9685039 = 2*1.9685039 = 3.9370078
        assertEquals(3.9370078f, evaluateNumericExpression("(1+((3-1)/2))*1.9685039"), 0.0001f);
        
        // Test expression like: ($row-1)*$rowSpacing
        // Simulate: row=2, rowSpacing=1.7047743848
        // (2-1)*1.7047743848 = 1*1.7047743848 = 1.7047743848
        assertEquals(1.7047743848f, evaluateNumericExpression("(2-1)*1.7047743848"), 0.0001f);
        
        // Test expression like: ($offset+(($row-1)/2)-0.5)*$pointSpacing
        // Simulate: offset=0, row=2, pointSpacing=1.9685039
        // (0+((2-1)/2)-0.5)*1.9685039 = (0+0.5-0.5)*1.9685039 = 0*1.9685039 = 0
        assertEquals(0.0f, evaluateNumericExpression("(0+((2-1)/2)-0.5)*1.9685039"), 0.0001f);
        
        // Test ternary expression like: $flipBacking ? -2 : 2
        assertEquals(-2.0f, evaluateNumericExpression("true?-2:2"));
        assertEquals(2.0f, evaluateNumericExpression("false?-2:2"));
    }

    @Test
    @DisplayName("Test error cases")
    void testErrorCases() {
        // Test empty expression
        assertThrows(IllegalArgumentException.class, () -> evaluateExpression(""));
        
        // Test mismatched parentheses
        assertThrows(IllegalArgumentException.class, () -> evaluateExpression("(2+3"));
        assertThrows(IllegalArgumentException.class, () -> evaluateExpression("2+3)"));
        
        // Test mismatched ternary conditional
        assertThrows(IllegalArgumentException.class, () -> evaluateExpression("true?5"));
        assertThrows(IllegalArgumentException.class, () -> evaluateExpression("5:3"));
        
        // Test invalid number format
        assertThrows(NumberFormatException.class, () -> evaluateExpression("abc"));
    }

    @Test
    @DisplayName("Test whitespace handling")
    void testWhitespaceHandling() {
        assertEquals(7.0f, evaluateNumericExpression("3 + 4"));
        assertEquals(12.0f, evaluateNumericExpression("  3  *  4  "));
        assertTrue(evaluateBooleanExpression(" true && true "));
    }
}