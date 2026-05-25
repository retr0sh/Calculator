package com.example.calculator.logic;

public class CalculatorEvaluator {

    public static String evaluate(String expression) {
        try {
            // Пустое выражение = 0
            if (expression == null || expression.isEmpty()) return "0";

            // готовит строку для парсера заменой символов
            String expr = expression.replace(',', '.')
                    .replace("×", "*")
                    .replace("÷", "/")
                    .replace(" ", "");

            // парсер разбирает выражение по правилам математики
            double result = new Object() {
                int pos = -1;      // текущая позиция в строке
                int ch;            // текущий символ

                void nextChar() {  // взять следующий символ
                    ch = (++pos < expr.length()) ? expr.charAt(pos) : -1;
                }

                boolean eat(int c) {  // проверить и пропустить символ
                    if (ch == c) { nextChar(); return true; }
                    return false;
                }

                double parse() {      // начать разбор
                    nextChar();
                    return parseExpression();
                }

                // Сложение и вычитание
                double parseExpression() {
                    double x = parseTerm();
                    while (true) {
                        if (eat('+')) x += parseTerm();
                        else if (eat('-')) x -= parseTerm();
                        else return x;
                    }
                }

                // Умножение и деление
                double parseTerm() {
                    double x = parseFactor();
                    while (true) {
                        if (eat('*')) x *= parseFactor();
                        else if (eat('/')) x /= parseFactor();
                        else return x;
                    }
                }

                // Числа и скобки
                double parseFactor() {
                    if (eat('+')) return parseFactor();   // унарный плюс
                    if (eat('-')) return -parseFactor();  // унарный минус

                    double x;
                    int start = pos;

                    if (eat('(')) {                       // (выражение)
                        x = parseExpression();
                        eat(')');
                    } else if ((ch >= '0' && ch <= '9') || ch == '.') {  // число
                        while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                        x = Double.parseDouble(expr.substring(start, pos));
                    } else {
                        throw new RuntimeException("Ошибка");
                    }
                    return x;
                }
            }.parse();

            // Проверка на ошибку
            if (Double.isNaN(result) || Double.isInfinite(result)) return "Ошибка";

            // Форматируем результат: целые числа без .0, у дробных убираем лишние нули
            if (result == (long) result) return String.valueOf((long) result);
            return String.format("%.10f", result).replaceAll("0*$", "").replaceAll("\\.$", "");

        } catch (Exception e) {
            return "Ошибка";
        }
    }
}