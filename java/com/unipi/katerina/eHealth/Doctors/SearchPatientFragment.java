package com.unipi.katerina.eHealth.Doctors;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unipi.katerina.eHealth.R;
import com.unipi.katerina.eHealth.Models.Visit;
import com.unipi.katerina.eHealth.Adapters.VisitAdapter;

import java.util.ArrayList;
import java.util.List;


public class SearchPatientFragment extends Fragment {

    EditText editTextAMKA;
    Button btnSearch, btnAddVisit, btnAddPrescription;
    LinearLayout layoutPatientInfo;
    TextView textPatientFirstName, textPatientLastName, textPatientPhone, textPatientAFM, textPatientAddress, textPatientEmail;
    String currentPatientId; // Για αποθήκευση του UID
    RecyclerView recyclerViewVisits;
    List<Visit> visitList;
    VisitAdapter visitAdapter;
    FirebaseFirestore db;

    public SearchPatientFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search_patient, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editTextAMKA = view.findViewById(R.id.editTextAMKA);
        btnSearch = view.findViewById(R.id.btnSearch);
        layoutPatientInfo = view.findViewById(R.id.layoutPatientInfo);
        textPatientFirstName = view.findViewById(R.id.textPatientFirstName);
        textPatientLastName = view.findViewById(R.id.textPatientLastName);
        textPatientPhone = view.findViewById(R.id.textPatientPhone);
        textPatientAddress = view.findViewById(R.id.textPatientAddress);
        textPatientEmail = view.findViewById(R.id.textPatientEmail);
        textPatientAFM = view.findViewById(R.id.textPatientAFM);
        btnAddVisit = view.findViewById(R.id.btnAddVisit);
        btnAddPrescription = view.findViewById(R.id.btnAddPrescription);
        recyclerViewVisits = view.findViewById(R.id.recyclerViewVisits);

        recyclerViewVisits.setLayoutManager(new LinearLayoutManager(getContext()));
        visitList = new ArrayList<>();
        visitAdapter = new VisitAdapter(visitList);
        recyclerViewVisits.setAdapter(visitAdapter);

        db = FirebaseFirestore.getInstance();

        btnSearch.setOnClickListener(v -> searchPatientByAMKA());
        btnAddVisit.setOnClickListener(v -> openVisitFragment());
        btnAddPrescription.setOnClickListener(v -> openPrescriptionFragment());
    }

    private void searchPatientByAMKA() {
        String amka = editTextAMKA.getText().toString().trim();

        if (TextUtils.isEmpty(amka) || amka.length() != 11) {
            Toast.makeText(getContext(), "Παρακαλώ εισάγετε έγκυρο 11ψήφιο ΑΜΚΑ.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Patients")
                .whereEqualTo("amka", amka)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);

                        currentPatientId = doc.getId(); // Αποθήκευση UID

                        String firstName = doc.getString("FirstName");
                        String lastName = doc.getString("LastName");
                        String afm = doc.getString("afm");
                        String phone = doc.getString("phone");
                        String address = doc.getString("address");
                        String email = doc.getString("email");
                        String foundamka = doc.getString("amka");  // ΑΜΚΑ που βρέθηκε

                        layoutPatientInfo.setVisibility(View.VISIBLE);
                        textPatientFirstName.setText("Όνομα: " + firstName);
                        textPatientLastName.setText("Επώνυμο: " + lastName);
                        textPatientAFM.setText("ΑΦΜ: " + afm);
                        textPatientPhone.setText("Τηλέφωνο: " + phone);
                        textPatientAddress.setText("Διεύθυνση: " + address);
                        textPatientEmail.setText("Email: " + email);

                        btnAddVisit.setVisibility(View.VISIBLE);
                        btnAddVisit.setTag(foundamka); // Αποθήκευση του ΑΜΚΑ για το επόμενο fragment

                        btnAddPrescription.setVisibility(View.VISIBLE);
                        btnAddPrescription.setTag(doc.getId()); // αποθηκεύει UID ασθενή

                        fetchVisits(currentPatientId); // Φόρτωση επισκέψεων
                    } else {
                        layoutPatientInfo.setVisibility(View.GONE);
                        btnAddVisit.setVisibility(View.GONE);
                        recyclerViewVisits.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Δεν βρέθηκε ασθενής με αυτό το ΑΜΚΑ.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    layoutPatientInfo.setVisibility(View.GONE);
                    recyclerViewVisits.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Σφάλμα κατά την αναζήτηση: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void openVisitFragment() {
        if (currentPatientId == null) {
            Toast.makeText(getContext(), "Σφάλμα! Δεν έχει επιλεγεί ασθενής.", Toast.LENGTH_SHORT).show();
            return;
        }

        String amka = (String) btnAddVisit.getTag();

        Bundle bundle = new Bundle();
        bundle.putString("patientId", currentPatientId);
        bundle.putString("patientAMKA", amka);

        DoctorVisitFragment fragment = new DoctorVisitFragment();
        fragment.setArguments(bundle);

        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.doctor_fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void fetchVisits(String patientId) {
        db.collection("Visits")
                .whereEqualTo("patientId", patientId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    visitList.clear(); // καθάρισμα παλιών δεδομένων

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Visit visit = doc.toObject(Visit.class);
                        visitList.add(visit);
                    }

                    visitAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Αποτυχία ανάκτησης επισκέψεων: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void openPrescriptionFragment() {
        if (currentPatientId == null) {
            Toast.makeText(getContext(), "Δεν έχει επιλεγεί ασθενής.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Παίρνουμε το ΑΜΚΑ από το TextView ή το κουμπί (ανάλογα με τη ρύθμιση)
        String amka = null;
        if (btnAddVisit.getTag() != null) {
            amka = btnAddVisit.getTag().toString();
        }

        Bundle bundle = new Bundle();
        bundle.putString("patientId", currentPatientId);
        bundle.putString("patientName", textPatientFirstName.getText().toString().replace("Όνομα: ", ""));
        bundle.putString("patientAMKA", amka);

        NewPrescriptionFragment fragment = new NewPrescriptionFragment();
        fragment.setArguments(bundle);

        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.doctor_fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}