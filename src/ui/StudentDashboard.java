package ui;

import dao.AttendanceDAO;
import dao.CourseDAO;
import dao.GradeDAO;
import model.Attendance;
import model.Course;
import model.Grade;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentDashboard extends JFrame {

    private final int studentId;
    private final CourseDAO courseDAO = new CourseDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final GradeDAO gradeDAO = new GradeDAO();

    public StudentDashboard(int studentId) {
        this.studentId = studentId;

        setTitle("Student Dashboard");
        setSize(750, 500);
        setMinimumSize(new Dimension(700, 450));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("My Attendance", buildAttendancePanel());
        tabs.addTab("My Grades", buildGradesPanel());

        add(tabs);
    }

    private JPanel buildAttendancePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Course", "Present", "Total Classes", "Percentage"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JLabel overallLabel = new JLabel();
        overallLabel.setFont(overallLabel.getFont().deriveFont(Font.BOLD, 14f));
        panel.add(overallLabel, BorderLayout.SOUTH);

        try {
            List<Course> courses = courseDAO.getCoursesForStudent(studentId);
            List<Attendance> records = attendanceDAO.getAttendanceByStudent(studentId);

            Map<Integer, Integer> presentByCourse = new HashMap<>();
            Map<Integer, Integer> totalByCourse = new HashMap<>();
            for (Attendance a : records) {
                totalByCourse.merge(a.getCourseId(), 1, Integer::sum);
                if ("PRESENT".equals(a.getStatus())) {
                    presentByCourse.merge(a.getCourseId(), 1, Integer::sum);
                }
            }

            for (Course c : courses) {
                int present = presentByCourse.getOrDefault(c.getCourseId(), 0);
                int total = totalByCourse.getOrDefault(c.getCourseId(), 0);
                String percentage = total == 0 ? "N/A" : String.format("%.1f%%", (present * 100.0) / total);
                model.addRow(new Object[]{c.getCourseName(), present, total, percentage});
            }

            double overall = attendanceDAO.getOverallAttendancePercentage(studentId);
            overallLabel.setText(String.format("Overall attendance: %.1f%%", overall));

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load attendance: " + e.getMessage());
        }

        return panel;
    }

    private JPanel buildGradesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Course ID", "Exam", "Marks", "Max", "Percentage", "Date"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JLabel averageLabel = new JLabel();
        averageLabel.setFont(averageLabel.getFont().deriveFont(Font.BOLD, 14f));
        panel.add(averageLabel, BorderLayout.SOUTH);

        try {
            List<Grade> grades = gradeDAO.getGradesByStudent(studentId);
            for (Grade g : grades) {
                model.addRow(new Object[]{
                        g.getCourseId(), g.getExamType(), g.getMarksObtained(),
                        g.getMaxMarks(), String.format("%.1f%%", g.getPercentage()), g.getExamDate()
                });
            }
            double average = gradeDAO.getAveragePercentage(studentId);
            averageLabel.setText(String.format("Average score: %.1f%%", average));

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load grades: " + e.getMessage());
        }

        return panel;
    }
}
