package com.example.calculator.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calculator.data.HistoryDatabase;
import com.example.calculator.R;
import com.example.calculator.model.HistoryItem;

import java.util.List;

// Фрагмент для отображения истории
public class HistoryFragment extends Fragment {
    private RecyclerView recyclerView;   // список истории
    private HistoryAdapter adapter;      // адаптер для списка
    private HistoryDatabase database;    // база данных
    private OnHistoryItemClickListener listener;  // слушатель выбора записи

    // Интерфейс для передачи выбранного выражения в калькулятор
    public interface OnHistoryItemClickListener {
        void onHistoryItemSelected(String expression);  // вызывается при нажатии на запись
    }

    // Создает View фрагмента
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.item_history, container, false);
    }

    // Вызывается после создания View, настройка компонентов
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Инициализация базы данных
        database = HistoryDatabase.getInstance(requireContext());

        // Настройка RecyclerView
        recyclerView = view.findViewById(R.id.history_recycler_view);
        adapter = new HistoryAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // При нажатии на запись передаем выражение в калькулятор
        adapter.setOnItemClickListener(item -> {
            if (listener != null) {
                listener.onHistoryItemSelected(item.getExpression());
            }
        });

        // Загружаем историю из базы
        loadHistory();
    }

    // Загружает последние 20 записей из базы
    public void loadHistory() {
        new Thread(() -> {
            List<HistoryItem> history = database.historyDao().getLast20();  // берем из БД
            if (getActivity() != null) {
                // Обновляем список в главном потоке
                getActivity().runOnUiThread(() -> {
                    if (adapter != null) {
                        adapter.setHistoryList(history);
                    }
                });
            }
        }).start();
    }

    // Очищает всю историю
    public void clearHistory() {
        new Thread(() -> {
            database.historyDao().deleteAll();  // удаляем все записи
            if (getActivity() != null) {
                // Перезагружаем список
                getActivity().runOnUiThread(this::loadHistory);
            }
        }).start();
    }
}