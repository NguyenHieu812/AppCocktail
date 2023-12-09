package com.example.appcocktail.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cocktails")
public class Cocktail {
    @PrimaryKey
    public int id;
    public String name;
    public String ingredients;
    // Các trường dữ liệu khác và các phương thức getter/setter
}
