# 🏥 HMS Clinical Suite: Next-Gen Hospital Management

**HMS Clinical Suite** is an enterprise-grade, real-time healthcare management platform. It facilitates seamless collaboration between clinical departments—bridging the gap between front-desk operations, physician diagnostics, and ancillary services like Laboratory and Pharmacy.

Built with a high-concurrency architecture, the system ensures that patient data flows securely and instantaneously across the hospital's network.

---

## 🚀 Module Ecosystem

### 👨‍⚕️ Physician Portal (Doctor.java)
* **Live Clinical Feed:** Real-time polling engine for "Confirmed" patient queues.
* **Patient Longitudinal Record:** Full medical history access with automated clinical age calculation.
* **Status Logic:** Visual triage indicators (Vivid Red for Pending, Digital Green for Completed).

### 🏢 Patient Services (Reception.java)
* **Streamlined Intake:** Optimized patient registration and appointment scheduling.
* **Queue Management:** Centralized hub for managing "Pending" vs "Confirmed" status updates.

### 🧪 Diagnostic Intelligence (Laboratory.java)
* **Instant Result Feed:** 5-second polling interval ensures doctors see results the moment they are saved.
* **Lifecycle Automation:** Auto-transition of diagnostic requests from **REQUESTED** to **COMPLETED**.
* **Data Hygiene:** A "10-minute cleanup" rule to keep the active lab feed focused on urgent cases.

### 💊 Pharmacy & Revenue Cycle (Pharmacy.java)
* **Digital Dispensing:** Automated link between physician prescriptions and pharmacy inventory.
* **Integrated Billing:** Point-of-Sale (POS) logic designed for rapid outpatient processing.

---

## 🛠 Engineering Stack

| Layer | Technology |
| :--- | :--- |
| **Runtime** | Java 25 (OpenJDK) |
| **Data Engine** | MySQL 9.5 (Relational Management) |
| **Graphics** | Java Swing / AWT (Hard-coded Custom Components) |
| **External Libs** | JCalendar (Temporal Inputs), MySQL Connector-J |
| **Pattern** | Data Access Object (DAO) for clean separation of concerns |

---

## 📁 Integrity & Logic

* **Concurrency Management:** Utilizes `javax.swing.Timer` for non-blocking multi-threaded database synchronization.
* **SQL Resilience:** Leverages `COALESCE` and `IFNULL` to maintain UI stability during data entry.
* **Security:** `INNER JOIN` relational logic ensures data isolation—doctors only see relevant patient cohorts.

---

## ⚙️ Deployment Guide

1.  **Database Provisioning:** Execute the provided `.sql` schema in your MySQL environment.
2.  **Classpath Configuration:**
    * Include `mysql-connector-j-9.5.0.jar`
    * Include `jcalendar-1.4.jar` (Required for `com.toedter.calendar` modules).
3.  **Environment Variables:** Update `DatabaseConnection.java` with your specific instance credentials.
4.  **Entry Point:** Run `LoginPage.java` to access the secure authentication gateway.

---

## 👥 Engineering Team

| Lead Engineer | Clinical Module | Technical Contribution |
| :--- | :--- | :--- |
| **Abrham** | **Physician Suite** | DAO Implementation & Doctor Class Architecture. |
| **Wegen** | **Reception Module** | SQL Schema optimization & Intake Logic. |
| **Makbel** | **Admin Dashboard** | UX/UI aesthetic standards & JCalendar integration. |
| **Yeabsira** | **Laboratory Feed** | Real-time polling engine & Result synchronization. |
| **Eyob** | **Pharmacy & Billing** | Clinical color-palette design & Billing algorithms. |

---
*Optimizing clinical workflows through data-driven precision and architectural integrity.*
