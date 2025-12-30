package Model;

public class DoctorItem {
    private int doctorId;
    private String displayName;

    public DoctorItem(int doctorId, String displayName) {
        this.doctorId = doctorId;
        this.displayName = displayName;
    }

    public int getDoctorId() {
        return doctorId;
    }

    @Override
    public String toString() {
        return displayName; // what JComboBox shows
    }
}
