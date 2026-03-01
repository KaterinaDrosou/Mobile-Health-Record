package com.unipi.katerina.eHealth.Models;

public class Doctor {
    private String fullName;
    private String FullName; // <-- Για να “πιάσει” το Firestore field με κεφαλαίο
    private String specialty;
    private String email;
    private String clinicAddress;
    private String phone;
    private String uid;
    private String statusFromAdmin;

    public Doctor() {} // Για Firestore

    public Doctor(String fullName, String specialty, String email, String clinicAddress, String phone, String uid) {
        this.fullName = fullName;
        this.specialty = specialty;
        this.email = email;
        this.clinicAddress = clinicAddress;
        this.phone = phone;
        this.uid = uid;
        this.statusFromAdmin = "pending"; // προεπιλογή: εκκρεμεί έγκριση
    }

    public String getFullName() {
        return fullName != null ? fullName : FullName; // <-- Επιστρέφει όποιο δεν είναι null
            }
    public String getSpecialty() { return specialty; }
    public String getEmail() { return email; }
    public String getClinicAddress() { return clinicAddress; }
    public String getPhone() { return phone; }
    public String getUid() { return uid; }

    public String getStatusFromAdmin() { return statusFromAdmin; }
    public void setStatusFromAdmin(String statusFromAdmin) { this.statusFromAdmin = statusFromAdmin; }
    public void setUid(String uid) {this.uid = uid;}
}
