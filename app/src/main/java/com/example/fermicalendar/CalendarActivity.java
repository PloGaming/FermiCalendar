package com.example.fermicalendar;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.transition.MaterialSharedAxis;

public class CalendarActivity extends AppCompatActivity {

    private Fragment selectedFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            boolean forward = false;

            // Can't use the switch statement because it requires compile time constants
            if(item.getItemId() == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if(item.getItemId() == R.id.nav_search) {
                selectedFragment = new SearchFragment();
                forward = true;
            }

            // Add transitions effects
            selectedFragment.setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.X, forward));
            selectedFragment.setReturnTransition(new MaterialSharedAxis(MaterialSharedAxis.X, !forward));

            // Execute the exchange
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainerView, selectedFragment)
                    .commit();

            return true;
        });

        // Set initial fragment
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }
}
