package com.unipi.katerina.eHealth.Patients;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unipi.katerina.eHealth.Admin.AboutUsFragment;
import com.unipi.katerina.eHealth.MainActivity;
import com.unipi.katerina.eHealth.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PatientHomeFragment extends Fragment {

    TextView textWelcome, tvUpcomingTitle, tvNextAppointment, tvCountdown, notificationBadge;
    LinearLayout btnPrescription1, btnHistory, btnMyDoctors, btnFaq, btnVisit, btnSearchDoctor, btnBookAppointment, layoutUpcomingAppointments, containerUpcomingList, layoutNextAppointment;
    FirebaseFirestore db;
    String patientUid;
    android.os.CountDownTimer countDownTimer;
    ImageView menuIcon, notificationIcon;
    com.google.firebase.firestore.ListenerRegistration notificationListener;
    FrameLayout notificationContainer;
    android.widget.PopupWindow popupWindow;

    public PatientHomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_home, container, false);

        textWelcome = view.findViewById(R.id.textWelcome);
        btnHistory = view.findViewById(R.id.btnHistory);
        btnMyDoctors = view.findViewById(R.id.btnMyDoctors);
        btnFaq = view.findViewById(R.id.btnFaq);
        btnVisit = view.findViewById(R.id.btnVisit);
        btnSearchDoctor = view.findViewById(R.id.btnSearchDoctor);
        btnBookAppointment = view.findViewById(R.id.btnBookAppointment);
        btnPrescription1 = view.findViewById(R.id.btnPrescription1);
        layoutUpcomingAppointments = view.findViewById(R.id.layoutUpcomingAppointments);
        containerUpcomingList = view.findViewById(R.id.containerUpcomingList);
        tvUpcomingTitle = view.findViewById(R.id.tvUpcomingTitle);
        layoutNextAppointment = view.findViewById(R.id.layoutNextAppointment);
        tvNextAppointment = view.findViewById(R.id.tvNextAppointment);
        tvCountdown = view.findViewById(R.id.tvCountdown);
        menuIcon = view.findViewById(R.id.menuIcon);
        notificationIcon = view.findViewById(R.id.notificationIcon);
        notificationBadge = view.findViewById(R.id.notificationBadge);
        notificationContainer = view.findViewById(R.id.notificationContainer);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("Patients").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String firstName = documentSnapshot.getString("FirstName");
                            String greeting = getGreetingMessage();
                            if (firstName != null && !firstName.isEmpty()) {
                                textWelcome.setText(greeting + ", " + firstName);
                            } else {
                                textWelcome.setText(greeting + ", χρήστη");  // fallback
                            }
                        } else {
                            textWelcome.setText("Καλωσήρθες!"); // αν δεν βρεθεί το doc
                        }
                    })
                    .addOnFailureListener(e -> {
                        textWelcome.setText("Καλωσήρθες!"); // αν γίνει λάθος
                    });
        }

        db = FirebaseFirestore.getInstance();
        if (user != null) {
            patientUid = user.getUid();
            loadUpcomingAppointments();
            startNotificationBadgeListener();
        }

        setupMenu();

        setupButtons();

        notificationContainer.setOnClickListener(v -> {
            showNotificationsPopup(v);
        });

        return view;
    }

    private void setupButtons() {
        btnHistory.setOnClickListener(v -> openFragment(new PatientHistoryFragment()));
        btnMyDoctors.setOnClickListener(v -> openFragment(new MyDoctorsFragment()));
        btnFaq.setOnClickListener(v -> openFragment(new FAQFragment()));
        btnVisit.setOnClickListener(v -> openFragment(new MyAppointmentsFragment()));
        btnSearchDoctor.setOnClickListener(v -> openFragment(new SearchDoctorFragment()));
        btnBookAppointment.setOnClickListener(v -> openFragment(new BookAppointmentFragment()));
        btnPrescription1.setOnClickListener(v -> openFragment(new PatientPrescriptionsFragment()));
    }

    private void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.patient_fragment_container, fragment); // R.id.patient_fragment_container = ID του container στο activity
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void setupMenu() {
        menuIcon.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), menuIcon);
            popup.getMenuInflater().inflate(R.menu.menu_home, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();

                if (id == R.id.profile) {
                    openFragment(new PatientProfileFragment());
                    return true;
                } else if (id == R.id.support) {
                    openFragment(new AboutUsFragment());
                    return true;
                } else if (id == R.id.logout) {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                    return true;
                }
                return false;
            });

            popup.show();
        });
    }

    private String getGreetingMessage() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 12) {
            return getString(R.string.PatientHomeFragment_getGreetingMessage_GoodMorning);
        } else if (hour >= 12 && hour < 15) {
            return getString(R.string.PatientHomeFragment_getGreetingMessage_GoodAfternnon);
        } else if (hour >= 15 && hour < 21) {
            return getString(R.string.PatientHomeFragment_getGreetingMessage_GoodEvening);
        } else {
            return getString(R.string.PatientHomeFragment_getGreetingMessage_GoodNight);
        }
    }

    private void loadUpcomingAppointments() {
        if (patientUid == null) {
            return;
        }

        db.collection("Appointments")
                .whereEqualTo("patientUid", patientUid)
                .whereEqualTo("status", "Επιβεβαιωμένο")
                .get()
                .addOnSuccessListener(snapshot -> {
                    containerUpcomingList.removeAllViews();

                    List<Map<String, Object>> upcoming = new ArrayList<>();
                    Date now = new Date();

                    for (var doc : snapshot) {
                        String dateStr = doc.getString("date");
                        String timeStr = doc.getString("time");
                        if (dateStr == null || timeStr == null) {
                            continue; //Αν λείπει η ώρα ή η ημερομηνία τότε τα αγνοεί
                        }

                        try {
                            Date dateTime = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).parse(dateStr + " " + timeStr);
                            if (dateTime != null && dateTime.after(now)) {
                                Map<String, Object> item = new HashMap<>();
                                item.put("doctorName", doc.getString("doctorName"));
                                item.put("date", dateStr);
                                item.put("time", timeStr);
                                item.put("doctorSpecialty", doc.getString("doctorSpecialty"));
                                upcoming.add(item);
                            }
                        } catch (Exception ignored) {

                        }
                    }

                    // Ταξινόμηση με βάση την ημερομηνία
                    upcoming.sort((a, b) -> {
                        try {
                            var sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
                            Date d1 = sdf.parse(a.get("date") + " " + a.get("time"));
                            Date d2 = sdf.parse(b.get("date") + " " + b.get("time"));
                            return d1.compareTo(d2);
                        } catch (Exception e) {
                            return 0;
                        }
                    });

                    if (!upcoming.isEmpty()) {
                        Map<String, Object> nextAppt = upcoming.get(0);

                        String date = (String) nextAppt.get("date");
                        String time = (String) nextAppt.get("time");
                        String doctor = (String) nextAppt.get("doctorName");
                        String specialty = (String) nextAppt.get("doctorSpecialty");

                        tvNextAppointment.setText("📅 " + date + ", " + time + " | " + doctor + " (" + specialty + ")");
                        layoutNextAppointment.setVisibility(View.VISIBLE);

                        // Έναρξη countdown
                        startCountdown(date, time);
                    }

                    // Εμφάνιση μέχρι 3-4 ραντεβού
                    int limit = Math.min(4, upcoming.size());
                    for (int i = 1; i < limit; i++) {
                        Map<String, Object> appt = upcoming.get(i);
                        addAppointmentToLayout(
                                appt.get("date").toString(),
                                appt.get("time").toString(),
                                appt.get("doctorName").toString(),
                                appt.get("doctorSpecialty").toString()
                        );
                    }
                    // Αν δεν υπάρχουν ραντεβού δεν εμφανίζεται
                    layoutUpcomingAppointments.setVisibility(!upcoming.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> layoutUpcomingAppointments.setVisibility(View.GONE));
    }

    private void addAppointmentToLayout(String date, String time, String doctor, String specialty) {
        TextView tv = new TextView(getContext());
        tv.setText("🕒 " + date + ", " + time + " | " + doctor + " (" + specialty + ")");
        tv.setTextSize(14f);
        tv.setTextColor(getResources().getColor(android.R.color.black));
        tv.setPadding(0, 8, 0, 8);
        containerUpcomingList.addView(tv);
    }

    private void startCountdown(String dateStr, String timeStr) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
            java.util.Date appointmentDate = sdf.parse(dateStr + " " + timeStr);
            if (appointmentDate == null) {
                return;
            }

            long millisUntil = appointmentDate.getTime() - System.currentTimeMillis();
            if (millisUntil <= 0) {
                layoutNextAppointment.setVisibility(View.GONE);
                return;
            }

            countDownTimer = new android.os.CountDownTimer(millisUntil, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    long totalSeconds = millisUntilFinished / 1000;
                    long days = totalSeconds / 86400;
                    long hours = (totalSeconds % 86400) / 3600;
                    long minutes = (totalSeconds % 3600) / 60;
                    long seconds = totalSeconds % 60;

                    String formatted = String.format(Locale.getDefault(),
                            "Απομένουν: %02d ημέρες %02d:%02d:%02d", days, hours, minutes, seconds);
                    tvCountdown.setText(formatted);
                }

                @Override
                public void onFinish() {
                    layoutNextAppointment.setVisibility(View.GONE);
                    loadUpcomingAppointments(); // Επαναφόρτωση για να φέρει το επόμενο
                }
            }.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showNotificationsPopup(View anchor) {
        if (patientUid == null) {
            return;
        }

        // Μαρκάρουμε ως αναγνωσμένες
        db.collection("Notifications")
                .whereEqualTo("patientUid", patientUid)
                .whereEqualTo("isRead", false)
                .get()
                .addOnSuccessListener(snaps -> {
                    for (var d : snaps) {
                        d.getReference().update("isRead", true);
                    }
                });

        // Φέρνουμε τις 5 πιο πρόσφατες ειδοποιήσεις
        db.collection("Notifications")
                .whereEqualTo("patientUid", patientUid)
                .whereIn("status", Arrays.asList("Επιβεβαιωμένο", "Ακυρωμένο", "Νέα συνταγή")) //για τον ασθενή
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        showSimplePopup(anchor, "Δεν υπάρχουν νέες ειδοποιήσεις.");
                        return;
                    }

                    // Δημιουργία custom layout για το popup
                    View popupView = LayoutInflater.from(getContext()).inflate(R.layout.popup_notifications, null);
                    LinearLayout container = popupView.findViewById(R.id.containerNotifications);
                    TextView linkView = popupView.findViewById(R.id.linkAllNotifications);

                    List<String> unreadIds = new ArrayList<>();

                    for (var doc : snapshot) {
                        String msg = doc.getString("message");
                        String date = doc.getString("date");
                        String time = doc.getString("time");
                        boolean isRead = Boolean.TRUE.equals(doc.getBoolean("isRead"));

                        // Δημιουργία TextView
                        TextView tv = new TextView(getContext());
                        tv.setText("📅 " + date + " " + time + "\n" + msg);
                        tv.setTextSize(14f);
                        tv.setPadding(16, 12, 16, 12);
                        tv.setTextColor(getResources().getColor(android.R.color.black));

                        // Ελαφρύ separator
                        View divider = new View(getContext());
                        divider.setLayoutParams(new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT, 1));
                        divider.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));

                        container.addView(tv);
                        container.addView(divider);
                    }

                    // Όταν ανοίξει το popup, μαρκάρουμε ως αναγνωσμένες
                    if (!unreadIds.isEmpty()) {
                        for (String id : unreadIds) {
                            db.collection("Notifications")
                                    .document(id)
                                    .update("isRead", true);
                        }
                    }

                    // "Δες όλες τις ειδοποιήσεις"
                    linkView.setOnClickListener(v -> {
                        popupWindow.dismiss();
                        openFragment(new NotificationsFragment());
                    });

                    // Δημιουργία PopupWindow
                    popupWindow = new android.widget.PopupWindow(
                            popupView,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            true
                    );

                    // Στυλ popup
                    popupWindow.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.WHITE));
                    popupWindow.setElevation(12f);
                    popupWindow.setOutsideTouchable(true);
                    popupWindow.setFocusable(true);

                    // Μετάθεση για να εμφανιστεί σωστά κάτω από το καμπανάκι
                    anchor.post(() -> {
                        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                        int xOffset = -popupView.getMeasuredWidth() + anchor.getWidth();
                        popupWindow.showAsDropDown(anchor, xOffset, 15);
                    });
                });
    }

    private void showSimplePopup(View anchor, String message) {
        View popupView = LayoutInflater.from(getContext()).inflate(R.layout.popup_notifications, null);
        LinearLayout container = popupView.findViewById(R.id.containerNotifications);
        TextView tv = new TextView(getContext());
        tv.setText(message);
        tv.setPadding(8, 8, 8, 8);
        container.addView(tv);

        popupWindow = new android.widget.PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);
        popupWindow.setElevation(10f);
        popupWindow.showAsDropDown(anchor, -150, 0);
    }

    private void startNotificationBadgeListener() {
        if (patientUid == null) {
            return;
        }

        // Αν υπάρχει ήδη listener, το σταματάμε για να μην διπλασιάζεται
        if (notificationListener != null) {
            notificationListener.remove();
        }

        notificationListener = db.collection("Notifications")
                .whereEqualTo("patientUid", patientUid)
               // .whereEqualTo("status", "Ακυρωμένο")
                .whereEqualTo("isRead", false) // μόνο μη αναγνωσμένες
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null || snapshot == null) {
                        return;
                    }

                    int unreadCount = snapshot.size();
                    if (unreadCount > 0) {
                        notificationBadge.setText(String.valueOf(unreadCount));
                        notificationBadge.setVisibility(View.VISIBLE);
                    } else {
                        notificationBadge.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (notificationListener != null) {
            notificationListener.remove();
            notificationListener = null;
        }
    }
}