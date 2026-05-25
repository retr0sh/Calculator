package com.example.calculator.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calculator.R;
import com.example.calculator.model.HistoryItem;

import java.util.ArrayList;
import java.util.List;

// Адаптер для списка истории
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private List<HistoryItem> historyList = new ArrayList<>();  // список записей
    private OnItemClickListener listener;  // слушатель нажатий

    // Интерфейс, чтобы MainActivity могла реагировать на нажатия
    public interface OnItemClickListener {
        void onItemClick(HistoryItem item);  // вызывается при нажатии на строку
    }

    // Устанавливаем слушатель (вызывается из MainActivity)
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // Обновляем список и перерисовываем
    public void setHistoryList(List<HistoryItem> historyList) {
        this.historyList = historyList;
        notifyDataSetChanged();
    }

    // Создает новую строку списка для каждой записи
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Загружаем item_history для одной строки
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    // Заполняет строку данными
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryItem item = historyList.get(position);  // берем запись по позиции

        // Показываем данные на экране
        holder.expression.setText(item.getExpression() + " =");  // "2+2 ="
        holder.result.setText(item.getResult());                 // "4"
        holder.date.setText(item.getFormattedDate());            // "25.01.2024 15:30"

        // Когда пользователь нажимает на строку то оно передаёт нажатую запись
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    // Сколько всего записей в списке
    @Override
    public int getItemCount() {
        return historyList.size();
    }

    // класс для всех текстовых полей одной строки
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView expression, result, date;  // поля: выражение, результат, дата

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Находим текстовые поля в layout-строки
            expression = itemView.findViewById(R.id.history_expression);
            result = itemView.findViewById(R.id.history_result);
            date = itemView.findViewById(R.id.history_date);
        }
    }
}