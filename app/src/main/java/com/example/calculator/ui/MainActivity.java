package com.example.calculator.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.calculator.R;
import com.example.calculator.data.HistoryDatabase;
import com.example.calculator.logic.CalculatorEvaluator;
import com.example.calculator.model.HistoryItem;
import com.google.android.material.navigation.NavigationView;

// калькулятор
public class MainActivity extends AppCompatActivity {
    private TextView input, result;          // поле ввода и поле результата
    private StringBuilder expr = new StringBuilder();  // текущее выражение
    private String lastResult = "";          // последний результат (для цепочки вычислений)
    private boolean newCalc = false;         // флаг нового вычисления
    private HistoryDatabase database;        // база данных истории
    private static final int REQUEST_HISTORY = 1;  // код запроса для истории

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Подключаем базу данных
        database = HistoryDatabase.getInstance(this);

        // Настраиваем панель инструментов
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Находим текстовые поля
        input = findViewById(R.id.input_text);
        result = findViewById(R.id.result_text);
        input.setText("0");  // начальное значение

        // Настраиваем боковое меню
        setupDrawer();
        // Настраиваем кнопки калькулятора
        setupButtons();
    }

    // Настройка бокового меню (Drawer)
    private void setupDrawer() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navView = findViewById(R.id.nav_view);

        // Кнопка открытия/закрытия меню
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, findViewById(R.id.toolbar),
                android.R.string.ok, android.R.string.cancel);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Обработка выбора пунктов меню
        navView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_show_history) {
                // Открыть окно истории
                Intent intent = new Intent(this, HistoryActivity.class);
                startActivityForResult(intent, REQUEST_HISTORY);
            } else if (item.getItemId() == R.id.nav_clear_history) {
                // Очистить всю историю (в фоновом потоке)
                new Thread(() -> database.historyDao().deleteAll()).start();
            }
            drawer.closeDrawer(GravityCompat.START);  // закрываем меню
            return true;
        });
    }

    // Обработка возврата из HistoryActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Если вернулись из истории и выбрали запись
        if (requestCode == REQUEST_HISTORY && resultCode == RESULT_OK) {
            String expression = data.getStringExtra("expression");
            if (expression != null && !expression.isEmpty()) {
                expr.setLength(0);           // очищаем текущее выражение
                expr.append(expression);     // вставляем выбранное выражение
                newCalc = false;             // отключаем флаг нового вычисления
                update();                    // обновляем экран
            }
        }
    }

    // Настройка всех кнопок калькулятора
    private void setupButtons() {
        // Цифровые кнопки (0-9)
        int[] nums = {R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4,
                R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9};
        for (int id : nums) findViewById(id).setOnClickListener(this::onNumber);

        // Операторы
        findViewById(R.id.btn_add).setOnClickListener(v -> op("+"));        // сложение
        findViewById(R.id.btn_subtract).setOnClickListener(v -> op("-"));   // вычитание
        findViewById(R.id.btn_multiply).setOnClickListener(v -> op("*"));   // умножение
        findViewById(R.id.btn_divide).setOnClickListener(v -> op("/"));     // деление

        // Действия
        findViewById(R.id.btn_equals).setOnClickListener(v -> equals());    // равно
        findViewById(R.id.btn_clear).setOnClickListener(v -> clearLast());  // очистить последний
        findViewById(R.id.btn_all_clear).setOnClickListener(v -> clearAll()); // очистить всё
        findViewById(R.id.btn_decimal).setOnClickListener(v -> dot());      // точка
        findViewById(R.id.btn_sign).setOnClickListener(v -> sign());        // смена знака
        findViewById(R.id.btn_percent).setOnClickListener(v -> percent());  // процент
    }

    // Обработка нажатия на цифры
    private void onNumber(View v) {
        String num = ((TextView) v).getText().toString();

        // Если начато новое вычисление - очищаем
        if (newCalc) {
            expr.setLength(0);
            newCalc = false;
        }

        // Запрещаем ввод ведущего нуля
        if (expr.length() == 0 && num.equals("0")) return;

        expr.append(num);  // добавляем цифру
        update();          // обновляем экран
    }

    // Добавление оператора (+, -, *, /)
    private void op(String operator) {
        if (newCalc) newCalc = false;
        if (expr.length() == 0 && lastResult.isEmpty()) return;

        // Если выражение пустое, используем последний результат
        if (expr.length() == 0) expr.append(lastResult);

        if (expr.length() > 0) {
            char last = expr.charAt(expr.length() - 1);
            // Если последний символ - оператор, заменяем его
            if (last == '+' || last == '-' || last == '*' || last == '/') {
                expr.setCharAt(expr.length() - 1, operator.charAt(0));
            } else {
                expr.append(operator);  // добавляем оператор
            }
            update();
        }
    }

    // Вычисление результата (кнопка "=")
    private void equals() {
        if (expr.length() == 0) return;  // нет выражения

        String expression = expr.toString();
        String res = CalculatorEvaluator.evaluate(expression);  // вычисляем

        if (!res.equals("Ошибка")) {
            result.setText("= " + res);    // показываем результат
            lastResult = res;              // запоминаем результат
            newCalc = true;                // следующий ввод начнет новое выражение

            // Сохраняем в историю (в фоновом потоке)
            new Thread(() -> {
                HistoryItem item = new HistoryItem(expression, res);
                database.historyDao().insert(item);
            }).start();
        } else {
            result.setText("Ошибка");
        }
    }

    // Удаление последнего символа (кнопка C)
    private void clearLast() {
        if (newCalc) return;  // при новом вычислении не работает
        if (expr.length() > 0) {
            expr.deleteCharAt(expr.length() - 1);  // удаляем последний символ
            update();
        }
        if (expr.length() == 0) {
            input.setText("0");
            result.setText("");
        }
    }

    // Полная очистка (кнопка AC)
    private void clearAll() {
        expr.setLength(0);      // очищаем выражение
        lastResult = "";        // очищаем последний результат
        newCalc = false;        // сбрасываем флаг
        input.setText("0");     // показываем 0
        result.setText("");     // очищаем результат
    }

    // Добавление десятичной точки
    private void dot() {
        if (newCalc) {
            expr.setLength(0);
            newCalc = false;
        }

        String current = expr.toString();
        // Разбиваем на части по операторам
        String[] parts = current.split("[+\\-*/]");
        String lastNum = parts.length > 0 ? parts[parts.length - 1] : "";

        // Если в последнем числе нет точки - добавляем
        if (!lastNum.contains(".")) {
            if (current.length() == 0 || isOperator()) {
                expr.append("0.");  // начинаем с "0."
            } else {
                expr.append(".");
            }
            update();
        }
    }

    // Смена знака последнего числа (+/-)
    private void sign() {
        if (expr.length() == 0) return;

        String current = expr.toString();
        // Ищем последний оператор
        int idx = -1;
        for (int i = current.length() - 1; i >= 0; i--) {
            char c = current.charAt(i);
            if (c == '+' || c == '-' || c == '*' || c == '/') {
                idx = i;
                break;
            }
        }

        // Выделяем последнее число
        String num = (idx >= 0 && idx + 1 < current.length()) ? current.substring(idx + 1) : current;
        // Меняем знак
        String newNum = num.startsWith("-") ? num.substring(1) : "-" + num;

        // Собираем выражение заново
        expr.setLength(0);
        if (idx >= 0) {
            expr.append(current.substring(0, idx + 1)).append(newNum);
        } else {
            expr.append(newNum);
        }
        update();
    }

    // Преобразование последнего числа в процент (деление на 100)
    private void percent() {
        if (expr.length() == 0) return;

        try {
            String current = expr.toString();
            // Ищем последний оператор
            int idx = -1;
            for (int i = current.length() - 1; i >= 0; i--) {
                char c = current.charAt(i);
                if (c == '+' || c == '-' || c == '*' || c == '/') {
                    idx = i;
                    break;
                }
            }

            // Выделяем последнее число и делим на 100
            String num = (idx >= 0 && idx + 1 < current.length()) ? current.substring(idx + 1) : current;
            double val = Double.parseDouble(CalculatorEvaluator.evaluate(num));
            String percent = String.valueOf(val / 100.0);

            // Собираем выражение
            expr.setLength(0);
            if (idx >= 0) {
                expr.append(current.substring(0, idx + 1)).append(percent);
            } else {
                expr.append(percent);
            }
            update();
        } catch (Exception ignored) {}
    }

    // Проверка, является ли последний символ оператором
    private boolean isOperator() {
        if (expr.length() == 0) return false;
        char last = expr.charAt(expr.length() - 1);
        return last == '+' || last == '-' || last == '*' || last == '/';
    }

    // Обновление текста на экране
    private void update() {
        input.setText(expr.length() == 0 ? "0" : expr.toString());
    }
}