package com.unipi.katerina.eHealth.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.unipi.katerina.eHealth.Adapters.DoctorApprovalAdapter;
import com.unipi.katerina.eHealth.Models.Doctor;
import com.unipi.katerina.eHealth.MainActivity;
import com.unipi.katerina.eHealth.R;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerDoctors;
    private FirebaseFirestore db;
    private DoctorApprovalAdapter adapter;
    private List<Doctor> doctorList;
    private Button btnLogout;
    private FirebaseAuth auth;
    private Spinner spinnerFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        recyclerDoctors = findViewById(R.id.recyclerDoctors);
        db = FirebaseFirestore.getInstance();
        doctorList = new ArrayList<>();
        adapter = new DoctorApprovalAdapter(this, doctorList, db);
        btnLogout = findViewById(R.id.btnLogout);
        auth = FirebaseAuth.getInstance();
        spinnerFilter = findViewById(R.id.spinnerFilter);

        recyclerDoctors.setLayoutManager(new LinearLayoutManager(this));
        recyclerDoctors.setAdapter(adapter);

        listenForDoctorUpdates(); // real-time updates
        setUpLogoutBtn();
        setupFilterSpinner();

        //Φορτώνει τους pending γιατρούς όταν ανοίγει ο πίνακας
        spinnerFilter.setSelection(1); // δείχνει Pending (0=Όλοι, 1=Pending, 2=Approved, 3=Rejected) από strings.xml
        loadDoctorsByStatus("Pending");
    }

    private void listenForDoctorUpdates() {
        db.collection("Doctors")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Toast.makeText(AdminDashboardActivity.this, "Σφάλμα φόρτωσης: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Διαχείριση αλλαγών σε πραγματικό χρόνο
                        for (DocumentChange change : value.getDocumentChanges()) {
                            Doctor doctor = change.getDocument().toObject(Doctor.class);
                            doctor.setUid(change.getDocument().getId());

                            switch (change.getType()) {
                                case ADDED:
                                    doctorList.add(doctor);
                                    break;
                                case MODIFIED:
                                    // Αν αλλάξει κατάσταση (π.χ. approved/rejected)
                                    for (int i = 0; i < doctorList.size(); i++) {
                                        if (doctorList.get(i).getUid().equals(doctor.getUid())) {
                                            doctorList.set(i, doctor);
                                            break;
                                        }
                                    }
                                    break;
                                case REMOVED:
                                    // Αν αφαιρεθεί γιατρός
                                    doctorList.removeIf(d -> d.getUid().equals(doctor.getUid()));
                                    break;
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void setUpLogoutBtn() {
        btnLogout.setOnClickListener(v -> {
            auth.signOut();
           // Toast.makeText(this, "Αποσυνδεθήκατε επιτυχώς!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private void setupFilterSpinner() {
        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                loadDoctorsByStatus(selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadDoctorsByStatus(String status) {
        Query query;
        if (status.equals("Όλοι")) {
            query = db.collection("Doctors");
        } else {
            // lowercase matching Firestore values
            query = db.collection("Doctors")
                    .whereEqualTo("statusFromAdmin", status.toLowerCase());
        }

        query.addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(AdminDashboardActivity.this, "Σφάλμα φόρτωσης: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            doctorList.clear();
            assert value != null;
            for (QueryDocumentSnapshot doc : value) {
                Doctor doctor = doc.toObject(Doctor.class);
                doctor.setUid(doc.getId());
                doctorList.add(doctor);
            }

            adapter.notifyDataSetChanged();
        });
    }

    public void reloadCurrentFilter() {
        String currentFilter = spinnerFilter.getSelectedItem().toString();
        loadDoctorsByStatus(currentFilter);
    }
}