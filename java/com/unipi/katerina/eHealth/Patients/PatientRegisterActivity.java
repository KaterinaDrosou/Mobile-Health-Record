package com.unipi.katerina.eHealth.Patients;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unipi.katerina.eHealth.MainActivity;
import com.unipi.katerina.eHealth.R;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class PatientRegisterActivity extends AppCompatActivity {
    EditText editTextFirstName, editTextLastName, editTextBirthDate, editTextEmail, editTextPassword, editTextAMKA, editTextAFM, editTextAddress, editTextPhone, editTextConfirmPassword;
    Button btnRegister;
    TextView textViewPasswordStrength;
    Spinner spinnerGender;
    ImageView eyeIcon, eyeConfirmIcon ;
    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_patient_register);

        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextBirthDate = findViewById(R.id.editTextBirthDate);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextAMKA = findViewById(R.id.editTextAMKA);
        editTextAFM = findViewById(R.id.editTextAFM);
        editTextAddress = findViewById(R.id.editTextAddress);
        editTextPhone = findViewById(R.id.editTextPhone);
        btnRegister = findViewById(R.id.btnRegister);
        textViewPasswordStrength = findViewById(R.id.textPasswordStrength);
        eyeIcon = findViewById(R.id.imgTogglePassword);
        spinnerGender = findViewById(R.id.spinnerGender);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        eyeConfirmIcon = findViewById(R.id.imgToggleConfirmPassword);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnRegister.setOnClickListener(v -> registerPatient());
        editTextBirthDate.setOnClickListener(v -> showDatePickerDialog());

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

        // Κώδικας για drop down λίστα στο πεδίο Φύλο
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.gender_options, // από strings.xml
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);
    }

    private void registerPatient() {
        String fistName = editTextFirstName.getText().toString().trim();
        String lastName = editTextLastName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String birthdate = editTextBirthDate.getText().toString().trim();
        String amka = editTextAMKA.getText().toString().trim();
        String afm = editTextAFM.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String gender = spinnerGender.getSelectedItem().toString();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        // Έλεγχος για κενά πεδία
        if (fistName.isEmpty() || lastName.isEmpty() || email.isEmpty() || birthdate.isEmpty() || password.isEmpty() || amka.isEmpty() || afm.isEmpty() || address.isEmpty() || phone.isEmpty() || gender.isEmpty()) {
            showMessage("Σημαντικό!", "Παρακαλώ συμπληρώστε όλα τα πεδία!");
            return;
        }

        // Έλεγχος ψηφίων ΑΜΚΑ
        if (!amka.matches("\\d{11}")) {
            showMessage("Λάθος ΑΜΚΑ", "Ο ΑΜΚΑ πρέπει να περιέχει ακριβώς 11 ψηφία.");
            return;
        }

        // Έλεγχος ψηφίων ΑΦΜ
        if (!afm.matches("\\d{9}")) {
            showMessage("Λάθος ΑΦΜ", "Ο ΑΦΜ πρέπει να περιέχει ακριβώς 9 ψηφία.");
            return;
        }

        // Έλεγχος τηλεφώνου
        if (!phone.matches("\\d{10}")) {
            showMessage("Λάθος τηλέφωνο", "Το τηλέφωνο πρέπει να περιέχει ακριβώς 10 ψηφία.");
            return;
        }

        // Έλεγχος αν ταιριάζει ο κωδικός πρόσβασης
        if (!password.equals(confirmPassword)) {
            showMessage("Μη έγκυρος κωδικός", "Δεν ταιριάζει ο κωδικός πρόσβασης.");
            return;
        }

        //Έλεγχος ότι η διεύθυνση περιέχει ονομασία, αριθμό, περιοχή, ΤΚ
        String addressPattern = "^[Α-Ωα-ωΆ-ώ0-9\\s\\.]+\\s[0-9]+[α-ωΑ-Ω]*,\\s?[Α-Ωα-ωΆ-ώ\\s]+,\\s?\\d{5}$";
        if (!address.matches(addressPattern)) {
            showMessage("Μη έγκυρη Διεύθυνση", "Η διεύθυνση πρέπει να περιέχει οδό, αριθμό, περιοχή και ΤΚ 5 ψηφίων.\nΠαράδειγμα: Αγ. Δημητρίου 42, Αθήνα, 10561");
            return;
        }

        // Έλεγχος για ύπαρξη ΑΜΚΑ και ΑΦΜ στη βάση δεδομένων
        db.collection("Patients").whereEqualTo("amka", amka).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            // Αν υπάρχει ΑΜΚΑ, εμφάνιση σφάλματος
                            showMessage("Σφάλμα", "Ο ΑΜΚΑ αυτός χρησιμοποιείται ήδη από άλλον χρήστη.");
                        } else {
                            // Αν δεν υπάρχει ΑΜΚΑ, προχωράμε στον έλεγχο ΑΦΜ
                            db.collection("Patients").whereEqualTo("afm", afm).get()
                                    .addOnCompleteListener(taskAfm -> {
                                        if (taskAfm.isSuccessful()) {
                                            if (!taskAfm.getResult().isEmpty()) {
                                                // Αν υπάρχει ΑΦΜ, εμφάνιση σφάλματος
                                                showMessage("Σφάλμα", "Ο ΑΦΜ αυτός χρησιμοποιείται ήδη από άλλον χρήστη.");
                                            } else {
                                                // Αν δεν υπάρχει ούτε ΑΜΚΑ ούτε ΑΦΜ, προχωράμε με την εγγραφή
                                                createAccount(email, password, amka, afm, fistName, lastName, address, phone, birthdate, gender);
                                            }
                                        } else {
                                            showMessage("Σφάλμα", "Σφάλμα κατά τον έλεγχο ΑΦΜ.");
                                        }
                                    });
                        }
                    } else {
                        showMessage("Σφάλμα", "Σφάλμα κατά τον έλεγχο ΑΜΚΑ.");
                    }
                });
    }

    private void createAccount(String email, String password, String amka, String afm, String fistName, String lastName, String address, String phone, String birthdate, String gender) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            user.sendEmailVerification().addOnCompleteListener(verifyTask -> {
                                if (verifyTask.isSuccessful()) {
                                    Toast.makeText(this, "Στάλθηκε email επιβεβαίωσης λογαριασμού",Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(this, "Αποτυχία αποστολής email επιβεβαίωσης", Toast.LENGTH_LONG).show();
                                }
                            });

                            //Αποθήκευση στοιχείων στη ΒΔ
                            savePatientData(user.getUid(), fistName, lastName, email, amka, afm, address, phone, birthdate, gender);
                        }
                    } else {
                        Toast.makeText(PatientRegisterActivity.this, "Αδυναμία εγγραφής: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showMessage(String title, String message){
        new android.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .show();
    }

    //Για εμφάνιση ημερολόγιου
    public void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                PatientRegisterActivity.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Μορφοποίηση: DD/MM/YYYY
                    String formatDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear); // Ο μήνας αρχίζει από το 0
                    editTextBirthDate.setText(formatDate);
                },
                year, month, day
        );
        datePickerDialog.show();
    }
    private void savePatientData(String userId, String firstName, String lastName, String email, String amka, String afm, String address, String phone, String birthdate, String gender) {

        Map<String, Object> patientData = new HashMap<>();
        patientData.put("uid", userId);
        patientData.put("FirstName", firstName);
        patientData.put("LastName", lastName);
        patientData.put("email", email);
        patientData.put("amka", amka);
        patientData.put("afm", afm);
        patientData.put("address", address);
        patientData.put("phone", phone);
        patientData.put("birthdate", birthdate);
        patientData.put("gender", gender);

        // Δημιουργία καινούργιου document στο Patient collection με userID το document ID
        db.collection("Patients").document(userId)
                .set(patientData)
                .addOnSuccessListener(aVoid -> {
                    // Προσθήκη εγγραφής ρόλου στη συλλογή "Users"
                    Map<String, Object> userRole = new HashMap<>();
                    userRole.put("email", email);
                    userRole.put("role", "patient");

                    db.collection("Users").document(userId)
                            .set(userRole)
                            .addOnSuccessListener(unused -> {
                                FirebaseAuth.getInstance().signOut(); // Log out user μετά από αποθήκευση στοιχείων στη ΒΔ για αποφυγή auto sign-in
                                Toast.makeText(PatientRegisterActivity.this,
                                        "Ο λογαριασμός δημιουργήθηκε επιτυχώς!",
                                        Toast.LENGTH_LONG).show();
                                startActivity(new Intent(PatientRegisterActivity.this, MainActivity.class)); // Navigate to Main Activity
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(PatientRegisterActivity.this,
                                        "Σφάλμα αποθήκευσης ρόλου: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(PatientRegisterActivity.this,
                            "Αποτυχία αποθήκευσης στοιχείων χρήστη: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
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