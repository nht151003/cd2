package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import database.DBConnection;

public class TrackedKeywordDAO {
    private Connection connection;

    public TrackedKeywordDAO() {
        this.connection = DBConnection.getConnection();
    }

    public boolean addKeyword(String keyword, int userId) {
        String query = "INSERT INTO tracked_keywords (keyword, user_id) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, keyword);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error adding keyword: " + e.getMessage());
            return false;
        }
    }

    public boolean removeKeyword(String keyword, int userId) {
        String query = "DELETE FROM tracked_keywords WHERE keyword = ? AND user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, keyword);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error removing keyword: " + e.getMessage());
            return false;
        }
    }

    public List<String> getTrackedKeywords(int userId) {
        List<String> keywords = new ArrayList<>();
        String query = "SELECT keyword FROM tracked_keywords WHERE user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                keywords.add(rs.getString("keyword"));
            }
        } catch (SQLException e) {
            System.out.println("Error getting keywords: " + e.getMessage());
        }
        return keywords;
    }

    public List<Integer> getUserIdsByKeyword(String keyword) {
        List<Integer> userIds = new ArrayList<>();
        String query = "SELECT user_id FROM tracked_keywords WHERE keyword = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, keyword);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                userIds.add(rs.getInt("user_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userIds;
    }
}