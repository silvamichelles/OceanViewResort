# 🌊 Ocean View Resort Management System

A distributed hotel management system featuring a desktop application for admins and a web-based portal for guests.

## 🚀 Key Features
* **Admin Dashboard (JavaFX):** Manage rooms, guests, and reservations via a secure 3-tier desktop app.
* **Guest Portal (Web):** A premium web interface for guests to browse rooms and book their stay.
* **Centralized Database:** Powered by MySQL to sync data across both platforms in real-time.
* **Automated Testing:** JUnit test suites included for system reliability.

## 🛠️ Tech Stack
* **Language:** Java 17
* **Desktop UI:** JavaFX
* **Web Server:** Custom Java HttpServer (REST API)
* **Database:** MySQL
* **Build Tool:** Maven

## ⚙️ Setup & Run
1. Import the project into NetBeans or IntelliJ.
2. Run the `oceanviewdb.sql` script in your MySQL server.
3. Run `AppWrapper.java` to start both the Admin App and the Web Server.
4. Access the Guest Portal at `http://localhost:8080/`.

---
*Developed as part of the CIS6003 Assignment.*
