package com.unipi.katerina.eHealth.Patients;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unipi.katerina.eHealth.Adapters.VisitAdapter;
import com.unipi.katerina.eHealth.R;
import com.unipi.katerina.eHealth.Models.Visit;

import java.util.ArrayList;
import java.util.List;

public class DoctorVisitDetailsFragment extends Fragment {

    RecyclerView recyclerView;
    VisitAdapter adapter;
    List<Visit> visitList = new ArrayList<>();
    FirebaseFirestore db;
    TextView emptyText;
    String patientId, doctorUid;

    public DoctorVisitDetailsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_doctor_visit_details, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewDoctorVisits);
        emptyText = view.findViewById(R.id.emptyTextDoctorVisits);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new VisitAdapter(visitList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // Παίρνει το patientId και το doctorId από το Bundle που βρίσκεται στο MyDoctorsFragment
        if (getArguments() != null) {
            patientId = getArguments().getString("patientId");
            doctorUid = getArguments().getString("doctorId");
        }

        loadVisits();

        return view;
    }

    private void loadVisits() {
        if (patientId == null || doctorUid == null) {
            emptyText.setVisibility(View.VISIBLE);
            emptyText.setText("Λείπουν στοιχεία ασθενή ή ιατρού.");
            return;
        }

        db.collection("Patients")
                .document(patientId)
                .collection("Visits")
                .whereEqualTo("doctorUid", doctorUid)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    visitList.clear();

                    if (!querySnapshots.isEmpty()) {
                        for (DocumentSnapshot doc : querySnapshots) {
                            Visit visit = doc.toObject(Visit.class);
                            visitList.add(visit);
                        }
                        adapter.notifyDataSetChanged();
                        emptyText.setVisibility(View.GONE);
                    } else {
                        emptyText.setVisibility(View.VISIBLE);
                        emptyText.setText("Δεν βρέθηκαν επισκέψεις για αυτόν τον ιατρό.");
                    }
                })
                .addOnFailureListener(e -> {
                    emptyText.setVisibility(View.VISIBLE);
                    emptyText.setText("Σφάλμα κατά τη φόρτωση επισκέψεων.");
                });
    }
}