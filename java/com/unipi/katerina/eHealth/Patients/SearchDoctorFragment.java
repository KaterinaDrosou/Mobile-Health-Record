package com.unipi.katerina.eHealth.Patients;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.unipi.katerina.eHealth.Adapters.DoctorAdapter;
import com.unipi.katerina.eHealth.Models.Doctor;
import com.unipi.katerina.eHealth.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchDoctorFragment extends Fragment {

    RecyclerView recyclerView;
    DoctorAdapter doctorAdapter;
    Spinner spinnerSpecialty;
    TextView emptyText;
    List<Doctor> allDoctors = new ArrayList<>();
    SearchView searchDoctorByName;
    List<Doctor> filteredDoctors = new ArrayList<>();
    Button btnOpenMap, btnFavoritesFilter;
    FrameLayout mapContainer;
    boolean showOnlyFavorites = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search_doctor, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewAllDoctors);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        String patientId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Παίρνει το ID του ασθενούς
        doctorAdapter = new DoctorAdapter(patientId);
        recyclerView.setAdapter(doctorAdapter);

        spinnerSpecialty = view.findViewById(R.id.spinnerSpecialtyAllDoctors);
        emptyText = view.findViewById(R.id.emptyTextAllDoctors);
        searchDoctorByName = view.findViewById(R.id.searchDoctorByName);
        btnOpenMap = view.findViewById(R.id.btnOpenMap);
        mapContainer = view.findViewById(R.id.mapContainer);
        btnFavoritesFilter = view.findViewById(R.id.btnFavoritesFilter);

        btnFavoritesFilter.setOnClickListener(v -> {
            showOnlyFavorites = !showOnlyFavorites;

            // Αλλάζουμε το κείμενο του κουμπιού ανάλογα με την κατάσταση
            btnFavoritesFilter.setText(showOnlyFavorites ? "Προβολή Όλων" : "Αγαπημένοι Ιατροί ❤️");

            String selected = spinnerSpecialty.getSelectedItem().toString();
            String query = searchDoctorByName.getQuery().toString();
            applyFilter(selected, query);
        });

        loadAllDoctors();
        setupSearch();

        btnOpenMap.setOnClickListener(v -> {
            mapContainer.setVisibility(View.VISIBLE); // δείχνουμε το FrameLayout του χάρτη
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.mapContainer, new DoctorsLeafletMapFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void loadAllDoctors() {
        FirebaseFirestore.getInstance()
                .collection("Doctors")
                .get()
                .addOnSuccessListener(snapshot -> {
                    allDoctors.clear();

                    Set<String> specialtiesSet = new HashSet<>();
                    specialtiesSet.add("Όλοι"); // Προσθήκη default επιλογής

                    for (QueryDocumentSnapshot doc : snapshot) {
                        Doctor doctor = new Doctor(
                                doc.getString("FullName"),
                                doc.getString("specialty"),
                                doc.getString("email"),
                                doc.getString("clinicAddress"),
                                doc.getString("phone"),
                                doc.getString("uid")
                        );

                        allDoctors.add(doctor);

                        if (doctor.getSpecialty() != null && !doctor.getSpecialty().isEmpty()) {
                            specialtiesSet.add(doctor.getSpecialty());
                        }
                    }

                    List<String> specialtiesList = new ArrayList<>(specialtiesSet); // Μετατροπή σε λίστα
                    specialtiesList.remove("Όλοι"); // Αφαίρεση "Όλοι" για ταξινόμηση
                    specialtiesList.sort(String::compareToIgnoreCase); // Αλφαβητική ταξινόμηση
                    specialtiesList.add(0, "Όλοι"); // Προσθήκη "Όλοι" στην αρχή
                    setupSpinner(specialtiesList);
                    applyFilter("Όλοι", "");  // Default φίλτρο Όλοι
                })
                .addOnFailureListener(e ->
                        emptyText.setText("Σφάλμα κατά τη φόρτωση ιατρών.")
                );
    }

    private void setupSpinner(List<String> specialties) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, specialties);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSpecialty.setAdapter(adapter);
        // Θέτω ως default επιλογή το "Όλοι"
        int position = adapter.getPosition("Όλοι");
        if (position >= 0) {
            spinnerSpecialty.setSelection(position);
        }

        spinnerSpecialty.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = specialties.get(position);
                String query = searchDoctorByName.getQuery().toString();
                applyFilter(selected, query);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                applyFilter("Όλοι", "");
            }
        });
    }

    private void setupSearch() {
        searchDoctorByName.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String selected = spinnerSpecialty.getSelectedItem().toString();
                applyFilter(selected, query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String selected = spinnerSpecialty.getSelectedItem().toString();
                applyFilter(selected, newText);
                return true;
            }
        });
    }

    private void applyFilter(String specialtyFilter, String nameQuery) {
        filteredDoctors.clear();
        String lowerQuery = nameQuery.toLowerCase();

        for (Doctor doc : allDoctors) {
            boolean matchesSpecialty = specialtyFilter.equals("Όλοι") ||
                    (doc.getSpecialty() != null && doc.getSpecialty().equalsIgnoreCase(specialtyFilter));

            boolean matchesName = doc.getFullName() != null &&
                    doc.getFullName().toLowerCase().contains(lowerQuery);

            boolean matchesFavorites = !showOnlyFavorites ||
                    doctorAdapter.getFavoriteDoctorUids().contains(doc.getUid());

            if (matchesSpecialty && matchesName && matchesFavorites) {
                filteredDoctors.add(doc);
            }
        }
        doctorAdapter.setDoctors(filteredDoctors);

        if (filteredDoctors.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            emptyText.setText("Δεν βρέθηκαν ιατροί για αυτή την αναζήτηση.");
        } else {
            emptyText.setVisibility(View.GONE);
        }
    }
}