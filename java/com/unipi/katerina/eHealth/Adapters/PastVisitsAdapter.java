package com.unipi.katerina.eHealth.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.unipi.katerina.eHealth.R;
import java.util.List;
import java.util.Map;

public class PastVisitsAdapter extends RecyclerView.Adapter<PastVisitsAdapter.VH> {

    private final List<Map<String, Object>> visits;

    public PastVisitsAdapter(List<Map<String, Object>> visits) {

        this.visits = visits;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView textVisitDoctor, textVisitDoctorSpecialty, textVisitDate, textVisitDiagnosis, textVisitNotes;

        VH(@NonNull View v) {
            super(v);
            textVisitDoctor = v.findViewById(R.id.textVisitDoctor);
            textVisitDoctorSpecialty = v.findViewById(R.id.textVisitDoctorSpecialty);
            textVisitDate = v.findViewById(R.id.textVisitDate);
            textVisitDiagnosis = v.findViewById(R.id.textVisitDiagnosis);
            textVisitNotes = v.findViewById(R.id.textVisitNotes);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_visit, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Map<String, Object> visit = visits.get(pos);
        h.textVisitDoctor.setText((String) visit.get("doctorName"));
        h.textVisitDoctorSpecialty.setText((String) visit.get("doctorSpecialty"));
        h.textVisitDate.setText("Ημερομηνία: " + visit.get("date"));
        h.textVisitDiagnosis.setText("Διάγνωση: " + visit.get("diagnosis"));
        h.textVisitNotes.setText("Σημειώσεις" + visit.get("notes"));
    }

    @Override
    public int getItemCount() {

        return visits.size();
    }
}
