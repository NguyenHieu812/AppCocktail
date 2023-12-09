package com.example.appcocktail.ui;

import android.content.Context;

import androidx.room.Room;

import com.example.appcocktail.data.Cocktail;
import com.example.appcocktail.data.CocktailDao;
import com.example.appcocktail.data.CocktailDatabase;
import com.example.appcocktail.network.CocktailAPI;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.List;
public class CocktailRepository {
    private CocktailAPI cocktailAPI;
    private CocktailDao cocktailDao;

    public CocktailRepository(Context context) {
        // Khởi tạo và sử dụng Room Database
        CocktailDatabase db = Room.databaseBuilder(context.getApplicationContext(),
                CocktailDatabase.class, "cocktail-db").build();
        cocktailDao = db.cocktailDao();

        // Khởi tạo và sử dụng Retrofit để gửi yêu cầu đến API
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api-ninjas.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        cocktailAPI = retrofit.create(CocktailAPI.class);
    }

    public void getCocktailsFromAPIAndSaveToDB() {
        Call<List<Cocktail>> call = cocktailAPI.getCocktails();
        call.enqueue(new Callback<List<Cocktail>>() {
            @Override
            public void onResponse(Call<List<Cocktail>> call, Response<List<Cocktail>> response) {
                if (response.isSuccessful()) {
                    List<Cocktail> cocktails = response.body();
                    // Lưu dữ liệu từ API vào Room Database
                    new Thread(() -> {
                        cocktailDao.insertCocktails(cocktails);
                    }).start();
                } else {
                    // Xử lý lỗi
                }
            }

            @Override
            public void onFailure(Call<List<Cocktail>> call, Throwable t) {
                // Xử lý lỗi kết nối
            }
        });
    }

    // Các phương thức khác để thực hiện các tác vụ cụ thể, như thêm, xoá, sửa dữ liệu, vv.
}

