package com.unipi.katerina.eHealth.Doctors;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unipi.katerina.eHealth.R;

import java.text.SimpleDateFormat;
import java.util.*;

public class NewPrescriptionFragment extends Fragment {

    EditText editTextDoctor, editTextDoctorSpecialty, editTextDate, editTextAmka, etNotes;
    Button btnAddMedication, btnSavePrescription;
    LinearLayout containerMedications;
    FirebaseFirestore db;
    FirebaseAuth auth;
    String patientId, patientName, patientAMKA, doctorUid, doctorName, doctorSpecialty;

    public NewPrescriptionFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            patientId = getArguments().getString("patientId");
            patientName = getArguments().getString("patientName");
            patientAMKA = getArguments().getString("patientAMKA");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_prescription, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editTextDoctor = view.findViewById(R.id.editTextDoctorName);
        editTextDoctorSpecialty = view.findViewById(R.id.editTextDoctorSpecialty);
        editTextDate = view.findViewById(R.id.editTextDate);
        editTextAmka = view.findViewById(R.id.editTextAmka);
        etNotes = view.findViewById(R.id.etNotes);
        btnAddMedication = view.findViewById(R.id.btnAddMedication);
        btnSavePrescription = view.findViewById(R.id.btnSavePrescription);
        containerMedications = view.findViewById(R.id.containerMedications);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        doctorUid = auth.getCurrentUser().getUid();

        if (patientAMKA != null) {
            editTextAmka.setText(patientAMKA);
            editTextAmka.setEnabled(false);
        }

        String today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        editTextDate.setText(today);
        editTextDate.setEnabled(false);

        loadDoctorInfo();

        addMedicationField(); // Προσθήκη πρώτου φαρμάκου αυτόματα

        btnAddMedication.setOnClickListener(v -> addMedicationField());
        btnSavePrescription.setOnClickListener(v -> savePrescription());
    }

    private void loadDoctorInfo() {
        db.collection("Doctors").document(doctorUid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        doctorName = doc.getString("FullName");
                        doctorSpecialty = doc.getString("specialty");
                        editTextDoctor.setText(doctorName);
                        editTextDoctorSpecialty.setText(doctorSpecialty);
                        editTextDoctor.setEnabled(false);
                        editTextDoctorSpecialty.setEnabled(false);
                    }
                });
    }

    // Δημιουργεί ένα νέο “σετ” πεδίων φαρμάκου
    private void addMedicationField() {
        View medView = LayoutInflater.from(getContext())
                .inflate(R.layout.item_medication, containerMedications, false);

        Button btnRemove = medView.findViewById(R.id.btnRemoveMedication);

        btnRemove.setOnClickListener(v -> {
    if (containerMedications.getChildCount() > 1) {
        containerMedications.removeView(medView);
        Toast.makeText(getContext(), "Το φάρμακο αφαιρέθηκε.", Toast.LENGTH_SHORT).show();
    } else {
        Toast.makeText(getContext(), "Πρέπει να υπάρχει τουλάχιστον ένα φάρμακο.", Toast.LENGTH_SHORT).show();
    }
});
        containerMedications.addView(medView);
    }

    private void savePrescription() {
        String notes = etNotes.getText().toString().trim();
        String date = editTextDate.getText().toString().trim();
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());  // Παίρνουμε την τρέχουσα ώρα
        List<Map<String, Object>> medicationsList = new ArrayList<>();

        for (int i = 0; i < containerMedications.getChildCount(); i++) {
            View medView = containerMedications.getChildAt(i);

            EditText name = medView.findViewById(R.id.etMedicationName);
            EditText dosage = medView.findViewById(R.id.etMedicationDosage);
            EditText instructions = medView.findViewById(R.id.etMedicationInstructions);
            EditText duration = medView.findViewById(R.id.etMedicationDuration);

            String medName = name.getText().toString().trim();
            String medDosage = dosage.getText().toString().trim();
            String medInstr = instructions.getText().toString().trim();
            String medDuration = duration.getText().toString().trim();

            if (!TextUtils.isEmpty(medName)) {
                Map<String, Object> med = new HashMap<>();
                med.put("name", medName);
                med.put("dosage", medDosage);
                med.put("instructions", medInstr);
                med.put("duration", medDuration);
                medicationsList.add(med);
            }
        }

        if (medicationsList.isEmpty()) {
            Toast.makeText(getContext(), "Προσθέστε τουλάχιστον ένα φάρμακο.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Παίρνουμε το πλήρες όνομα του ασθενή από τη βάση
        db.collection("Patients").document(patientId)
                .get()
                .addOnSuccessListener(patientDoc -> {
                    String fullName = "";
                    if (patientDoc.exists()) {
                        String first = patientDoc.getString("FirstName");
                        String last = patientDoc.getString("LastName");
                        fullName = (first != null ? first : "") + " " + (last != null ? last : "");
                        fullName = fullName.trim();
                    }

                    Map<String, Object> prescription = new HashMap<>();
                    prescription.put("doctorUid", doctorUid);
                    prescription.put("doctorName", doctorName);
                    prescription.put("doctorSpecialty", doctorSpecialty);
                    prescription.put("patientUid", patientId);
                    prescription.put("patientName", fullName);
                    prescription.put("patientAmka", patientAMKA);
                    prescription.put("date", date);
                    prescription.put("time", time);
                    prescription.put("medications", medicationsList);
                    prescription.put("notes", notes);
                    prescription.put("timestamp", FieldValue.serverTimestamp());

                    db.collection("Prescriptions")
                            .add(prescription)
                            .addOnSuccessListener(ref -> {
                                Toast.makeText(getContext(), "✅ Συνταγή αποθηκεύτηκε!", Toast.LENGTH_SHORT).show();
                                containerMedications.removeAllViews();
                                addMedicationField();
                                etNotes.setText("");

                                // Δημιουργία ειδοποίησης χωρίς patientName
                                Map<String, Object> notification = new HashMap<>();
                                notification.put("patientUid", patientId);
                                notification.put("doctorUid", doctorUid);
                                notification.put("doctorName", doctorName);
                                notification.put("doctorSpecialty", doctorSpecialty);
                                notification.put("message",
                                        "Ο γιατρός " + doctorName + " (" + doctorSpecialty + ") έγραψε νέα συνταγή στις " + date + " " + time);
                                notification.put("status", "Νέα συνταγή");
                                notification.put("isRead", false);
                                notification.put("timestamp", com.google.firebase.Timestamp.now());
                                notification.put("date", date);
                                notification.put("time", time);

                                db.collection("Notifications")
                                        .add(notification)
                                        .addOnSuccessListener(nRef ->
                                                Toast.makeText(getContext(),
                                                        "Ο ασθενής ειδοποιήθηκε για τη νέα συνταγή.",
                                                        Toast.LENGTH_SHORT).show()
                                        )
                                        .addOnFailureListener(e ->
                                                Toast.makeText(getContext(),
                                                        "⚠️ Αποτυχία ειδοποίησης: " + e.getMessage(),
                                                        Toast.LENGTH_LONG).show());
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(),
                                            "⚠️ Σφάλμα: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "⚠️ Αποτυχία ανάκτησης στοιχείων ασθενή: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }
}