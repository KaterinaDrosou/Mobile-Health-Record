package com.unipi.katerina.eHealth.Patients;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.unipi.katerina.eHealth.Adapters.AppointmentsAdapter;
import com.unipi.katerina.eHealth.R;
import com.unipi.katerina.eHealth.Models.AppointmentItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MyAppointmentsFragment extends Fragment {

    SwipeRefreshLayout swipeRefresh;
    RecyclerView recyclerView;
    View emptyView;
    Button btnPastVisits;
    String patientId;
    FirebaseFirestore db;
    final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    AppointmentsAdapter adapter;
    final List<AppointmentItem> data = new ArrayList<>();

    public MyAppointmentsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_my_appointments, container, false);

        swipeRefresh = v.findViewById(R.id.swipeRefresh);
        recyclerView = v.findViewById(R.id.recyclerAppointments);
        emptyView = v.findViewById(R.id.emptyState);
        btnPastVisits = v.findViewById(R.id.btnPastVisits);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AppointmentsAdapter(data, this::cancelAppointment);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        patientId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadAppointments();
            }
        });

        // Άνοιγμα παλιών επισκέψεων
        btnPastVisits.setOnClickListener(v1 -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.patient_fragment_container, new PastVisitsFragment())
                    .addToBackStack(null)
                    .commit();
        });

        loadAppointments();
        return v;
    }

    private void loadAppointments() {
        if (patientId == null) {
            Toast.makeText(getContext(), "Δεν βρέθηκε ασθενής.", Toast.LENGTH_SHORT).show();
            swipeRefresh.setRefreshing(false);
            return;
        }

        swipeRefresh.setRefreshing(true);
        db.collection("Appointments")
                .whereEqualTo("patientUid", patientId)
                .whereEqualTo("status", "Επιβεβαιωμένο")
                .get()
                .addOnSuccessListener(snapshot -> {
                    data.clear();

                    Date now = new Date();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        String status = doc.getString("status");
                        String date = doc.getString("date");
                        String time = doc.getString("time");

                        if (date == null || time == null) {
                            continue;
                        }

                        Date apptDate = parseDateTime(date, time);
                        if (apptDate == null || apptDate.before(now)) {
                            continue;
                        }

                        AppointmentItem item = new AppointmentItem(
                                doc.getId(),
                                doc.getString("appointmentId"),
                                doc.getString("doctorUid"),
                                doc.getString("doctorName"),
                                doc.getString("doctorSpecialty"),
                                doc.getString("patientUid"),
                                doc.getString("patientName"),
                                date,
                                time,
                                status
                        );
                        data.add(item);
                    }

                    data.sort(Comparator.comparing(a -> parseDateTime(a.date, a.time)));
                    adapter.notifyDataSetChanged();
                    swipeRefresh.setRefreshing(false);
                    updateEmptyState();
                })
                .addOnFailureListener(e -> {
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(getContext(), "Σφάλμα φόρτωσης ραντεβού.", Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                });
    }

    private Date parseDateTime(String date, String time) {
        try {
            return dateFormat.parse(date + " " + time);
        } catch (ParseException e) {
            return null;
        }
    }

    private void updateEmptyState() {
        if (data.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void cancelAppointment(AppointmentItem item) {
        new android.app.AlertDialog.Builder(getContext())
                .setTitle("Επιβεβαίωση ακύρωσης")
                .setMessage("Είσαι σίγουρος ότι θέλεις να ακυρώσεις αυτό το ραντεβού με τον/την " + item.doctorName + ";")
                .setPositiveButton("Ναι", (dialog, which) -> {
                    // Δημιουργία ειδοποίησης προς τον γιατρό
                    Map<String, Object> notification = new HashMap<>();
                    notification.put("doctorUid", item.doctorUid);
                    notification.put("doctorName", item.doctorName);
                    notification.put("patientName", item.patientName);
                    notification.put("patientUid", item.patientUid);
                    notification.put("message", "Ο ασθενής " + item.patientName + " ακύρωσε το ραντεβού στις " + item.date + " " + item.time + ".");
                    notification.put("status", "Ακυρωμένο από τον ασθενή");
                    notification.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());
                    notification.put("isRead", false);
                    notification.put("date", item.date);
                    notification.put("time", item.time);

                    // Στέλνουμε την ειδοποίηση στο Firestore
                    db.collection("Notifications")
                            .add(notification)
                            .addOnSuccessListener(docRef -> {
                                // Μόλις δημιουργηθεί η ειδοποίηση, αλλάζει το status του ραντεβού
                                db.collection("Appointments").document(item.firestoreId)
                                        .update("status", "Ακυρωμένο από τον ασθενή")
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(getContext(),
                                                    "Το ραντεβού ακυρώθηκε και ειδοποιήθηκε ο ιατρός.",
                                                    Toast.LENGTH_SHORT).show();
                                            data.remove(item);
                                            adapter.notifyDataSetChanged();
                                            updateEmptyState();
                                        })
                                        .addOnFailureListener(e ->
                                                Toast.makeText(getContext(),
                                                        "Σφάλμα κατά τη διαγραφή: " + e.getMessage(),
                                                        Toast.LENGTH_SHORT).show());
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(),
                                            "Σφάλμα κατά την αποστολή ειδοποίησης: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Όχι", (dialog, which) -> dialog.dismiss())
                .show();
    }
}