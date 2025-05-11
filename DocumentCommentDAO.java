package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import database.DBConnection;

public class DocumentCommentDAO {
    private Connection connection;

    public DocumentCommentDAO() {
        this.connection = DBConnection.getConnection();
    }

    public boolean addComment(int documentId, int userId, String comment) {
        String query = "INSERT INTO document_comments (document_id, user_id, comment) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, documentId);
            ps.setInt(2, userId);
            ps.setString(3, comment);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error adding comment: " + e.getMessage());
            return false;
        }
    }

    public List<CommentInfo> getComments(int documentId) {
        List<CommentInfo> comments = new ArrayList<>();
        String query = "SELECT c.comment, c.created_at, u.username FROM document_comments c JOIN users u ON c.user_id = u.id WHERE c.document_id = ? ORDER BY c.created_at ASC";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, documentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                comments.add(new CommentInfo(
                    rs.getString("username"),
                    rs.getString("comment"),
                    rs.getTimestamp("created_at")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error getting comments: " + e.getMessage());
        }
        return comments;
    }

    public static class CommentInfo {
        public String username;
        public String comment;
        public java.sql.Timestamp createdAt;
        public CommentInfo(String username, String comment, java.sql.Timestamp createdAt) {
            this.username = username;
            this.comment = comment;
            this.createdAt = createdAt;
        }
    }
}