package com.example.calculator.data;

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

// адаптер для RecyclerView (список истории)
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private List<HistoryItem> historyList = new ArrayList<>();  // данные для отображения
    private OnItemClickListener listener;        // обработчик клика по элементу
    private OnItemDeleteListener deleteListener; // обработчик удаления элемента

    // интерфейс для обработки клика по элементу истории
    public interface OnItemClickListener {
        void onItemClick(HistoryItem item);
    }

    // интерфейс для обработки удаления элемента
    public interface OnItemDeleteListener {
        void onDeleteClick(HistoryItem item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnItemDeleteListener(OnItemDeleteListener listener) {
        this.deleteListener = listener;
    }

    // установка нового списка и обновление отображения
    public void setHistoryList(List<HistoryItem> historyList) {
        this.historyList = historyList;
        notifyDataSetChanged();  // перерисовка всего списка
    }

    // создание нового ViewHolder (элемента списка) из layout-файла
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    // привязка данных к ViewHolder (заполнение полей элемента)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryItem item = historyList.get(position);
        holder.expressionText.setText(item.getExpression() + " =");  // выражение
        holder.resultText.setText(item.getResult());                 // результат
        holder.dateText.setText(item.getFormattedDate());            // дата

        // клик по элементу - загрузить выражение в калькулятор
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });

        // клик по кнопке удаления
        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onDeleteClick(item);
        });
    }

    // количество элементов в списке
    @Override
    public int getItemCount() {
        return historyList.size();
    }

    // внутренний класс для хранения ссылок на элементы интерфейса
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView expressionText, resultText, dateText, deleteButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            expressionText = itemView.findViewById(R.id.history_expression);
            resultText = itemView.findViewById(R.id.history_result);
            dateText = itemView.findViewById(R.id.history_date);
        }
    }
}