package controller;

import java.util.List;

import dao.SharedDocumentDAO;
import model.Document;
public class SharedDocumentController {
    private final SharedDocumentDAO sharedDocumentDAO = new SharedDocumentDAO();

    // GET: /api/shared/to/{userId}
    public List<Document> getSharedDocuments(int userId) {
        return sharedDocumentDAO.getSharedDocuments(userId);
    }

    // POST: /api/shared/share
    public boolean shareDocument(int documentId, int sharedBy, int sharedTo) {
        return sharedDocumentDAO.shareDocument(documentId, sharedBy, sharedTo);
    }
}