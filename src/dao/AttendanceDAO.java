package dao;

import db.DBConnection;
import model.Attendance;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDAO {

    public boolean markAttendance(Attendance a) throws SQLException {
        String sql = "INSERT INTO attendance (student_id, course_id, class_date, status) VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE status = VALUES(status)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, a.getStudentId());
            ps.setInt(2, a.getCourseId());
            ps.setDate(3, a.getClassDate());
            ps.setString(4, a.getStatus());
            return ps.executeUpdate() > 0;
        }
    }

    public List<Attendance> getAttendanceByStudent(int studentId) throws SQLException {
        List<Attendance> records = new ArrayList<>();
        String sql = "SELECT * FROM attendance WHERE student_id=? ORDER BY class_date DESC";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                records.add(mapRow(rs));
            }
        }
        return records;
    }

    public double getAttendancePercentage(int studentId, int courseId) throws SQLException {
        String sql = "SELECT " +
                "SUM(CASE WHEN status='PRESENT' THEN 1 ELSE 0 END) AS present_count, " +
                "COUNT(*) AS total_count " +
                "FROM attendance WHERE student_id=? AND course_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, courseId);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt("total_count") > 0) {
                return (rs.getInt("present_count") * 100.0) / rs.getInt("total_count");
            }
        }
        return 0.0;
    }

    public double getOverallAttendancePercentage(int studentId) throws SQLException {
        String sql = "SELECT " +
                "SUM(CASE WHEN status='PRESENT' THEN 1 ELSE 0 END) AS present_count, " +
                "COUNT(*) AS total_count " +
                "FROM attendance WHERE student_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt("total_count") > 0) {
                return (rs.getInt("present_count") * 100.0) / rs.getInt("total_count");
            }
        }
        return 0.0;
    }

    private Attendance mapRow(ResultSet rs) throws SQLException {
        return new Attendance(
                rs.getInt("attendance_id"),
                rs.getInt("student_id"),
                rs.getInt("course_id"),
                rs.getDate("class_date"),
                rs.getString("status")
        );
    }
}
