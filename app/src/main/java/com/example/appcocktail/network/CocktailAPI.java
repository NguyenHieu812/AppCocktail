package com.example.appcocktail.network;

import com.example.appcocktail.data.Cocktail;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface CocktailAPI {
    @GET("/api/cocktails")
    Call<List<Cocktail>> getCocktails();
    // Các phương thức CRUD khác
}
