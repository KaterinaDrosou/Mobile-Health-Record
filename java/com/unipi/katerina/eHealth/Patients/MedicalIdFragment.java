package com.unipi.katerina.eHealth.Patients;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unipi.katerina.eHealth.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MedicalIdFragment extends Fragment {

    TextView textUserInfo;
    EditText editBloodType, editHeight, editWeight, editAllergies, editMedications, editChronicDiseases, editEmergencyContact1, editRelation1, editEmergencyContact2, editRelation2;
    Button btnSave, btnEdit;
    FirebaseAuth auth;
    FirebaseFirestore db;
    boolean isEditing = false;

    public MedicalIdFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_medical_id, container, false);

        textUserInfo = view.findViewById(R.id.textUserInfo);
        editBloodType = view.findViewById(R.id.editBloodType);
        editHeight = view.findViewById(R.id.editHeight);
        editWeight = view.findViewById(R.id.editWeight);
        editAllergies = view.findViewById(R.id.editAllergies);
        editMedications = view.findViewById(R.id.editMedications);
        editChronicDiseases = view.findViewById(R.id.editChronicDiseases);
        editEmergencyContact1 = view.findViewById(R.id.editEmergencyContact1);
        editRelation1 = view.findViewById(R.id.editRelation1);
        editEmergencyContact2 = view.findViewById(R.id.editEmergencyContact2);
        editRelation2 = view.findViewById(R.id.editRelation2);
        btnSave = view.findViewById(R.id.btnSave);
        btnEdit = view.findViewById(R.id.btnEdit);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loadMedicalData();

        btnSave.setOnClickListener(v -> {
            saveMedicalData();
            toggleEditMode();
        });

        btnEdit.setOnClickListener(v -> {
            toggleEditMode();  // Κλείδωμα μετά την αποθήκευση
        });

        return view;
    }

    private void loadMedicalData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            return;
        }

        db.collection("Patients").document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                            if (doc.exists()) {
                                String firstName = doc.getString("FirstName");
                                String lastName = doc.getString("LastName");
                                String dateOfBirth = doc.getString("birthdate");

                                int age = calculateAge(dateOfBirth);
                                textUserInfo.setText(firstName + " " + lastName +", " + age + " ετών ");
                            }
                        });

        db.collection("Patients").document(user.getUid())
                .collection("Medical ID").document("Data")
                        .get()
                                .addOnSuccessListener(doc -> {
                                    if (doc.exists()) {
                                        editBloodType.setText(doc.getString("bloodType"));
                                        editHeight.setText(doc.getString("height"));
                                        editWeight.setText(doc.getString("weight"));
                                        editAllergies.setText(doc.getString("allergies"));
                                        editMedications.setText(doc.getString("medications"));
                                        editChronicDiseases.setText(doc.getString("chronicDiseases"));
                                        editEmergencyContact1.setText(doc.getString("emergencyContact1"));
                                        editRelation1.setText(doc.getString("relation1"));
                                        editEmergencyContact2.setText(doc.getString("emergencyContact2"));
                                        editRelation2.setText(doc.getString("relation2"));
                                    }
                                });
    }

    private void saveMedicalData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("bloodType", editBloodType.getText().toString().trim());
        data.put("height", editHeight.getText().toString().trim());
        data.put("weight", editWeight.getText().toString().trim());
        data.put("allergies", editAllergies.getText().toString().trim());
        data.put("medications", editMedications.getText().toString().trim());
        data.put("chronicDiseases", editChronicDiseases.getText().toString().trim());
        data.put("emergencyContact1", editEmergencyContact1.getText().toString().trim());
        data.put("relation1", editRelation1.getText().toString().trim());
        data.put("emergencyContact2", editEmergencyContact2.getText().toString().trim());
        data.put("relation2", editRelation2.getText().toString().trim());

        db.collection("Patients").document(user.getUid())
                .collection("Medical ID").document("Data")
                .set(data)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "Αποθηκεύτηκε!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Σφάλμα αποθήκευσης", Toast.LENGTH_SHORT).show();
                });
    }

    private int calculateAge(String dobString) {
        if (dobString == null || dobString.isEmpty()) {
            return 0;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date dob = sdf.parse(dobString);

            Calendar birth = Calendar.getInstance();
            birth.setTime(dob);

            Calendar today = Calendar.getInstance();

            int age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
            if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }
            return age;

        } catch (Exception e) {
            return 0;
        }
    }

    private void toggleEditMode() {
        isEditing = !isEditing;

        editBloodType.setEnabled(isEditing);
        editHeight.setEnabled(isEditing);
        editWeight.setEnabled(isEditing);
        editAllergies.setEnabled(isEditing);
        editMedications.setEnabled(isEditing);
        editChronicDiseases.setEnabled(isEditing);
        editEmergencyContact1.setEnabled(isEditing);
        editRelation1.setEnabled(isEditing);
        editEmergencyContact2.setEnabled(isEditing);
        editRelation2.setEnabled(isEditing);

        btnSave.setEnabled(isEditing);
        btnEdit.setText(isEditing ? "❌ Ακύρωση" : "✏️ Επεξεργασία");

        if (!isEditing) {
            // Αν κάνει ακύρωση τότε φορτώνει ξανά τα δεδομένα
            loadMedicalData();
        }
    }
}