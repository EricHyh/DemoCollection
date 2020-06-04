package com.hyh.arithmetic.count;

/**
 * @author Administrator
 * @description
 * @data 2020/6/4
 */
public class ComputeUtil {

    private static final String DOUBLE_REGEX = "^(\\s*-?\\s*\\d+)(\\.\\d+)?$";

    public static double compute(String expression) {
        expression = expression.trim();

        char operator = '\u0000';
        int operatorIndex = -1;
        int length = expression.length();
        int bracketCount = 0;

        for (int index = 0; index < length; index++) {
            char c = expression.charAt(index);
            if (index == 0 && (c == '+' || c == '-')) continue;
            if (c == '(') {
                bracketCount++;
            } else if (c == ')') {
                bracketCount--;
            } else if (c == '+' || c == '-') {
                if (bracketCount == 0) {
                    operatorIndex = index;
                    operator = c;
                    break;
                }
            } else if (c == '*' || c == '/') {
                if (bracketCount == 0) {
                    if (operatorIndex == -1) {
                        operatorIndex = index;
                        operator = c;
                    }
                }
            }
        }

        if (operatorIndex == -1) {
            if (expression.matches(DOUBLE_REGEX)) {
                return Double.parseDouble(expression.replaceAll("\\s", ""));
            } else {
                expression = expression.substring(1, expression.length() - 1).trim();
                return compute(expression);
            }
        }

        String expression1 = expression.substring(0, operatorIndex);
        String expression2 = expression.substring(operatorIndex + 1);

        switch (operator) {
            case '+': {
                return add(expression1, expression2);
            }
            case '-': {
                return subtract(expression1, expression2);
            }
            case '*': {
                return multiply(expression1, expression2);
            }
            case '/': {
                return divide(expression1, expression2);
            }
        }
        return 0;
    }

    public static double add(String expression1, String expression2) {
        double num1;
        double num2;
        if (expression1.trim().matches(DOUBLE_REGEX)) {
            num1 = Double.parseDouble(expression1.replaceAll("\\s", ""));
        } else {
            num1 = compute(expression1);
        }
        if (expression2.trim().matches(DOUBLE_REGEX)) {
            num2 = Double.parseDouble(expression2.replaceAll("\\s", ""));
        } else {
            num2 = compute(expression2);
        }
        return num1 + num2;
    }

    public static double subtract(String expression1, String expression2) {
        double num1;
        double num2;
        if (expression1.trim().matches(DOUBLE_REGEX)) {
            num1 = Double.parseDouble(expression1.replaceAll("\\s", ""));
        } else {
            num1 = compute(expression1);
        }
        if (expression2.trim().matches(DOUBLE_REGEX)) {
            num2 = Double.parseDouble(expression2.replaceAll("\\s", ""));
        } else {
            num2 = compute(expression2);
        }
        return num1 - num2;
    }

    public static double multiply(String expression1, String expression2) {
        double num1;
        double num2;
        if (expression1.trim().matches(DOUBLE_REGEX)) {
            num1 = Double.parseDouble(expression1.replaceAll("\\s", ""));
        } else {
            num1 = compute(expression1);
        }
        if (expression2.trim().matches(DOUBLE_REGEX)) {
            num2 = Double.parseDouble(expression2.replaceAll("\\s", ""));
        } else {
            num2 = compute(expression2);
        }
        return num1 * num2;
    }

    public static double divide(String expression1, String expression2) {
        double num1;
        double num2;
        if (expression1.trim().matches(DOUBLE_REGEX)) {
            num1 = Double.parseDouble(expression1.replaceAll("\\s", ""));
        } else {
            num1 = compute(expression1);
        }
        if (expression2.trim().matches(DOUBLE_REGEX)) {
            num2 = Double.parseDouble(expression2.replaceAll("\\s", ""));
        } else {
            num2 = compute(expression2);
        }
        return num1 / num2;
    }
}