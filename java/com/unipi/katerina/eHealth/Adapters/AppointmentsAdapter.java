package com.unipi.katerina.eHealth.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.unipi.katerina.eHealth.Models.AppointmentItem;
import com.unipi.katerina.eHealth.Patients.MyAppointmentsFragment;
import com.unipi.katerina.eHealth.R;

import java.util.List;

public class AppointmentsAdapter extends RecyclerView.Adapter<AppointmentsAdapter.VH> {

    public interface OnCancelClickListener {
        void onCancel(AppointmentItem item);
    }
    private final List<AppointmentItem> items;
    private final OnCancelClickListener listener;

    public AppointmentsAdapter(List<AppointmentItem> items, OnCancelClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    static class VH extends RecyclerView.ViewHolder {
        CardView card;
        TextView tvDoctor, tvSpecialty, tvDateTime, tvStatus;
        Button btnCancel;

        VH(@NonNull View v) {
            super(v);
            card = (CardView) v;
            tvDoctor = v.findViewById(R.id.tvDoctor);
            tvSpecialty = v.findViewById(R.id.tvSpecialty);
            tvDateTime = v.findViewById(R.id.tvDateTime);
            tvStatus = v.findViewById(R.id.tvStatus);
            btnCancel = v.findViewById(R.id.btnCancel);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        AppointmentItem it = items.get(position);

        h.tvDoctor.setText(it.doctorName);
        h.tvSpecialty.setText(it.doctorSpecialty);
        h.tvDateTime.setText(it.date + " • " + it.time);
        h.tvStatus.setText(statusLabel(it.status));

        h.btnCancel.setEnabled("Επιβεβαιωμένο".equalsIgnoreCase(it.status));
        h.btnCancel.setOnClickListener(v -> {
            if (listener != null) listener.onCancel(it);
        });
    }

    private String statusLabel(String s) {
        if (s == null) {
            return "Άγνωστο";
        }
        switch (s) {
            case "Επιβεβαιωμένο": return "Επιβεβαιωμένο";
            case "Ακυρωμένο": return "Ακυρωμένο";
            default: return s;
        }
    }

    @Override
    public int getItemCount() {
        return items.size(); }
}