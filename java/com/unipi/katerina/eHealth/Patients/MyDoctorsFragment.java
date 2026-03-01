package com.unipi.katerina.eHealth.Patients;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MyDoctorsFragment extends Fragment {

    RecyclerView recyclerView;
    DoctorAdapter doctorAdapter;
    Spinner spinnerSpecialty;
    List<Doctor> allDoctors = new ArrayList<>(); // όλοι οι γιατροί
    List<Doctor> filteredDoctors = new ArrayList<>(); // φιλτραρισμένοι

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_doctors, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewDoctors);
        spinnerSpecialty = view.findViewById(R.id.spinnerSpecialty);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        String patientId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Παίρνει το ID του ασθενούς
        doctorAdapter = new DoctorAdapter(patientId);
        recyclerView.setAdapter(doctorAdapter);

        loadMyDoctors();

        return view;
    }

    private void loadMyDoctors() {
        String patientId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("Patients")
                .document(patientId)
                .collection("Visits")
                .get()
                .addOnSuccessListener(visitsSnapshot -> {
                    Set<String> doctorUids = new HashSet<>();
                    for (QueryDocumentSnapshot doc : visitsSnapshot) {
                        String doctorUid = doc.getString("doctorUid");
                        if (doctorUid != null) {
                            doctorUids.add(doctorUid);
                        }
                    }

                    if (!doctorUids.isEmpty()) {
                        fetchDoctors(new ArrayList<>(doctorUids));
                    }
                });
    }

    private void fetchDoctors(List<String> doctorUids) {
        FirebaseFirestore.getInstance()
                .collection("Doctors")
                .whereIn("uid", doctorUids)
                .get()
                .addOnSuccessListener(doctorsSnapshot -> {
                    allDoctors.clear();
                    for (QueryDocumentSnapshot doc : doctorsSnapshot) {
                        Doctor doctor = new Doctor(
                                doc.getString("FullName"),
                                doc.getString("specialty"),
                                doc.getString("email"),
                                doc.getString("clinicAddress"),
                                doc.getString("phone"),
                                doc.getString("uid")
                        );
                        allDoctors.add(doctor);
                    }

                    setupSpecialtySpinner();
                    applyFilter("Όλες"); // δείχνουμε όλους στην αρχή

                    // Ορίζουμε το click listener για κάθε γιατρό
                    doctorAdapter.setOnItemClickListener(doctor -> {
                        DoctorVisitDetailsFragment fragment = new DoctorVisitDetailsFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("patientId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        bundle.putString("doctorId", doctor.getUid());
                        fragment.setArguments(bundle);

                        getParentFragmentManager()
                                .beginTransaction()
                                .replace(R.id.patient_fragment_container, fragment)
                                .addToBackStack(null)
                                .commit();
                    });
                });
    }

    private void setupSpecialtySpinner() {
        // Βρίσκουμε όλες τις ειδικότητες χωρίς διπλότυπα
        Set<String> specialtiesSet = new HashSet<>();
        for (Doctor doctor : allDoctors) {
            if (doctor.getSpecialty() != null) {
                specialtiesSet.add(doctor.getSpecialty());
            }
        }

        List<String> specialtiesList = new ArrayList<>(specialtiesSet);
        Collections.sort(specialtiesList);
        specialtiesList.add(0, "Όλες");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                specialtiesList
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSpecialty.setAdapter(adapter);

        spinnerSpecialty.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = specialtiesList.get(position);
                applyFilter(selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                applyFilter("Όλες");
            }
        });
    }

    private void applyFilter(String specialty) {
        filteredDoctors.clear();
        if (specialty.equals("Όλες")) {
            filteredDoctors.addAll(allDoctors);
        } else {
            for (Doctor doctor : allDoctors) {
                if (specialty.equals(doctor.getSpecialty())) {
                    filteredDoctors.add(doctor);
                }
            }
        }
        doctorAdapter.setDoctors(filteredDoctors);
    }
}