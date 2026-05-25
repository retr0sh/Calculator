package com.example.calculator.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calculator.R;
import com.example.calculator.data.HistoryDatabase;
import com.example.calculator.model.HistoryItem;

import java.util.List;

// окно истории
public class HistoryActivity extends AppCompatActivity {

    private HistoryDatabase database;  // база данных
    private HistoryAdapter adapter;    // адаптер для списка

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);  // загружаем интерфейс

        // Настраиваем панель инструментов (кнопка "Назад")
        Toolbar toolbar = findViewById(R.id.toolbar_history);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);  // показываем стрелку назад
        toolbar.setNavigationOnClickListener(v -> finish());     // при нажатии закрываем окно

        // Подключаемся к базе данных
        database = HistoryDatabase.getInstance(this);

        // Настраиваем список
        RecyclerView recyclerView = findViewById(R.id.history_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));  // вертикальный список

        // Создаем адаптер и привязываем к списку
        adapter = new HistoryAdapter();
        recyclerView.setAdapter(adapter);

        // Когда пользователь нажимает на запись в истории
        adapter.setOnItemClickListener(item -> {
            // Создаем Intent для возврата результата
            Intent intent = new Intent();
            intent.putExtra("expression", item.getExpression());  // кладем выражение в Intent
            setResult(RESULT_OK, intent);  // отправляем результат обратно в MainActivity
            finish();  // закрываем окно истории
        });

        // Загружаем записи из базы
        loadHistory();
    }

    // Загружает последние 20 записей из базы данных
    private void loadHistory() {
        // Запрос к базе делаем в отдельном потоке (чтобы не тормозить интерфейс)
        new Thread(() -> {
            List<HistoryItem> history = database.historyDao().getLast20();  // берем из БД
            // Обновляем список в главном потоке (только там можно менять интерфейс)
            runOnUiThread(() -> adapter.setHistoryList(history));
        }).start();
    }
}