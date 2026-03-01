package com.unipi.katerina.eHealth.Doctors;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unipi.katerina.eHealth.R;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class DoctorVisitFragment extends Fragment {

    EditText editTextDate, editTextDoctor, editTextDiagnosis, editTextNotes, editTextNextAppointment, editTextAMKA, editTextDoctorSpecialty, editTextNextAppointmentTime;
    Button btnSaveVisit;
    FirebaseFirestore db;
    FirebaseAuth auth;
    String patientId; // Ο ασθενής στον οποίο θα προστεθεί η επίσκεψη (π.χ. από προηγούμενη αναζήτηση)
    String patientAMKA;

    public DoctorVisitFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Παίρνει το patientId και το ΑΜΚΑ από το Bundle που βρίσκεται στο SearchPatientFragment
        if (getArguments() != null) {
            patientId = getArguments().getString("patientId");
            patientAMKA = getArguments().getString("patientAMKA");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_doctor_visit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editTextDate = view.findViewById(R.id.editTextDate);
        editTextDoctor = view.findViewById(R.id.editTextDoctorName);
        editTextDiagnosis = view.findViewById(R.id.editTextDiagnosis);
        editTextNotes = view.findViewById(R.id.editTextNotes);
        editTextNextAppointment = view.findViewById(R.id.editTextNextAppointment);
        editTextAMKA = view.findViewById(R.id.editTextAMKA);
        btnSaveVisit = view.findViewById(R.id.btnSaveVisit);
        editTextDoctorSpecialty = view.findViewById(R.id.editTextDoctorSpecialty);
        editTextNextAppointmentTime = view.findViewById(R.id.editTextNextAppointmentTime);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        if (patientAMKA != null) {
            editTextAMKA.setText(patientAMKA);
            editTextAMKA.setEnabled(false);  // Να μην μπορεί να αλλαχθεί
        }

        // Θέτουμε αυτόματα την σημερινή ημερομηνία στο editTextDate
        Calendar calendar = Calendar.getInstance();
        String todayDate = String.format("%02d/%02d/%04d",
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.YEAR));
        editTextDate.setText(todayDate);

        loadDoctorInfo();  // Φορτώνει το όνομα ιατρού και την ειδικότητα από Firestore

        // Click listeners για τα πεδία ημερομηνίας
        editTextDate.setOnClickListener(v -> showDatePicker(editTextDate));
        editTextNextAppointment.setOnClickListener(v -> showDatePicker(editTextNextAppointment));

        btnSaveVisit.setOnClickListener(v -> submitVisit());
    }

    private void showDatePicker(EditText targetEditText) {
        final Calendar calendar = Calendar.getInstance();

        String currentText = targetEditText.getText().toString();
        if (!currentText.isEmpty()) {
            String[] parts = currentText.split("/");
            if (parts.length == 3) {
                try {
                    int day = Integer.parseInt(parts[0]);
                    int month = Integer.parseInt(parts[1]) - 1; // 0-based
                    int year = Integer.parseInt(parts[2]);
                    calendar.set(year, month, day);
                } catch (NumberFormatException e) {
                    // Αν υπάρχει λάθος, κρατάμε την τρέχουσα ημερομηνία
                }
            }
        }

        new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    String dateStr = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);
                    targetEditText.setText(dateStr);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))
                .show();
    }
    private void submitVisit() {
        String date = editTextDate.getText().toString().trim();
        String doctorName = editTextDoctor.getText().toString().trim();
        String diagnosis = editTextDiagnosis.getText().toString().trim();
        String notes = editTextNotes.getText().toString().trim();
        String nextAppointment = editTextNextAppointment.getText().toString().trim();
        String nextTime = editTextNextAppointmentTime.getText().toString().trim();
        String doctorUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String doctorSpecialty = editTextDoctorSpecialty.getText().toString().trim();

        if (TextUtils.isEmpty(date) || TextUtils.isEmpty(diagnosis)) {
            Toast.makeText(getContext(), "Συμπληρώστε την ημερομηνία και τη διάγνωση.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> visit = new HashMap<>();
        visit.put("date", date);
        visit.put("doctorName", doctorName);
        visit.put("doctorUid", doctorUid);
        visit.put("doctorSpecialty", doctorSpecialty);
        visit.put("diagnosis", diagnosis);
        visit.put("notes", notes);
        visit.put("nextAppointment", nextAppointment);
        visit.put("amka", patientAMKA);
        visit.put("timestamp", com.google.firebase.Timestamp.now());

        db.collection("Patients")
                .document(patientId)
                .collection("Visits")
                .add(visit)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Η επίσκεψη καταχωρήθηκε!", Toast.LENGTH_SHORT).show();

                    // Αν έχει συμπληρωθεί επόμενο ραντεβού τότε δημιουργούμε αυτόματα "Επιβεβαιωμένο" Appointment
                    if (!TextUtils.isEmpty(nextAppointment)) {

                        // Πρώτα παίρνουμε το όνομα του ασθενή
                        db.collection("Patients").document(patientId).get()
                                .addOnSuccessListener(patientDoc -> {
                                    String firstName = patientDoc.getString("FirstName");
                                    String lastName = patientDoc.getString("LastName");

                                    String name = "";
                                    if (firstName != null) {
                                        name += firstName;
                                    }
                                    if (lastName != null) {
                                        name += " " + lastName;
                                    }
                                    if (TextUtils.isEmpty(name)) {
                                        name = "Άγνωστος ασθενής";
                                    }

                                    final String patientName = name; // απαραίτητο για χρήση μέσα σε lambda

                                    // Δημιουργία appointment
                                    Map<String, Object> appointment = new HashMap<>();
                                    appointment.put("date", nextAppointment);
                                    appointment.put("time", nextTime);
                                    appointment.put("doctorUid", doctorUid);
                                    appointment.put("doctorName", doctorName);
                                    appointment.put("doctorSpecialty", doctorSpecialty);
                                    appointment.put("patientUid", patientId);
                                    appointment.put("patientAMKA", patientAMKA);
                                    appointment.put("patientName", patientName);
                                    appointment.put("status", "Επιβεβαιωμένο");
                                    appointment.put("timestamp", com.google.firebase.Timestamp.now());

                                    db.collection("Appointments")
                                            .add(appointment)
                                            .addOnSuccessListener(docRef -> {
                                              // αποθήκευση του appointmentId στο ίδιο το document
                                                db.collection("Appointments")
                                                        .document(docRef.getId())
                                                        .update("appointmentId", docRef.getId());

                                                Toast.makeText(getContext(),
                                                        "Το επόμενο ραντεβού προστέθηκε αυτόματα!",
                                                        Toast.LENGTH_SHORT).show();

                                                // Δημιουργία ειδοποίησης
                                                Map<String, Object> notification = new HashMap<>();
                                                notification.put("patientUid", patientId);
                                                notification.put("patientName", patientName);
                                                notification.put("doctorUid", doctorUid);
                                                notification.put("message",
                                                        "Ο γιατρός " + doctorName + " προγραμμάτισε νέο ραντεβού στις " + nextAppointment + " " + nextTime);
                                                notification.put("status", "Επιβεβαιωμένο");
                                                notification.put("isRead", false);
                                                notification.put("timestamp", com.google.firebase.Timestamp.now());
                                                notification.put("date", nextAppointment);
                                                notification.put("time", nextTime);

                                                db.collection("Notifications").add(notification);
                                            })
                                            .addOnFailureListener(e ->
                                                    Toast.makeText(getContext(),
                                                            "Αποτυχία καταχώρησης ραντεβού: " + e.getMessage(),
                                                            Toast.LENGTH_LONG).show());
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(), "Αποτυχία φόρτωσης στοιχείων ασθενή.", Toast.LENGTH_SHORT).show());
                    }
                    clearFields();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Σφάλμα: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void clearFields() {
        editTextDiagnosis.setText("");
        editTextNotes.setText("");
        editTextNextAppointment.setText("");
        editTextNextAppointmentTime.setText("");
    }

    private void loadDoctorInfo() {
        String doctorId = auth.getCurrentUser().getUid();
        db.collection("Doctors").document(doctorId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String doctorName = documentSnapshot.getString("FullName");
                        String doctorSpecialty = documentSnapshot.getString("specialty");
                        if (doctorName != null) {
                            editTextDoctor.setText(doctorName);
                            editTextDoctor.setEnabled(false); // για να μην αλλάζει ο ιατρός
                            editTextDoctorSpecialty.setText(doctorSpecialty);
                            editTextDoctorSpecialty.setEnabled(false); // για να μην αλλάζει η ειδικότητα
                            editTextDate.setEnabled(false); //για να μην αλλάζει η ημερομηνία επίσκεψης
                        }
                    } else {
                        editTextDoctor.setText("Άγνωστος Ιατρός");
                    }
                })
                .addOnFailureListener(e -> {
                    editTextDoctor.setText("Άγνωστος Ιατρός");
                    Toast.makeText(getContext(), "Σφάλμα φόρτωσης ονόματος ιατρού", Toast.LENGTH_SHORT).show();
                });
    }
}