package com.unipi.katerina.eHealth.Patients;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import com.unipi.katerina.eHealth.R;
import com.unipi.katerina.eHealth.Models.Visit;
import com.unipi.katerina.eHealth.Adapters.VisitAdapter;
import java.util.*;

public class PatientVisitFragment extends Fragment {
    RecyclerView recyclerVisits;
    Button btnClearDateFilter;
    List<Visit> allVisits = new ArrayList<>(); // Κρατάει όλες τις επισκέψεις
    VisitAdapter visitAdapter;
    List<Visit> visitList = new ArrayList<>();
    FirebaseFirestore db;
    EditText editTextFilterDate;
    Spinner spinnerSpecialty;
    SearchView searchDoctor;
    String selectedSpecialty = "Όλες";
    String searchQuery = "";

    public PatientVisitFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_patient_visit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerVisits = view.findViewById(R.id.recyclerVisits);
        editTextFilterDate = view.findViewById(R.id.editTextFilterDate);
        spinnerSpecialty = view.findViewById(R.id.spinnerSpecialty);
        searchDoctor = view.findViewById(R.id.searchDoctor);
        btnClearDateFilter = view.findViewById(R.id.btnClearDateFilter);

        recyclerVisits.setLayoutManager(new LinearLayoutManager(getContext()));
        visitAdapter = new VisitAdapter(visitList);
        recyclerVisits.setAdapter(visitAdapter);

        db = FirebaseFirestore.getInstance();

        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Βρίσκουμε το document ID του ασθενούς με το uid από το collection Patients
        db.collection("Patients")
                .whereEqualTo("uid", currentUserUid)
                .get()
                .addOnSuccessListener(patientSnapshot -> {
                    if (!patientSnapshot.isEmpty()) {
                        String patientDocId = patientSnapshot.getDocuments().get(0).getId();
                        loadVisitsForPatient(patientDocId);
                    } else {
                        Toast.makeText(getContext(), "Δεν βρέθηκε ασθενής με το uid!", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Σφάλμα στη φόρτωση ασθενών: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });

        // DatePicker για επιλογή ημερομηνίας φιλτραρίσματος
        editTextFilterDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                    (view1, year1, month1, dayOfMonth) -> {
                        String selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, month1 + 1, year1);
                        editTextFilterDate.setText(selectedDate);
                        filterVisitsByDate(selectedDate);
                    }, year, month, day);
            datePickerDialog.show();
        });

        btnClearDateFilter.setOnClickListener(v -> {
            editTextFilterDate.setText("");
            selectedSpecialty = "Όλες";
            searchQuery = "";
            searchDoctor.setQuery("", false);
            spinnerSpecialty.setSelection(0);
            applyFilters();
        });

        // Αναζήτηση με όνομα γιατρού
        searchDoctor.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchQuery = newText.toLowerCase();
                applyFilters();
                return true;
            }
        });
    }

    private void loadVisitsForPatient(String patientDocId) {
        db.collection("Patients").document(patientDocId)
                .collection("Visits")
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(visitsSnapshot -> {
                    allVisits.clear();
                    for (DocumentSnapshot doc : visitsSnapshot) {
                        Visit visit = doc.toObject(Visit.class);
                        if (visit != null) allVisits.add(visit);
                    }
                    setupSpecialtySpinner();
                    applyFilters();
                });
    }

    private void setupSpecialtySpinner() {
        // Μαζεύουμε τις ειδικότητες δυναμικά
        Set<String> specialties = new HashSet<>();
        for (Visit v : allVisits) {
            if (v.getDoctorSpecialty() != null)
                specialties.add(v.getDoctorSpecialty());
        }

        List<String> specialtiesList = new ArrayList<>();
        specialtiesList.add("Όλες");
        specialtiesList.addAll(specialties);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                specialtiesList
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSpecialty.setAdapter(adapter);

        spinnerSpecialty.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                selectedSpecialty = specialtiesList.get(pos);
                applyFilters();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void applyFilters() {
        String dateFilter = editTextFilterDate.getText().toString();
        visitList.clear();

        for (Visit v : allVisits) {
            boolean matchesDate = dateFilter.isEmpty() || (v.getDate() != null && v.getDate().equals(dateFilter));
            boolean matchesSpecialty = selectedSpecialty.equals("Όλες") ||
                    (v.getDoctorSpecialty() != null && v.getDoctorSpecialty().equals(selectedSpecialty));
            boolean matchesName = searchQuery.isEmpty() ||
                    (v.getDoctorName() != null && v.getDoctorName().toLowerCase().contains(searchQuery));

            if (matchesDate && matchesSpecialty && matchesName)
                visitList.add(v);
        }

        visitAdapter.updateList(visitList);
    }

    private void filterVisitsByDate(String dateFilter) {
        if (dateFilter.isEmpty()) {
            visitAdapter.updateList(visitList);
        } else {
            List<Visit> filtered = new ArrayList<>();
            for (Visit visit : visitList) {
                if (visit.getDate() != null && visit.getDate().equals(dateFilter)) {
                    filtered.add(visit);
                }
            }
            visitAdapter.updateList(filtered);
        }
    }
}