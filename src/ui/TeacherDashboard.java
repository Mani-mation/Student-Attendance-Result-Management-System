package ui;

import dao.AttendanceDAO;
import dao.CourseDAO;
import dao.GradeDAO;
import dao.StudentDAO;
import model.Attendance;
import model.Course;
import model.Grade;
import model.Student;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public class TeacherDashboard extends JFrame {

    private final StudentDAO studentDAO = new StudentDAO();
    private final CourseDAO courseDAO = new CourseDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final GradeDAO gradeDAO = new GradeDAO();

    private DefaultTableModel studentTableModel;
    private JTable studentTable;
    private JTextField searchField;

    private JComboBox<Student> attendanceStudentBox;
    private JComboBox<Course> attendanceCourseBox;
    private JTextField attendanceDateField;
    private JComboBox<String> attendanceStatusBox;
    private DefaultTableModel attendanceTableModel;
    private JTable attendanceTable;

    private JComboBox<Student> gradeStudentBox;
    private JComboBox<Course> gradeCourseBox;
    private JComboBox<String> examTypeBox;
    private JTextField marksField;
    private JTextField maxMarksField;
    private JTextField examDateField;
    private DefaultTableModel gradeTableModel;
    private JTable gradeTable;

    public TeacherDashboard() {
        setTitle("Teacher Dashboard");
        setSize(1000, 650);
        setMinimumSize(new Dimension(950, 550));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Students", buildStudentsPanel());
        tabs.addTab("Attendance", buildAttendancePanel());
        tabs.addTab("Grades", buildGradesPanel());

        add(tabs);
        refreshStudentTable(null);
    }

    // ---------------- Students tab ----------------

    private JPanel buildStudentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        JButton refreshButton = new JButton("Show All");
        topPanel.add(new JLabel("Search by name or roll no:"));
        topPanel.add(searchField);
        topPanel.add(searchButton);
        topPanel.add(refreshButton);
        panel.add(topPanel, BorderLayout.NORTH);

        studentTableModel = new DefaultTableModel(
                new Object[]{"ID", "Roll No", "Full Name", "Email", "Phone", "Year"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        studentTable = new JTable(studentTableModel);
        panel.add(new JScrollPane(studentTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Add");
        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");
        bottomPanel.add(addButton);
        bottomPanel.add(editButton);
        bottomPanel.add(deleteButton);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        searchButton.addActionListener(e -> refreshStudentTable(searchField.getText().trim()));
        refreshButton.addActionListener(e -> {
            searchField.setText("");
            refreshStudentTable(null);
        });
        addButton.addActionListener(e -> openStudentForm(null));
        editButton.addActionListener(e -> {
            Student selected = getSelectedStudent();
            if (selected != null) {
                openStudentForm(selected);
            }
        });
        deleteButton.addActionListener(e -> deleteSelectedStudent());

        return panel;
    }

    private void refreshStudentTable(String keyword) {
        try {
            List<Student> students = (keyword == null || keyword.isEmpty())
                    ? studentDAO.getAllStudents()
                    : studentDAO.searchStudents(keyword);

            studentTableModel.setRowCount(0);
            for (Student s : students) {
                studentTableModel.addRow(new Object[]{
                        s.getStudentId(), s.getRollNo(), s.getFullName(),
                        s.getEmail(), s.getPhone(), s.getYearLevel()
                });
            }
            reloadComboBoxes();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load students: " + e.getMessage());
        }
    }

    private Student getSelectedStudent() {
        int row = studentTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a student first.");
            return null;
        }
        int id = (int) studentTableModel.getValueAt(row, 0);
        try {
            return studentDAO.getStudentById(id);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load student: " + e.getMessage());
            return null;
        }
    }

    private void openStudentForm(Student existing) {
        JTextField rollNoField = new JTextField(existing != null ? existing.getRollNo() : "");
        JTextField nameField = new JTextField(existing != null ? existing.getFullName() : "");
        JTextField emailField = new JTextField(existing != null ? existing.getEmail() : "");
        JTextField phoneField = new JTextField(existing != null ? existing.getPhone() : "");
        JTextField yearField = new JTextField(existing != null ? String.valueOf(existing.getYearLevel()) : "1");

        JPanel form = new JPanel(new GridLayout(5, 2, 5, 5));
        form.add(new JLabel("Roll No:"));
        form.add(rollNoField);
        form.add(new JLabel("Full Name:"));
        form.add(nameField);
        form.add(new JLabel("Email:"));
        form.add(emailField);
        form.add(new JLabel("Phone:"));
        form.add(phoneField);
        form.add(new JLabel("Year Level:"));
        form.add(yearField);

        int result = JOptionPane.showConfirmDialog(this, form,
                existing == null ? "Add Student" : "Edit Student", JOptionPane.OK_CANCEL_OPTION);

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            int yearLevel = Integer.parseInt(yearField.getText().trim());
            Student s = new Student(
                    existing != null ? existing.getStudentId() : 0,
                    rollNoField.getText().trim(),
                    nameField.getText().trim(),
                    emailField.getText().trim(),
                    phoneField.getText().trim(),
                    yearLevel
            );

            if (existing == null) {
                studentDAO.addStudent(s);
            } else {
                studentDAO.updateStudent(s);
            }
            refreshStudentTable(null);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Year level must be a number.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Save failed: " + e.getMessage());
        }
    }

    private void deleteSelectedStudent() {
        Student selected = getSelectedStudent();
        if (selected == null) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete " + selected.getFullName() + "? This also removes their attendance and grades.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            studentDAO.deleteStudent(selected.getStudentId());
            refreshStudentTable(null);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Delete failed: " + e.getMessage());
        }
    }

    // ---------------- Attendance tab ----------------

    private JPanel buildAttendancePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        attendanceStudentBox = new JComboBox<>();
        attendanceCourseBox = new JComboBox<>();
        attendanceDateField = new JTextField(java.time.LocalDate.now().toString(), 10);
        attendanceStatusBox = new JComboBox<>(new String[]{"PRESENT", "ABSENT"});
        JButton markButton = new JButton("Mark Attendance");

        form.add(new JLabel("Student:"));
        form.add(attendanceStudentBox);
        form.add(new JLabel("Course:"));
        form.add(attendanceCourseBox);
        form.add(new JLabel("Date (YYYY-MM-DD):"));
        form.add(attendanceDateField);
        form.add(new JLabel("Status:"));
        form.add(attendanceStatusBox);
        form.add(markButton);

        panel.add(form, BorderLayout.NORTH);

        attendanceTableModel = new DefaultTableModel(
                new Object[]{"Date", "Course ID", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        attendanceTable = new JTable(attendanceTableModel);
        panel.add(new JScrollPane(attendanceTable), BorderLayout.CENTER);

        markButton.addActionListener(e -> markAttendance());
        attendanceStudentBox.addActionListener(e -> refreshAttendanceTable());

        return panel;
    }

    private void markAttendance() {
        Student student = (Student) attendanceStudentBox.getSelectedItem();
        Course course = (Course) attendanceCourseBox.getSelectedItem();
        if (student == null || course == null) {
            JOptionPane.showMessageDialog(this, "Select a student and a course.");
            return;
        }
        try {
            Date date = Date.valueOf(attendanceDateField.getText().trim());
            Attendance a = new Attendance(0, student.getStudentId(), course.getCourseId(),
                    date, (String) attendanceStatusBox.getSelectedItem());
            attendanceDAO.markAttendance(a);
            refreshAttendanceTable();
            JOptionPane.showMessageDialog(this, "Attendance recorded.");
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Date must be in YYYY-MM-DD format.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to mark attendance: " + e.getMessage());
        }
    }

    private void refreshAttendanceTable() {
        Student student = (Student) attendanceStudentBox.getSelectedItem();
        attendanceTableModel.setRowCount(0);
        if (student == null) {
            return;
        }
        try {
            List<Attendance> records = attendanceDAO.getAttendanceByStudent(student.getStudentId());
            for (Attendance a : records) {
                attendanceTableModel.addRow(new Object[]{a.getClassDate(), a.getCourseId(), a.getStatus()});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load attendance: " + e.getMessage());
        }
    }

    // ---------------- Grades tab ----------------

    private JPanel buildGradesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        gradeStudentBox = new JComboBox<>();
        gradeCourseBox = new JComboBox<>();
        examTypeBox = new JComboBox<>(new String[]{"MIDTERM", "FINAL", "QUIZ", "ASSIGNMENT"});
        marksField = new JTextField(5);
        maxMarksField = new JTextField("100", 5);
        examDateField = new JTextField(java.time.LocalDate.now().toString(), 10);
        JButton addGradeButton = new JButton("Add Grade");

        form.add(new JLabel("Student:"));
        form.add(gradeStudentBox);
        form.add(new JLabel("Course:"));
        form.add(gradeCourseBox);
        form.add(new JLabel("Exam:"));
        form.add(examTypeBox);
        form.add(new JLabel("Marks:"));
        form.add(marksField);
        form.add(new JLabel("Max:"));
        form.add(maxMarksField);
        form.add(new JLabel("Date:"));
        form.add(examDateField);
        form.add(addGradeButton);

        panel.add(form, BorderLayout.NORTH);

        gradeTableModel = new DefaultTableModel(
                new Object[]{"Course ID", "Exam", "Marks", "Max", "Percentage"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        gradeTable = new JTable(gradeTableModel);
        panel.add(new JScrollPane(gradeTable), BorderLayout.CENTER);

        addGradeButton.addActionListener(e -> addGrade());
        gradeStudentBox.addActionListener(e -> refreshGradeTable());

        return panel;
    }

    private void addGrade() {
        Student student = (Student) gradeStudentBox.getSelectedItem();
        Course course = (Course) gradeCourseBox.getSelectedItem();
        if (student == null || course == null) {
            JOptionPane.showMessageDialog(this, "Select a student and a course.");
            return;
        }
        try {
            double marks = Double.parseDouble(marksField.getText().trim());
            double maxMarks = Double.parseDouble(maxMarksField.getText().trim());
            Date examDate = Date.valueOf(examDateField.getText().trim());

            Grade g = new Grade(0, student.getStudentId(), course.getCourseId(),
                    (String) examTypeBox.getSelectedItem(), marks, maxMarks, examDate);
            gradeDAO.addGrade(g);
            refreshGradeTable();
            JOptionPane.showMessageDialog(this, "Grade added.");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Marks and max marks must be numbers.");
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Date must be in YYYY-MM-DD format.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to add grade: " + e.getMessage());
        }
    }

    private void refreshGradeTable() {
        Student student = (Student) gradeStudentBox.getSelectedItem();
        gradeTableModel.setRowCount(0);
        if (student == null) {
            return;
        }
        try {
            List<Grade> grades = gradeDAO.getGradesByStudent(student.getStudentId());
            for (Grade g : grades) {
                gradeTableModel.addRow(new Object[]{
                        g.getCourseId(), g.getExamType(), g.getMarksObtained(),
                        g.getMaxMarks(), String.format("%.1f%%", g.getPercentage())
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load grades: " + e.getMessage());
        }
    }

    // ---------------- Shared ----------------

    private void reloadComboBoxes() {
        try {
            List<Student> students = studentDAO.getAllStudents();
            List<Course> courses = courseDAO.getAllCourses();

            attendanceStudentBox.removeAllItems();
            gradeStudentBox.removeAllItems();
            for (Student s : students) {
                attendanceStudentBox.addItem(s);
                gradeStudentBox.addItem(s);
            }

            attendanceCourseBox.removeAllItems();
            gradeCourseBox.removeAllItems();
            for (Course c : courses) {
                attendanceCourseBox.addItem(c);
                gradeCourseBox.addItem(c);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load dropdown data: " + e.getMessage());
        }
    }
}
