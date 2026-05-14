package com.example.calculator.ui;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calculator.R;
import com.example.calculator.data.HistoryDatabase;
import com.example.calculator.model.HistoryItem;

import java.text.SimpleDateFormat;
import java.util.*;

// Активность для отображения полной истории вычислений
// ВНИМАНИЕ: исправлено название с HistoryAvtivity на HistoryActivity
public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;    // список истории
    private HistoryAdapter adapter;       // адаптер для списка
    private HistoryDatabase database;     // база данных

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // настройка панели инструментов (Toolbar)
        Toolbar toolbar = findViewById(R.id.toolbar_history);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);  // кнопка "Назад"

        // обработка кнопки "Назад"
        toolbar.setNavigationOnClickListener(v -> finish());

        // инициализация базы данных
        database = HistoryDatabase.getInstance(this);

        // настройка RecyclerView
        recyclerView = findViewById(R.id.history_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new HistoryAdapter();
        recyclerView.setAdapter(adapter);

        loadHistory();  // загрузка истории
    }

    // загрузка истории из БД (в фоновом потоке)
    private void loadHistory() {
        new Thread(() -> {
            List<HistoryItem> history = database.historyDao().getLast20();
            runOnUiThread(() -> adapter.setHistoryList(history));
        }).start();
    }

    // ВНУТРЕННИЙ КЛАСС АДАПТЕРА (дублируется с HistoryAdapter.java)
    // РЕКОМЕНДАЦИЯ: удалить этот класс и использовать отдельный файл HistoryAdapter
    class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
        private List<HistoryItem> list = new ArrayList<>();

        // установка списка и обновление
        public void setHistoryList(List<HistoryItem> list) {
            this.list = list;
            notifyDataSetChanged();  // перерисовка всего списка
        }

        // создание ViewHolder
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_history, parent, false);
            return new ViewHolder(view);
        }

        // привязка данных к элементу списка
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            HistoryItem item = list.get(position);
            holder.expression.setText(item.getExpression() + " =");  // выражение
            holder.result.setText(item.getResult());                 // результат
            // форматирование даты
            holder.date.setText(new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                    .format(new Date(item.getTimestamp())));
        }

        // количество элементов
        @Override
        public int getItemCount() {
            return list.size();
        }

        // ViewHolder - хранит ссылки на элементы интерфейса одной строки списка
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView expression, result, date;

            ViewHolder(View v) {
                super(v);
                expression = v.findViewById(R.id.history_expression);
                result = v.findViewById(R.id.history_result);
                date = v.findViewById(R.id.history_date);
            }
        }
    }
}