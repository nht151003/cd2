package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import database.DBConnection;
import model.Document;

public class SharedDocumentDAO {
    private Connection connection;

    public SharedDocumentDAO() {
        this.connection = DBConnection.getConnection();
    }

    public boolean shareDocument(int documentId, int sharedBy, int sharedTo) {
        String query = "INSERT INTO shared_documents (document_id, shared_by, shared_to) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, documentId);
            ps.setInt(2, sharedBy);
            ps.setInt(3, sharedTo);
            boolean result = ps.executeUpdate() > 0;
            if (result) {
                // Hiển thị thông báo cho user nhận nếu đang đăng nhập (giả lập: show popup)
                javax.swing.SwingUtilities.invokeLater(() -> {
                    javax.swing.JOptionPane.showMessageDialog(null, "Bạn vừa được chia sẻ một tài liệu mới!", "Thông báo chia sẻ", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                });
            }
            return result;
        } catch (SQLException e) {
            System.out.println("Error sharing document: " + e.getMessage());
            return false;
        }
    }

    public List<Document> getSharedDocuments(int userId) {
        List<Document> documents = new ArrayList<>();
        String query = "SELECT d.* FROM documents d JOIN shared_documents s ON d.id = s.document_id WHERE s.shared_to = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                documents.add(new Document(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("field"),
                    rs.getString("note"),
                    rs.getString("keywords"),
                    rs.getString("content"),
                    rs.getInt("user_id")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error getting shared documents: " + e.getMessage());
        }
        return documents;
    }
}