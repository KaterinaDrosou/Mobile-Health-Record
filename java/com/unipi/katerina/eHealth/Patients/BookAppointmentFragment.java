package com.unipi.katerina.eHealth.Patients;

import android.app.DatePickerDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.unipi.katerina.eHealth.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class BookAppointmentFragment extends Fragment {

    Spinner spinnerDoctors;
    Button btnSelectDate, btnBook;
    GridLayout gridTimeSlots;
    FirebaseFirestore db;
    String patientId, selectedDoctorUid, selectedDoctorName, selectedDoctorSpecialty, selectedDate, selectedTime;
    Map<String, Map<String, String>> weeklySchedule = new HashMap<>();
    final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public BookAppointmentFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_book_appointment, container, false);

        spinnerDoctors = view.findViewById(R.id.spinnerDoctors);
        btnSelectDate = view.findViewById(R.id.btnSelectDate);
        btnBook = view.findViewById(R.id.btnBook);
        gridTimeSlots = view.findViewById(R.id.gridTimeSlots);

        db = FirebaseFirestore.getInstance();
        patientId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadDoctors();
        setupDatePicker();
        btnBook.setOnClickListener(v -> saveAppointment());

        return view;
    }

    private void loadDoctors() {
        db.collection("Doctors").get().addOnSuccessListener(snapshot -> {
            List<String> displayList = new ArrayList<>();
            List<String> uids = new ArrayList<>();
            List<String> names = new ArrayList<>();
            List<String> specialties = new ArrayList<>();

            for (QueryDocumentSnapshot doc : snapshot) {
                String name = doc.getString("FullName");
                String specialty = doc.getString("specialty");
                String uid = doc.getString("uid");

                displayList.add(name + " - " + specialty);
                uids.add(uid);
                names.add(name);
                specialties.add(specialty);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_spinner_item, displayList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerDoctors.setAdapter(adapter);

            spinnerDoctors.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedDoctorUid = uids.get(position);
                    selectedDoctorName = names.get(position);
                    selectedDoctorSpecialty = specialties.get(position);
                    loadDoctorSchedule(selectedDoctorUid);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        });
    }

    // Φορτώνει το ωράριο του ιατρού
    private void loadDoctorSchedule(String doctorUid) {
        db.collection("Doctors").document(doctorUid).get().addOnSuccessListener(doc -> {
            if (doc.exists() && doc.get("weeklySchedule") != null) {
                weeklySchedule = (Map<String, Map<String, String>>) doc.get("weeklySchedule");
            }
        });
    }

    //Ημερολόγιο
    private void setupDatePicker() {
        btnSelectDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(
                    getContext(),
                    (view, year, month, dayOfMonth) -> {
                        Calendar selectedCal = Calendar.getInstance();
                        selectedCal.set(year, month, dayOfMonth);

                        // Μετατροπή ημέρας σε ελληνικό όνομα
                        String dayName = getGreekDayName(selectedCal.get(Calendar.DAY_OF_WEEK));

                        if (!weeklySchedule.containsKey(dayName)) {
                            Toast.makeText(getContext(), "Ο γιατρός δεν εργάζεται " + dayName, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                        btnSelectDate.setText(selectedDate);
                        loadAvailableSlots(dayName, selectedDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            // Περιορισμός στους επόμενες 3 μήνες
            Calendar maxDate = Calendar.getInstance();
            maxDate.add(Calendar.MONTH, 3);
            dialog.getDatePicker().setMinDate(System.currentTimeMillis());
            dialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
            dialog.show();
        });
    }

    // Φορτώνει τις διαθέσιμες ώρες ανά ημέρα
    private void loadAvailableSlots(String dayName, String date) {
        gridTimeSlots.removeAllViews();

        Map<String, String> hours = weeklySchedule.get(dayName);
        if (hours == null) {
            Toast.makeText(getContext(), "Δεν βρέθηκαν ώρες για " + dayName, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Date start = sdf.parse(hours.get("start"));
            Date end = sdf.parse(hours.get("end"));

            if (start == null || end == null) {
                return;
            }

            Calendar cal = Calendar.getInstance();
            cal.setTime(start);

            List<String> slots = new ArrayList<>();
            while (cal.getTime().before(end)) {
                slots.add(sdf.format(cal.getTime()));
                cal.add(Calendar.MINUTE, 30); // ημίωρα
            }

            // Αναζήτηση κρατημένων ραντεβού από το collection "Appointments"
            db.collection("Appointments")
                    .whereEqualTo("doctorUid", selectedDoctorUid)
                    .whereEqualTo("date", date)
                    .whereEqualTo("status", "Επιβεβαιωμένο")
                    .get()
                    .addOnSuccessListener(q -> {
                        Set<String> booked = new HashSet<>();
                        for (QueryDocumentSnapshot doc : q) {
                            booked.add(doc.getString("time"));
                        }
                        showTimeSlots(slots, booked);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Σφάλμα φόρτωσης ωρών!", Toast.LENGTH_SHORT).show());

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    // Εμφανίζει τις ώρες
    private void showTimeSlots(List<String> slots, Set<String> booked) {
        gridTimeSlots.setColumnCount(3);
        for (String slot : slots) {
            Button btn = new Button(getContext());
            btn.setText(slot);
            btn.setPadding(8, 8, 8, 8);

            if (booked.contains(slot)) {
                btn.setEnabled(false);
                btn.setAlpha(0.5f);
                btn.setBackgroundResource(R.drawable.rounded_button_gray);
            } else {
                btn.setBackgroundResource(R.drawable.rounded_button);
                btn.setOnClickListener(v -> {
                    selectedTime = slot;
                    clearSlotSelection();
                    btn.setBackgroundResource(R.drawable.selected_button);
                });
            }
            gridTimeSlots.addView(btn);
        }

        if (gridTimeSlots.getChildCount() == 0) {
            Toast.makeText(getContext(), "Δεν υπάρχουν διαθέσιμες ώρες.", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearSlotSelection() {
        for (int i = 0; i < gridTimeSlots.getChildCount(); i++) {
            View view = gridTimeSlots.getChildAt(i);
            if (view instanceof Button) {
                view.setBackgroundResource(R.drawable.rounded_button);
            }
        }
    }

    private void saveAppointment() {
        if (selectedDoctorUid == null || selectedDate == null || selectedTime == null) {
            Toast.makeText(getContext(), "Συμπληρώστε γιατρό, ημερομηνία και ώρα!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Φορτώνει το όνομα του ασθενή
        db.collection("Patients").document(patientId).get().addOnSuccessListener(patientDoc -> {
            String firstName = patientDoc.getString("FirstName");
            String lastName = patientDoc.getString("LastName");
            String patientName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
            patientName = patientName.trim();

            Map<String, Object> data = new HashMap<>();
            data.put("doctorUid", selectedDoctorUid);
            data.put("doctorName", selectedDoctorName);
            data.put("doctorSpecialty", selectedDoctorSpecialty);
            data.put("patientUid", patientId);
            data.put("patientName", patientName);
            data.put("date", selectedDate);
            data.put("time", selectedTime);
            data.put("status", "Εκκρεμεί");
            data.put("timestamp", new Date());

            // Δημιουργία document reference ώστε να βάλω το appointmentId
            DocumentReference newAppointmentRef = db.collection("Appointments").document();
            data.put("appointmentId", newAppointmentRef.getId());

            newAppointmentRef.set(data)
                    .addOnSuccessListener(doc -> {
                        Toast.makeText(getContext(), "✅ Επιτυχής κράτηση!", Toast.LENGTH_SHORT).show();
                        clearForm();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "❌ Σφάλμα κατά την αποθήκευση!", Toast.LENGTH_SHORT).show());
        });
    }

    private void clearForm() {
        btnSelectDate.setText("Επιλέξτε ημερομηνία");
        gridTimeSlots.removeAllViews();
        selectedDate = null;
        selectedTime = null;
    }

    private String getGreekDayName(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.MONDAY: return "Δευτέρα";
            case Calendar.TUESDAY: return "Τρίτη";
            case Calendar.WEDNESDAY: return "Τετάρτη";
            case Calendar.THURSDAY: return "Πέμπτη";
            case Calendar.FRIDAY: return "Παρασκευή";
            case Calendar.SATURDAY: return "Σάββατο";
            case Calendar.SUNDAY: return "Κυριακή";
            default: return "";
        }
    }
}