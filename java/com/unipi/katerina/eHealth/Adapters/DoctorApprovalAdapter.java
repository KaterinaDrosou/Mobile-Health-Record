package com.unipi.katerina.eHealth.Adapters;

import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unipi.katerina.eHealth.Admin.AdminDashboardActivity;
import com.unipi.katerina.eHealth.Models.Doctor;
import com.unipi.katerina.eHealth.R;

import java.util.List;

public class DoctorApprovalAdapter extends RecyclerView.Adapter<DoctorApprovalAdapter.ViewHolder> {

    private Context context;
    private List<Doctor> doctors;
    private FirebaseFirestore db;

    public DoctorApprovalAdapter(Context context, List<Doctor> doctors, FirebaseFirestore db) {
        this.context = context;
        this.doctors = doctors;
        this.db = db;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_doctor_approval, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Doctor doctor = doctors.get(position);
        holder.txtName.setText(doctor.getFullName());
        holder.txtEmail.setText(doctor.getEmail());
        holder.txtSpecialty.setText("Ειδικότητα: " + doctor.getSpecialty());
        holder.txtStatus.setText("Κατάσταση: " + statusLabel(doctor.getStatusFromAdmin()));

        holder.btnApprove.setOnClickListener(v -> updateStatus(doctor, "approved"));
        holder.btnReject.setOnClickListener(v -> updateStatus(doctor, "rejected"));

        // Απόκρυψη κουμπιών όταν δεν είναι pending
        if (!doctor.getStatusFromAdmin().equalsIgnoreCase("pending")) {
            holder.btnApprove.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
        } else {
            holder.btnApprove.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.VISIBLE);
        }
    }

    private void updateStatus(Doctor doctor, String status) {
        db.collection("Doctors").document(doctor.getUid())
                .update("statusFromAdmin", status)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(context, "Η κατάσταση ενημερώθηκε σε " + status, Toast.LENGTH_SHORT).show();

                    // Ενημέρωση της κατάστασης και τοπικά
                    doctor.setStatusFromAdmin(status);
                    notifyDataSetChanged();

                    // Αν ο admin είναι σε φίλτρο (π.χ. Pending), να φύγει αυτόματα από τη λίστα
                    if (context instanceof AdminDashboardActivity) {
                        ((AdminDashboardActivity) context).reloadCurrentFilter();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Σφάλμα: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {

        return doctors.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtEmail, txtSpecialty, txtStatus;
        Button btnApprove, btnReject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtDoctorName);
            txtEmail = itemView.findViewById(R.id.txtDoctorEmail);
            txtSpecialty = itemView.findViewById(R.id.txtDoctorSpecialty);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }

    private String statusLabel(String s) {
        if (s == null) {
            return "Pending";
        }
        switch (s.toLowerCase()) {
            case "approved": return "Εγκρίθηκε ✅";
            case "rejected": return "Απορρίφθηκε ❌";
            case "pending": return "Σε αναμονή ⏳";
            default: return s;
        }
    }
}