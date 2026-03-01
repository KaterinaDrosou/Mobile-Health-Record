# 📱 eHealth – Electronic Health Record Android Application
## 📌 Overview
**eHealth** is an Android mobile application developed as part of my **Master's Thesis**.
The application provides a digital Electronic Health Record (EHR) system allowing patients and doctors to manage medical data securely and efficiently.

### 🎯 Features
👤 **Patient**
- Medical profile management
- Medical history management
- Appointment booking & cancellation
- View prescriptions
- PDF export of prescriptions
- Doctor search & filtering
- Favorites doctors
- Map view of doctors
- Password strength validation
- Account management

🩺 **Doctor**
- Profile management
- Appointment management
- Prescription creation
- Visit history tracking

🛡️ **Admin**
- Review and approve/reject doctor registrations
- Monitor system activity
- Ensure data integrity and platform moderation
- Handle support requests submitted by users
- Maintain role-based access control

🔐 **Security**
- Firebase Authentication
- Role-based access (Patient / Doctor / Admin)
- Field validation (AMKA, AFM, phone, address)
- Password strength evaluation

### 🛠️ Technologies Used
- Built using **Android Studio** with **Java** for development
- **Firebase Console** (Firestore Database & Authentication)
- **Leaflet** (Open-source JavaScript library for interactive maps)

### ⚙️ How It Works

1️⃣ **User Registration**
- Users register as either Patient or Doctor.
- Doctor accounts require Admin approval before access is granted.

2️⃣ **Authentication & Role Assignment**
- Firebase Authentication verifies credentials and assigns role-based access.

3️⃣ **Patient Flow**
- Patients manage their medical profile and history.
- They search for doctors and book appointments based on availability.
- After visits, they can view diagnoses and prescriptions.

4️⃣ **Doctor Flow**
- Doctors manage their professional profile and availability.
- They review appointments and record visit details.
- Prescriptions are generated and linked to patient records.

5️⃣ **Admin Flow**
- Admin reviews new doctor registrations.
- Admin ensures system moderation and handles user support requests.
- All data is securely stored in Firebase Firestore and synchronized in real time.
