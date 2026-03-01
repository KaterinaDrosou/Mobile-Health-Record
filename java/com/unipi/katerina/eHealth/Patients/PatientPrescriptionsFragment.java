package com.unipi.katerina.eHealth.Patients;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.unipi.katerina.eHealth.R;

import java.util.List;
import java.util.Map;

public class PatientPrescriptionsFragment extends Fragment {

    LinearLayout containerPrescriptions;
    FirebaseFirestore db;
    String patientUid;

    public PatientPrescriptionsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_prescriptions, container, false);

        containerPrescriptions = view.findViewById(R.id.containerPrescriptions);
        db = FirebaseFirestore.getInstance();
        patientUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadPrescriptions();
        return view;
    }

    private void loadPrescriptions() {
        db.collection("Prescriptions")
                .whereEqualTo("patientUid", patientUid)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    containerPrescriptions.removeAllViews();

                    if (snapshot.isEmpty()) {
                        TextView empty = new TextView(getContext());
                        empty.setText("Δεν υπάρχουν καταχωρημένες συνταγές.");
                        empty.setTextColor(getResources().getColor(android.R.color.darker_gray));
                        empty.setPadding(16, 16, 16, 16);
                        containerPrescriptions.addView(empty);
                        return;
                    }

                    for (QueryDocumentSnapshot doc : snapshot) {
                        addPrescriptionCard(doc);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Σφάλμα φόρτωσης συνταγών: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }

    private void addPrescriptionCard(QueryDocumentSnapshot doc) {
        String date = doc.getString("date");
        String doctorName = doc.getString("doctorName");
        String specialty = doc.getString("doctorSpecialty");
        List<Map<String, Object>> meds = (List<Map<String, Object>>) doc.get("medications");

        // Δημιουργία κάρτας
        CardView card = new CardView(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 8, 0, 8);
        card.setLayoutParams(params);
        card.setRadius(20f);
        card.setCardElevation(6f);
        card.setUseCompatPadding(true);

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 24, 24, 24);

        // Πληροφορίες συνταγής
        TextView tvHeader = new TextView(getContext());
        tvHeader.setText("👨‍⚕️ " + doctorName + " (" + specialty + ")\n📅 " + date);
        tvHeader.setTextSize(16f);
        tvHeader.setPadding(0, 0, 0, 8);
        layout.addView(tvHeader);

        // Φάρμακα (σύντομη λίστα)
        if (meds != null && !meds.isEmpty()) {
            for (Map<String, Object> med : meds) {
                String medName = (String) med.get("name");
                String dosage = (String) med.get("dosage");

                TextView tvMed = new TextView(getContext());
                tvMed.setText("💊 " + medName + " — " + dosage);
                tvMed.setTextSize(14f);
                layout.addView(tvMed);
            }
        } else {
            TextView none = new TextView(getContext());
            none.setText("Δεν υπάρχουν φάρμακα.");
            none.setTextColor(getResources().getColor(android.R.color.darker_gray));
            layout.addView(none);
        }

        // Κουμπί προβολής λεπτομερειών
        Button btnDetails = new Button(getContext());
        btnDetails.setText("Προβολή λεπτομερειών συνταγής");
        btnDetails.setBackgroundTintList(getContext().getResources().getColorStateList(R.color.black));
        btnDetails.setTextColor(getResources().getColor(android.R.color.white));
        btnDetails.setPadding(0, 8, 0, 0);
        layout.addView(btnDetails);

        // Click -> ανοίγει αναλυτικό fragment
        btnDetails.setOnClickListener(v -> openPrescriptionDetails(doc.getId()));

        card.addView(layout);
        containerPrescriptions.addView(card);
    }

    private void openPrescriptionDetails(String prescriptionId) {
        Bundle bundle = new Bundle();
        bundle.putString("prescriptionId", prescriptionId);

        PrescriptionDetailsFragment fragment = new PrescriptionDetailsFragment();
        fragment.setArguments(bundle);

        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.patient_fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}