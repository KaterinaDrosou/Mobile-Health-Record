package com.unipi.katerina.eHealth.Models;

public class AppointmentItem {
    public final String firestoreId;
    public final String appointmentId;
    public final String doctorUid;
    public final String doctorName;
    public final String doctorSpecialty;
    public final String patientUid;
    public final String patientName;
    public final String date;
    public final String time;
    public final String status;

    public AppointmentItem(String firestoreId, String appointmentId, String doctorUid, String doctorName,
                           String doctorSpecialty, String patientUid, String patientName, String date,
                           String time, String status) {
        this.firestoreId = firestoreId;
        this.appointmentId = appointmentId;
        this.doctorUid = doctorUid;
        this.doctorName = doctorName;
        this.doctorSpecialty = doctorSpecialty;
        this.patientUid = patientUid;
        this.patientName = patientName;
        this.date = date;
        this.time = time;
        this.status = status;
    }
}
