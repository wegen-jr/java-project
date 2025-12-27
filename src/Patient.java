public class Patient {
    private String patientId, name, gender, lastVisit, status;
    private int age;

    // --- Empty Constructor (Required for your current logic) ---
    public Patient() {}

    // --- Full Constructor (Optional but very helpful for clean code) ---
    public Patient(String patientId, String name, int age, String gender, String lastVisit, String status) {
        this.patientId = patientId;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.lastVisit = lastVisit;
        this.status = status;
    }

    // --- Getters and Setters (Keeping your exact structure) ---
    public String getPatientId() { return patientId; }
    public void setPatientId(String id) { this.patientId = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String g) { this.gender = g; }

    public String getLastVisit() { return lastVisit; }
    public void setLastVisit(String v) { this.lastVisit = v; }

    public String getStatus() { return status; }
    public void setStatus(String s) { this.status = s; }

    // Helper method to display patient info in the console for debugging
    @Override
    public String toString() {
        return "Patient ID: " + patientId + " | Name: " + name;
    }
}
