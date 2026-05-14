package com.example.calculator.model;  // ВНИМАНИЕ: папка model, а должна быть fragment или ui

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calculator.data.HistoryAdapter;
import com.example.calculator.data.HistoryDatabase;
import com.example.calculator.R;

import java.util.List;

// фрагмент для отображения истории
public class HistoryFragment extends Fragment {
    private RecyclerView recyclerView;           // список истории
    private HistoryAdapter adapter;              // адаптер для списка
    private HistoryDatabase database;            // база данных
    private OnHistoryItemClickListener listener; // слушатель кликов по истории

    // интерфейс для передачи выбранного выражения в калькулятор
    public interface OnHistoryItemClickListener {
        void onHistoryItemSelected(String expression);
    }

    // установка слушателя кликов
    public void setOnHistoryItemClickListener(OnHistoryItemClickListener listener) {
        this.listener = listener;
    }

    // создание View фрагмента
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // ПРОБЛЕМА: здесь должен быть R.layout.fragment_history, а не item_history
        return inflater.inflate(R.layout.item_history, container, false);
    }

    // вызывается после создания View, настройка компонентов
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // инициализация базы данных
        database = HistoryDatabase.getInstance(requireContext());

        // настройка RecyclerView
        recyclerView = view.findViewById(R.id.history_recycler_view);
        adapter = new HistoryAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // клик по элементу истории - передаем выражение в калькулятор
        adapter.setOnItemClickListener(item -> {
            if (listener != null) {
                listener.onHistoryItemSelected(item.getExpression());
            }
        });

        // удаление элемента истории
        adapter.setOnItemDeleteListener(item -> {
            new Thread(() -> {
                database.historyDao().deleteById(item.getId());  // удаление из БД
                if (getActivity() != null) {
                    getActivity().runOnUiThread(this::loadHistory);  // обновление списка
                }
            }).start();
        });

        loadHistory();  // загрузка истории при запуске
    }

    // загрузка истории из базы данных в фоновом потоке
    public void loadHistory() {
        new Thread(() -> {
            List<HistoryItem> history = database.historyDao().getLast20();  // последние 20 записей
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (adapter != null) {
                        adapter.setHistoryList(history);  // обновление адаптера
                    }
                });
            }
        }).start();
    }

    // очистка всей истории (фоновый поток)
    public void clearHistory() {
        new Thread(() -> {
            database.historyDao().deleteAll();  // удаление всех записей
            if (getActivity() != null) {
                getActivity().runOnUiThread(this::loadHistory);  // перезагрузка списка
            }
        }).start();
    }
}