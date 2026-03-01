package com.unipi.katerina.eHealth.Doctors;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.unipi.katerina.eHealth.MainActivity;
import com.unipi.katerina.eHealth.R;

import java.util.HashMap;
import java.util.Map;


public class DoctorProfileFragment extends Fragment {

    TextView textWelcome;
    EditText editFullName, editPhone, editAddress, editTextEmail,
            startMonday, endMonday, startTuesday, endTuesday, startWednesday, endWednesday, startThursday,
            endThursday, startFriday, endFriday, startSaturday, endSaturday, startSunday, endSunday;
    Button btnSave, btnChangePassword, btnDeleteAccount, btnEdit;
    CheckBox checkMonday, checkTuesday, checkWednesday, checkThursday, checkFriday, checkSaturday, checkSunday;
    FirebaseAuth auth;
    FirebaseFirestore db;
    boolean isEditing = false;

    public DoctorProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_doctor_profile, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        textWelcome = view.findViewById(R.id.textWelcome);
        editTextEmail = view.findViewById(R.id.editTextEmail);
        editFullName = view.findViewById(R.id.editFullName);
        editPhone = view.findViewById(R.id.editPhone);
        editAddress = view.findViewById(R.id.editAddress);
        btnSave = view.findViewById(R.id.btnSave);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);
        btnDeleteAccount = view.findViewById(R.id.btnDeleteAccount);
        btnEdit = view.findViewById(R.id.btnEdit);

        checkMonday = view.findViewById(R.id.checkMonday);
        checkTuesday = view.findViewById(R.id.checkTuesday);
        checkWednesday = view.findViewById(R.id.checkWednesday);
        checkThursday = view.findViewById(R.id.checkThursday);
        checkFriday = view.findViewById(R.id.checkFriday);
        checkSaturday = view.findViewById(R.id.checkSaturday);
        checkSunday = view.findViewById(R.id.checkSunday);

        startMonday = view.findViewById(R.id.startMonday);
        endMonday = view.findViewById(R.id.endMonday);
        startTuesday = view.findViewById(R.id.startTuesday);
        endTuesday = view.findViewById(R.id.endTuesday);
        startWednesday = view.findViewById(R.id.startWednesday);
        endWednesday = view.findViewById(R.id.endWednesday);
        startThursday = view.findViewById(R.id.startThursday);
        endThursday = view.findViewById(R.id.endThursday);
        startFriday = view.findViewById(R.id.startFriday);
        endFriday = view.findViewById(R.id.endFriday);
        startSaturday = view.findViewById(R.id.startSaturday);
        endSaturday = view.findViewById(R.id.endSaturday);
        startSunday = view.findViewById(R.id.startSunday);
        endSunday = view.findViewById(R.id.endSunday);

        loadUserData();
        setWorkingHoursEditable(false);

        btnSave.setOnClickListener(v -> {
            updateUserData();
            toggleEditMode();
        });

        btnEdit.setOnClickListener( v -> {
            loadUserData();
            toggleEditMode();  // Κλείδωμα μετά την αποθήκευση
        });

        btnChangePassword.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null && user.getEmail() != null) {
                FirebaseAuth.getInstance().sendPasswordResetEmail(user.getEmail())
                        .addOnSuccessListener(unused -> Toast.makeText(getContext(),
                                "Έχει σταλεί email για αλλαγή κωδικού", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(getContext(),
                                "Σφάλμα: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });

        btnDeleteAccount.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Επιβεβαίωση διαγραφής")
                    .setMessage("Είσαι σίγουρος/η ότι θέλεις να διαγράψεις τον λογαριασμό σου; Αυτή η ενέργεια δεν μπορεί να αναιρεθεί!")
                    .setPositiveButton("Διαγραφή", (dialog, which) -> {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user == null) return;

                        String uid = user.getUid();
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        WriteBatch batch = db.batch();

                        // 1. Doctors
                        batch.delete(db.collection("Doctors").document(uid));

                        // 2. Users
                        batch.delete(db.collection("Users").document(uid));

                        // 3. Notifications
                        db.collection("Notifications").whereEqualTo("doctorUid", uid).get()
                                .addOnSuccessListener(notif -> {

                                    for (DocumentSnapshot doc : notif)
                                        batch.delete(doc.getReference());

                                    // 4. Appointments
                                    db.collection("Appointments").whereEqualTo("doctorUid", uid).get()
                                            .addOnSuccessListener(apps -> {

                                                for (DocumentSnapshot doc : apps)
                                                    batch.delete(doc.getReference());

                                                // 5. Prescriptions
                                                db.collection("Prescriptions").whereEqualTo("doctorUid", uid).get()
                                                        .addOnSuccessListener(pres -> {

                                                            for (DocumentSnapshot doc : pres)
                                                                batch.delete(doc.getReference());

                                                            // Execute batch
                                                            batch.commit().addOnSuccessListener(x -> {

                                                                // 6. Delete Auth
                                                                user.delete().addOnSuccessListener(a -> {
                                                                    Toast.makeText(getContext(),
                                                                            "Ο λογαριασμός διαγράφηκε οριστικά!",
                                                                            Toast.LENGTH_LONG).show();
                                                                    startActivity(new Intent(getActivity(), MainActivity.class));
                                                                    requireActivity().finish();
                                                                });

                                                            }).addOnFailureListener(e ->
                                                                    Toast.makeText(getContext(), "Σφάλμα Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show()
                                                            );

                                                        }).addOnFailureListener(e ->
                                                                Toast.makeText(getContext(), "Σφάλμα Prescription: " + e.getMessage(), Toast.LENGTH_LONG).show()
                                                        );

                                            }).addOnFailureListener(e ->
                                                    Toast.makeText(getContext(), "Σφάλμα Appointments: " + e.getMessage(), Toast.LENGTH_LONG).show()
                                            );

                                }).addOnFailureListener(e ->
                                        Toast.makeText(getContext(), "Σφάλμα Notifications: " + e.getMessage(), Toast.LENGTH_LONG).show()
                                );
                    })
                    .setNegativeButton("Ακύρωση", (d, w) -> d.dismiss())
                    .show();
        });

        return view;
    }

    private void loadUserData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            return;
        }

        db.collection("Doctors")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String fullName = doc.getString("FullName");
                        if (fullName != null && !fullName.isEmpty()) {
                            textWelcome.setText("Καλώς ήρθες, " + fullName + " 👋");
                        } else {
                            textWelcome.setText("Καλώς ήρθες 👋");
                        }

                        editFullName.setText(fullName);
                        editPhone.setText(doc.getString("phone"));
                        editAddress.setText(doc.getString("clinicAddress"));
                        Map<String, Object> schedule = (Map<String, Object>) doc.get("weeklySchedule");
                        if (schedule != null) {
                            setDaySchedule(schedule, "Δευτέρα", checkMonday, startMonday, endMonday);
                            setDaySchedule(schedule, "Τρίτη", checkTuesday, startTuesday, endTuesday);
                            setDaySchedule(schedule, "Τετάρτη", checkWednesday, startWednesday, endWednesday);
                            setDaySchedule(schedule, "Πέμπτη", checkThursday, startThursday, endThursday);
                            setDaySchedule(schedule, "Παρασκευή", checkFriday, startFriday, endFriday);
                            setDaySchedule(schedule, "Σάββατο", checkSaturday, startSaturday, endSaturday);
                            setDaySchedule(schedule, "Κυριακή", checkSunday, startSunday, endSunday);
                        }

                        editTextEmail.setText(user.getEmail());
                    } else {
                        textWelcome.setText("Καλώς ήρθες 👋");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Σφάλμα φόρτωσης", Toast.LENGTH_SHORT).show();
                    textWelcome.setText("Καλώς ήρθες 👋");
                });
    }

    private void setDaySchedule(Map<String, Object> schedule, String day, CheckBox checkBox, EditText start, EditText end) {
        Map<String, Object> dayMap = (Map<String, Object>) schedule.get(day);
        if (dayMap != null) {
            checkBox.setChecked(Boolean.TRUE.equals(dayMap.get("enabled")));
            start.setText((String) dayMap.get("start"));
            end.setText((String) dayMap.get("end"));
        }
    }

    private void updateUserData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("FullName", editFullName.getText().toString().trim());
        updates.put("phone", editPhone.getText().toString().trim());
        updates.put("clinicAddress", editAddress.getText().toString().trim());
        Map<String, Object> weeklySchedule = new HashMap<>();
        weeklySchedule.put("Δευτέρα", createDayMap(checkMonday, startMonday, endMonday));
        weeklySchedule.put("Τρίτη", createDayMap(checkTuesday, startTuesday, endTuesday));
        weeklySchedule.put("Τετάρτη", createDayMap(checkWednesday, startWednesday, endWednesday));
        weeklySchedule.put("Πέμπτη", createDayMap(checkThursday, startThursday, endThursday));
        weeklySchedule.put("Παρασκευή", createDayMap(checkFriday, startFriday, endFriday));
        weeklySchedule.put("Σάββατο", createDayMap(checkSaturday, startSaturday, endSaturday));
        weeklySchedule.put("Κυριακή", createDayMap(checkSunday, startSunday, endSunday));
        updates.put("weeklySchedule", weeklySchedule);

        db.collection("Doctors").document(user.getUid())
                .update(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "Επιτυχής αποθήκευση", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Αποτυχία αποθήκευσης", Toast.LENGTH_SHORT).show();
                });
    }

    private Map<String, Object> createDayMap(CheckBox checkBox, EditText start, EditText end) {
        Map<String, Object> map = new HashMap<>();
        map.put("enabled", checkBox.isChecked());
        map.put("start", start.getText().toString().trim());
        map.put("end", end.getText().toString().trim());
        return map;
    }

    private void toggleEditMode() {
        isEditing = !isEditing;

        editTextEmail.setEnabled(isEditing);
        editPhone.setEnabled(isEditing);
        editAddress.setEnabled(isEditing);

        // Ενεργοποιεί ή απενεργοποιεί όλα τα πεδία ωραρίου
        setWorkingHoursEditable(isEditing);

        btnSave.setEnabled(isEditing);
        btnEdit.setText(isEditing ? "❌ Ακύρωση" : "✏️ Επεξεργασία");

        if (!isEditing) {
            // Αν κάνει ακύρωση τότε φορτώνει ξανά τα δεδομένα
            loadUserData();
        }
    }

    private void setWorkingHoursEditable(boolean enabled) {
        checkMonday.setEnabled(enabled);
        checkTuesday.setEnabled(enabled);
        checkWednesday.setEnabled(enabled);
        checkThursday.setEnabled(enabled);
        checkFriday.setEnabled(enabled);
        checkSaturday.setEnabled(enabled);
        checkSunday.setEnabled(enabled);

        startMonday.setEnabled(enabled);
        endMonday.setEnabled(enabled);
        startTuesday.setEnabled(enabled);
        endTuesday.setEnabled(enabled);
        startWednesday.setEnabled(enabled);
        endWednesday.setEnabled(enabled);
        startThursday.setEnabled(enabled);
        endThursday.setEnabled(enabled);
        startFriday.setEnabled(enabled);
        endFriday.setEnabled(enabled);
        startSaturday.setEnabled(enabled);
        endSaturday.setEnabled(enabled);
        startSunday.setEnabled(enabled);
        endSunday.setEnabled(enabled);
    }
}