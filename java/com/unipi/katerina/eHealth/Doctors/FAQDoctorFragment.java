package com.unipi.katerina.eHealth.Doctors;

import android.graphics.Typeface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.unipi.katerina.eHealth.R;

public class FAQDoctorFragment extends Fragment {

    LinearLayout faqDoctorContainer;

    public FAQDoctorFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_faq_doctor, container, false);
        faqDoctorContainer = view.findViewById(R.id.faqDoctorContainer);

        // Ερωτήσεις & απαντήσεις
        addFAQ("Πως αλλάζω τον κωδικό πρόσβασης ;",
                "Από το προφίλ σας, πατήστε \"Αλλαγή Κωδικού\" και θα σας σταλεί email.");
        addFAQ("Ξέχασα τον κωδικό πρόσβασης. Τι να κάνω;",
                "Στην οθόνη σύνδεσης, πατήστε 'Ξέχασα τον κωδικό' για επαναφορά μέσω email.");
        addFAQ("Πως μπορώ να αναζητήσω έναν ασθενή με βάση το ΑΜΚΑ;",
                "Από την ενότητα \"Αναζήτηση ασθενούς\" μπορείτε να αναζητήσετε έναν ασθενή με βάση το ΑΜΚΑ.");
        addFAQ("Πως μπορώ να αλλάξω email;",
                "Για λόγους ασφαλείας, η αλλαγή email γίνεται μέσω επικοινωνίας με την υποστήριξη.");
        addFAQ("Πως να επικοινωνήσω με την υποστήριξη της εφαρμογής;",
                "Επιλέγεται το κουμπί Υποστήριξη που βρίσκεται στο menu option και πατάτε Υποβολή αιτήματος.");
        addFAQ("Πως μπορώ να συνταγογραφήσω φάρμακα για έναν ασθενή;", "Αφού κάνετε πρώτα αναζήτηση ασθενούς με βάση το ΑΜΚΑ από την ενότητα \"Αναζήτηση ασθενούς\", πατάτε το κουμπί Συνταγογράφηση.");
        addFAQ("Πως μπορώ να κάνω μια καταγραφή επίσκεψης για έναν ασθενή;", "Αφού κάνετε πρώτα αναζήτηση ασθενούς με βάση το ΑΜΚΑ από την ενότητα \"Αναζήτηση ασθενούς\", πατάτε το κουμπί Καταγραφή επίσκεψης.");

        return view;
    }

    private void addFAQ(String question, String answer) {
        TextView questionView = new TextView(getContext());
        questionView.setText("❓ " + question);
        questionView.setTextSize(18);
        questionView.setTypeface(Typeface.DEFAULT_BOLD);
        questionView.setPadding(0, 24, 0, 8);

        TextView answerView = new TextView(getContext());
        answerView.setText(answer);
        answerView.setVisibility(View.GONE);
        answerView.setTextSize(16);
        answerView.setPadding(24, 0, 0, 8);

        // Toggle προβολής απάντησης
        questionView.setOnClickListener(v -> {
            if (answerView.getVisibility() == View.GONE) {
                answerView.setVisibility(View.VISIBLE);
            } else {
                answerView.setVisibility(View.GONE);
            }
        });

        faqDoctorContainer.addView(questionView);
        faqDoctorContainer.addView(answerView);
    }
}