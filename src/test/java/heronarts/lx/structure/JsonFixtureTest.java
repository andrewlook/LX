package heronarts.lx.structure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.Constructor;

/**
 * Unit tests for JsonFixture expression evaluation functionality.
 * Tests the private _evaluateExpression method using reflection.
 * 
 * This test class focuses on testing the expression evaluation logic
 * without requiring a full LX context setup.
 */
public class JsonFixtureTest {

    private Object fixture;
    private Method evaluateExpressionMethod;
    private Class<?> numericClass;
    private Class<?> booleanClass; 
    private Field numericField;
    private Field booleanField;

    @BeforeEach
    void setUp() throws Exception {
        // Use reflection to create JsonFixture instance and access private members
        // This approach allows testing without dealing with complex dependencies
        
        Class<?> fixtureClass = JsonFixture.class;
        
        // Get the private constructor or create a mock approach
        // Since JsonFixture requires LX, we'll mock or create a minimal setup
        try {
            // Try to create with minimal setup
            Constructor<?> constructor = fixtureClass.getDeclaredConstructors()[0];
            constructor.setAccessible(true);
            
            // Create minimal parameters - this might fail, but we'll handle it
            // For now, let's try null parameters
            Object[] params = new Object[constructor.getParameterCount()];
            fixture = constructor.newInstance(params);
        } catch (Exception e) {
            // If direct instantiation fails, we'll create a standalone test
            fixture = null;
        }
        
        // Get access to the private _evaluateExpression method
        evaluateExpressionMethod = fixtureClass.getDeclaredMethod("_evaluateExpression", String.class);
        evaluateExpressionMethod.setAccessible(true);
        
        // Get access to the inner classes for result extraction
        numericClass = Class.forName("heronarts.lx.structure.JsonFixture$ExpressionResult$Numeric");
        booleanClass = Class.forName("heronarts.lx.structure.JsonFixture$ExpressionResult$Boolean");
        
        numericField = numericClass.getDeclaredField("number");
        numericField.setAccessible(true);
        
        booleanField = booleanClass.getDeclaredField("bool");
        booleanField.setAccessible(true);
    }

    private Object evaluateExpression(String expression) throws Exception {
        if (fixture == null) {
            throw new Exception("JsonFixture instance could not be created - test setup failed");
        }
        return evaluateExpressionMethod.invoke(fixture, expression);
    }

    private float getNumericResult(Object result) throws Exception {
        return (Float) numericField.get(result);
    }

    private boolean getBooleanResult(Object result) throws Exception {
        return (Boolean) booleanField.get(result);
    }

    @Test
    @DisplayName("Test basic numeric literals")
    void testNumericLiterals() throws Exception {
        assertEquals(42.0f, getNumericResult(evaluateExpression("42")));
        assertEquals(3.14f, getNumericResult(evaluateExpression("3.14")));
        assertEquals(-5.0f, getNumericResult(evaluateExpression("-5")));
        assertEquals(0.0f, getNumericResult(evaluateExpression("0")));
    }

    @Test
    @DisplayName("Test boolean literals")
    void testBooleanLiterals() throws Exception {
        assertTrue(getBooleanResult(evaluateExpression("true")));
        assertFalse(getBooleanResult(evaluateExpression("false")));
    }

    @Test
    @DisplayName("Test basic arithmetic operations")
    void testBasicArithmetic() throws Exception {
        assertEquals(7.0f, getNumericResult(evaluateExpression("3+4")));
        assertEquals(-1.0f, getNumericResult(evaluateExpression("3-4")));
        assertEquals(12.0f, getNumericResult(evaluateExpression("3*4")));
        assertEquals(0.75f, getNumericResult(evaluateExpression("3/4")));
        assertEquals(1.0f, getNumericResult(evaluateExpression("3%2")));
        assertEquals(9.0f, getNumericResult(evaluateExpression("3^2")));
    }

    @Test
    @DisplayName("Test operator precedence")
    void testOperatorPrecedence() throws Exception {
        assertEquals(14.0f, getNumericResult(evaluateExpression("2+3*4")));
        assertEquals(20.0f, getNumericResult(evaluateExpression("(2+3)*4")));
        assertEquals(11.0f, getNumericResult(evaluateExpression("3+2^3")));
        assertEquals(125.0f, getNumericResult(evaluateExpression("(3+2)^3")));
    }

    @Test
    @DisplayName("Test nested parentheses")
    void testNestedParentheses() throws Exception {
        assertEquals(42.0f, getNumericResult(evaluateExpression("((((42))))")));
        assertEquals(50.0f, getNumericResult(evaluateExpression("2*(3+(4*5))")));
        assertEquals(22.0f, getNumericResult(evaluateExpression("2*((3+4)*2-3)")));
    }

    @Test
    @DisplayName("Test comparison operations")
    void testComparisonOperations() throws Exception {
        assertTrue(getBooleanResult(evaluateExpression("5>3")));
        assertFalse(getBooleanResult(evaluateExpression("3>5")));
        assertTrue(getBooleanResult(evaluateExpression("5>=5")));
        assertTrue(getBooleanResult(evaluateExpression("3<5")));
        assertFalse(getBooleanResult(evaluateExpression("5<3")));
        assertTrue(getBooleanResult(evaluateExpression("3<=3")));
        assertTrue(getBooleanResult(evaluateExpression("5==5")));
        assertFalse(getBooleanResult(evaluateExpression("5==3")));
        assertTrue(getBooleanResult(evaluateExpression("5!=3")));
        assertFalse(getBooleanResult(evaluateExpression("5!=5")));
    }

