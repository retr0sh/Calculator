package com.example.calculator.logic;

import java.util.*;

public class CalculatorEvaluator {

    // главный метод: вычисление выражения и возврат результата в виде строки
    public static String evaluate(String expression) {
        if (expression == null || expression.isEmpty()) {
            return "0";  // пустое выражение = 0
        }

        try {
            // подготовка выражения: замена запятых на точки, символов × и ÷
            String processed = expression.replace(',', '.')
                    .replace("×", "*")
                    .replace("÷", "/")
                    .replace(" ", "");

            double result = evaluateExpression(processed);  // вычисление

            // проверка на некорректный результат
            if (Double.isNaN(result) || Double.isInfinite(result)) {
                return "Ошибка";
            }

            // форматирование: целые числа без .0
            if (result == (long) result) {
                return String.valueOf((long) result);
            } else {
                String formatted = String.format("%.10f", result);
                formatted = formatted.replaceAll("0*$", "").replaceAll("\\.$", "");
                return formatted;
            }
        } catch (Exception e) {
            return "Ошибка";  // любая ошибка = "Ошибка"
        }
    }

    // вычисление выражения
    private static double evaluateExpression(String expression) {
        List<String> tokens = tokenizeWithNegatives(expression);  // разбор на токены
        List<String> rpn = convertToRPN(tokens);                 // преобразование в ОПН
        return evaluateRPN(rpn);                                 // вычисление ОПН
    }

    // разбиение строки на токены с поддержкой унарного минуса
    private static List<String> tokenizeWithNegatives(String expression) {
        List<String> tokens = new ArrayList<>();
        StringBuilder currentNumber = new StringBuilder();
        boolean expectNumber = true;  // ожидаем число (для определения унарного минуса)

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            if (Character.isDigit(c) || c == '.') {
                currentNumber.append(c);  // собираем число
                expectNumber = false;
            } else if (c == '+' || c == '*' || c == '/') {
                if (currentNumber.length() > 0) {
                    tokens.add(currentNumber.toString());
                    currentNumber.setLength(0);
                }
                tokens.add(String.valueOf(c));
                expectNumber = true;  // после оператора ожидаем число
            } else if (c == '-') {
                if (expectNumber) {
                    currentNumber.append('-');  // унарный минус - часть числа
                } else {
                    if (currentNumber.length() > 0) {
                        tokens.add(currentNumber.toString());
                        currentNumber.setLength(0);
                    }
                    tokens.add("-");  // бинарный минус - оператор
                    expectNumber = true;
                }
            }
        }

        if (currentNumber.length() > 0) {
            tokens.add(currentNumber.toString());  // последнее число
        }

        return tokens;
    }

    // преобразование инфиксной записи в обратную польскую нотацию (ОПН) - алгоритм сортировочной станции
    private static List<String> convertToRPN(List<String> tokens) {
        List<String> output = new ArrayList<>();      // выходная очередь
        Stack<String> operators = new Stack<>();       // стек операторов

        // приоритеты операторов
        Map<String, Integer> precedence = new HashMap<>();
        precedence.put("+", 1);
        precedence.put("-", 1);
        precedence.put("*", 2);
        precedence.put("/", 2);

        for (String token : tokens) {
            if (isNumber(token)) {
                output.add(token);  // числа сразу в выход
            } else if (isOperator(token)) {
                // выталкиваем операторы с большим или равным приоритетом
                while (!operators.isEmpty() && isOperator(operators.peek()) &&
                        precedence.get(operators.peek()) >= precedence.get(token)) {
                    output.add(operators.pop());
                }
                operators.push(token);
            }
        }

        // выталкиваем оставшиеся операторы
        while (!operators.isEmpty()) {
            output.add(operators.pop());
        }
        return output;
    }

    // вычисление выражения в обратной польской нотации
    private static double evaluateRPN(List<String> rpn) {
        Stack<Double> stack = new Stack<>();

        for (String token : rpn) {
            if (isNumber(token)) {
                stack.push(Double.parseDouble(token));  // число в стек
            } else if (isOperator(token)) {
                if (stack.size() < 2) {
                    throw new IllegalArgumentException("Недостаточно операндов");
                }
                double b = stack.pop();  // правый операнд
                double a = stack.pop();  // левый операнд
                switch (token) {
                    case "+": stack.push(a + b); break;
                    case "-": stack.push(a - b); break;
                    case "*": stack.push(a * b); break;
                    case "/":
                        if (b == 0) throw new ArithmeticException("Деление на ноль");
                        stack.push(a / b);
                        break;
                }
            }
        }

        if (stack.size() != 1) {
            throw new IllegalArgumentException("Некорректное выражение");
        }
        return stack.pop();
    }

    // проверка: является ли токен числом (целым или дробным, положительным или отрицательным)
    private static boolean isNumber(String token) {
        return token.matches("-?\\d+(\\.\\d+)?");
    }

    // проверка: является ли токен оператором
    private static boolean isOperator(String token) {
        return token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/");
    }
}