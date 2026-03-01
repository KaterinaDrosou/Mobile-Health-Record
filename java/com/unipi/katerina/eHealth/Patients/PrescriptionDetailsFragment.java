package com.unipi.katerina.eHealth.Patients;

import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.pdf.BaseFont;
import com.unipi.katerina.eHealth.R;

import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class PrescriptionDetailsFragment extends Fragment {
    FirebaseFirestore db;
    LinearLayout containerDetails;
    String prescriptionId;
    Button btnExportPdf;
    String doctor, specialty, date, patientAmka, notes;   // Για τα δεδομένα συνταγής
    List<Map<String, Object>> meds;

    public PrescriptionDetailsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_prescription_details, container, false);
        containerDetails = v.findViewById(R.id.containerPrescriptionDetails);
        btnExportPdf = v.findViewById(R.id.btnExportPdf);

        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            prescriptionId = getArguments().getString("prescriptionId");
        }

        loadPrescriptionDetails();
        btnExportPdf.setOnClickListener(view -> exportToPdf());

        return v;
    }

    private void loadPrescriptionDetails() {
        db.collection("Prescriptions").document(prescriptionId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        return;
                    }

                    date = doc.getString("date");
                    doctor = doc.getString("doctorName");
                    specialty = doc.getString("doctorSpecialty");
                    patientAmka = doc.getString("patientAmka");
                    notes = doc.getString("notes");
                    meds = (List<Map<String, Object>>) doc.get("medications");

                    TextView header = new TextView(getContext());
                    header.setText("👨‍⚕️ " + doctor + " (" + specialty + ")\n📅 " + date + "\nΑΜΚΑ: " + patientAmka);
                    header.setTextSize(16f);
                    header.setPadding(0, 0, 0, 16);
                    containerDetails.addView(header);

                    if (meds != null && !meds.isEmpty()) {
                        for (Map<String, Object> med : meds) {
                            CardView card = new CardView(getContext());
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT);
                            params.setMargins(0, 8, 0, 8);
                            card.setLayoutParams(params);
                            card.setRadius(16f);
                            card.setCardElevation(4f);

                            LinearLayout inner = new LinearLayout(getContext());
                            inner.setOrientation(LinearLayout.VERTICAL);
                            inner.setPadding(24, 20, 24, 20);

                            String name = (String) med.get("name");
                            String dosage = (String) med.get("dosage");
                            String instr = (String) med.get("instructions");
                            String duration = (String) med.get("duration");

                            TextView tvName = new TextView(getContext());
                            tvName.setText("💊 " + name);
                            tvName.setTextSize(16f);
                            tvName.setPadding(0, 0, 0, 8);

                            TextView tvDosage = new TextView(getContext());
                            tvDosage.setText("Δοσολογία: " + dosage);

                            TextView tvInstr = new TextView(getContext());
                            tvInstr.setText("Οδηγίες: " + instr);

                            TextView tvDur = new TextView(getContext());
                            tvDur.setText("Διάρκεια: " + duration);

                            inner.addView(tvName);
                            inner.addView(tvDosage);
                            inner.addView(tvInstr);
                            inner.addView(tvDur);
                            card.addView(inner);

                            containerDetails.addView(card);
                        }
                    }

                    if (notes != null && !notes.isEmpty()) {
                        TextView tvNotes = new TextView(getContext());
                        tvNotes.setText("\n📝 Παρατηρήσεις: " + notes);
                        tvNotes.setTextSize(15f);
                        tvNotes.setPadding(0, 12, 0, 0);
                        containerDetails.addView(tvNotes);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Σφάλμα: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Δημιουργία PDF και αποθήκευση απευθείας στα Downloads με πλήρη υποστήριξη ελληνικών και σωστή μορφοποίηση
    private void exportToPdf() {
        Document document = new Document();
        try {
            // Δημιουργούμε προσωρινά το PDF
            File tempFile = new File(requireContext().getCacheDir(), "Prescription_" + prescriptionId + ".pdf");
            PdfWriter.getInstance(document, new FileOutputStream(tempFile));
            document.open();

            // Φορτώνουμε ελληνική γραμματοσειρά (θα δουλέψει σε όλες τις συσκευές γιατί χρησιμοποιεί system font DejaVu)
            BaseFont greekBase = BaseFont.createFont("assets/fonts/DejaVuSans.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font titleFont = new Font(greekBase, 20, Font.BOLD);
            Font sectionFont = new Font(greekBase, 14, Font.BOLD);
            Font labelFont = new Font(greekBase, 12, Font.BOLD);
            Font normalFont = new Font(greekBase, 12, Font.NORMAL);
            Font smallFont = new Font(greekBase, 10, Font.ITALIC);

            // Λογότυπο
            try {
                InputStream is = getResources().openRawResource(R.drawable.logo_image2);
                byte[] logoBytes = new byte[is.available()];
                is.read(logoBytes);
                is.close();

                com.itextpdf.text.Image logo = com.itextpdf.text.Image.getInstance(logoBytes);
                logo.scaleToFit(100, 100);
                logo.setAlignment(com.itextpdf.text.Image.ALIGN_CENTER);
                document.add(logo);
                document.add(new Paragraph("\n"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Επικεφαλίδα
            Paragraph header = new Paragraph("ΗΛΕΚΤΡΟΝΙΚΗ ΣΥΝΤΑΓΗ\n\n", titleFont);
            header.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(header);

            // Πληροφορίες
            Paragraph info = new Paragraph();
            info.add(new Chunk("Ημερομηνία: ", labelFont));
            info.add(new Chunk(date + "\n", normalFont));

            String currentTime = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(new java.util.Date());
            info.add(new Chunk("Ώρα: ", labelFont));
            info.add(new Chunk(currentTime + "\n", normalFont));

            info.add(new Chunk("Ιατρός: ", labelFont));
            info.add(new Chunk(doctor + " (" + specialty + ")\n", normalFont));

            info.add(new Chunk("ΑΜΚΑ Ασθενή: ", labelFont));
            info.add(new Chunk(patientAmka + "\n\n", normalFont));

            document.add(info);

            // Διαχωριστική γραμμή
            com.itextpdf.text.pdf.draw.LineSeparator line = new com.itextpdf.text.pdf.draw.LineSeparator();
            document.add(line);
            document.add(new Paragraph("\n"));

            // Φάρμακα
            document.add(new Paragraph("🩺 ΣΥΝΤΑΓΟΓΡΑΦΗΣΗ ΦΑΡΜΑΚΩΝ\n\n", sectionFont));

            if (meds != null && !meds.isEmpty()) {
                int count = 1;
                for (Map<String, Object> med : meds) {
                    String name = (String) med.get("name");
                    String dosage = (String) med.get("dosage");
                    String instr = (String) med.get("instructions");
                    String duration = (String) med.get("duration");

                    Paragraph medPara = new Paragraph(count + ". " + name + "\n", sectionFont);

                    if (!TextUtils.isEmpty(dosage)) {
                        medPara.add(new Chunk("Δοσολογία: ", labelFont));
                        medPara.add(new Chunk(dosage + "\n", normalFont));
                    }

                    if (!TextUtils.isEmpty(instr)) {
                        medPara.add(new Chunk("Οδηγίες: ", labelFont));
                        medPara.add(new Chunk(instr + "\n", normalFont));
                    }

                    if (!TextUtils.isEmpty(duration)) {
                        medPara.add(new Chunk("Διάρκεια: ", labelFont));
                        medPara.add(new Chunk(duration + "\n\n", normalFont));
                    }

                    document.add(medPara);
                    count++;
                }
            } else {
                document.add(new Paragraph("Δεν υπάρχουν φάρμακα στη συνταγή.\n\n", normalFont));
            }

            // Παρατηρήσεις
            if (notes != null && !notes.isEmpty()) {
                document.add(new Paragraph("📝 Παρατηρήσεις / Οδηγίες Ιατρού:\n", sectionFont));
                document.add(new Paragraph(notes + "\n\n", normalFont));
            }

            // Υπογραφή
            document.add(line);
            Paragraph footer = new Paragraph(
                    "Υπογραφή Ιατρού: " + doctor + "\n\n" +
                            "Δημιουργήθηκε μέσω εφαρμογής eHealth",
                    smallFont
            );
            footer.setAlignment(Paragraph.ALIGN_RIGHT);
            document.add(footer);

            document.close();

            // Αποθήκευση στα Downloads με MediaStore
            android.content.ContentValues values = new android.content.ContentValues();
            values.put(android.provider.MediaStore.Downloads.DISPLAY_NAME, "Prescription_" + prescriptionId + ".pdf");
            values.put(android.provider.MediaStore.Downloads.MIME_TYPE, "application/pdf");
            values.put(android.provider.MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Prescriptions");

            android.net.Uri uri = requireContext().getContentResolver()
                    .insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

            if (uri != null) {
                try (java.io.OutputStream out = requireContext().getContentResolver().openOutputStream(uri);
                     java.io.FileInputStream in = new java.io.FileInputStream(tempFile)) {

                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        out.write(buffer, 0, len);
                    }
                }
            }

            Toast.makeText(requireContext(),
                    "Το PDF αποθηκεύτηκε στα Downloads/Prescriptions",
                    Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(),
                    "⚠️ Σφάλμα PDF: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }
}