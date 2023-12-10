package com.example.appcocktail.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.appcocktail.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private Button button;
    private TextView textView;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        // Hide action bar
        try {
            ActionBar actionBar = this.getSupportActionBar();
            assert actionBar != null;
            actionBar.hide();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        // Set up UI components
        textView = findViewById(R.id.user_details);
        button = findViewById(R.id.logout);

        // Check if user is logged in
        if (user == null) {
            // Redirect to Login if user is not logged in
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        } else {
            // Set user email if logged in
            textView.setText(user.getEmail());
        }

        // Handle click on logout button
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        // Set up bottom navigation fragment swapping
        setUpBottomNavigation();
    }

    private void setUpBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int nav_current = bottomNavigationView.getSelectedItemId();
            int nav_next = item.getItemId();
            if (nav_next != nav_current && nav_next == R.id.search) {
                setFragment(SearchFragment.newInstance());
                return true;
            } else if (nav_next == R.id.home) {
                setFragment(DrinkFragment.newInstance(null));
                return true;
            } else if (nav_next != nav_current && nav_next == R.id.favourites) {
                setFragment(FavouriteFragment.newInstance());
                return true;
            }
            return false;
        });
        bottomNavigationView.setSelectedItemId(R.id.home);
    }

    /**
     * Set fragment to be displayed in main container
     *
     * @param fragment fragment to be displayed
     */
    private void setFragment(Fragment fragment) {
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.main_container, fragment)
                .addToBackStack("main_stack")
                .commit();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 1) {
            fragmentManager.popBackStackImmediate();
        } else {
            Toast.makeText(getApplicationContext(), getResources().getText(R.string.confirm_exit), Toast.LENGTH_SHORT).show();
            super.onBackPressed();
        }
    }
}
