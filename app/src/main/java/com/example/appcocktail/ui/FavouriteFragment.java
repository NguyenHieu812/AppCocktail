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
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.appcocktail.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FavouriteFragment extends Fragment {

    public FavouriteFragment() {
        // Required empty public constructor
    }

    /**
     * Get fragment with drinks added as favourite
     *
     * @return A new instance of fragment FavouriteFragment.
     */
    public static FavouriteFragment newInstance() {
        FavouriteFragment fragment = new FavouriteFragment();
        Bundle args = new Bundle();
        //
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        assert container != null;
        container.removeAllViews();

        LinearLayout main_layout = new LinearLayout(getContext());
        main_layout.setOrientation(LinearLayout.VERTICAL);
        container.addView(main_layout);
        TextView title = new TextView(getContext());
        title.setPadding(60, 18, 60, 18);
        title.setTextSize(26);
        title.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        title.setTextColor(getResources().getColor(R.color.white, null));
        title.setText(getText(R.string.favourites_title));
        main_layout.addView(title);
        LinearLayout content_layout = new LinearLayout(this.getContext());
        content_layout.setOrientation(LinearLayout.VERTICAL);
        ScrollView scroll_view = new ScrollView(getContext());
        scroll_view.addView(content_layout);
        main_layout.addView(scroll_view);
        content_layout.removeAllViews();

        Database db = new Database(getContext());
        ArrayList<HashMap<String, String>> drinks = db.getFavourites();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        try{
            // no drinks in favourites
            if(drinks.size() == 0){
                TextView no_results_view = new TextView(getContext());
                no_results_view.setTextSize(20);
                no_results_view.setPadding(0, 20, 0, 20);
                no_results_view.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                no_results_view.setTextColor(getResources().getColor(R.color.light_gray, null));
                no_results_view.setText(getString(R.string.no_favourite_drinks));
                content_layout.addView(no_results_view);
            }
            // render favourite drinks
            for(HashMap<String, String> drink : drinks) {
                LinearLayout entry_layout = new LinearLayout(getContext());
                entry_layout.setOrientation(LinearLayout.HORIZONTAL);
                entry_layout.setVerticalGravity(View.TEXT_ALIGNMENT_CENTER);
                entry_layout.setPadding(0, 5, 0, 5);
                ImageView thumbnail = new ImageView(getContext());
                thumbnail.setLayoutParams(new ViewGroup.LayoutParams(300, 300));
                thumbnail.setScaleType(ImageView.ScaleType.FIT_CENTER);
                thumbnail.setPadding(10,0, 10, 0);
                WebImageRenderer renderer = new WebImageRenderer(getActivity(), drink.get(Database.FAVOURITES_THUMBNAIL_URL), thumbnail);
                executor.execute(renderer);
                entry_layout.addView(thumbnail);

                LinearLayout desc_layout = new LinearLayout(getContext());
                desc_layout.setOrientation(LinearLayout.VERTICAL);
                TextView name = new TextView(getContext());
                name.setText(drink.get(Database.FAVOURITES_NAME));
                name.setTextColor(getResources().getColor(R.color.white, null));
                desc_layout.addView(name);
                TextView desc = new TextView(getContext());
                desc.setText(drink.get(Database.FAVOURITES_CATEGORY));
                desc.setTextColor(getResources().getColor(R.color.light_gray, null));
                desc_layout.addView(desc);
                TextView added_at = new TextView(getContext());
                added_at.setPadding(0, 80, 0, 0);
                added_at.setText(getText(R.string.added_at_label)+" "+drink.get(Database.FAVOURITES_ADDED_AT));
                added_at.setTextColor(getResources().getColor(R.color.light_gray, null));
                desc_layout.addView(added_at);
                entry_layout.addView(desc_layout);
                entry_layout.setOnClickListener(v -> getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_container,
                                DrinkFragment.newInstance(db.getDrink(Integer.parseInt(Objects.requireNonNull(drink.get(Database.FAVOURITES_ID))))),
                                "DrinkDetailsFragment")
                        .addToBackStack("DrinkDetailsFragment")
                        .commit()
                );
                content_layout.addView(entry_layout);
            }
            TextView results_footer = new TextView(getContext());
            results_footer.setPadding(60, 18, 60, 18);
            results_footer.setTextSize(26);
            results_footer.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            results_footer.setTextColor(getResources().getColor(R.color.light_gray, null));
            results_footer.setText(". . .");
            content_layout.addView(results_footer);
        } catch(Exception e){
            e.printStackTrace();
            TextView no_results_view = new TextView(getContext());
            no_results_view.setTextSize(20);
            no_results_view.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            no_results_view.setTextColor(getResources().getColor(R.color.light_gray, null));
            no_results_view.setText(getString(R.string.error_no_results));
            content_layout.addView(no_results_view);
        } finally {
            executor.shutdown();
            db.close();
        }


        return inflater.inflate(R.layout.fragment_favourite, container, false);
    }
}

