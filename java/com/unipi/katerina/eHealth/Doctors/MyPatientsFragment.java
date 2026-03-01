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
import android.widget.SearchView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unipi.katerina.eHealth.Adapters.PatientAdapter;
import com.unipi.katerina.eHealth.Models.Patient;
import com.unipi.katerina.eHealth.R;

import java.util.ArrayList;
import java.util.List;

public class MyPatientsFragment extends Fragment {
    RecyclerView recyclerView;
    PatientAdapter adapter;
    List<Patient> patientList = new ArrayList<>();
    List<Patient> filteredList = new ArrayList<>();
    FirebaseFirestore db;
    FirebaseAuth auth;
    SearchView searchView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_patients, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewPatients);
        searchView = view.findViewById(R.id.searchAmka);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        //adapter = new PatientAdapter(filteredList); // συνδέουμε το adapter με τη filteredList
        adapter = new PatientAdapter(filteredList, patient -> {
            openPatientDetailsFragment(patient);
        });
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        loadMyPatients();

        // Listener για το search
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterPatients(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterPatients(newText);
                return true;
            }
        });

        return view;
    }

    private void loadMyPatients() {
        String doctorUid = auth.getCurrentUser().getUid();

        db.collection("Patients").get().addOnSuccessListener(patientSnapshots -> {
            patientList.clear();
            filteredList.clear();

            for (DocumentSnapshot patientDoc : patientSnapshots) {
                String patientId = patientDoc.getId();

                db.collection("Patients")
                        .document(patientId)
                        .collection("Visits")
                        .whereEqualTo("doctorUid", doctorUid) // για να βλέπει τις επισκέψεις μόνο του συνδεδεμένου ιατρού
                        .get()
                        .addOnSuccessListener(visitSnapshots -> {
                            if (!visitSnapshots.isEmpty()) {
                                Patient patient = patientDoc.toObject(Patient.class);
                                if (patient != null) {
                                    patient.setId(patientId);  // κάνουμε set το ID
                                    patientList.add(patient);
                                    filteredList.add(patient); // αρχικά δείχνουμε όλους
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        });
            }
        });
    }

    private void filterPatients(String query) {
        filteredList.clear();
        if (query == null || query.isEmpty()) {
            filteredList.addAll(patientList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Patient p : patientList) {
                if (p.getAmka() != null && p.getAmka().toLowerCase().contains(lowerQuery)) {
                    filteredList.add(p);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void openPatientDetailsFragment(Patient patient) {
        String doctorUid = auth.getCurrentUser().getUid();
        String patientId = patient.getId();

        if (doctorUid == null || patientId == null) {
            Toast.makeText(getContext(), "Λείπουν στοιχεία ασθενή ή γιατρού", Toast.LENGTH_SHORT).show();
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putString("patientId", patientId);
        bundle.putString("doctorUid", doctorUid);

        PatientDetailsFragment fragment = new PatientDetailsFragment();
        fragment.setArguments(bundle);

        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.doctor_fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}