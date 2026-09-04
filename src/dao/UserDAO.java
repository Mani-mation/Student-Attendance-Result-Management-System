package dao;

import db.DBConnection;
import db.PasswordUtil;
import model.User;

import java.sql.*;

public class UserDAO {

    public User login(String username, String plainPassword) throws SQLException {
        String sql = "SELECT * FROM users WHERE username=? AND password=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, PasswordUtil.hash(plainPassword));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Integer linkedId = rs.getObject("linked_student_id") != null
                        ? rs.getInt("linked_student_id") : null;
                return new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        linkedId
                );
            }
        }
        return null;
    }
}
