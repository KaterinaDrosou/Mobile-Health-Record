package com.unipi.katerina.eHealth.Patients;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class PatientProfileFragment extends Fragment {

    TextView textWelcome;
    EditText editFirstName, editLastName, editPhone, editAFM, editAMKA, editAddress, editBirthdate, editTextEmail;
    Button btnSave, btnChangePassword, btnDeleteAccount, btnEdit;
    FirebaseAuth auth;
    FirebaseFirestore db;
    boolean isEditing = false;

    public PatientProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_profile, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        textWelcome = view.findViewById(R.id.textWelcome);
        editTextEmail = view.findViewById(R.id.editTextEmail);
        editFirstName = view.findViewById(R.id.editFirstName);
        editLastName = view.findViewById(R.id.editLastName);
        editPhone = view.findViewById(R.id.editPhone);
        editAFM = view.findViewById(R.id.editAFM);
        editAMKA = view.findViewById(R.id.editAMKA);
        editAddress = view.findViewById(R.id.editAddress);
        editBirthdate = view.findViewById(R.id.editBirthdate);
        btnSave = view.findViewById(R.id.btnSave);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);
        btnDeleteAccount = view.findViewById(R.id.btnDeleteAccount);
        btnEdit = view.findViewById(R.id.btnEdit);

        loadUserData();

        btnSave.setOnClickListener(v -> {
            updateUserData();
            toggleEditMode();
        });

        btnEdit.setOnClickListener(v -> {
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
                    .setTitle("Διαγραφή λογαριασμού")
                    .setMessage("Είσαι σίγουρος/η ότι θέλεις να διαγράψεις τον λογαριασμό σου; Αυτή η ενέργεια δεν μπορεί να αναιρεθεί!")
                    .setPositiveButton("Διαγραφή", (dialog, which) -> {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            deleteAccountAndAllData(user.getUid(), user);
                        }
                    })
                    .setNegativeButton("Ακύρωση", null)
                    .show();
        });

        return view;
    }

    private void loadUserData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            return;
        }

        String userId = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Patients").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String firstName = documentSnapshot.getString("FirstName");
                        if (firstName != null && !firstName.isEmpty()) {
                            textWelcome.setText("Καλώς ήρθες, " + firstName + " 👋");
                        } else {
                            // fallback αν δεν υπάρχει firstname
                            String email = user.getEmail();
                            if (email != null && !email.isEmpty()) {
                                String namePart = email.split("@")[0];
                                textWelcome.setText("Καλώς ήρθες, " + namePart + " 👋");
                            } else {
                                textWelcome.setText("Καλώς ήρθες! 👋");
                            }
                        }
                    } else {
                        // fallback αν δεν βρεθεί το doc
                        String email = user.getEmail();
                        if (email != null && !email.isEmpty()) {
                            String namePart = email.split("@")[0];
                            textWelcome.setText("Καλώς ήρθες, " + namePart + " 👋");
                        } else {
                            textWelcome.setText("Καλώς ήρθες! 👋");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // fallback αν υπάρχει κάποιο σφάλμα στη Firestore ανάκτηση
                    String email = user.getEmail();
                    if (email != null && !email.isEmpty()) {
                        String namePart = email.split("@")[0];
                        textWelcome.setText("Καλώς ήρθες, " + namePart + " 👋");
                    } else {
                        textWelcome.setText("Καλώς ήρθες! 👋");
                    }
                });

        db.collection("Patients").document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        editFirstName.setText(doc.getString("FirstName"));
                        editLastName.setText(doc.getString("LastName"));
                        editPhone.setText(doc.getString("phone"));
                        editAFM.setText(doc.getString("afm"));
                        editAMKA.setText(doc.getString("amka"));
                        editAddress.setText(doc.getString("address"));
                        editBirthdate.setText(doc.getString("birthdate"));
                        editTextEmail.setText(user.getEmail());
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Σφάλμα φόρτωσης", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUserData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("FirstName", editFirstName.getText().toString().trim());
        updates.put("LastName", editLastName.getText().toString().trim());
        updates.put("phone", editPhone.getText().toString().trim());
        updates.put("afm", editAFM.getText().toString().trim());
        updates.put("amka", editAMKA.getText().toString().trim());
        updates.put("address", editAddress.getText().toString().trim());
        updates.put("birthdate", editBirthdate.getText().toString().trim());

        db.collection("Patients").document(user.getUid())
                .update(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "Επιτυχής αποθήκευση", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Αποτυχία αποθήκευσης", Toast.LENGTH_SHORT).show();
                });
    }

    private void toggleEditMode() {
        isEditing = !isEditing;

        editPhone.setEnabled(isEditing);
        editAddress.setEnabled(isEditing);
        editTextEmail.setEnabled(isEditing);

        btnSave.setEnabled(isEditing);
        btnEdit.setText(isEditing ? "❌ Ακύρωση" : "✏️ Επεξεργασία");

        if (!isEditing) {
            // Αν κάνει ακύρωση τότε φορτώνει ξανά τα δεδομένα
            loadUserData();
        }
    }

    private void deleteAccountAndAllData(String uid, FirebaseUser user) {

        // 1. Διαγραφή Subcollections από Patients/{uid}
        String[] subCollections = {"Visits", "Medical ID", "Medical History", "FavoriteDoctors"};
        for (String sub : subCollections) {
            db.collection("Patients").document(uid).collection(sub)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        WriteBatch subBatch = db.batch();
                        for (DocumentSnapshot doc : snapshot) {
                            subBatch.delete(doc.getReference());
                        }
                        subBatch.commit();
                    });
        }

        // 2. Διαγραφή εγγράφων όπου patientUid == uid
        deleteWhereEqual("Prescriptions", "patientUid", uid);
        deleteWhereEqual("Appointments", "patientUid", uid);
        deleteWhereEqual("Notifications", "patientUid", uid);

        // 3. Διαγραφή document από Patients
        db.collection("Patients").document(uid).delete();

        // 4. Διαγραφή doc από Users
        db.collection("Users").document(uid).delete();

        // 5. Διαγραφή Authentication user
        user.delete().addOnSuccessListener(a -> {
            Toast.makeText(getContext(), "Ο λογαριασμός διαγράφηκε οριστικά!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(getActivity(), MainActivity.class));
            requireActivity().finish();
        }).addOnFailureListener(e ->
                Toast.makeText(getContext(), "Σφάλμα Auth: " + e.getMessage(), Toast.LENGTH_LONG).show()
        );
    }

    private void deleteWhereEqual(String collection, String field, String value) {
        db.collection(collection).whereEqualTo(field, value)
                .get()
                .addOnSuccessListener(query -> {
                    WriteBatch batch = db.batch();
                    for (DocumentSnapshot doc : query) {
                        batch.delete(doc.getReference());
                    }
                    batch.commit();
                });
    }
}