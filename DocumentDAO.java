package dao;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import database.DBConnection;
import model.Document;

public class DocumentDAO {
    private Connection connection;

    public DocumentDAO() {
        this.connection = DBConnection.getConnection();
    }
    

    public boolean addDocument(Document doc) {
        if (connection == null) {
            System.out.println("Database connection is null");
            return false;
        }

        String query = "INSERT INTO documents (title, author, field, note, keywords, content, user_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, doc.getTitle());
            ps.setString(2, doc.getAuthor());
            ps.setString(3, doc.getField());
            ps.setString(4, doc.getNote());
            ps.setString(5, doc.getKeywords());
            ps.setString(6, doc.getContent());
            ps.setInt(7, doc.getUserId());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        doc.setId(generatedKeys.getInt(1));
                        System.out.println("Document added successfully with ID: " + doc.getId());
                        notifyTrackedUsers(doc);
                        return true;
                    }
                }
            }
            System.out.println("No rows affected when adding document");
            return false;
        } catch (SQLException e) {
            System.out.println("Error adding document: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void notifyTrackedUsers(Document doc) {
        TrackedKeywordDAO trackedKeywordDAO = new TrackedKeywordDAO();
        String[] keywords = doc.getKeywords().split(",");
        java.util.Set<Integer> notifiedUserIds = new java.util.HashSet<>();
        for (String keyword : keywords) {
            String trimmed = keyword.trim();
            if (trimmed.isEmpty()) {
				continue;
			}
            java.util.List<Integer> userIds = trackedKeywordDAO.getUserIdsByKeyword(trimmed);
            for (Integer userId : userIds) {
                if (!notifiedUserIds.contains(userId)) {
                    if (userId == doc.getUserId()) {
                        javax.swing.SwingUtilities.invokeLater(() -> {
                            javax.swing.JOptionPane.showMessageDialog(null, "Có tài liệu mới liên quan đến từ khóa bạn theo dõi: " + trimmed, "Thông báo", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                        });
                    }
                    notifiedUserIds.add(userId);
                }
            }
        }
    }

    public boolean updateDocument(Document doc) {
        if (connection == null) {
            System.out.println("Database connection is null");
            return false;
        }

        String query = "UPDATE documents SET title=?, author=?, field=?, note=?, keywords=?, content=? WHERE id=? AND user_id=?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, doc.getTitle());
            ps.setString(2, doc.getAuthor());
            ps.setString(3, doc.getField());
            ps.setString(4, doc.getNote());
            ps.setString(5, doc.getKeywords());
            ps.setString(6, doc.getContent());
            ps.setInt(7, doc.getId());
            ps.setInt(8, doc.getUserId());

            int affectedRows = ps.executeUpdate();
            System.out.println("Rows affected when updating document: " + affectedRows);
            return affectedRows > 0;
        } catch (SQLException e) {
            System.out.println("Error updating document: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteDocument(int docId, int userId) {
        if (connection == null) {
            System.out.println("Database connection is null");
            return false;
        }

        String query = "DELETE FROM documents WHERE id=? AND user_id=?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, docId);
            ps.setInt(2, userId);

            int affectedRows = ps.executeUpdate();
            System.out.println("Rows affected when deleting document: " + affectedRows);
            return affectedRows > 0;
        } catch (SQLException e) {
            System.out.println("Error deleting document: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Document> searchDocuments(String keyword, String author, String field, int userId) {
        if (connection == null) {
            System.out.println("Database connection is null");
            return new ArrayList<>();
        }

        List<Document> documents = new ArrayList<>();
        StringBuilder query = new StringBuilder("SELECT * FROM documents WHERE user_id=?");
        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (keyword != null && !keyword.isEmpty()) {
            query.append(" AND (title LIKE ? OR keywords LIKE ? OR content LIKE ?)");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }
        if (author != null && !author.isEmpty()) {
            query.append(" AND author LIKE ?");
            params.add("%" + author + "%");
        }
        if (field != null && !field.isEmpty()) {
            query.append(" AND field LIKE ?");
            params.add("%" + field + "%");
        }

        try (PreparedStatement ps = connection.prepareStatement(query.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
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
            System.out.println("Error searching documents: " + e.getMessage());
            e.printStackTrace();
        }
        return documents;
    }

    public List<Document> getAllDocuments(int userId) {
        if (userId == -1) {
            // Lấy tất cả tài liệu
            List<Document> documents = new ArrayList<>();
            String query = "SELECT * FROM documents";
            try (PreparedStatement ps = connection.prepareStatement(query)) {
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
                System.out.println("Error getting all documents: " + e.getMessage());
            }
            return documents;
        } else {
            // Lấy tài liệu của user
            return searchDocuments(null, null, null, userId);
        }
    }

    public List<String> getAllFields() {
        List<String> fields = new ArrayList<>();
        String query = "SELECT DISTINCT field FROM documents WHERE field IS NOT NULL AND field <> ''";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                fields.add(rs.getString("field"));
            }
        } catch (SQLException e) {
            System.out.println("Error getting fields: " + e.getMessage());
        }
        return fields;
    }
 public boolean restoreFromArchive(int archiveId, int userId) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Lấy thông tin từ bảng lưu trữ
            Document archivedDoc = getArchivedDocument(archiveId, userId);
            if (archivedDoc == null) {
                conn.rollback();
                return false;
            }

            // 2. Chèn lại vào bảng chính
            String restoreQuery = "INSERT INTO documents (title, author, field, note, keywords, content, user_id) " +
                               "VALUES (?, ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement restoreStmt = conn.prepareStatement(restoreQuery, Statement.RETURN_GENERATED_KEYS)) {
                restoreStmt.setString(1, archivedDoc.getTitle());
                restoreStmt.setString(2, archivedDoc.getAuthor());
                restoreStmt.setString(3, archivedDoc.getField());
                restoreStmt.setString(4, archivedDoc.getNote());
                restoreStmt.setString(5, archivedDoc.getKeywords());
                restoreStmt.setString(6, archivedDoc.getContent());
                restoreStmt.setInt(7, archivedDoc.getUserId());
                restoreStmt.executeUpdate();
            }

            // 3. Xóa khỏi bảng lưu trữ
            String deleteQuery = "DELETE FROM document_archive WHERE id = ?";
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {
                deleteStmt.setInt(1, archiveId);
                int deleted = deleteStmt.executeUpdate();
                
                if (deleted > 0) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            }
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) {}
            }
        }
    }
 public boolean moveToArchive(int docId, int userId) throws SQLException {
     if (connection == null) {
         throw new SQLException("Database connection is null");
     }

     // 1. Lấy thông tin tài liệu từ bảng chính
     Document doc = getDocumentById(docId, userId);
     if (doc == null) {
         return false;
     }

     // 2. Chèn vào bảng lưu trữ
     String archiveQuery = "INSERT INTO document_archive (original_doc_id, title, author, field, note, keywords, content, user_id) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
     
     try (PreparedStatement archiveStmt = connection.prepareStatement(archiveQuery)) {
         archiveStmt.setString(1, String.valueOf(docId));
         archiveStmt.setString(2, doc.getTitle());
         archiveStmt.setString(3, doc.getAuthor());
         archiveStmt.setString(4, doc.getField());
         archiveStmt.setString(5, doc.getNote());
         archiveStmt.setString(6, doc.getKeywords());
         archiveStmt.setString(7, doc.getContent());
         archiveStmt.setInt(8, doc.getUserId());
         archiveStmt.executeUpdate();
     }

     // 3. Xóa khỏi bảng chính
     String deleteQuery = "DELETE FROM documents WHERE id=? AND user_id=?";
     try (PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery)) {
         deleteStmt.setInt(1, docId);
         deleteStmt.setInt(2, userId);
         return deleteStmt.executeUpdate() > 0;
     }
 }

 
 private Document getDocumentById(int docId, int userId) {
	// TODO Auto-generated method stub
	return null;
}


 public Document getArchivedDocument(int archiveId, int userId) throws SQLException {
     String query = "SELECT * FROM document_archive WHERE id = ? AND user_id = ?";
     
     try (PreparedStatement ps = connection.prepareStatement(query)) {
         ps.setInt(1, archiveId);
         ps.setInt(2, userId);
         
         ResultSet rs = ps.executeQuery();
         if (rs.next()) {
             Document doc = new Document(
                 rs.getInt("id"),
                 rs.getString("title"),
                 rs.getString("author"),
                 rs.getString("field"),
                 rs.getString("note"),
                 rs.getString("keywords"),
                 rs.getString("content"),
                 rs.getInt("user_id")
             );
             doc.setOriginalDocId(rs.getString("original_doc_id"));
             doc.setArchivedDate(rs.getTimestamp("archived_at"));
             return doc;
         }
     }
     return null;
 }

 /**
  * Xóa tài liệu từ lưu trữ
  */
