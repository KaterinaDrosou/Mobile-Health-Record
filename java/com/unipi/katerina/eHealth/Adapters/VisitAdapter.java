package com.unipi.katerina.eHealth.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.unipi.katerina.eHealth.R;
import com.unipi.katerina.eHealth.Models.Visit;

import java.util.List;

public class VisitAdapter extends RecyclerView.Adapter<VisitAdapter.VisitViewHolder> {

    private List<Visit> visitList;

    public VisitAdapter(List<Visit> visitList) {

        this.visitList = visitList;
    }

    @NonNull
    @Override
    public VisitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_visit, parent, false);
        return new VisitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VisitViewHolder holder, int position) {
        Visit visit = visitList.get(position);
        holder.bind(visit);
    }

    @Override
    public int getItemCount() { //Επιστρέφει πόσα items έχει η λίστα

        return visitList.size();
    }

    public void updateList(List<Visit> newList) {
        visitList = newList;
        notifyDataSetChanged();
    }

    public static class VisitViewHolder extends RecyclerView.ViewHolder {
        TextView textDate, textDoctor, textDiagnosis, textNotes, textNextAppointment, textVisitDoctorSpecialty;

        public VisitViewHolder(@NonNull View itemView) {
            super(itemView);
            textDate = itemView.findViewById(R.id.textVisitDate);
            textDoctor = itemView.findViewById(R.id.textVisitDoctor);
            textDiagnosis = itemView.findViewById(R.id.textVisitDiagnosis);
            textNotes = itemView.findViewById(R.id.textVisitNotes);
            textNextAppointment = itemView.findViewById(R.id.textVisitNextAppointment);
            textVisitDoctorSpecialty = itemView.findViewById(R.id.textVisitDoctorSpecialty);
        }

        public void bind(Visit visit) {
            textDate.setText("Ημερομηνία: " + visit.getDate());
            textDoctor.setText("Ιατρός: " + visit.getDoctorName());
            textVisitDoctorSpecialty.setText("Ειδικότητα: " + visit.getDoctorSpecialty());
            textDiagnosis.setText("Διάγνωση: " + visit.getDiagnosis());
            textNotes.setText("Σημειώσεις: " + visit.getNotes());
            textNextAppointment.setText("Επόμενο ραντεβού: " + visit.getNextAppointment());
        }
    }
}