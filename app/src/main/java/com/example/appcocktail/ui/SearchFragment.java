package com.example.appcocktail.ui;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.example.appcocktail.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

public class SearchFragment extends Fragment {
    private LinearLayout content_layout;
    private AtomicBoolean loading_finished;
    private EditText search_view;
    private String recently_searched;

    public SearchFragment() {
        // Required empty public constructor
    }
    /**
     * Get fragment that enables user to search for drinks
     *
     * @return A new instance of fragment SearchFragment.
     */
    public static SearchFragment newInstance() {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        //
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        assert container != null;
        container.removeAllViews();
        loading_finished = new AtomicBoolean();
        // get fragment layout context
        LinearLayout main_layout = new LinearLayout(getContext());
        main_layout.setOrientation(LinearLayout.VERTICAL);
        main_layout.setPadding(0, 10, 0, 0);
        // scrollable layout setup
        ScrollView scroll_view = new ScrollView(this.getContext());
        scroll_view.setPadding(0, 10, 0, 0);

        search_view = new EditText(getContext());
        search_view.setPadding(60, 18, 60, 18);
        search_view.setTextColor(getResources().getColor(R.color.white, null));
        search_view.setHintTextColor(getResources().getColor(R.color.light_gray, null));
        search_view.setHint(getResources().getString(R.string.search_hint));
        search_view.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.search_border, null));
        search_view.setEms(20);
        search_view.setSingleLine();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            search_view.setTextCursorDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.edittext_cursor, null));
        }

        content_layout = new LinearLayout(this.getContext());
        content_layout.setOrientation(LinearLayout.VERTICAL);
        scroll_view.addView(content_layout);
        main_layout.addView(search_view);
        main_layout.addView(scroll_view);
        container.addView(main_layout);

        search_view.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                String phrase = editable.toString().replaceAll("\\s+", "_");
                if(phrase.length() > 0 && loading_finished.get()) {
                    renderSearchElements(phrase);
                    recently_searched = phrase;
                }
            }
        });
        //show some random drinks
        String random_empty_search = String.valueOf((char) ThreadLocalRandom.current().nextInt(97, 123));
        renderSearchElements(random_empty_search);
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    private void renderSearchElements(@NonNull String search_phrase){
        loading_finished.set(false);
        content_layout.removeAllViews();
        CountDownLatch latch = new CountDownLatch(1);
        ExecutorService main_executor = Executors.newSingleThreadExecutor();
        ApiHandler api = new ApiHandler(ApiHandler.SEARCH_DRINK_BY_NAME, search_phrase, latch);
        try {
            main_executor.execute(api);
            latch.await();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            main_executor.shutdown();
        }
        ExecutorService executor = Executors.newFixedThreadPool(10);
        try {
            JSONObject drink_data = api.getDrinkData();
            try{
                JSONArray drinks = drink_data.getJSONArray("drinks");
                for(int i=0; i<drinks.length(); i++) {
                    JSONObject drink = drinks.getJSONObject(i);
                    LinearLayout entry_layout = new LinearLayout(getContext());
                    entry_layout.setOrientation(LinearLayout.HORIZONTAL);
                    entry_layout.setVerticalGravity(View.TEXT_ALIGNMENT_CENTER);
                    entry_layout.setPadding(0, 5, 0, 5);
                    ImageView thumbnail = new ImageView(getContext());
                    thumbnail.setLayoutParams(new ViewGroup.LayoutParams(300, 300));
                    thumbnail.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    thumbnail.setPadding(10,0, 10, 0);
                    WebImageRenderer renderer = new WebImageRenderer(getActivity(), drink.get(ApiHandler.DRINK_THUMBNAIL).toString(), thumbnail);
                    executor.execute(renderer);
                    entry_layout.addView(thumbnail);

                    LinearLayout desc_layout = new LinearLayout(getContext());
                    desc_layout.setOrientation(LinearLayout.VERTICAL);
                    TextView name = new TextView(getContext());
                    name.setText(drink.get(ApiHandler.DRINK_NAME).toString());
                    name.setTextColor(getResources().getColor(R.color.white, null));
                    desc_layout.addView(name);
                    TextView desc = new TextView(getContext());
                    desc.setText(drink.get(ApiHandler.DRINK_CATEGORY).toString());
                    desc.setTextColor(getResources().getColor(R.color.light_gray, null));
                    desc_layout.addView(desc);
                    entry_layout.addView(desc_layout);
                    entry_layout.setOnClickListener(v -> getParentFragmentManager()
                            .beginTransaction()
                            .replace(R.id.main_container, DrinkFragment.newInstance(drink), "DrinkDetailsFragment")
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
            }
            // remove loading indicator ???
        }catch(Exception e){
            e.printStackTrace();
            Toast.makeText(this.getContext(), getString(R.string.error_fetch_api), Toast.LENGTH_LONG).show();
            // change loading indicator to error indicator ???
        }finally {
            executor.shutdown();
            loading_finished.set(true);
        }
    }

    @Override
    public void onResume() {
        search_view.setText(recently_searched);
        super.onResume();
    }
}
