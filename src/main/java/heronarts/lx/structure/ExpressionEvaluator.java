package heronarts.lx.structure;

/**
 * Interface for evaluating mathematical and boolean expressions.
 * 
 * This interface provides methods to evaluate string expressions and return
 * results as either numeric (float) or boolean values. Implementations should
 * support standard mathematical operations, boolean logic, comparisons, 
 * parentheses, and conditional operators.
 */
public interface ExpressionEvaluator {
    
    /**
     * Evaluates a string expression and returns the numeric result.
     * 
     * @param expression The expression to evaluate (e.g., "3+4*2", "sin(45)", "(2+3)^2")
     * @return The numeric result as a float
     * @throws IllegalArgumentException if the expression is invalid or does not evaluate to a number
     */
    float evaluateNumeric(String expression);
    
    /**
     * Evaluates a string expression and returns the boolean result.
     * 
     * @param expression The expression to evaluate (e.g., "true && false", "5 > 3", "x == y")
     * @return The boolean result
     * @throws IllegalArgumentException if the expression is invalid or does not evaluate to a boolean
     */
    boolean evaluateBoolean(String expression);
    
    /**
     * Evaluates a string expression and returns a generic result object.
     * The result can be either numeric or boolean depending on the expression.
     * 
     * @param expression The expression to evaluate
     * @return An ExpressionResult object containing either a numeric or boolean value
     * @throws IllegalArgumentException if the expression is invalid
     */
    ExpressionResult evaluate(String expression);
    
    /**
     * Base class for expression evaluation results.
     * Results can be either numeric (float) or boolean values.
     */
    abstract class ExpressionResult {
        
        /**
         * Result containing a numeric (float) value.
         */
        public static class Numeric extends ExpressionResult {
            private final float number;
            
            public Numeric(float number) {
                this.number = number;
            }
            
            public float getValue() {
                return number;
            }
            
            @Override
            public String toString() {
                return String.valueOf(this.number);
            }
        }
        
        /**
         * Result containing a boolean value.
         */
        public static class Boolean extends ExpressionResult {
            public static final Boolean TRUE = new Boolean(true);
            public static final Boolean FALSE = new Boolean(false);
            
            private final boolean bool;
            
            public Boolean(boolean bool) {
                this.bool = bool;
            }
            
            public boolean getValue() {
                return bool;
            }
            
            @Override
            public String toString() {
                return String.valueOf(this.bool);
            }
        }
    }
}