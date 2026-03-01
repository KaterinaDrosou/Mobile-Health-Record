package com.unipi.katerina.eHealth.Doctors;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.unipi.katerina.eHealth.R;

public class DoctorDashboardActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_dashboard);

        bottomNavigation = findViewById(R.id.bottom_Navigation);

        // Load αρχική σελίδα
        loadFragment(new DoctorHomeFragment());

        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int id = item.getItemId();

            if (id == R.id.navigation_home) {
                selectedFragment = new DoctorHomeFragment();
            } else if (id == R.id.navigation_search_patient) {
                selectedFragment = new SearchPatientFragment();
            } else if (id == R.id.navigation_my_patients) {
                selectedFragment = new MyPatientsFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }

            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.doctor_fragment_container, fragment)
                .commit();
    }
}