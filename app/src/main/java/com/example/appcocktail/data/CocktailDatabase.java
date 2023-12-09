package com.example.appcocktail.data;
import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
@Database(entities = {Cocktail.class}, version = 1)
public abstract class CocktailDatabase extends RoomDatabase {
    public abstract CocktailDao cocktailDao();
    // Singleton pattern để đảm bảo chỉ có một instance của Database được tạo
    private static volatile CocktailDatabase INSTANCE;

    public static CocktailDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (CocktailDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    CocktailDatabase.class, "cocktail_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}