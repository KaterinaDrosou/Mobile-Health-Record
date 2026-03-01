package com.unipi.katerina.eHealth.Patients;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.unipi.katerina.eHealth.R;

public class PatientDashboardActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_dashboard);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Load αρχική σελίδα
        loadFragment(new PatientHomeFragment());

        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selectedFragment = new PatientHomeFragment();
            } else if (id == R.id.nav_medical_id) {
                selectedFragment = new MedicalIdFragment();
            } else if (id == R.id.nav_history) {
                selectedFragment = new PatientHistoryFragment();
            } else if (id == R.id.nav_visit) {
                selectedFragment = new PatientVisitFragment();
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
                .replace(R.id.patient_fragment_container, fragment)
                .commit();
    }
}