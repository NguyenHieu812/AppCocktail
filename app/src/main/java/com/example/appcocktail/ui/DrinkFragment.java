package com.example.appcocktail.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.example.appcocktail.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DrinkFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String DRINK = "drink";


    private JSONObject drink;
    private String parsed_ingredients;
    private boolean parse_failed = false;
    private boolean drink_is_favourite;
    private boolean internet_available;

    public DrinkFragment() {
        // Required empty public constructor
    }

    /**
     * Get fragment with drink details
     *
     * @param drink json object with drink data, <br> random drink if null is provided
     * @return A new instance of fragment DetailsFragment.
     */
    public static DrinkFragment newInstance(@Nullable JSONObject drink) {
        DrinkFragment fragment = new DrinkFragment();
        Bundle args = new Bundle();
        if(drink != null) args.putString(DRINK, drink.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().getString(DRINK, null) != null) {
            try {
                drink = new JSONObject(getArguments().getString(DRINK));
            }catch (Exception e){
                parse_failed = true;
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        assert container != null;
        assert getContext() != null;
        container.removeAllViews();
        parsed_ingredients = "";
        // Inflate the layout for this fragment
        // add loading indicator ???
        internet_available = ApiHandler.checkForInternet(getContext());
        // await for api response
        if(drink == null && !parse_failed) {
            CountDownLatch latch = new CountDownLatch(1);
            ExecutorService main_executor = Executors.newSingleThreadExecutor();
            ApiHandler api = new ApiHandler(ApiHandler.GET_RANDOM_DRINK, null, latch);
            try {
                main_executor.execute(api);
                latch.await();
                drink = api.getDrinkData().getJSONArray("drinks").getJSONObject(0);
            } catch (Exception e) {
                e.printStackTrace();
                drink = null;
            } finally {
                main_executor.shutdown();
            }
        }
        // drink is favourite
        Database db = new Database(getContext());
        try {
            drink_is_favourite = db.drinkIsFavourite(drink.getInt(ApiHandler.DRINK_ID));
        }catch (Exception e){
            drink_is_favourite = false;
        }
        db.close();
        // layout setup
        LinearLayout main_layout = new LinearLayout(this.getContext());
        main_layout.setOrientation(LinearLayout.VERTICAL);
        container.addView(main_layout);
        if(!parse_failed && (internet_available || drink != null)) {
            TextView category_view = new TextView(getContext());
            category_view.setPadding(0, 40, 0, 40);
            category_view.setTextColor(getResources().getColor(R.color.light_gray, null));
            category_view.setTextSize(20);
            category_view.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            main_layout.addView(category_view);
            ImageView thumbnail_view = new ImageView(getContext());
            thumbnail_view.setMinimumWidth(600);
            thumbnail_view.setMinimumHeight(600);
            main_layout.addView(thumbnail_view);
            TextView name_view = new TextView(getContext());
            name_view.setPadding(0, 20, 0, 20);
            name_view.setTextColor(getResources().getColor(R.color.white, null));
            name_view.setTextSize(30);
            name_view.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            main_layout.addView(name_view);
            ScrollView recipe_scroll_container = new ScrollView(getContext());
            LinearLayout recipe_view = new LinearLayout(getContext());
            recipe_view.setOrientation(LinearLayout.VERTICAL);
            recipe_scroll_container.addView(recipe_view);
            TextView recipe_instructions = new TextView(getContext());
            recipe_instructions.setTextColor(getResources().getColor(R.color.white, null));
            recipe_instructions.setTextSize(14);
            recipe_instructions.setPadding(0, 20, 0, 20);
            recipe_instructions.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            recipe_view.addView(recipe_instructions);
            TextView glass_suggestion = new TextView(getContext());
            glass_suggestion.setTextColor(getResources().getColor(R.color.light_gray, null));
            glass_suggestion.setTextSize(14);
            glass_suggestion.setPadding(0, 20, 0, 20);
            glass_suggestion.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            recipe_view.addView(glass_suggestion);
            // preferred glass
            main_layout.addView(recipe_scroll_container);
            // render elements
            ExecutorService executor = Executors.newSingleThreadExecutor();
            try {
                assert drink != null;
                WebImageRenderer image_renderer = new WebImageRenderer(getActivity(), drink.getString(ApiHandler.DRINK_THUMBNAIL), thumbnail_view);
                executor.execute(image_renderer);
                name_view.setText(drink.getString(ApiHandler.DRINK_NAME));
                category_view.setText(drink.getString(ApiHandler.DRINK_CATEGORY));
                glass_suggestion.setText(getResources().getText(R.string.glass_suggestion_label)+" "+drink.getString(ApiHandler.DRINK_GLASS));
                recipe_instructions.setText(drink.getString(ApiHandler.DRINK_INSTRUCTIONS));
                int recipe_step = 1;
                try{
                    while(!drink.getString(ApiHandler.DRINK_INGREDIENT_BASE+recipe_step).equals("null")){
                        LinearLayout ingredient_row = new LinearLayout(getContext());
                        ingredient_row.setOrientation(LinearLayout.HORIZONTAL);
                        ingredient_row.setWeightSum(1f);
                        TextView ingredient_view = new TextView(getContext());
                        ingredient_view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,0.5f));
                        ingredient_view.setText(drink.getString(ApiHandler.DRINK_INGREDIENT_BASE+recipe_step));
                        ingredient_view.setTextColor(getResources().getColor(R.color.light_gray, null));
                        ingredient_view.setPadding(10, 5, 10, 5);
                        ingredient_view.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
                        ingredient_row.addView(ingredient_view);
                        TextView measure_view = new TextView(getContext());
                        measure_view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,0.5f));
                        String measure = drink.getString(ApiHandler.DRINK_MEASURE_BASE+recipe_step).replaceAll("\n", "");
                        if(measure.contains("null"))
                            measure_view.setText("At your discretion");
                        else
                            measure_view.setText(parseToMl(measure));
                        measure_view.setTextColor(getResources().getColor(R.color.light_gray, null));
                        measure_view.setPadding(10, 5, 10, 5);
                        measure_view.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                        ingredient_row.addView(measure_view);
                        recipe_view.addView(ingredient_row);
                        recipe_step += 1;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                TextView results_footer = new TextView(getContext());
                results_footer.setPadding(60, 18, 60, 18);
                results_footer.setTextSize(26);
                results_footer.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                results_footer.setTextColor(getResources().getColor(R.color.light_gray, null));
                results_footer.setText(". . .");
                recipe_view.addView(results_footer);
                // remove loading indicator ???
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this.getContext(), getString(R.string.error_fetch_api), Toast.LENGTH_LONG).show();
                // change loading indicator to error indicator ???
            } finally {
                executor.shutdown();
            }
        } else if(parse_failed && internet_available) {
            TextView no_results_view = new TextView(getContext());
            no_results_view.setTextSize(20);
            no_results_view.setPadding(0, 20, 0, 20);
            no_results_view.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            no_results_view.setTextColor(getResources().getColor(R.color.light_gray, null));
            no_results_view.setText(getString(R.string.error_json_parse));
            main_layout.addView(no_results_view);
        } else {
            TextView no_results_view = new TextView(getContext());
            no_results_view.setTextSize(20);
            no_results_view.setPadding(0, 20, 0, 20);
            no_results_view.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            no_results_view.setTextColor(getResources().getColor(R.color.light_gray, null));
            no_results_view.setText(getString(R.string.no_internet));
            main_layout.addView(no_results_view);
        }
        return inflater.inflate(R.layout.fragment_drink, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FloatingActionButton favourite_button = view.findViewById(R.id.floatingActionButton);
        if(drink_is_favourite) {
            favourite_button.setForeground(ResourcesCompat.getDrawable(getResources(), R.drawable.favourite_filled, null));
        }else{
            favourite_button.setForeground(ResourcesCompat.getDrawable(getResources(), R.drawable.favourite, null));
        }
        favourite_button.setOnClickListener(v -> {
            Database db = new Database(getContext());
            int id;
            try{
                id = drink.getInt(ApiHandler.DRINK_ID);
            }catch (Exception e){
                id = -1;
            }
            if(!db.drinkIsFavourite(id)) {
                try {
                    String name = drink.getString(ApiHandler.DRINK_NAME);
                    String category = drink.getString(ApiHandler.DRINK_CATEGORY);
                    String glass = drink.getString(ApiHandler.DRINK_GLASS);
                    String thumbnail_url = drink.getString(ApiHandler.DRINK_THUMBNAIL);
                    String instructions = drink.getString(ApiHandler.DRINK_INSTRUCTIONS);
                    int recipe_step = 1;
                    try {
                        while (!drink.getString(ApiHandler.DRINK_INGREDIENT_BASE + recipe_step).equals("null")) {
                            parsed_ingredients += drink.getString(ApiHandler.DRINK_INGREDIENT_BASE + recipe_step) + ";" + drink.getString(ApiHandler.DRINK_MEASURE_BASE + recipe_step) + "\n";
                            recipe_step += 1;
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    if (db.addToFavourites(id, name, category, glass, instructions, parsed_ingredients, thumbnail_url) > 0) {
                        Toast.makeText(getContext(), getResources().getText(R.string.added_to_favourites), Toast.LENGTH_SHORT).show();
                        v.setForeground(ResourcesCompat.getDrawable(getResources(), R.drawable.favourite_filled, null));
                    } else {
                        Toast.makeText(getContext(), getResources().getText(R.string.error_added_to_favourites), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), getResources().getText(R.string.error_json_parse), Toast.LENGTH_SHORT).show();
                }
            }else{
                if(db.deleteFromFavourites(id) > 0){
                    Toast.makeText(getContext(), getResources().getText(R.string.removed_from_favourites), Toast.LENGTH_SHORT).show();
                    v.setForeground(ResourcesCompat.getDrawable(getResources(), R.drawable.favourite, null));
                }else{
                    Toast.makeText(getContext(), getResources().getText(R.string.error_removed_from_favourites), Toast.LENGTH_SHORT).show();
                }
            }
        });

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        internet_available = ApiHandler.checkForInternet(requireContext());
        if(internet_available || drink != null) {
            try {
                this.requireView().findViewById(R.id.floatingActionButton).setVisibility(View.VISIBLE);
            }catch (Exception e){
                e.printStackTrace();
            }
        } else {
            try {
                this.requireView().findViewById(R.id.floatingActionButton).setVisibility(View.INVISIBLE);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        super.onResume();
    }

    /**
     * Parses measurements in given string if there are any of these:<br>
     * oz(ounce), qt(quart), cl(centiliters).<br>
     *
     * @param source_string string containing liquid measurement
     * @return string with measure units converted to milliliters, if there are none values of those
     *          specified unedited string is returned
     */
    private String parseToMl(String source_string){
        // freedom unit to ml
        double oz = 30.00;
        double qt = 1136.50;
        double pint = 568.0;
        double cl = 10.00;

        int measure_pos;
        double multiplier;
        String[] separated_source = source_string.split(" ");
        if (source_string.contains("oz")) {
            measure_pos = indexOf(separated_source, "oz");
            multiplier = oz;
        } else if(source_string.contains("qt")) {
            measure_pos = indexOf(separated_source, "qt");
            multiplier = qt;
        } else if(source_string.contains("cl")) {
            measure_pos = indexOf(separated_source, "cl");
            multiplier = cl;
        } else if(source_string.contains("pint")) {
            measure_pos = indexOf(separated_source, "pint");
            multiplier = pint;
        } else {
            return source_string;
        }
        double conversion_helper = 0;
        ArrayList<String> converted_string = new ArrayList<>();
        for(int i=0;i<measure_pos;i++){
            try {
                if (separated_source[i].contains("/")) {
                    String[] values = separated_source[i].split("/");
                    conversion_helper += (Double.parseDouble(values[0]) / Double.parseDouble(values[1])) * multiplier;
                } else if (separated_source[i].contains("-")) {
                    String[] values = separated_source[i].split("-");
                    converted_string.add(Integer.parseInt(values[0]) * multiplier + "-" + Integer.parseInt(values[1]) * multiplier);
                    converted_string.add("ml");
                    conversion_helper = -1;
                    break;
                } else {
                    conversion_helper += Double.parseDouble(separated_source[i]) * multiplier;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if(conversion_helper > 0) {
            converted_string.add(String.valueOf((int) conversion_helper));
            converted_string.add("ml");
        }
        converted_string.addAll(Arrays.asList(separated_source).subList(measure_pos + 1, separated_source.length));
        return String.join(" ", converted_string);
    }

    private int indexOf(String[] haystack, String needle){
        int index = -1;
        for(int i=0;i<haystack.length;i++){
            if(haystack[i].equals(needle)){
                index = i;
                break;
            }
        }
        return index;
    }
}
