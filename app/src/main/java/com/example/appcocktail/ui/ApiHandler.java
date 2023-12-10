package com.example.appcocktail.ui;
import android.content.Context;
import android.net.ConnectivityManager;

import androidx.annotation.Nullable;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class ApiHandler implements Runnable{
    public static final int GET_RANDOM_DRINK = 0;
    // names need to be lowercase and separated by floor(_)
    public static final int SEARCH_DRINK_BY_NAME = 1;
    public static final int SEARCH_ALCOHOLIC = 2;
    public static final int GET_DRINK_DETAILS = 3;
    public static final int GET_INGREDIENT_DETAILS = 4;

    public static final String DRINK_ID = "idDrink";
    public static final String DRINK_NAME = "strDrink";
    public static final String DRINK_CATEGORY = "strCategory";
    public static final String DRINK_GLASS = "strGlass";
    public static final String DRINK_INSTRUCTIONS = "strInstructions";
    public static final String DRINK_THUMBNAIL = "strDrinkThumb";
    public static final String DRINK_INGREDIENT_BASE = "strIngredient";
    public static final String DRINK_MEASURE_BASE = "strMeasure";
    public static final String DRINK_ADDED_AT = "strAddedAt";

    private URL api_url;
    private JSONObject drink_data = null;
    private final CountDownLatch latch;

    /**
     * Runnable that is responsible for fetching data from API,
     * after running an instance you can call latch.await() after
     * passing a CountDownLatch as 3rd parameter.
     *
     * @param type type of api method
     * @param argument argument to be passed to api
     * @param latch a CountDownLatch that will notify calling thread after fetching data from API
     */
    public ApiHandler(int type, @Nullable String argument, CountDownLatch latch){
        String API_KEY = "1";
        String BASE_URL = "https://www.thecocktaildb.com/api/json/v1/"+API_KEY+"/";
        this.latch = latch;

        try {
            String url;
            switch (type){
                case GET_RANDOM_DRINK:
                    url = BASE_URL+"random.php";
                    this.api_url = new URL(url);
                    break;
                case SEARCH_DRINK_BY_NAME:
                    assert argument != null;
                    url = BASE_URL+"search.php?s=" + argument;
                    this.api_url = new URL(url);
                    break;
                case SEARCH_ALCOHOLIC:
                    url = BASE_URL+"filter.php?a=" + argument;
                    this.api_url = new URL(url);
                    break;
                case GET_DRINK_DETAILS:
                    url = BASE_URL+"lookup.php?i=" + argument;
                    this.api_url = new URL(url);
                    break;
                case GET_INGREDIENT_DETAILS:
                    url = BASE_URL+"filter.php?i=" + argument;
                    this.api_url = new URL(url);
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Fetch data from api and parse it to JSONObject
     * @return data fetched from api
     */
    private JSONObject getData() {
        try {
            Scanner scanner = new Scanner(api_url.openStream());
            JSONTokener json_encoder = new JSONTokener(scanner.useDelimiter("\\Z").next());
            JSONObject json = new JSONObject(json_encoder);
            scanner.close();
            return json;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Return data fetched from api, null otherwise
     * @return json data
     */
    public JSONObject getDrinkData(){
        return this.drink_data;
    }

    @Override
    public void run() {
        try {
            this.drink_data = this.getData();
            // release calling thread lock
            latch.countDown();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Checks whether internet connection is available
     * @param context application context
     * @return True if internet connection is available
     */
    public static boolean checkForInternet(Context context){
        ConnectivityManager connectivity_manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return (connectivity_manager.getActiveNetworkInfo() != null
                && connectivity_manager.getActiveNetworkInfo().isAvailable()
                && connectivity_manager.getActiveNetworkInfo().isConnected());
    }

}