    @Test
    @DisplayName("Test logical operations")
    void testLogicalOperations() throws Exception {
        assertTrue(getBooleanResult(evaluateExpression("true&&true")));
        assertFalse(getBooleanResult(evaluateExpression("true&&false")));
        assertTrue(getBooleanResult(evaluateExpression("true||false")));
        assertFalse(getBooleanResult(evaluateExpression("false||false")));
        
        // Test both forms of logical operators
        assertTrue(getBooleanResult(evaluateExpression("true&true")));
        assertTrue(getBooleanResult(evaluateExpression("true|false")));
    }

    @Test
    @DisplayName("Test unary operators")
    void testUnaryOperators() throws Exception {
        assertEquals(-5.0f, getNumericResult(evaluateExpression("-5")));
        assertEquals(5.0f, getNumericResult(evaluateExpression("--5")));
        assertEquals(-5.0f, getNumericResult(evaluateExpression("---5")));
        
        assertFalse(getBooleanResult(evaluateExpression("!true")));
        assertTrue(getBooleanResult(evaluateExpression("!false")));
        assertTrue(getBooleanResult(evaluateExpression("!!true")));
    }

    @Test
    @DisplayName("Test simple functions")
    void testSimpleFunctions() throws Exception {
        assertEquals(0.0f, getNumericResult(evaluateExpression("sin0")), 0.0001f);
        assertEquals(1.0f, getNumericResult(evaluateExpression("cos0")), 0.0001f);
        assertEquals(5.0f, getNumericResult(evaluateExpression("abs-5")));
        assertEquals(3.0f, getNumericResult(evaluateExpression("sqrt9")));
        assertEquals(2.0f, getNumericResult(evaluateExpression("floor2.7")));
        assertEquals(3.0f, getNumericResult(evaluateExpression("ceil2.3")));
        assertEquals(3.0f, getNumericResult(evaluateExpression("round2.7")));
    }

    @Test
    @DisplayName("Test ternary conditional operator")
    void testTernaryConditional() throws Exception {
        assertEquals(10.0f, getNumericResult(evaluateExpression("true?10:20")));
        assertEquals(20.0f, getNumericResult(evaluateExpression("false?10:20")));
        assertEquals(5.0f, getNumericResult(evaluateExpression("3>2?5:7")));
        assertEquals(7.0f, getNumericResult(evaluateExpression("2>3?5:7")));
        
        // Nested ternary
        assertEquals(1.0f, getNumericResult(evaluateExpression("true?true?1:2:3")));
        assertEquals(2.0f, getNumericResult(evaluateExpression("true?false?1:2:3")));
        assertEquals(3.0f, getNumericResult(evaluateExpression("false?true?1:2:3")));
    }

    @Test
    @DisplayName("Test complex expressions similar to LXF file")
    void testLXFLikeExpressions() throws Exception {
        // Test expression like: ($offset+(($row-1)/2))*$pointSpacing
        // We'll simulate with actual numbers since variables aren't available in this context
        
        // Simulate: offset=0, row=2, pointSpacing=1.9685039
        // (0+((2-1)/2))*1.9685039 = (0+(1/2))*1.9685039 = 0.5*1.9685039 = 0.98425195
        assertEquals(0.98425195f, getNumericResult(evaluateExpression("(0+((2-1)/2))*1.9685039")), 0.0001f);
        
        // Simulate: offset=1, row=3, pointSpacing=1.9685039
        // (1+((3-1)/2))*1.9685039 = (1+1)*1.9685039 = 2*1.9685039 = 3.9370078
        assertEquals(3.9370078f, getNumericResult(evaluateExpression("(1+((3-1)/2))*1.9685039")), 0.0001f);
        
        // Test expression like: ($row-1)*$rowSpacing
        // Simulate: row=2, rowSpacing=1.7047743848
        // (2-1)*1.7047743848 = 1*1.7047743848 = 1.7047743848
        assertEquals(1.7047743848f, getNumericResult(evaluateExpression("(2-1)*1.7047743848")), 0.0001f);
        
        // Test expression like: ($offset+(($row-1)/2)-0.5)*$pointSpacing
        // Simulate: offset=0, row=2, pointSpacing=1.9685039
        // (0+((2-1)/2)-0.5)*1.9685039 = (0+0.5-0.5)*1.9685039 = 0*1.9685039 = 0
        assertEquals(0.0f, getNumericResult(evaluateExpression("(0+((2-1)/2)-0.5)*1.9685039")), 0.0001f);
        
        // Test ternary expression like: $flipBacking ? -2 : 2
        assertEquals(-2.0f, getNumericResult(evaluateExpression("true?-2:2")));
        assertEquals(2.0f, getNumericResult(evaluateExpression("false?-2:2")));
    }

    @Test
    @DisplayName("Test error cases")
    void testErrorCases() throws Exception {
        // Test empty expression
        assertThrows(Exception.class, () -> evaluateExpression(""));
        
        // Test mismatched parentheses
        assertThrows(Exception.class, () -> evaluateExpression("(2+3"));
        assertThrows(Exception.class, () -> evaluateExpression("2+3)"));
        
        // Test mismatched ternary conditional
        assertThrows(Exception.class, () -> evaluateExpression("true?5"));
        assertThrows(Exception.class, () -> evaluateExpression("5:3"));
        
        // Test invalid number format
        assertThrows(Exception.class, () -> evaluateExpression("abc"));
    }

    @Test
    @DisplayName("Test whitespace handling")
    void testWhitespaceHandling() throws Exception {
        assertEquals(7.0f, getNumericResult(evaluateExpression("3 + 4")));
        assertEquals(12.0f, getNumericResult(evaluateExpression("  3  *  4  ")));
        assertTrue(getBooleanResult(evaluateExpression(" true && true ")));
    }
}