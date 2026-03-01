package com.unipi.katerina.eHealth.Patients;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unipi.katerina.eHealth.R;

import java.util.HashMap;
import java.util.Map;


public class PatientHistoryFragment extends Fragment {

    EditText editSurgeries, editAllergies, editMedications, editChronicDiseases, editFamilyHistory, editVaccinations, editHabits, editSymptoms;
    Button btnSaveHistory, btnEditHistory;
    FirebaseAuth auth;
    FirebaseFirestore db;
    boolean isEditing = false;

    public PatientHistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_patient_history, container, false);

        editSurgeries = view.findViewById(R.id.editSurgeries);
        editAllergies = view.findViewById(R.id.editAllergies);
        editMedications = view.findViewById(R.id.editMedications);
        editChronicDiseases = view.findViewById(R.id.editChronicDiseases);
        editFamilyHistory = view.findViewById(R.id.editFamilyHistory);
        editVaccinations = view.findViewById(R.id.editVaccinations);
        editHabits = view.findViewById(R.id.editHabits);
        editSymptoms = view.findViewById(R.id.editSymptoms);
        btnSaveHistory = view.findViewById(R.id.btnSaveHistory);
        btnEditHistory = view.findViewById(R.id.btnEditHistory);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loadMedicalHistory();

        btnSaveHistory.setOnClickListener(v -> {
            saveMedicalHistory();
            toggleEditMode();
        });

        btnEditHistory.setOnClickListener(v -> {
            toggleEditMode();  // Κλείδωμα μετά την αποθήκευση
        });

        return view;
    }

    private void saveMedicalHistory() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("surgeries", editSurgeries.getText().toString().trim());
        data.put("allergies", editAllergies.getText().toString().trim());
        data.put("medications", editMedications.getText().toString().trim());
        data.put("chronicDiseases", editChronicDiseases.getText().toString().trim());
        data.put("familyHistory", editFamilyHistory.getText().toString().trim());
        data.put("vaccinations", editVaccinations.getText().toString().trim());
        data.put("habits", editHabits.getText().toString().trim());
        data.put("symptoms", editSymptoms.getText().toString().trim());

        db.collection("Patients").document(user.getUid())
                .collection("Medical History").document("Data")
                .set(data)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Ιατρικό ιστορικό αποθηκεύτηκε!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Σφάλμα αποθήκευσης", Toast.LENGTH_SHORT).show());
    }
    private void loadMedicalHistory() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            return;
        }

        db.collection("Patients").document(user.getUid())
                .collection("Medical History").document("Data")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        editSurgeries.setText(documentSnapshot.getString("surgeries"));
                        editAllergies.setText(documentSnapshot.getString("allergies"));
                        editMedications.setText(documentSnapshot.getString("medications"));
                        editChronicDiseases.setText(documentSnapshot.getString("chronicDiseases"));
                        editFamilyHistory.setText(documentSnapshot.getString("familyHistory"));
                        editVaccinations.setText(documentSnapshot.getString("vaccinations"));
                        editHabits.setText(documentSnapshot.getString("habits"));
                        editSymptoms.setText(documentSnapshot.getString("symptoms"));
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Σφάλμα φόρτωσης ιατρικού ιστορικού", Toast.LENGTH_SHORT).show());
    }

    private void toggleEditMode() {
        isEditing = !isEditing;

        editSurgeries.setEnabled(isEditing);
        editAllergies.setEnabled(isEditing);
        editMedications.setEnabled(isEditing);
        editChronicDiseases.setEnabled(isEditing);
        editFamilyHistory.setEnabled(isEditing);
        editVaccinations.setEnabled(isEditing);
        editHabits.setEnabled(isEditing);
        editSymptoms.setEnabled(isEditing);

        btnSaveHistory.setEnabled(isEditing);
        btnEditHistory.setText(isEditing ? "❌ Ακύρωση" : "✏️ Επεξεργασία");

        if (!isEditing) {
            // Αν κάνει ακύρωση τότε φορτώνει ξανά τα δεδομένα
            loadMedicalHistory();
        }
    }
}