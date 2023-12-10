package com.example.appcocktail.ui;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Database extends SQLiteOpenHelper {
    public static final String DB_NAME = "cocktailDB";
    public static final int DB_VERSION = 1;
    public static final String TABLE_FAVOURITES = "favourites";
    public static final String FAVOURITES_ID = "id";
    public static final String FAVOURITES_NAME = "name";
    public static final String FAVOURITES_CATEGORY = "category";
    public static final String FAVOURITES_GLASS = "glass";
    public static final String FAVOURITES_INSTRUCTIONS = "instructions";
    // ingredient;measure\n
    public static final String FAVOURITES_INGREDIENTS = "ingredients";
    public static final String FAVOURITES_THUMBNAIL_URL = "thumbnail_url";
    public static final String FAVOURITES_ADDED_AT = "added_at";

    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");

    public Database(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String create_favourites_table = "CREATE TABLE "+TABLE_FAVOURITES+"(" +
                FAVOURITES_ID + " INTEGER PRIMARY KEY UNIQUE NOT NULL," +
                FAVOURITES_NAME + " VARCHAR(50) NOT NULL," +
                FAVOURITES_CATEGORY + " VARCHAR(50) NOT NULL," +
                FAVOURITES_GLASS + " VARCHAR(50) NOT NULL," +
                FAVOURITES_INSTRUCTIONS + " LONGTEXT NOT NULL," +
                FAVOURITES_INGREDIENTS + " LONGTEXT NOT NULL," +
                FAVOURITES_THUMBNAIL_URL + " VARCHAR(50) NOT NULL," +
                FAVOURITES_ADDED_AT + " DATE NOT NULL" +
                ");";
        db.execSQL(create_favourites_table);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_FAVOURITES);
        onCreate(db);
    }

    /**
     * Adds a drink to database
     * @param id drink id
     * @param name drink name
     * @param category drink category
     * @param glass preferred glass
     * @param instructions drink preparation instructions
     * @param ingredients drink ingredients
     * @param thumbnail_url drink thumbnail url
     * @return number of rows inserted, -1 if no drink was inserted
     */
    public long addToFavourites(int id, String name, String category, String glass, String instructions, String ingredients, String thumbnail_url){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FAVOURITES_ID, id);
        values.put(FAVOURITES_NAME, name);
        values.put(FAVOURITES_CATEGORY, category);
        values.put(FAVOURITES_GLASS, glass);
        values.put(FAVOURITES_INSTRUCTIONS, instructions);
        values.put(FAVOURITES_INGREDIENTS, ingredients);
        values.put(FAVOURITES_THUMBNAIL_URL, thumbnail_url);
        values.put(FAVOURITES_ADDED_AT, formatter.format(new Date()));
        long affected = db.insert(TABLE_FAVOURITES, null, values);
        db.close();
        return affected;
    }

    /**
     * Deletes drink from favourites
     * @param id drink id
     * @return number of rows deleted, 0 if no drink was deleted
     */
    public int deleteFromFavourites(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        int affected = db.delete(TABLE_FAVOURITES, FAVOURITES_ID+" = ?", new String[]{String.valueOf(id)});
        db.close();
        return affected;
    }

    /**
     * Return an ArrayList of HashMaps containing id, name, category, thumbnail url and added at
     * of drinks added as favourites.
     * @return list with info of drinks
     */
    public ArrayList<HashMap<String, String>> getFavourites(){
        ArrayList<HashMap<String, String>> entries = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FAVOURITES, new String[]{FAVOURITES_ID, FAVOURITES_NAME, FAVOURITES_CATEGORY, FAVOURITES_THUMBNAIL_URL, FAVOURITES_ADDED_AT}, null, null, null, null, FAVOURITES_ADDED_AT+" DESC");
        while(cursor.moveToNext()){
            HashMap<String, String> entry = new HashMap<>();
            entry.put(FAVOURITES_ID, cursor.getString(cursor.getColumnIndexOrThrow(FAVOURITES_ID)));
            entry.put(FAVOURITES_NAME, cursor.getString(cursor.getColumnIndexOrThrow(FAVOURITES_NAME)));
            entry.put(FAVOURITES_CATEGORY, cursor.getString(cursor.getColumnIndexOrThrow(FAVOURITES_CATEGORY)));
            entry.put(FAVOURITES_THUMBNAIL_URL, cursor.getString(cursor.getColumnIndexOrThrow(FAVOURITES_THUMBNAIL_URL)));
            entry.put(FAVOURITES_ADDED_AT, cursor.getString(cursor.getColumnIndexOrThrow(FAVOURITES_ADDED_AT)));
            entries.add(entry);
        }
        cursor.close();
        db.close();
        return entries;
    }

    public JSONObject getDrink(int id){
        JSONObject drink = new JSONObject();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FAVOURITES, new String[]{FAVOURITES_ID, FAVOURITES_NAME, FAVOURITES_CATEGORY, FAVOURITES_GLASS, FAVOURITES_INSTRUCTIONS, FAVOURITES_INGREDIENTS, FAVOURITES_THUMBNAIL_URL, FAVOURITES_ADDED_AT}, FAVOURITES_ID+" = ?", new String[]{String.valueOf(id)}, null, null, null);
        if(cursor.moveToFirst()){
            try {
                drink.put(ApiHandler.DRINK_ID, cursor.getString(cursor.getColumnIndexOrThrow(FAVOURITES_ID)));
                drink.put(ApiHandler.DRINK_NAME, cursor.getString(cursor.getColumnIndexOrThrow(FAVOURITES_NAME)));
                drink.put(ApiHandler.DRINK_CATEGORY, cursor.getString(cursor.getColumnIndexOrThrow(FAVOURITES_CATEGORY)));
                drink.put(ApiHandler.DRINK_GLASS, cursor.getString(cursor.getColumnIndexOrThrow(FAVOURITES_GLASS)));
                drink.put(ApiHandler.DRINK_INSTRUCTIONS, cursor.getString(cursor.getColumnIndexOrThrow(FAVOURITES_INSTRUCTIONS)));
                //map ingredients
                String[] entries = cursor.getString(cursor.getColumnIndexOrThrow(FAVOURITES_INGREDIENTS)).split("\n");
                for(int i=1;i<=entries.length;i++) {
                    String[] ingredients_measures = entries[i-1].split(";");
                    drink.put(ApiHandler.DRINK_INGREDIENT_BASE + i, ingredients_measures[0]);
                    drink.put(ApiHandler.DRINK_MEASURE_BASE + i, ingredients_measures[1]);
                }
                drink.put(ApiHandler.DRINK_THUMBNAIL, cursor.getString(cursor.getColumnIndexOrThrow(FAVOURITES_THUMBNAIL_URL)));
                drink.put(ApiHandler.DRINK_ADDED_AT, cursor.getString(cursor.getColumnIndexOrThrow(FAVOURITES_ADDED_AT)));
            }catch (Exception e){
                e.printStackTrace();
                drink = null;
            }
        }else{
            drink = null;
        }
        cursor.close();
        db.close();
        return drink;
    }

    /**
     * Checks if drink is added to favourites
     * @param id drink id
     * @return True if drink is added to favourites
     */
    public boolean drinkIsFavourite(int id){
        boolean exists;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FAVOURITES, new String[]{FAVOURITES_ID}, FAVOURITES_ID+" = ?", new String[]{String.valueOf(id)}, null, null, null);
        exists = cursor.moveToFirst();
        cursor.close();
        db.close();
        return exists;
    }
}