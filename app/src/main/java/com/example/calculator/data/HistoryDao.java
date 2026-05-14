package com.example.calculator.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.calculator.model.HistoryItem;

import java.util.List;

// DAO - интерфейс для работы с базой данных истории
@Dao
public interface HistoryDao {

    // вставка нового элемента в таблицу истории
    @Insert
    void insert(HistoryItem item);

    // получение последних 20 записей, отсортированных по убыванию времени (новые сверху)
    @Query("SELECT * FROM history ORDER BY timestamp DESC LIMIT 20")
    List<HistoryItem> getLast20();

    // удаление всех записей из истории
    @Query("DELETE FROM history")
    void deleteAll();

    // удаление конкретной записи по её идентификатору
    @Query("DELETE FROM history WHERE id = :id")
    void deleteById(long id);
}