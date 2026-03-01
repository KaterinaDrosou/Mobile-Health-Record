package com.unipi.katerina.eHealth.Doctors;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.*;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unipi.katerina.eHealth.MainActivity;
import com.unipi.katerina.eHealth.R;

import java.text.SimpleDateFormat;
import java.util.*;

public class DoctorRegisterActivity extends AppCompatActivity {

    EditText editTextFullName, editTextClinicAddress, editTextEmail, editTextPassword, editTextPhone, editTextConfirmPassword,
            startMonday, endMonday, startTuesday, endTuesday, startWednesday, endWednesday, startThursday, endThursday, startFriday, endFriday, startSaturday, endSaturday, startSunday, endSunday;
    Button btnRegister;
    ImageView eyeIcon, eyeConfirmIcon;
    Spinner spinnerSpecialty;
    TextView textViewPasswordStrength;
    FirebaseAuth auth;
    FirebaseFirestore db;
    CheckBox checkMonday, checkTuesday, checkWednesday, checkThursday, checkFriday, checkSaturday, checkSunday;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_doctor_register);

        editTextFullName = findViewById(R.id.editTextFullName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextClinicAddress = findViewById(R.id.editTextAddress);
        spinnerSpecialty = findViewById(R.id.spinnerSpecialty);
        btnRegister = findViewById(R.id.btnRegister);
        textViewPasswordStrength = findViewById(R.id.textPasswordStrength);
        eyeIcon = findViewById(R.id.imgTogglePassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        eyeConfirmIcon = findViewById(R.id.imgToggleConfirmPassword);

        // Πεδία για κάθε ημέρα
        checkMonday = findViewById(R.id.checkMonday);
        checkTuesday = findViewById(R.id.checkTuesday);
        checkWednesday = findViewById(R.id.checkWednesday);
        checkThursday = findViewById(R.id.checkThursday);
        checkFriday = findViewById(R.id.checkFriday);
        checkSaturday = findViewById(R.id.checkSaturday);
        checkSunday = findViewById(R.id.checkSunday);

        startMonday = findViewById(R.id.startMonday);
        endMonday = findViewById(R.id.endMonday);
        startTuesday = findViewById(R.id.startTuesday);
        endTuesday = findViewById(R.id.endTuesday);
        startWednesday = findViewById(R.id.startWednesday);
        endWednesday = findViewById(R.id.endWednesday);
        startThursday = findViewById(R.id.startThursday);
        endThursday = findViewById(R.id.endThursday);
        startFriday = findViewById(R.id.startFriday);
        endFriday = findViewById(R.id.endFriday);
        startSaturday = findViewById(R.id.startSaturday);
        endSaturday = findViewById(R.id.endSaturday);
        startSunday = findViewById(R.id.startSunday);
        endSunday = findViewById(R.id.endSunday);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //Spinner Ειδικοτήτων
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.doctor_specialties,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSpecialty.setAdapter(adapter);

        btnRegister.setOnClickListener(v -> registerDoctor());

        editTextPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = s.toString();
                if (password.isEmpty()) {
                    textViewPasswordStrength.setVisibility(View.GONE);
                } else {
                    textViewPasswordStrength.setVisibility(View.VISIBLE);
                    String strength = isStrongPassword(password);

                    //Εμφάνιση μηνύματος για κωδικό πρόσβασης
                    switch (strength) {
                        case "invalid":
                            textViewPasswordStrength.setText(R.string.PasswordMainText);
                            textViewPasswordStrength.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                            break;
                        case "weak":
                            textViewPasswordStrength.setText(R.string.WeakPassword);
                            textViewPasswordStrength.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                            break;
                        case "medium":
                            textViewPasswordStrength.setText(R.string.MediumPassword);
                            textViewPasswordStrength.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                            break;
                        case "strong":
                            textViewPasswordStrength.setText(R.string.StrongPassword);
                            textViewPasswordStrength.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                            break;
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //Κώδικας για toggle ματιού στο πεδίο κωδικός πρόσβασης
        eyeIcon.setOnClickListener(v -> {
            if (editTextPassword.getTransformationMethod() instanceof PasswordTransformationMethod) {
                //Εμφάνιση κωδικού
                editTextPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                eyeIcon.setImageResource(R.drawable.visibility_24px);
            } else {
                //Απόκρυψη κωδικού
                editTextPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                eyeIcon.setImageResource(R.drawable.visibility_off_24px);
            }
            //Τοποθέτηση κέρσοσα στο τέλος
            editTextPassword.setSelection(editTextPassword.getText().length());
        });

        //Κώδικας για toggle ματιού στο πεδίο επαλήθευση κωδικού πρόσβαση
        eyeConfirmIcon.setOnClickListener(v -> {
            if (editTextConfirmPassword.getTransformationMethod() instanceof PasswordTransformationMethod) {
                // Εμφάνιση κωδικού
                editTextConfirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                eyeConfirmIcon.setImageResource(R.drawable.visibility_24px);
            } else {
                // Απόκρυψη κωδικού
                editTextConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                eyeConfirmIcon.setImageResource(R.drawable.visibility_off_24px);
            }
            // Κέρσορας στο τέλος
            editTextConfirmPassword.setSelection(editTextConfirmPassword.getText().length());
        });
    }
    private void registerDoctor() {
        String fullName = editTextFullName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String clinicAddress = editTextClinicAddress.getText().toString().trim();
        String specialty = spinnerSpecialty.getSelectedItem().toString();

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || phone.isEmpty() || clinicAddress.isEmpty() || specialty.isEmpty()) {
            showMessage("Σημαντικό!", "Παρακαλώ συμπληρώστε όλα τα πεδία!");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showMessage("Μη έγκυρος κωδικός", "Δεν ταιριάζει ο κωδικός πρόσβασης.");
            return;
        }

        if (!phone.matches("\\d{10}")) {
            showMessage("Λάθος τηλέφωνο", "Το τηλέφωνο πρέπει να περιέχει 10 ψηφία.");
            return;
        }

        // Έλεγχος ότι δεν υπάρχουν αγγλικά γράμματα
        if (clinicAddress.matches(".*[A-Za-z].*")) {
            showMessage("Σφάλμα", "Δεν επιτρέπονται λατινικοί χαρακτήρες. Χρησιμοποίησε μόνο ελληνικά.");
            return;
        }

        //Έλεγχος ότι η διεύθυνση περιέχει ονομασία, αριθμό, περιοχή, ΤΚ
        String addressPattern = "^[Α-Ωα-ωΆ-ώ\\.\\s]+\\s\\d+[\\-\\d]*,\\s?[Α-Ωα-ωΆ-ώ\\s]+,\\s?\\d{5}$";
        if (!clinicAddress.matches(addressPattern)) {
            showMessage("Μη έγκυρη Διεύθυνση", "Η διεύθυνση πρέπει να περιέχει οδό, αριθμό, περιοχή και ΤΚ 5 ψηφίων.\nΠαράδειγμα: Αγ. Δημητρίου 42, Αθήνα, 10561");
            return;
        }

        // Δημιουργία εβδομαδιαίου προγράμματος
        Map<String, Map<String, String>> weeklySchedule = new HashMap<>();
        addDaySchedule(weeklySchedule, "Δευτέρα", checkMonday, startMonday, endMonday);
        addDaySchedule(weeklySchedule, "Τρίτη", checkTuesday, startTuesday, endTuesday);
        addDaySchedule(weeklySchedule, "Τετάρτη", checkWednesday, startWednesday, endWednesday);
        addDaySchedule(weeklySchedule, "Πέμπτη", checkThursday, startThursday, endThursday);
        addDaySchedule(weeklySchedule, "Παρασκευή", checkFriday, startFriday, endFriday);
        addDaySchedule(weeklySchedule, "Σάββατο", checkSaturday, startSaturday, endSaturday);
        addDaySchedule(weeklySchedule, "Κυριακή", checkSunday, startSunday, endSunday);

        if (weeklySchedule.isEmpty()) {
            showMessage("Προσοχή", "Παρακαλώ ορίστε ωράριο για τουλάχιστον μία ημέρα.");
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                          //  user.sendEmailVerification();
                            saveDoctorData(user.getUid(), fullName, email, phone, clinicAddress, specialty, weeklySchedule);
                        }
                    } else {
                        Toast.makeText(this, "Αποτυχία εγγραφής: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void addDaySchedule(Map<String, Map<String, String>> weeklySchedule, String day, CheckBox checkBox, EditText start, EditText end) {
        if (checkBox.isChecked()) {
            String startHour = start.getText().toString().trim();
            String endHour = end.getText().toString().trim();

            // Validation: 24ωρη μορφή HH:mm
            if (!startHour.matches("^([01]\\d|2[0-3]):[0-5]\\d$") || !endHour.matches("^([01]\\d|2[0-3]):[0-5]\\d$")) {
                showMessage("Μη έγκυρη ώρα (" + day + ")", "Η ώρα πρέπει να είναι στη μορφή 24ωρου, π.χ. 09:00 ή 17:30");
                return;
            }

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                Date s = sdf.parse(startHour);
                Date e = sdf.parse(endHour);
                if (s != null && e != null && !e.after(s)) {
                    showMessage("Λάθος " + day, "Η ώρα λήξης πρέπει να είναι μετά την έναρξη (" + day + ")");
                    return;
                }
            } catch (Exception ignored) {}

            Map<String, String> hours = new HashMap<>();
            hours.put("start", startHour);
            hours.put("end", endHour);
            weeklySchedule.put(day, hours);
        }
    }

    private void saveDoctorData(String userId, String fullName, String email, String phone, String clinicAddress, String specialty, Map<String, Map<String, String>> weeklySchedule) {

        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", userId);
        userData.put("FullName", fullName);
        userData.put("email", email);
        userData.put("phone", phone);
        userData.put("clinicAddress", clinicAddress);
        userData.put("specialty", specialty);
        userData.put("weeklySchedule", weeklySchedule);
        userData.put("statusFromAdmin", "pending");

        // Αποθήκευση γιατρού στη συλλογή "Doctors"
        db.collection("Doctors").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    // Προσθήκη εγγραφής ρόλου στη συλλογή "Users"
                    Map<String, Object> userRole = new HashMap<>();
                    userRole.put("email", email);
                    userRole.put("role", "doctor");

                    db.collection("Users").document(userId)
                            .set(userRole)
                            .addOnSuccessListener(unused -> {
                                FirebaseAuth.getInstance().signOut();
                                Toast.makeText(this, "Ο λογαριασμός δημιουργήθηκε επιτυχώς! Αναμένεται έγκριση διαχειριστή.", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(this, MainActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Σφάλμα αποθήκευσης ρόλου: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Σφάλμα αποθήκευσης δεδομένων: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void showMessage(String title, String message) {
        new android.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .show();
    }

    private String isStrongPassword(String password) {
        if (password.length() < 8 || password.length() > 20) {
            return "invalid";
        }

        boolean hasLetter = password.matches(".*[A-Za-zΑ-Ωα-ω].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSymbol = password.matches(".*[^A-Za-z0-9Α-Ωα-ω].*");

        if (hasLetter && hasDigit && hasSymbol) {
            return "strong";
        } else if ((hasLetter && hasDigit) || (hasLetter && hasSymbol) || (hasDigit && hasSymbol)) {
            return "medium";
        } else {
            return "weak";
        }
    }
}