package com.unipi.katerina.eHealth.Doctors;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.unipi.katerina.eHealth.Adapters.VisitAdapter;
import com.unipi.katerina.eHealth.R;
import com.unipi.katerina.eHealth.Models.Visit;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PatientDetailsFragment extends Fragment {

    RecyclerView recyclerView;
    VisitAdapter adapter;
    List<Visit> visitList = new ArrayList<>();
    List<Visit> allVisits = new ArrayList<>();  // Κρατάμε όλα τα visits χωρίς φιλτράρισμα
    FirebaseFirestore db;
    TextView emptyText;
    Spinner spinnerSpecialtyFilter, spinnerDateFilter;
    String patientId;
    List<String> specialtiesList = new ArrayList<>();

    public PatientDetailsFragment() {
        // Required empty public constructor
    }

    public static PatientDetailsFragment newInstance(String patientId) {
        PatientDetailsFragment fragment = new PatientDetailsFragment();
        Bundle args = new Bundle();
        args.putString("patientId", patientId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            patientId = getArguments().getString("patientId");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_patient_details, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewVisits);
        emptyText = view.findViewById(R.id.emptyTextVisits);
        spinnerSpecialtyFilter = view.findViewById(R.id.spinnerSpecialtyFilter);
        spinnerDateFilter = view.findViewById(R.id.spinnerDateFilter);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new VisitAdapter(visitList);
        recyclerView.setAdapter(adapter);

        loadSpecialtiesFromFirestore();
        loadVisits();
        setupDateSpinner();

        return view;
    }

    private void setupDateSpinner() {
        String[] dateOptions = {"Χωρίς ταξινόμηση", "Πιο πρόσφατη πρώτη", "Πιο παλιά πρώτη"};
        ArrayAdapter<String> dateAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, dateOptions);
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDateFilter.setAdapter(dateAdapter);

        spinnerDateFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { applyFilters(); }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadVisits() {
        if (patientId == null) {
            emptyText.setVisibility(View.VISIBLE);
            emptyText.setText("Δεν βρέθηκε ID ασθενούς.");
            return;
        }

        db.collection("Patients")
                .document(patientId)
                .collection("Visits")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allVisits.clear();

                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Visit visit = doc.toObject(Visit.class);
                            if (visit != null) allVisits.add(visit);
                        }
                        applyFilters();
                    } else {
                        emptyText.setVisibility(View.VISIBLE);
                        emptyText.setText("Δεν βρέθηκαν επισκέψεις.");
                    }
                })
                .addOnFailureListener(e -> {
                    emptyText.setVisibility(View.VISIBLE);
                    emptyText.setText("Σφάλμα κατά τη φόρτωση επισκέψεων.");
                });
    }

    private void applyFilters() {
        if (allVisits.isEmpty()) return;

        String specialtyFilter = spinnerSpecialtyFilter.getSelectedItem() != null
                ? spinnerSpecialtyFilter.getSelectedItem().toString()
                : "Όλες";

        String dateFilter = spinnerDateFilter.getSelectedItem() != null
                ? spinnerDateFilter.getSelectedItem().toString()
                : "Χωρίς ταξινόμηση";

        List<Visit> filteredList = new ArrayList<>();

        for (Visit visit : allVisits) {
            boolean matchesSpecialty = specialtyFilter.equals("Όλες") ||
                    (visit.getDoctorSpecialty() != null && visit.getDoctorSpecialty().equals(specialtyFilter));

            if (matchesSpecialty) filteredList.add(visit);
        }

        // Ταξινόμηση ανά ημερομηνία
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        if (dateFilter.equals("Πιο πρόσφατη πρώτη")) {
            filteredList.sort((v1, v2) -> {
                try {
                    Date d1 = sdf.parse(v1.getDate());
                    Date d2 = sdf.parse(v2.getDate());
                    return d2.compareTo(d1);
                } catch (Exception e) { return 0; }
            });
        } else if (dateFilter.equals("Πιο παλιά πρώτη")) {
            filteredList.sort((v1, v2) -> {
                try {
                    Date d1 = sdf.parse(v1.getDate());
                    Date d2 = sdf.parse(v2.getDate());
                    return d1.compareTo(d2);
                } catch (Exception e) { return 0; }
            });
        }

        visitList.clear();
        visitList.addAll(filteredList);
        adapter.notifyDataSetChanged();

        if (visitList.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            emptyText.setText("Δεν βρέθηκαν επισκέψεις με τα κριτήρια αναζήτησης.");
        } else {
            emptyText.setVisibility(View.GONE);
        }
    }

    // Διαβάζει τις ειδικότητες δυναμικά από το collection "Doctors"
    private void loadSpecialtiesFromFirestore() {
        db.collection("Doctors")
                .get()
                .addOnSuccessListener(snapshot -> {
                    Set<String> specialtySet = new HashSet<>();

                    // Προσθέτουμε όλες τις ειδικότητες
                    for (QueryDocumentSnapshot doc : snapshot) {
                        String specialty = doc.getString("specialty");
                        if (specialty != null && !specialty.trim().isEmpty()) {
                            specialtySet.add(specialty);
                        }
                    }

                    // Δημιουργούμε τη λίστα και ταξινομούμε αλφαβητικά
                    specialtiesList = new ArrayList<>(specialtySet);
                    Collections.sort(specialtiesList, String.CASE_INSENSITIVE_ORDER);

                    // Εισάγουμε το "Όλες" πάνω πάνω
                    specialtiesList.add(0, "Όλες");

                    // Φτιάχνουμε τον adapter
                    ArrayAdapter<String> specialtyAdapter = new ArrayAdapter<>(
                            getContext(),
                            android.R.layout.simple_spinner_item,
                            specialtiesList
                    );
                    specialtyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerSpecialtyFilter.setAdapter(specialtyAdapter);

                    spinnerSpecialtyFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            applyFilters();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });
                })
                .addOnFailureListener(e -> {
                    specialtiesList.clear();
                    specialtiesList.add("Όλες");
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            getContext(),
                            android.R.layout.simple_spinner_item,
                            specialtiesList
                    );
                    spinnerSpecialtyFilter.setAdapter(adapter);
                });
    }
}