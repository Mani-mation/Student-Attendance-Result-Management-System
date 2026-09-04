package model;

public class Student {

    private int studentId;
    private String rollNo;
    private String fullName;
    private String email;
    private String phone;
    private int yearLevel;

    public Student() {
    }

    public Student(int studentId, String rollNo, String fullName, String email, String phone, int yearLevel) {
        this.studentId = studentId;
        this.rollNo = rollNo;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.yearLevel = yearLevel;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getYearLevel() {
        return yearLevel;
    }

    public void setYearLevel(int yearLevel) {
        this.yearLevel = yearLevel;
    }

    @Override
    public String toString() {
        return fullName + " (" + rollNo + ")";
    }
}
