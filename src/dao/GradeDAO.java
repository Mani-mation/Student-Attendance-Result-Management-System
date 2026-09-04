package dao;

import db.DBConnection;
import model.Grade;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GradeDAO {

    public boolean addGrade(Grade g) throws SQLException {
        String sql = "INSERT INTO grades (student_id, course_id, exam_type, marks_obtained, max_marks, exam_date) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, g.getStudentId());
            ps.setInt(2, g.getCourseId());
            ps.setString(3, g.getExamType());
            ps.setDouble(4, g.getMarksObtained());
            ps.setDouble(5, g.getMaxMarks());
            ps.setDate(6, g.getExamDate());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateGrade(Grade g) throws SQLException {
        String sql = "UPDATE grades SET marks_obtained=?, max_marks=?, exam_date=? WHERE grade_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setDouble(1, g.getMarksObtained());
            ps.setDouble(2, g.getMaxMarks());
            ps.setDate(3, g.getExamDate());
            ps.setInt(4, g.getGradeId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteGrade(int gradeId) throws SQLException {
        String sql = "DELETE FROM grades WHERE grade_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, gradeId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Grade> getGradesByStudent(int studentId) throws SQLException {
        List<Grade> grades = new ArrayList<>();
        String sql = "SELECT * FROM grades WHERE student_id=? ORDER BY exam_date DESC";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                grades.add(mapRow(rs));
            }
        }
        return grades;
    }

    public double getAveragePercentage(int studentId) throws SQLException {
        List<Grade> grades = getGradesByStudent(studentId);
        if (grades.isEmpty()) {
            return 0.0;
        }
        double total = 0;
        for (Grade g : grades) {
            total += g.getPercentage();
        }
        return total / grades.size();
    }

    private Grade mapRow(ResultSet rs) throws SQLException {
        return new Grade(
                rs.getInt("grade_id"),
                rs.getInt("student_id"),
                rs.getInt("course_id"),
                rs.getString("exam_type"),
                rs.getDouble("marks_obtained"),
                rs.getDouble("max_marks"),
                rs.getDate("exam_date")
        );
    }
}