public boolean deleteFromArchive(int docId, int userId) throws SQLException {
    if (connection == null) {
        throw new SQLException("Database connection is null");
    }

    String query = "DELETE FROM document_archive WHERE id=? AND user_id=?";
    try (PreparedStatement ps = connection.prepareStatement(query)) {
        ps.setInt(1, docId);
        ps.setInt(2, userId);
        return ps.executeUpdate() > 0;
    }
}
/**
 * Lấy danh sách tài liệu đã lưu trữ
 */
public List<Document> getArchivedDocuments(int userId) throws SQLException {
    if (connection == null) {
        throw new SQLException("Database connection is null");
    }

    List<Document> documents = new ArrayList<>();
    String query = "SELECT * FROM document_archive WHERE user_id=? ORDER BY archived_at DESC";
    
    try (PreparedStatement ps = connection.prepareStatement(query)) {
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        
        while (rs.next()) {
            Document doc = new Document(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getString("field"),
                rs.getString("note"),
                rs.getString("keywords"),
                rs.getString("content"),
                rs.getInt("user_id")
            );
            doc.setArchivedDate(rs.getTimestamp("archived_at"));
            documents.add(doc);
        }
    }
    return documents;
}


    /**
     * Kiểm tra xem tài liệu có trong mục lưu trữ không
     */
    public boolean isArchived(int docId) throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is null");
        }

        String query = "SELECT is_archived FROM documents WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, docId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt("is_archived") == 1;
        }
    }
 // Trong DocumentDAO, thêm phương thức kiểm tra
    public void checkArchiveColumnsExist() throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet columns = metaData.getColumns(null, null, "documents", "is_archived");
        if (!columns.next()) {
            throw new SQLException("Column 'is_archived' does not exist in 'documents' table");
        }
        columns = metaData.getColumns(null, null, "documents", "archived_date");
        if (!columns.next()) {
            throw new SQLException("Column 'archived_date' does not exist in 'documents' table");
        }
    }
    
}