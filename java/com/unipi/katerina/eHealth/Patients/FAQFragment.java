package com.unipi.katerina.eHealth.Patients;

import android.graphics.Typeface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.unipi.katerina.eHealth.R;

public class FAQFragment extends Fragment {

    LinearLayout faqContainer;

    public FAQFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_faq, container, false);
        faqContainer = view.findViewById(R.id.faqContainer);

        // Ερωτήσεις & απαντήσεις
        addFAQ("Πως αλλάζω τον κωδικό πρόσβασης;",
                "Από το προφίλ σας, πατήστε \"Αλλαγή Κωδικού\" και θα σας σταλεί email.");
        addFAQ("Ξέχασα τον κωδικό πρόσβασης. Τι να κάνω;",
                "Στην οθόνη σύνδεσης, πατήστε 'Ξέχασα τον κωδικό' για επαναφορά μέσω email.");
        addFAQ("Που μπορώ να δω το ιατρικό ID μου;",
                "Στην επιλογή \"Ιατρικό ID \" από την αρχική οθόνη.");
        addFAQ("Πώς μπορώ να επεξεργαστώ το Ιατρικό ID μου;",
                "Από την ενότητα \"Ιατρικό ID\", πατήστε \"Επεξεργασία\".");
        addFAQ("Που μπορώ να δω το ιατρικό μου ιστορικό;",
                "Στην επιλογή \"Ιατρικό Ιστορικό \" από την αρχική οθόνη.");
        addFAQ("Μπορώ να αλλάξω το email μου;",
                "Για λόγους ασφαλείας, η αλλαγή email γίνεται μέσω επικοινωνίας με την υποστήριξη.");
        addFAQ("Πώς διαγράφω τον λογαριασμό μου;",
                "Από την ενότητα \"Προφίλ\" πατήστε το κουμπί \"Διαγραφή λογαριασμού\". ");
        addFAQ("Πώς μπορώ να κλείσω ραντεβού;",
                "Μέσω της οθόνης \"Κλείσε Ραντεβού\", επιλέγοντας γιατρό και ώρα");
        addFAQ("Μπορώ να ακυρώσω ένα ραντεβού;",
                "Ναι, από την οθόνη \"Τα Ραντεβού μου\" μπορείτε να δείτε τα μελλοντικά ραντεβού και να ακυρώσετε αυτό που δεν θέλετε.");
        addFAQ("Πώς μπορώ να αλλάξω email;",
                "Για λόγους ασφαλείας, η αλλαγή email γίνεται μέσω επικοινωνίας με την υποστήριξη.");
        addFAQ("Πώς να επικοινωνήσω με την υποστήριξη της εφαρμογής;",
                "Επιλέγεται το κουμπί Υποστήριξη που βρίσκεται στο menu option και πατάτε Υποβολή αιτήματος.");
        addFAQ("Θα λάβω υπενθύμιση για το ραντεβού μου;",
                "Ναι, εμφανίζεται ειδοποίηση στην αρχική οθόνη");
        addFAQ("Πώς επιλέγω γιατρό; ",
                "Από την ενότητα \"Αναζήτηση ιατρών \" μπορείτε να δείτε όλους τους ιατρούς.");

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

        faqContainer.addView(questionView);
        faqContainer.addView(answerView);
    }
}