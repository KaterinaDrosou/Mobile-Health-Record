package com.unipi.katerina.eHealth.Doctors;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unipi.katerina.eHealth.Admin.AboutUsFragment;
import com.unipi.katerina.eHealth.MainActivity;
import com.unipi.katerina.eHealth.R;

import java.text.SimpleDateFormat;
import java.util.*;

public class DoctorHomeFragment extends Fragment {

    TextView textWelcome, tvUpcomingTitleDoctor, notificationBadgeDoctor;
    LinearLayout btnSearchPatient, btnMyPatients, btnFaq, btnMyAppointments, layoutUpcomingAppointmentsDoctor, containerUpcomingListDoctor;
    ImageView menuIcon, notificationIconDoctor;
    FirebaseFirestore db;
    String doctorUid;
    com.google.firebase.firestore.ListenerRegistration notificationListenerDoctor;
    android.widget.PopupWindow popupWindowDoctor;
    final SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_doctor_home, container, false);

        textWelcome = view.findViewById(R.id.textWelcome);
        menuIcon = view.findViewById(R.id.menuIcon);
        btnSearchPatient = view.findViewById(R.id.btnSearchPatient);
        btnMyPatients = view.findViewById(R.id.btnMyPatients);
        btnFaq = view.findViewById(R.id.btnFaq);
        btnMyAppointments = view.findViewById(R.id.btnMyAppointments);
        layoutUpcomingAppointmentsDoctor = view.findViewById(R.id.layoutUpcomingAppointmentsDoctor);
        tvUpcomingTitleDoctor = view.findViewById(R.id.tvUpcomingTitleDoctor);
        containerUpcomingListDoctor = view.findViewById(R.id.containerUpcomingListDoctor);
        notificationIconDoctor = view.findViewById(R.id.notificationIconDoctor);
        notificationBadgeDoctor = view.findViewById(R.id.notificationBadgeDoctor);
        FrameLayout notificationDoctorContainer = view.findViewById(R.id.notificationDoctorContainer);

        notificationDoctorContainer.setOnClickListener(v -> {
            showDoctorNotificationsPopup(v);
        });

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            doctorUid = user.getUid();
        }

        loadDoctorWelcomeMessage();
        setupButtons();
        setupPopupMenu();

        // upcoming appointments
        if (doctorUid != null) {
            loadUpcomingDoctorAppointments();
            startNotificationBadgeListenerDoctor();
        }

        return view;
    }

    private void loadDoctorWelcomeMessage() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }

        String uid = user.getUid();
        String greeting = getGreetingMessage();

        FirebaseFirestore.getInstance()
                .collection("Doctors")
                .whereEqualTo("uid", uid)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        String fullName = query.getDocuments().get(0).getString("FullName");
                        textWelcome.setText(greeting + ", " + (fullName != null ? fullName : "ιατρέ"));
                    } else {
                        textWelcome.setText(greeting + ", ιατρέ");
                    }
                })
                .addOnFailureListener(e -> textWelcome.setText(greeting + ", σφάλμα φόρτωσης"));
    }

    private void setupButtons() {
        btnSearchPatient.setOnClickListener(v -> openFragment(new SearchPatientFragment()));
        btnMyPatients.setOnClickListener(v -> openFragment(new MyPatientsFragment()));
        btnMyAppointments.setOnClickListener(v -> openFragment(new DoctorScheduleFragment()));
        btnFaq.setOnClickListener(v -> openFragment(new FAQDoctorFragment()));
    }

    private void setupPopupMenu() {
        menuIcon.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), menuIcon);
            popup.getMenuInflater().inflate(R.menu.menu_home, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.profile) {
                    openFragment(new DoctorProfileFragment());
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

            Animation fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in);
            menuIcon.startAnimation(fadeIn);

            popup.setOnDismissListener(menu -> {
                Animation fadeOut = AnimationUtils.loadAnimation(requireContext(), android.R.anim.fade_out);
                menuIcon.startAnimation(fadeOut);
            });

            popup.show();
        });
    }

    private void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.doctor_fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private String getGreetingMessage() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 12) {
            return getString(R.string.DoctorHomeFragment_getGreetingMessage_GoodMorning);
        } else if (hour >= 12 && hour < 15) {
            return getString(R.string.DoctorHomeFragment_getGreetingMessage_GoodAfternoon);
        } else if (hour >= 15 && hour < 21) {
            return getString(R.string.DoctorHomeFragment_getGreetingMessage_GoodEvening);
        } else {
            return getString(R.string.DoctorHomeFragment_getGreetingMessage_GoodNight);
        }
    }

    // Φόρτωση επόμενων 3 ραντεβού για τον γιατρό
    private void loadUpcomingDoctorAppointments() {
        db.collection("Appointments")
                .whereEqualTo("doctorUid", doctorUid)
                .whereEqualTo("status", "Επιβεβαιωμένο")
                .get()
                .addOnSuccessListener(snapshot -> {

                    containerUpcomingListDoctor.removeAllViews();

                    List<Map<String, Object>> upcoming = new ArrayList<>();
                    Date now = new Date();

                    for (var doc : snapshot) {
                        String dateStr = doc.getString("date");
                        String timeStr = doc.getString("time");
                        String patientName = doc.getString("patientName");

                        if (dateStr == null || timeStr == null) {
                            continue;
                        }

                        try {
                            Date dateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                    .parse(dateStr + " " + timeStr);
                            if (dateTime != null && dateTime.after(now)) {
                                Map<String, Object> appt = new HashMap<>();
                                appt.put("date", dateStr);
                                appt.put("time", timeStr);
                                appt.put("patientName", patientName);
                                upcoming.add(appt);
                            }
                        } catch (Exception ignored) {}
                    }

                    // ταξινόμηση με βάση ημερομηνία/ώρα
                    upcoming.sort((a, b) -> {
                        try {
                            var sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                            Date d1 = sdf.parse(a.get("date") + " " + a.get("time"));
                            Date d2 = sdf.parse(b.get("date") + " " + b.get("time"));
                            return d1.compareTo(d2);
                        } catch (Exception e) {
                            return 0;
                        }
                    });

                    if (upcoming.isEmpty()) {
                        layoutUpcomingAppointmentsDoctor.setVisibility(View.GONE);
                        return;
                    }

                    layoutUpcomingAppointmentsDoctor.setVisibility(View.VISIBLE);

                    int limit = Math.min(3, upcoming.size());
                    for (int i = 0; i < limit; i++) {
                        Map<String, Object> appt = upcoming.get(i);
                        addDoctorAppointmentToLayout(
                                appt.get("date").toString(),
                                appt.get("time").toString(),
                                appt.get("patientName") != null ? appt.get("patientName").toString() : "-"
                        );
                    }
                })
                .addOnFailureListener(e -> layoutUpcomingAppointmentsDoctor.setVisibility(View.GONE));
    }
    private void addDoctorAppointmentToLayout(String date, String time, String patientName) {
        TextView tv = new TextView(getContext());
        tv.setText("🕒 " + date + ", " + time + " | Ασθενής: " + patientName);
        tv.setTextSize(14f);
        tv.setTextColor(getResources().getColor(android.R.color.black));
        tv.setPadding(0, 8, 0, 8);
        containerUpcomingListDoctor.addView(tv);

        // προαιρετικό separator
        View divider = new View(getContext());
        divider.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 1));
        divider.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        containerUpcomingListDoctor.addView(divider);
    }

    private void showDoctorNotificationsPopup(View anchor) {
        if (doctorUid == null) {
            return;
        }

        // Μαρκάρουμε ως αναγνωσμένες όλες τις "Ακυρωμένες από τον ασθενή"
        db.collection("Notifications")
                .whereEqualTo("doctorUid", doctorUid)
                .whereEqualTo("status", "Ακυρωμένο από τον ασθενή")
                .whereEqualTo("isRead", false)
                .get()
                .addOnSuccessListener(snaps -> {
                    for (var d : snaps) {
                        d.getReference().update("isRead", true);
                    }
                });

        // Παίρνουμε τις τελευταίες 5 ακυρώσεις
        db.collection("Notifications")
                .whereEqualTo("doctorUid", doctorUid)
                .whereEqualTo("status", "Ακυρωμένο από τον ασθενή")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener(snapshot -> {
                    android.util.Log.d("DoctorNotifications", "Found " + snapshot.size() + " docs for doctor");
                    if (snapshot.isEmpty()) {
                        showSimplePopupDoctor(anchor, "Δεν υπάρχουν νέες ακυρώσεις ραντεβού.");
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
                            db.collection("Notifications").document(id).update("isRead", true);
                        }
                    }

                    // "Δες όλες τις ειδοποιήσεις"
                    linkView.setOnClickListener(v -> {
                        popupWindowDoctor.dismiss();
                        openFragment(new NotificationsDoctorFragment());
                    });

                    // Δημιουργία PopupWindow
                    popupWindowDoctor = new android.widget.PopupWindow(
                            popupView,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            true
                    );

                    popupWindowDoctor.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.WHITE));
                    popupWindowDoctor.setElevation(12f);
                    popupWindowDoctor.setOutsideTouchable(true);
                    popupWindowDoctor.setFocusable(true);

                    // Μετάθεση για να εμφανιστεί σωστά κάτω από το καμπανάκι
                    anchor.post(() -> {
                        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                        int xOffset = -popupView.getMeasuredWidth() + anchor.getWidth();
                        popupWindowDoctor.showAsDropDown(anchor, xOffset, 15);
                    });
                });
    }

    private void showSimplePopupDoctor(View anchor, String message) {
        View popupView = LayoutInflater.from(getContext()).inflate(R.layout.popup_notifications, null);
        LinearLayout container = popupView.findViewById(R.id.containerNotifications);
        TextView tv = new TextView(getContext());
        tv.setText(message);
        tv.setPadding(8, 8, 8, 8);
        container.addView(tv);

        popupWindowDoctor = new android.widget.PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);
        popupWindowDoctor.setElevation(10f);
        popupWindowDoctor.showAsDropDown(anchor, -150, 0);
    }

    private void startNotificationBadgeListenerDoctor() {
        if (doctorUid == null) {
            return;
        }

        // Αν υπάρχει ήδη listener, τον σταματάμε για να μην διπλασιάζεται
        if (notificationListenerDoctor != null) notificationListenerDoctor.remove();

        notificationListenerDoctor = db.collection("Notifications")
                .whereEqualTo("doctorUid", doctorUid)
                .whereEqualTo("status", "Ακυρωμένο από τον ασθενή")
                .whereEqualTo("isRead", false)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null || snapshot == null) return;

                    int unreadCount = snapshot.size();
                    if (unreadCount > 0) {
                        notificationBadgeDoctor.setText(String.valueOf(unreadCount));
                        notificationBadgeDoctor.setVisibility(View.VISIBLE);
                    } else {
                        notificationBadgeDoctor.setVisibility(View.GONE);
                    }
                });
    }
}