package com.unipi.katerina.eHealth.Patients;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.unipi.katerina.eHealth.Adapters.PastVisitsAdapter;
import com.unipi.katerina.eHealth.R;

import java.util.*;

public class PastVisitsFragment extends Fragment {

    RecyclerView recyclerView;
    View emptyView;
    FirebaseFirestore db;
    String patientId;
    final List<Map<String, Object>> visits = new ArrayList<>();
    PastVisitsAdapter adapter;

    public PastVisitsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_past_visits, container, false);

        recyclerView = view.findViewById(R.id.recyclerPastVisits);
        emptyView = view.findViewById(R.id.emptyState);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PastVisitsAdapter(visits);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        patientId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        loadPastVisits();
        return view;
    }

    private void loadPastVisits() {
        if (patientId == null) {
            return;
        }

        db.collection("Patients")
                .document(patientId)
                .collection("Visits")
                .get()
                .addOnSuccessListener(snapshot -> {
                    visits.clear();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        visits.add(doc.getData());
                    }

                    if (visits.isEmpty()) {
                        emptyView.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        emptyView.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Σφάλμα φόρτωσης επισκέψεων", Toast.LENGTH_SHORT).show());
    }
}