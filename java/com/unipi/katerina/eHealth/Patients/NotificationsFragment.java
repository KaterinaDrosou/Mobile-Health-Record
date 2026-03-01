package com.unipi.katerina.eHealth.Patients;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.unipi.katerina.eHealth.R;

import java.util.Arrays;

public class NotificationsFragment extends Fragment {

    LinearLayout containerAll;
    FirebaseFirestore db;

    public NotificationsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        containerAll = view.findViewById(R.id.containerAllNotifications);
        db = FirebaseFirestore.getInstance();

        String patientUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadAllNotifications(patientUid);

        return view;
    }

    private void loadAllNotifications(String uid) {
        db.collection("Notifications")
                .whereEqualTo("patientUid", uid)
                .whereIn("status", Arrays.asList("Επιβεβαιωμένο", "Ακυρωμένο", "Νέα συνταγή")) //μόνο αυτά για τον ασθενή
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    containerAll.removeAllViews();

                    if (snapshot.isEmpty()) {
                        TextView empty = new TextView(getContext());
                        empty.setText("Δεν υπάρχουν ειδοποιήσεις.");
                        empty.setTextColor(getResources().getColor(android.R.color.darker_gray));
                        empty.setPadding(16, 16, 16, 16);
                        containerAll.addView(empty);
                        return;
                    }

                    for (var doc : snapshot) {
                        String msg = doc.getString("message");
                        String date = doc.getString("date");
                        String time = doc.getString("time");
                        String status = doc.getString("status");
                        Boolean isRead = doc.getBoolean("isRead");

                        // Κάρτα για κάθε ειδοποίηση
                        CardView card = new CardView(getContext());
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                        params.setMargins(0, 8, 0, 8);
                        card.setLayoutParams(params);
                        card.setRadius(18f);
                        card.setCardElevation(6f);
                        card.setUseCompatPadding(true);
                        card.setCardBackgroundColor(getResources().getColor(
                                (isRead != null && !isRead)
                                        ? R.color.light_blue_50
                                        : android.R.color.white
                        ));

                        // Εσωτερική διάταξη
                        LinearLayout innerLayout = new LinearLayout(getContext());
                        innerLayout.setOrientation(LinearLayout.VERTICAL);
                        innerLayout.setPadding(24, 20, 24, 20);

                        // Ημερομηνία + ώρα
                        TextView tvDate = new TextView(getContext());
                        tvDate.setText("📅 " + date + "  " + time);
                        tvDate.setTextColor(getResources().getColor(R.color.black));
                        tvDate.setTextSize(13f);

                        // Μήνυμα
                        TextView tvMsg = new TextView(getContext());
                        tvMsg.setText(msg);
                        tvMsg.setTextSize(16f);
                        tvMsg.setPadding(0, 6, 0, 0);

                        if ("Επιβεβαιωμένο".equals(status)) {
                            tvMsg.setTextColor(getResources().getColor(android.R.color.black));
                            tvMsg.setText("✅ " + msg);
                        } else if ("Ακυρωμένο".equals(status)) {
                            tvMsg.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                            tvMsg.setText("❌ " + msg);
                        } else if ("Νέα συνταγή".equals(status)) {
                            tvMsg.setTextColor(getResources().getColor(R.color.black));
                            tvMsg.setText("💊 " + msg);
                        } else {
                            tvMsg.setTextColor(getResources().getColor(android.R.color.black));
                        }

                        // Προσθήκη views στην κάρτα
                        innerLayout.addView(tvDate);
                        innerLayout.addView(tvMsg);
                        card.addView(innerLayout);
                        containerAll.addView(card);
                    }
                })
                .addOnFailureListener(e -> {
                    TextView error = new TextView(getContext());
                    error.setText("Σφάλμα φόρτωσης ειδοποιήσεων:\n" + e.getMessage());
                    error.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    containerAll.addView(error);
                });
    }
}