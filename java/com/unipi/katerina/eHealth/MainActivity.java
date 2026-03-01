package com.unipi.katerina.eHealth;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.cardview.widget.CardView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.unipi.katerina.eHealth.Doctors.DoctorRegisterActivity;
import com.unipi.katerina.eHealth.Patients.PatientRegisterActivity;

public class MainActivity extends AppCompatActivity {
    Button btnAlreadyHaveAccount, btnTicket;
    CardView cardPatient, cardDoctor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Αρχικοποίηση κουμπιών
        cardPatient = findViewById(R.id.cardPatient);
        cardDoctor = findViewById(R.id.cardDoctor);
        btnAlreadyHaveAccount = findViewById(R.id.btnAlreadyHaveAccount);
        btnTicket = findViewById(R.id.btnTicket);

        cardPatient.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PatientRegisterActivity.class);
            startActivity(intent);
        });

        cardDoctor.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DoctorRegisterActivity.class);
            startActivity(intent);
        });

        btnTicket.setOnClickListener(v -> {
            String ticketUrl = "https://www.atlassian.com/software/jira?campaign=18442480203&adgroup=140479881486&targetid=kwd-855725830&matchtype=e&network=g&device=c&device_model=&creative=687972959756&keyword=jira&placement=&target=&ds_eid=700000001558501&ds_e1=GOOGLE&gad_source=1&gad_campaignid=18442480203&gclid=EAIaIQobChMIqeC4oMT2kAMV54lQBh3D8irYEAAYASAAEgIW1_D_BwE";
            Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(ticketUrl));
            startActivity(intent);
        });
    }

    //Κουμπί για σύνδεση
    public void goLogin(View view){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}