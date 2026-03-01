package com.unipi.katerina.eHealth.Models;

public class Visit {
    private String date;
    private String doctorName;
    private String doctorUid;
    private String diagnosis;
    private String doctorSpecialty;
    private String notes;
    private String nextAppointment;
    private String patientAmka;
    private String patientID;
    private java.util.Date timestamp;

    public Visit() {
        // απαραίτητος ο άδειος constructor για Firestore
    }

    public Visit(String date, String doctorName, String doctorUid, String diagnosis, String notes, String doctorSpecialty, String nextAppointment, String patientAmka, String patientID, java.util.Date timestamp) {
        this.date = date;
        this.doctorName = doctorName;
        this.doctorUid = doctorUid;
        this.diagnosis = diagnosis;
        this.notes = notes;
        this.doctorSpecialty = doctorSpecialty;
        this.nextAppointment = nextAppointment;
        this.patientAmka = patientAmka;
        this.patientID = patientID;
        this.timestamp = timestamp;
    }

    // Getters & Setters
    public String getDate() {
        return date;
    }
    public String getDoctorName() {
        return doctorName;
    }
    public String getDiagnosis() {
        return diagnosis;
    }
    public String getNotes() {
        return notes;
    }
    public String getDoctorSpecialty() {
        return doctorSpecialty;
    }
    public String getNextAppointment() {
        return nextAppointment;
    }
    public String getPatientAmka() {
        return patientAmka;
    }
    public String getPatientID() {
        return patientID;
    }
    public java.util.Date getTimeStamp() {
        return timestamp;
    }

    public String getDoctorUid() {
        return doctorUid;
    }


    public void setDate(String date) {
        this.date = date;
    }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }
    public void  setDoctorSpecialty(String doctorSpecialty) {this.doctorSpecialty = doctorSpecialty; }
    public void setNextAppointment(String nextAppointment) { this.nextAppointment = nextAppointment; }
    public void setPatientAmka(String patientAmka) {
        this.patientAmka = patientAmka;
    }
    public void setPatientID(String patientID) {
        this.patientID = patientID;
    }
    public void setTimeStamp(java.util.Date timeStamp) {
        this.timestamp = timeStamp;
    }
    public void setDoctorUid(String doctorUid) {
        this.doctorUid = doctorUid;
    }
}