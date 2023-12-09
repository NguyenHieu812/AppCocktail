package com.example.appcocktail.data;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface CocktailDao {
    @Query("SELECT * FROM cocktails")
    List<Cocktail> getAllCocktails();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCocktails(List<Cocktail> cocktails);
    // Các phương thức CRUD khác
}
