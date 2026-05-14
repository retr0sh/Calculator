package com.example.calculator.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
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
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // UI элементы
    private TextView inputText;      // поле для ввода выражения
    private TextView resultText;     // поле для отображения результата
    private DrawerLayout drawerLayout;  // боковое меню (навигация)

    // Данные
    private HistoryDatabase database;    // база данных для истории
    private StringBuilder currentInput;  // текущее вводимое выражение
    private String lastResult = "";      // последний вычисленный результат
    private boolean isNewCalculation = false;  // флаг нового вычисления

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // инициализация базы данных
        database = HistoryDatabase.getInstance(this);
        currentInput = new StringBuilder();

        setupUI();              // настройка интерфейса
        setupNavigationDrawer(); // настройка бокового меню
        setupButtons();         // настройка кнопок
    }

    // настройка элементов интерфейса
    private void setupUI() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        inputText = findViewById(R.id.input_text);
        resultText = findViewById(R.id.result_text);
        inputText.setText("0");  // начальное значение
    }

    // настройка бокового навигационного меню
    private void setupNavigationDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // кнопка открытия/закрытия меню
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, findViewById(R.id.toolbar),
                android.R.string.ok, android.R.string.cancel);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // обработка выбора пунктов меню
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_show_history) {
                // открыть активность с историей
                startActivity(new Intent(MainActivity.this, HistoryActivity.class));
            } else if (itemId == R.id.nav_clear_history) {
                // очистить историю
                clearHistory();
                Toast.makeText(this, "История очищена", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.nav_clear_all) {
                // очистить все (историю и текущий ввод)
                clearAll();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    // назначение обработчиков для всех кнопок калькулятора
    private void setupButtons() {
        // цифровые кнопки от 0 до 9
        int[] numberIds = {R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4,
                R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9};

        for (int id : numberIds) {
            findViewById(id).setOnClickListener(this::onNumberClick);
        }

        // операторы
        findViewById(R.id.btn_add).setOnClickListener(v -> appendOperator("+"));
        findViewById(R.id.btn_subtract).setOnClickListener(v -> appendOperator("-"));
        findViewById(R.id.btn_multiply).setOnClickListener(v -> appendOperator("*"));
        findViewById(R.id.btn_divide).setOnClickListener(v -> appendOperator("/"));

        // действия
        findViewById(R.id.btn_equals).setOnClickListener(this::onEqualsClick);
        findViewById(R.id.btn_clear).setOnClickListener(v -> clearLastChar());
        findViewById(R.id.btn_all_clear).setOnClickListener(v -> clearAll());
        findViewById(R.id.btn_decimal).setOnClickListener(v -> appendDecimal());
        findViewById(R.id.btn_sign).setOnClickListener(v -> changeSign());
        findViewById(R.id.btn_percent).setOnClickListener(v -> calculatePercent());
    }

    // обработчик нажатия на цифровые кнопки
    private void onNumberClick(View view) {
        String number = ((TextView) view).getText().toString();

        // если начато новое вычисление, очищаем ввод
        if (isNewCalculation) {
            currentInput.setLength(0);
            isNewCalculation = false;
        }

        // запрет на ввод ведущего нуля
        if (currentInput.length() == 0 && number.equals("0")) {
            return;
        }

        currentInput.append(number);
        updateDisplay();  // обновление экрана
    }

    // добавление оператора (+, -, *, /)
    private void appendOperator(String operator) {
        // сброс флага нового вычисления
        if (isNewCalculation) {
            isNewCalculation = false;
        }

        // если нет ввода и нет результата - ничего не делаем
        if (currentInput.length() == 0 && lastResult.isEmpty()) {
            return;
        }

        // если ввод пуст, но есть результат - используем результат
        if (currentInput.length() == 0 && !lastResult.isEmpty()) {
            currentInput.append(lastResult);
        }

        // замена последнего оператора, если он есть
        if (currentInput.length() > 0) {
            char lastChar = currentInput.charAt(currentInput.length() - 1);
            if (lastChar == '+' || lastChar == '-' || lastChar == '*' || lastChar == '/') {
                currentInput.setCharAt(currentInput.length() - 1, operator.charAt(0));
            } else {
                currentInput.append(operator);
            }
            updateDisplay();
        }
    }

    // вычисление результата при нажатии "="
    private void onEqualsClick(View view) {
        if (currentInput.length() == 0) {
            return;  // нет выражения для вычисления
        }

        String expression = currentInput.toString();
        String result = CalculatorEvaluator.evaluate(expression);  // вычисление

        if (!result.equals("Ошибка")) {
            // успешное вычисление
            resultText.setText("= " + result);
            lastResult = result;
            isNewCalculation = true;  // следующий ввод начнет новое выражение
            saveToHistory(expression, result);  // сохранение в историю
            showHistoryPreview();  // уведомление о сохранении
        } else {
            // ошибка в выражении
            resultText.setText("Ошибка");
            Toast.makeText(this, "Ошибка в выражении", Toast.LENGTH_SHORT).show();
        }
    }

    // сохранение вычисления в базу данных (в фоновом потоке)
    private void saveToHistory(String expression, String result) {
        new Thread(() -> {
            HistoryItem item = new HistoryItem(expression, result);
            database.historyDao().insert(item);  // вставка в БД
        }).start();
    }

    // показ уведомления о сохранении (последнее вычисление)
    private void showHistoryPreview() {
        new Thread(() -> {
            List<HistoryItem> history = database.historyDao().getLast20();
            if (!history.isEmpty()) {
                HistoryItem last = history.get(0);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Сохранено: " + last.getExpression() + " = " + last.getResult(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    // полная очистка истории (фоновый поток)
    private void clearHistory() {
        new Thread(() -> {
            database.historyDao().deleteAll();  // удаление всех записей
        }).start();
    }

    // удаление последнего символа (кнопка C)
    private void clearLastChar() {
        if (isNewCalculation) {
            return;  // при новом вычислении очистка не работает
        }

        if (currentInput.length() > 0) {
            currentInput.deleteCharAt(currentInput.length() - 1);
            updateDisplay();
        }

        // если все удалили, показываем 0
        if (currentInput.length() == 0) {
            inputText.setText("0");
            resultText.setText("");
        }
    }

    // полная очистка всего (кнопка AC)
    private void clearAll() {
        currentInput.setLength(0);
        lastResult = "";
        isNewCalculation = false;
        inputText.setText("0");
        resultText.setText("");
    }

    // добавление десятичной точки
    private void appendDecimal() {
        if (isNewCalculation) {
            currentInput.setLength(0);
            isNewCalculation = false;
        }

        String current = currentInput.toString();
        // находим последнее число в выражении
        String[] parts = current.split("[+\\-*/]");
        String lastNumber = parts.length > 0 ? parts[parts.length - 1] : "";

        // проверка - нет ли уже точки в последнем числе
        if (!lastNumber.contains(".")) {
            if (current.length() == 0 || isLastCharOperator()) {
                currentInput.append("0.");  // добавляем "0." если нужно
            } else {
                currentInput.append(".");
            }
            updateDisplay();
        }
    }

    // изменение знака последнего числа (+/-)
    private void changeSign() {
        if (currentInput.length() == 0) return;

        String current = currentInput.toString();
        // поиск последнего оператора
        int lastOperatorIndex = -1;
        for (int i = current.length() - 1; i >= 0; i--) {
            char c = current.charAt(i);
            if (c == '+' || c == '-' || c == '*' || c == '/') {
                lastOperatorIndex = i;
                break;
            }
        }

        // извлечение последнего числа
        String numberPart = (lastOperatorIndex >= 0 && lastOperatorIndex + 1 < current.length())
                ? current.substring(lastOperatorIndex + 1) : current;

        // смена знака
        String newNumber = numberPart.startsWith("-") ? numberPart.substring(1) : "-" + numberPart;

        // сборка выражения
        String newExpression;
        if (lastOperatorIndex >= 0) {
            newExpression = current.substring(0, lastOperatorIndex + 1) + newNumber;
        } else {
            newExpression = newNumber;
        }

        currentInput.setLength(0);
        currentInput.append(newExpression);
        updateDisplay();
    }

    // вычисление процента от последнего числа
    private void calculatePercent() {
        if (currentInput.length() == 0) return;

        try {
            String current = currentInput.toString();
            // поиск последнего оператора
            int lastOperatorIndex = -1;
            for (int i = current.length() - 1; i >= 0; i--) {
                char c = current.charAt(i);
                if (c == '+' || c == '-' || c == '*' || c == '/') {
                    lastOperatorIndex = i;
                    break;
                }
            }

            // извлечение последнего числа
            String numberPart = (lastOperatorIndex >= 0 && lastOperatorIndex + 1 < current.length())
                    ? current.substring(lastOperatorIndex + 1) : current;

            // вычисление процента
            double value = Double.parseDouble(CalculatorEvaluator.evaluate(numberPart));
            String percent = String.valueOf(value / 100.0);

            // сборка выражения с процентом
            String newExpression;
            if (lastOperatorIndex >= 0) {
                newExpression = current.substring(0, lastOperatorIndex + 1) + percent;
            } else {
                newExpression = percent;
            }

            currentInput.setLength(0);
            currentInput.append(newExpression);
            updateDisplay();
        } catch (Exception e) {
            // игнорируем ошибки
        }
    }

    // проверка, является ли последний символ оператором
    private boolean isLastCharOperator() {
        if (currentInput.length() == 0) return false;
        char lastChar = currentInput.charAt(currentInput.length() - 1);
        return lastChar == '+' || lastChar == '-' || lastChar == '*' || lastChar == '/';
    }

    // обновление текста на экране
    private void updateDisplay() {
        inputText.setText(currentInput.length() == 0 ? "0" : currentInput.toString());
    }
}