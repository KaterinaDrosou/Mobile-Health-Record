package com.unipi.katerina.eHealth.Doctors;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.unipi.katerina.eHealth.R;

import java.text.SimpleDateFormat;
import java.util.*;

public class DoctorScheduleFragment extends Fragment {

    Button btnSelectDate;
    LinearLayout containerAppointments;
    TextView tvSelectedDate;
    FirebaseFirestore db;
    String doctorUid;
    final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public DoctorScheduleFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_doctor_schedule, container, false);

        btnSelectDate = v.findViewById(R.id.btnSelectDate);
        containerAppointments = v.findViewById(R.id.containerAppointments);
        tvSelectedDate = v.findViewById(R.id.tvSelectedDate);

        db = FirebaseFirestore.getInstance();
        doctorUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        btnSelectDate.setOnClickListener(view -> showDatePicker());

        // Αυτόματα εμφανίζει τα σημερινά ραντεβού
        String today = sdf.format(new Date());
        tvSelectedDate.setText("📅 " + today);
        loadAppointmentsForDate(today);

        return v;
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    String date = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                    tvSelectedDate.setText("📅 " + date);
                    loadAppointmentsForDate(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void loadAppointmentsForDate(String date) {
        containerAppointments.removeAllViews();

        db.collection("Appointments")
                .whereEqualTo("doctorUid", doctorUid)
                .whereEqualTo("date", date)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        showEmptyState();
                        return;
                    }

                    List<Map<String, Object>> appointments = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", doc.getId());
                        item.put("time", doc.getString("time"));
                        item.put("patientName", doc.getString("patientName"));
                        item.put("status", doc.getString("status"));
                        appointments.add(item);
                    }

                    // Ταξινόμηση ανά ώρα
                    appointments.sort(Comparator.comparing(a -> a.get("time").toString()));

                    for (Map<String, Object> appt : appointments) {
                        addAppointmentView(
                                appt.get("id").toString(),
                                appt.get("time").toString(),
                                appt.get("patientName").toString(),
                                appt.get("status") != null ? appt.get("status").toString() : "Επιβεβαιωμένο"
                        );
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Σφάλμα φόρτωσης ραντεβού!", Toast.LENGTH_SHORT).show());
    }

    private void addAppointmentView(String appointmentId, String time, String patientName, String status) {
        View card = getLayoutInflater().inflate(R.layout.item_doctor_appointment, containerAppointments, false);

        TextView tvTime = card.findViewById(R.id.tvTime);
        TextView tvPatient = card.findViewById(R.id.tvPatient);
        TextView tvStatus = card.findViewById(R.id.tvStatus);
        Button btnConfirm = card.findViewById(R.id.btnConfirm);
        Button btnCancel = card.findViewById(R.id.btnCancel);

        tvTime.setText("🕒 " + time);
        tvPatient.setText("👤 " + patientName);

        // Εμφάνιση κατάστασης
        switch (status) {
            case "Επιβεβαιωμένο":
                tvStatus.setText("✅ Επιβεβαιωμένο");
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_light));
                btnConfirm.setVisibility(View.GONE);
                btnCancel.setVisibility(View.GONE);
                break;
            case "Ακυρωμένο":
                tvStatus.setText("❌ Ακυρωμένο");
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                btnConfirm.setVisibility(View.GONE);
                btnCancel.setVisibility(View.GONE);
                break;
            default:
                tvStatus.setText("🕓 Εκκρεμεί");
                tvStatus.setTextColor(getResources().getColor(android.R.color.darker_gray));
                btnConfirm.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                break;
        }

        // Επιβεβαίωση ραντεβού
        btnConfirm.setOnClickListener(v -> {
            db.collection("Appointments").document(appointmentId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String patientUid = documentSnapshot.getString("patientUid");
                            String dateValue = documentSnapshot.getString("date");
                            String timeValue = documentSnapshot.getString("time");
                            String doctorName = documentSnapshot.getString("doctorName");

                            db.collection("Appointments").document(appointmentId)
                                    .update("status", "Επιβεβαιωμένο")
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(getContext(), "Το ραντεβού επιβεβαιώθηκε.", Toast.LENGTH_SHORT).show();
                                        tvStatus.setText("✅ Επιβεβαιωμένο");
                                        tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_light));
                                        btnConfirm.setVisibility(View.GONE);
                                        btnCancel.setVisibility(View.GONE);

                                        // Δημιουργία ειδοποίησης
                                        String message = "Το ραντεβού σας επιβεβαιώθηκε από τον " + doctorName + ".";
                                        createNotification(patientUid, message, "Επιβεβαιωμένο", dateValue, timeValue, doctorName, doctorUid, patientName);
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(getContext(), "Σφάλμα επιβεβαίωσης!", Toast.LENGTH_SHORT).show());
                        }
                    });
        });

        // Ακύρωση ραντεβού
        btnCancel.setOnClickListener(v -> {
            db.collection("Appointments").document(appointmentId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String patientUid = documentSnapshot.getString("patientUid");
                            String dateValue = documentSnapshot.getString("date");
                            String timeValue = documentSnapshot.getString("time");
                            String doctorName = documentSnapshot.getString("doctorName");

                            db.collection("Appointments").document(appointmentId)
                                    .update("status", "Ακυρωμένο")
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(getContext(), "Το ραντεβού ακυρώθηκε.", Toast.LENGTH_SHORT).show();
                                        tvStatus.setText("❌ Ακυρωμένο");
                                        tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                                        btnConfirm.setVisibility(View.GONE);
                                        btnCancel.setVisibility(View.GONE);

                                        // Δημιουργία ειδοποίησης στο Firestore
                                        String message = "Το ραντεβού σας ακυρώθηκε από τον " + doctorName + ".";
                                        createNotification(patientUid, message, "Ακυρωμένο", dateValue, timeValue, doctorName, doctorUid, patientName);
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(getContext(), "Σφάλμα ακύρωσης!", Toast.LENGTH_SHORT).show());
                        }
                    });
        });

        containerAppointments.addView(card);
    }

    private void showEmptyState() {
        containerAppointments.removeAllViews();
        TextView tv = new TextView(getContext());
        tv.setText("Δεν υπάρχουν ραντεβού για αυτή την ημέρα.");
        tv.setTextColor(getResources().getColor(android.R.color.darker_gray));
        tv.setPadding(8, 16, 8, 16);
        containerAppointments.addView(tv);
    }

    private void createNotification(String patientUid, String message, String status, String date, String time, String doctorName, String doctorUid, String patientName) {
        Map<String, Object> notif = new HashMap<>();
        notif.put("patientUid", patientUid);
        notif.put("patientName", patientName);
        notif.put("message", message);
        notif.put("status", status);
        notif.put("date", date);
        notif.put("time", time);
        notif.put("doctorUid", doctorUid);
        notif.put("doctorName", doctorName);
        notif.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());
        notif.put("isRead", false);

        db.collection("Notifications").add(notif)
                .addOnSuccessListener(docRef ->
                        Toast.makeText(getContext(), "📩 Ειδοποίηση στάλθηκε στον ασθενή.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "⚠️ Σφάλμα κατά την αποστολή ειδοποίησης.", Toast.LENGTH_SHORT).show());
    }
}