package com.unipi.katerina.eHealth.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.unipi.katerina.eHealth.Models.Doctor;
import com.unipi.katerina.eHealth.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder> {

    private List<Doctor> doctors = new ArrayList<>();
    private OnItemClickListener listener;
    private Set<String> favoriteDoctorUids = new HashSet<>(); //Κρατάει UIDs των γιατρών που είναι αγαπημένοι
    private String patientId; // ID ασθενούς
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public DoctorAdapter(String patientId) {
        this.patientId = patientId;
        loadFavorites();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {

        this.listener = listener;
    }

    public void setDoctors(List<Doctor> doctors) {  //δίνει τη λίστα των γιατρών και ανανεώνει την οθόνη (notifyDataSetChanged()
        this.doctors = doctors;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_doctor, parent, false);
        return new DoctorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorViewHolder holder, int position) {
        Doctor doctor = doctors.get(position);
        holder.bind(doctor, listener, favoriteDoctorUids, patientId);
    }

    @Override
    public int getItemCount() {

        return doctors.size();
    }
    private void loadFavorites() {
        db.collection("Patients")
                .document(patientId)
                .collection("FavoriteDoctors")
                .get()
                .addOnSuccessListener(snapshot -> {
                    favoriteDoctorUids.clear();
                    snapshot.forEach(doc -> favoriteDoctorUids.add(doc.getId()));
                    notifyDataSetChanged();
                });
    }
    public static class DoctorViewHolder extends RecyclerView.ViewHolder {  //κρατάει τα views της κάρτας
        TextView tvName, tvSpecialty, tvEmail, tvAddress, tvPhone;
        ImageButton btnFavorite;

        public DoctorViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvSpecialty = itemView.findViewById(R.id.tvSpecialty);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }

        public void bind(final Doctor doctor, final OnItemClickListener listener, final Set<String> favoriteDoctorUids, String patientId) {

            tvName.setText(doctor.getFullName());
            tvSpecialty.setText(doctor.getSpecialty());
            tvEmail.setText(doctor.getEmail());
            tvAddress.setText(doctor.getClinicAddress());
            tvPhone.setText(doctor.getPhone());

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Ορισμός εικόνας καρδιάς
            if (favoriteDoctorUids.contains(doctor.getUid())) {
                btnFavorite.setImageResource(R.drawable.filled_favorite_icon);
                btnFavorite.setImageTintList(null); // ΔΕΝ βάζει tint στην γεμάτη καρδιά
               // btnFavorite.setColorFilter(itemView.getResources().getColor(android.R.color.holo_red_light));
            } else {
                btnFavorite.setImageResource(R.drawable.favorite_icon);
                btnFavorite.setImageTintList(null);
             //   btnFavorite.setColorFilter(itemView.getResources().getColor(android.R.color.darker_gray));
            }

            btnFavorite.setOnClickListener(v -> {
                String doctorUid = doctor.getUid();
                boolean isFavorite = favoriteDoctorUids.contains(doctorUid);

                if (isFavorite) {
                    // Αφαίρεση από collection
                    db.collection("Patients")
                            .document(patientId)
                            .collection("FavoriteDoctors")
                            .document(doctorUid)
                            .delete();

                    favoriteDoctorUids.remove(doctorUid);
                    btnFavorite.setImageResource(R.drawable.favorite_icon);
                    btnFavorite.setImageTintList(null);
                } else {
                    // Προσθήκη στη collection
                    db.collection("Patients")
                            .document(patientId)
                            .collection("FavoriteDoctors")
                            .document(doctorUid)
                            .set(doctor); // αποθηκεύει το αντικείμενο Doctor

                    favoriteDoctorUids.add(doctorUid);
                    btnFavorite.setImageResource(R.drawable.filled_favorite_icon);
                    btnFavorite.setImageTintList(null);
                }
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(doctor);
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Doctor doctor);
    }

    public Set<String> getFavoriteDoctorUids() {
        return favoriteDoctorUids;
    }
}
