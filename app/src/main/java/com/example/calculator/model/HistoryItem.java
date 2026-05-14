package com.example.calculator.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Entity(tableName = "history")  // таблица в БД Room
public class HistoryItem {
    @PrimaryKey(autoGenerate = true)
    private long id;              // уникальный идентификатор (автоинкремент)
    private String expression;   // математическое выражение (например: "2+2")
    private String result;       // результат вычисления (например: "4")
    private long timestamp;      // время создания записи (Unix timestamp)

    // конструктор - создает запись истории с текущим временем
    public HistoryItem(String expression, String result) {
        this.expression = expression;
        this.result = result;
        this.timestamp = System.currentTimeMillis();  // текущее время в миллисекундах
    }

    // геттеры и сеттеры
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getExpression() { return expression; }
    public void setExpression(String expression) { this.expression = expression; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    // форматирование даты для отображения (день.месяц.год часы:минуты)
    public String getFormattedDate() {
        return new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                .format(new Date(timestamp));
    }
}