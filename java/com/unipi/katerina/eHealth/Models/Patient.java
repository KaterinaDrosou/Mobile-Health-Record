package com.unipi.katerina.eHealth.Models;


public class Patient {
    private String id;
    private String FirstName;
    private String LastName;
    private String Amka;
    private String Email;
    private String doctorUid;

    public Patient() {
        //Χρειάζεται όταν χρησιμοποιούμε Firebase, για να μπορεί να φτιάξει αντικείμενα αυτόματα
    }

    // Getters and setters
    public String getId() { return id; }
    public String getFirstName() {
        return FirstName;
    }
    public String getDoctorUid() { return doctorUid; }
    public String getLastName() {
        return LastName;
    }
    public String getAmka() {
        return Amka;
    }
    public String getEmail() {
        return Email;
    }


    public void setId(String id) { this.id = id; }
    public void setFirstName(String firstName) {
        FirstName = firstName;
    }
    public void setLastName(String lastName) {
        LastName = lastName;
    }
    public void setAmka(String amka) {
        Amka = amka;
    }
    public void setEmail(String email) {
        Email = email;
    }
    public void setDoctorUid(String doctorUid) { this.doctorUid = doctorUid; }
}