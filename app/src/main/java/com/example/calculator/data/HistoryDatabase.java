package com.example.calculator.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.calculator.model.HistoryItem;

// класс базы данных Room
// entities = список таблиц в БД
// version = версия схемы БД
// exportSchema = экспортировать схему в JSON
@Database(entities = {HistoryItem.class}, version = 1, exportSchema = false)
public abstract class HistoryDatabase extends RoomDatabase {

    private static HistoryDatabase instance;  // синглтон-экземпляр БД

    // метод для получения DAO
    public abstract HistoryDao historyDao();

    // получение экземпляра базы данных (паттерн Singleton)
    // synchronized - потокобезопасность
    public static synchronized HistoryDatabase getInstance(Context context) {
        if (instance == null) {
            // создание или открытие базы данных
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            HistoryDatabase.class, "history_database")  // имя файла БД
                    .fallbackToDestructiveMigration()  // при смене версии - удалить старые данные
                    .build();
        }
        return instance;
    }
}