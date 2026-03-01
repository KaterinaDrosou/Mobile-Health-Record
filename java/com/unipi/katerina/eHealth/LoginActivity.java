package com.unipi.katerina.eHealth;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unipi.katerina.eHealth.Admin.AdminDashboardActivity;
import com.unipi.katerina.eHealth.Doctors.DoctorDashboardActivity;
import com.unipi.katerina.eHealth.Patients.PatientDashboardActivity;

public class LoginActivity extends AppCompatActivity {
    EditText editLoginEmail, editLoginPassword;
    TextView TextviewforgotPassword;
    ImageView eyeIcon;
    Button btnLogin;
    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        editLoginEmail = findViewById(R.id.editLoginEmail);
        editLoginPassword = findViewById(R.id.editLoginPassword);
        eyeIcon = findViewById(R.id.imgTogglePassword);
        TextviewforgotPassword = findViewById(R.id.textViewForgotPassword);
        btnLogin = findViewById(R.id.btnLogin);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnLogin.setOnClickListener(v -> loginUser());

        //Κώδικας για επαναφορά κωδικού πρόσβασης
        TextviewforgotPassword.setOnClickListener(v -> {
            String email = editLoginEmail.getText().toString().trim();

            if (email.isEmpty()) {
                showMessage("Προσοχή!", "Παρακαλώ συμπληρώστε το email σας για να λάβετε σύνδεσμο επαναφοράς κωδικού πρόσβασης.");
                return;
            }

            auth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    showMessage("Επιτυχία!", "Στάλθηκε email επαναφοράς κωδικού");
                } else {
                    showMessage("Σφάλμα!", "Αποτυχία αποστολής email. Παρακαλώ ελέξτε το email που δώσατε!");
                }
            });
        });

        //Κώδικας για toggle ματιού στο πεδίο κωδικός πρόσβασης
        eyeIcon.setOnClickListener(v -> {
            if (editLoginPassword.getTransformationMethod() instanceof PasswordTransformationMethod) {
                //Εμφάνιση κωδικού
                editLoginPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                eyeIcon.setImageResource(R.drawable.visibility_24px);
            } else {
                //Απόκρυψη κωδικού
                editLoginPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                eyeIcon.setImageResource(R.drawable.visibility_off_24px);
            }
            //Τοποθέτηση κέρσοσα στο τέλος
            editLoginPassword.setSelection(editLoginPassword.getText().length());
        });
    }

    private void loginUser() {
        String email = editLoginEmail.getText().toString().trim();
        String password = editLoginPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showMessage("Σημαντικό!", "Παρακαλώ συμπληρώστε όλα τα πεδία!");
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser currentUser = auth.getCurrentUser();
                        if (currentUser != null) {
                            // Έλεγχος ρόλου πριν τον έλεγχο email
                            checkUserRole(currentUser.getUid());
                        }
                    } else {
                        showMessage("Σφάλμα!", "Αδυναμία σύνδεσης. Παρακαλώ ελέγξτε τα στοιχεία σας.");
                    }
                });
    }

    private void checkUserRole(String uid) {
        db.collection("Users").document(uid).get()
                .addOnSuccessListener(userDoc -> {
                    if (userDoc.exists()) {
                        String role = userDoc.getString("role");
                        FirebaseUser currentUser = auth.getCurrentUser();

                        // 1. Admin → μπαίνει χωρίς τίποτα
                        if ("admin".equals(role)) {
                            startActivity(new Intent(LoginActivity.this, AdminDashboardActivity.class));
                            finish();
                            return;
                        }

                        // 2. Doctor → ΔΕΝ ελέγχει email, μόνο approval
                        if ("doctor".equals(role)) {
                            db.collection("Doctors").document(uid).get()
                                    .addOnSuccessListener(doc -> {
                                        if (doc.exists()) {
                                            String approval = doc.getString("statusFromAdmin");
                                            if ("approved".equals(approval)) {
                                                startActivity(new Intent(LoginActivity.this, DoctorDashboardActivity.class));
                                                finish();
                                            } else if ("pending".equals(approval)) {
                                                showMessage("Εκκρεμεί έγκριση", "Η εγγραφή σας εκκρεμεί έγκριση από τον διαχειριστή.");
                                                auth.signOut();
                                            } else if ("rejected".equals(approval)) {
                                                showMessage("Απόρριψη εγγραφής", "Η εγγραφή σας απορρίφθηκε. Επικοινωνήστε με τον διαχειριστή.");
                                                auth.signOut();
                                            } else {
                                                showMessage("Πρόβλημα", "Η κατάστασή σας δεν είναι έγκυρη.");
                                                auth.signOut();
                                            }
                                        } else {
                                            showMessage("Σφάλμα", "Δεν βρέθηκαν στοιχεία για το λογαριασμό σας.");
                                            auth.signOut();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        showMessage("Σφάλμα", "Αποτυχία φόρτωσης δεδομένων γιατρού: " + e.getMessage());
                                        auth.signOut();
                                    });
                            return;
                        }

                        // 3. Patient → θέλει email verification
                        if ("patient".equals(role)) {
                            if (currentUser != null && !currentUser.isEmailVerified()) {
                                currentUser.sendEmailVerification()
                                        .addOnSuccessListener(aVoid ->
                                                showMessage("Απαιτείται επιβεβαίωση email", "Το email σας δεν έχει επιβεβαιωθεί. Ελέγξτε τα εισερχόμενά σας."))
                                        .addOnFailureListener(e ->
                                                showMessage("Σφάλμα", "Δεν ήταν δυνατή η αποστολή email: " + e.getMessage()));
                                auth.signOut();
                                return;
                            }

                            startActivity(new Intent(LoginActivity.this, PatientDashboardActivity.class));
                            finish();
                            return;
                        }

                        // Αν δεν είναι κανένας από τους 3
                        showMessage("Σφάλμα", "Μη αναγνωρισμένος ρόλος χρήστη.");
                        auth.signOut();

                    } else {
                        showMessage("Σφάλμα", "Δεν βρέθηκε ρόλος για αυτόν το χρήστη.");
                        auth.signOut();
                    }
                })
                .addOnFailureListener(e -> {
                    showMessage("Σφάλμα", "Αδυναμία πρόσβασης στα στοιχεία χρήστη: " + e.getMessage());
                    auth.signOut();
                });
    }

    private void showMessage(String title, String message){
        new android.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .show();
    }
}