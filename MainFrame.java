package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;

import org.json.JSONArray;
import org.json.JSONObject;

import com.formdev.flatlaf.FlatLightLaf;

import dao.DocumentCommentDAO;
import dao.DocumentDAO;
import dao.SharedDocumentDAO;
import dao.TrackedKeywordDAO;
import dao.UserDAO;
import model.Document;
import model.User;

public class MainFrame extends JFrame {
    private JTable documentTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> fieldFilterCombo;
    private JComboBox<String> searchTypeCombo;
    private JButton addButton, editButton, deleteButton, shareButton, commentButton, trackButton, exportButton, searchButton;
    private User currentUser;
    private DocumentDAO documentDAO;
    private TrackedKeywordDAO trackedKeywordDAO;
    private JPanel mainPanel;
    private JLabel welcomeLabel;
    private JPanel searchPanel;
    private JPanel buttonPanel;
    private JScrollPane tableScrollPane;
    private JTabbedPane tabbedPane;

    public MainFrame(User user) {
        try {
            FlatLightLaf.setup();
        } catch (Exception ex) {
            System.out.println("Không thể thiết lập FlatLaf: " + ex.getMessage());
        }

        this.currentUser = user;
        this.documentDAO = new DocumentDAO();
        this.trackedKeywordDAO = new TrackedKeywordDAO();

        initComponents();
        loadDocuments();
        loadFields();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initComponents() {
        setTitle("Quản lý Tài liệu Nghiên cứu Khoa học");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JPanel documentPanel = createDocumentPanel();
        tabbedPane.addTab("Tài liệu", createImage("document"), documentPanel);

        JPanel archivePanel = createArchivePanel();
        tabbedPane.addTab("Lưu trữ", createImage("archive"), archivePanel);

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel);
    }

    private Icon createImage(String type) {
        // Có thể thay bằng icon thực tế nếu có
        return null;
    }

    private JPanel createDocumentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel chào mừng
        JPanel welcomePanel = new JPanel(new BorderLayout());
        welcomePanel.setOpaque(false);
        welcomeLabel = new JLabel("Xin chào, " + currentUser.getUsername() + "!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setForeground(new Color(41, 128, 185));
        welcomePanel.add(welcomeLabel, BorderLayout.WEST);

        // Tạo thanh tìm kiếm nâng cao
        searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setOpaque(false);
        searchPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(41, 128, 185), 1),
            "Tìm kiếm nâng cao",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14),
            new Color(41, 128, 185)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Dòng 1: Tìm kiếm cơ bản
        gbc.gridx = 0;
        gbc.gridy = 0;
        searchPanel.add(new JLabel("Từ khóa:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchPanel.add(searchField, gbc);

        gbc.gridx = 4;
        gbc.gridwidth = 1;
        searchButton = createStyledButton("Tìm kiếm", getIcon("search", 20));
        searchButton.setPreferredSize(new Dimension(120, 30));
        searchPanel.add(searchButton, gbc);

        // Dòng 2: Bộ lọc
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        searchPanel.add(new JLabel("Lĩnh vực:"), gbc);

        gbc.gridx = 1;
        fieldFilterCombo = new JComboBox<>();
        fieldFilterCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchPanel.add(fieldFilterCombo, gbc);

        gbc.gridx = 2;
        searchPanel.add(new JLabel("Loại tìm kiếm:"), gbc);

        gbc.gridx = 3;
        searchTypeCombo = new JComboBox<>(new String[]{"Tất cả", "Tiêu đề", "Tác giả", "Từ khóa", "Nội dung"});
        searchTypeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchPanel.add(searchTypeCombo, gbc);

        gbc.gridx = 4;
        JButton advancedButton = createStyledButton("Nâng cao", getIcon("advanced", 20));
        advancedButton.setPreferredSize(new Dimension(120, 30));
        searchPanel.add(advancedButton, gbc);

        // Bảng dữ liệu
        String[] columns = {"ID", "Tiêu đề", "Tác giả", "Lĩnh vực", "Ghi chú", "Từ khóa", "Nội dung/Link", "Loại"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        documentTable = new JTable(tableModel);
        documentTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        documentTable.setRowHeight(30);
        documentTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        documentTable.getTableHeader().setBackground(new Color(41, 128, 185));
        documentTable.getTableHeader().setForeground(Color.WHITE);

        tableScrollPane = new JScrollPane(documentTable);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        // Panel nút chức năng - ĐÃ ĐƯỢC CẢI TIẾN
        buttonPanel = new JPanel(new GridLayout(2, 5, 10, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        Dimension buttonSize = new Dimension(150, 40);

        addButton = createStyledButton("Thêm", getIcon("add", 20));
        editButton = createStyledButton("Sửa", getIcon("edit", 20));
        deleteButton = createStyledButton("Xóa", getIcon("delete", 20));
        shareButton = createStyledButton("Chia sẻ", getIcon("share", 20));
        commentButton = createStyledButton("Bình luận", getIcon("comment", 20));
        trackButton = createStyledButton("Theo dõi", getIcon("track", 20));
        exportButton = createStyledButton("Xuất", getIcon("export", 20));
        JButton viewButton = createStyledButton("Xem chi tiết", getIcon("view", 20));
        JButton archiveButton = createStyledButton("Lưu trữ", getIcon("archive", 20));
        JButton refreshButton = createStyledButton("Làm mới", getIcon("refresh", 20));

        addButton.setPreferredSize(buttonSize);
        editButton.setPreferredSize(buttonSize);
        deleteButton.setPreferredSize(buttonSize);
        shareButton.setPreferredSize(buttonSize);
        commentButton.setPreferredSize(buttonSize);
        trackButton.setPreferredSize(buttonSize);
        exportButton.setPreferredSize(buttonSize);
        viewButton.setPreferredSize(buttonSize);
        archiveButton.setPreferredSize(buttonSize);
        refreshButton.setPreferredSize(buttonSize);

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(shareButton);
        buttonPanel.add(commentButton);
        buttonPanel.add(trackButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(viewButton);
        buttonPanel.add(archiveButton);
        buttonPanel.add(refreshButton);

        // Sắp xếp các thành phần
        panel.add(welcomePanel, BorderLayout.NORTH);
        panel.add(searchPanel, BorderLayout.CENTER);
        panel.add(tableScrollPane, BorderLayout.CENTER);

        JPanel buttonContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonContainer.setOpaque(false);
        buttonContainer.add(buttonPanel);
        panel.add(buttonContainer, BorderLayout.SOUTH);

        // Thêm sự kiện
        addButton.addActionListener(e -> showAddDialog());
        editButton.addActionListener(e -> showEditDialog());
        deleteButton.addActionListener(e -> deleteDocument());
        searchButton.addActionListener(e -> searchDocuments());
        exportButton.addActionListener(e -> exportToJSON());
        trackButton.addActionListener(e -> showTrackDialog());
        shareButton.addActionListener(e -> shareDocument());
        commentButton.addActionListener(e -> showCommentDialog());
        viewButton.addActionListener(e -> showViewDialog());
        archiveButton.addActionListener(e -> moveToArchive());
        advancedButton.addActionListener(e -> showAdvancedSearchDialog());
        refreshButton.addActionListener(e -> loadDocuments());

        return panel;
    }

    private JPanel createArchivePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel tìm kiếm
        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setOpaque(false);
        searchPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 100), 1),
            "Tìm kiếm tài liệu lưu trữ",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14),
            new Color(100, 100, 100)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridx = 0;
        gbc.gridy = 0;
        searchPanel.add(new JLabel("Từ khóa:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        JTextField archiveSearchField = new JTextField();
        archiveSearchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchPanel.add(archiveSearchField, gbc);

        gbc.gridx = 4;
        gbc.gridwidth = 1;
        JButton archiveSearchButton = createStyledButton("Tìm kiếm", getIcon("search", 20));
        archiveSearchButton.setPreferredSize(new Dimension(120, 30));
        searchPanel.add(archiveSearchButton, gbc);

        // Bảng dữ liệu lưu trữ
        String[] columns = {"ID", "Tiêu đề", "Tác giả", "Lĩnh vực", "Ngày lưu trữ"};
        DefaultTableModel archiveModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable archiveTable = new JTable(archiveModel);
        archiveTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        archiveTable.setRowHeight(30);
        archiveTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        archiveTable.getTableHeader().setBackground(new Color(100, 100, 100));
        archiveTable.getTableHeader().setForeground(Color.WHITE);

        JScrollPane archiveScrollPane = new JScrollPane(archiveTable);
        archiveScrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        // Panel nút chức năng
        JPanel archiveButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        archiveButtonPanel.setOpaque(false);

        JButton restoreButton = createStyledButton("Khôi phục", getIcon("restore", 20));
        JButton deleteArchiveButton = createStyledButton("Xóa vĩnh viễn", getIcon("delete", 20));
        JButton exportArchiveButton = createStyledButton("Xuất lưu trữ", getIcon("export", 20));

        archiveButtonPanel.add(restoreButton);
        archiveButtonPanel.add(deleteArchiveButton);
        archiveButtonPanel.add(exportArchiveButton);

        // Thêm các thành phần vào panel chính
        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(archiveScrollPane, BorderLayout.CENTER);
        panel.add(archiveButtonPanel, BorderLayout.SOUTH);

        // Thêm sự kiện
        restoreButton.addActionListener(e -> restoreFromArchive());
        deleteArchiveButton.addActionListener(e -> deleteFromArchive());
        exportArchiveButton.addActionListener(e -> exportArchive());
        archiveSearchButton.addActionListener(e -> searchArchivedDocuments(archiveSearchField.getText()));

        return panel;
    }

 private void searchArchivedDocuments(String keyword) {
    JTable archiveTable = getArchiveTable();
    if (archiveTable == null) {
        JOptionPane.showMessageDialog(this, "Không tìm thấy bảng lưu trữ!",
                                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        return;
    }

    DefaultTableModel archiveModel = (DefaultTableModel) archiveTable.getModel();
    archiveModel.setRowCount(0); // Xóa dữ liệu cũ

    try {
        List<Document> archivedDocs = documentDAO.getArchivedDocuments(currentUser.getId());
        for (Document doc : archivedDocs) {
            if (shouldDisplayDocument(doc, keyword)) {
                archiveModel.addRow(new Object[]{
                    doc.getId(),
                    doc.getTitle(),
                    doc.getAuthor(),
                    doc.getField(),
                    doc.getArchivedDate() != null ?
                        new SimpleDateFormat("dd/MM/yyyy HH:mm").format(doc.getArchivedDate()) : "N/A"
                });
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Lỗi khi tải tài liệu lưu trữ: " + e.getMessage(),
                                    "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}

private boolean shouldDisplayDocument(Document doc, String keyword) {
    if (keyword == null || keyword.isEmpty()) {
        return true;
    }
    String lowerKeyword = keyword.toLowerCase();
    return (doc.getTitle() != null && doc.getTitle().toLowerCase().contains(lowerKeyword)) ||
           (doc.getAuthor() != null && doc.getAuthor().toLowerCase().contains(lowerKeyword)) ||
           (doc.getField() != null && doc.getField().toLowerCase().contains(lowerKeyword));
}
    private void showAdvancedSearchDialog() {
        JDialog dialog = new JDialog(this, "Tìm kiếm nâng cao", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Tiêu đề
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("Tìm kiếm nâng cao tài liệu");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panel.add(titleLabel, gbc);

        // Các trường tìm kiếm
        gbc.gridwidth = 1;
        gbc.gridy++;
        panel.add(new JLabel("Tiêu đề:"), gbc);

        gbc.gridx = 1;
        JTextField titleField = new JTextField(20);
        panel.add(titleField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Tác giả:"), gbc);

        gbc.gridx = 1;
        JTextField authorField = new JTextField(20);
        panel.add(authorField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Từ khóa:"), gbc);

        gbc.gridx = 1;
        JTextField keywordField = new JTextField(20);
        panel.add(keywordField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Lĩnh vực:"), gbc);

        gbc.gridx = 1;
        JComboBox<String> fieldCombo = new JComboBox<>();
        fieldCombo.addItem("Tất cả");
        List<String> fields = documentDAO.getAllFields();
        for (String field : fields) {
            fieldCombo.addItem(field);
        }
        panel.add(fieldCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Ngày từ:"), gbc);

        gbc.gridx = 1;
        JTextField dateFromField = new JTextField(10);
        panel.add(dateFromField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Ngày đến:"), gbc);

        gbc.gridx = 1;
        JTextField dateToField = new JTextField(10);
        panel.add(dateToField, gbc);

        // Nút tìm kiếm
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;
        JButton searchButton = new JButton("Tìm kiếm");
        searchButton.setPreferredSize(new Dimension(120, 30));
        panel.add(searchButton, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }


    private void restoreFromArchive() {
        JTable archiveTable = getArchiveTable();
        if (archiveTable == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy bảng lưu trữ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int selectedRow = archiveTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tài liệu cần khôi phục!");
            return;
        }

        int archiveId = (int) archiveTable.getModel().getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc muốn khôi phục tài liệu này từ lưu trữ?",
            "Xác nhận khôi phục",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (documentDAO.restoreFromArchive(archiveId, currentUser.getId())) {
                    JOptionPane.showMessageDialog(this, "Khôi phục tài liệu thành công!");
                    loadDocuments();
                    loadArchivedDocuments();
                } else {
                    JOptionPane.showMessageDialog(this, "Không thể khôi phục tài liệu!");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                    "Lỗi hệ thống: " + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            }       }
        }
    private void deleteFromArchive() {
        JTable archiveTable = getArchiveTable();
        if (archiveTable == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy bảng lưu trữ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int selectedRow = archiveTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tài liệu cần xóa!");
            return;
        }

        int archiveId = (int) archiveTable.getModel().getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc muốn xóa vĩnh viễn tài liệu này từ lưu trữ?",
            "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (documentDAO.deleteFromArchive(archiveId, currentUser.getId())) {
                    JOptionPane.showMessageDialog(this, "Đã xóa tài liệu khỏi lưu trữ!");
                    loadArchivedDocuments();
                } else {
                    JOptionPane.showMessageDialog(this, "Không thể xóa tài liệu!");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                    "Lỗi hệ thống: " + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

private void exportArchive() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Lưu file JSON lưu trữ");
    fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JSON files", "json"));

    if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
        try {
            JSONArray jsonArray = new JSONArray();
            List<Document> archivedDocs = documentDAO.getArchivedDocuments(currentUser.getId());

            for (Document doc : archivedDocs) {
                JSONObject jsonDoc = new JSONObject();
                jsonDoc.put("id", doc.getId());
                jsonDoc.put("original_doc_id", doc.getOriginalDocId());
                jsonDoc.put("title", doc.getTitle());
                jsonDoc.put("author", doc.getAuthor());
                jsonDoc.put("field", doc.getField());
                jsonDoc.put("note", doc.getNote());
                jsonDoc.put("keywords", doc.getKeywords());
                jsonDoc.put("content", doc.getContent());
                jsonDoc.put("archived_date", doc.getArchivedDate());
                jsonArray.put(jsonDoc);
            }

            try (FileWriter file = new FileWriter(fileChooser.getSelectedFile())) {
                file.write(jsonArray.toString(4));
                JOptionPane.showMessageDialog(this, "Xuất dữ liệu lưu trữ thành công!");
            }
        } catch (IOException | SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi xuất dữ liệu: " + e.getMessage());
        }
    }
}
    private JButton createStyledButton(String text, Icon icon) {
        JButton button = new JButton(text, icon);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(41, 128, 185));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(120, 35));
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setIconTextGap(8);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(41, 128, 185), 1),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(52, 152, 219));
                button.setBorder(BorderFactory.createLineBorder(new Color(52, 152, 219), 2));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(41, 128, 185));
                button.setBorder(BorderFactory.createLineBorder(new Color(41, 128, 185), 1));
            }
        });
        return button;
    }


    private void loadDocuments() {
        tableModel.setRowCount(0);
        String selectedField = null;
        if (fieldFilterCombo != null && fieldFilterCombo.getSelectedIndex() > 0) {
            selectedField = (String) fieldFilterCombo.getSelectedItem();
        }
        List<Document> allDocs = documentDAO.getAllDocuments(-1);
        List<Document> sharedDocs = new SharedDocumentDAO().getSharedDocuments(currentUser.getId());
        java.util.Set<Integer> sharedDocIds = new java.util.HashSet<>();
        for (Document doc : sharedDocs) {
            sharedDocIds.add(doc.getId());
        }
        for (Document doc : allDocs) {
            if (selectedField != null && !selectedField.equals(doc.getField())) {
				continue;
			}
            String type;
            if (doc.isOwnedBy(currentUser.getId())) {
                type = "Của tôi";
            } else if (sharedDocIds.contains(doc.getId())) {
                type = "Chia sẻ";
            } else {
                type = "Khác";
            }
            tableModel.addRow(new Object[]{
                doc.getId(),
                doc.getTitle(),
                doc.getAuthor(),
                doc.getField(),
                doc.getNote(),
                doc.getKeywords(),
                doc.getContent(),
                type
            });
        }
    }

    private void showAddDialog() {
        JDialog dialog = new JDialog(this, "Thêm tài liệu mới", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(7, 2, 5, 5));
        JTextField titleField = new JTextField();
        JTextField authorField = new JTextField();
        JTextField fieldField = new JTextField();
        JTextArea noteArea = new JTextArea(3, 20);
        JTextField keywordsField = new JTextField();
        JTextField contentField = new JTextField();

        panel.add(new JLabel("Tiêu đề:"));
        panel.add(titleField);
        panel.add(new JLabel("Tác giả:"));
        panel.add(authorField);
        panel.add(new JLabel("Lĩnh vực:"));
        panel.add(fieldField);
        panel.add(new JLabel("Ghi chú:"));
        panel.add(new JScrollPane(noteArea));
        panel.add(new JLabel("Từ khóa:"));
        panel.add(keywordsField);
        panel.add(new JLabel("Nội dung/Link:"));
        panel.add(contentField);

        JButton saveButton = new JButton("Lưu");
        saveButton.addActionListener(e -> {
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String field = fieldField.getText().trim();
            String note = noteArea.getText().trim();
            String keywords = keywordsField.getText().trim();
            String content = contentField.getText().trim();

            // Validate input
            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập tiêu đề tài liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Document doc = new Document(0, title, author, field, note, keywords, content, currentUser.getId());
                if (documentDAO.addDocument(doc)) {
                    JOptionPane.showMessageDialog(dialog, "Thêm tài liệu thành công!");
                    dialog.dispose();
                    loadDocuments();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Lỗi khi thêm tài liệu! Vui lòng thử lại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(saveButton, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showEditDialog() {
        int selectedRow = documentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tài liệu cần sửa!");
            return;
        }

        int docId = (int) tableModel.getValueAt(selectedRow, 0);
        String title = (String) tableModel.getValueAt(selectedRow, 1);
        String author = (String) tableModel.getValueAt(selectedRow, 2);
        String field = (String) tableModel.getValueAt(selectedRow, 3);
        String note = (String) tableModel.getValueAt(selectedRow, 4);
        String keywords = (String) tableModel.getValueAt(selectedRow, 5);
        String content = (String) tableModel.getValueAt(selectedRow, 6);

        JDialog dialog = new JDialog(this, "Sửa tài liệu", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(7, 2, 5, 5));
        JTextField titleField = new JTextField(title);
        JTextField authorField = new JTextField(author);
        JTextField fieldField = new JTextField(field);
        JTextArea noteArea = new JTextArea(note, 3, 20);
        JTextField keywordsField = new JTextField(keywords);
        JTextField contentField = new JTextField(content);

        panel.add(new JLabel("Tiêu đề:"));
        panel.add(titleField);
        panel.add(new JLabel("Tác giả:"));
        panel.add(authorField);
        panel.add(new JLabel("Lĩnh vực:"));
        panel.add(fieldField);
        panel.add(new JLabel("Ghi chú:"));
        panel.add(new JScrollPane(noteArea));
        panel.add(new JLabel("Từ khóa:"));
        panel.add(keywordsField);
        panel.add(new JLabel("Nội dung/Link:"));
        panel.add(contentField);

        JButton saveButton = new JButton("Lưu");
        saveButton.addActionListener(e -> {
            Document doc = new Document(docId,
                titleField.getText(),
                authorField.getText(),
                fieldField.getText(),
                noteArea.getText(),
                keywordsField.getText(),
                contentField.getText(),
                currentUser.getId()
            );
            if (documentDAO.updateDocument(doc)) {
                JOptionPane.showMessageDialog(dialog, "Cập nhật tài liệu thành công!");
                dialog.dispose();
                loadDocuments();
            } else {
                JOptionPane.showMessageDialog(dialog, "Lỗi khi cập nhật tài liệu!");
            }
        });

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(saveButton, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void deleteDocument() {
        int selectedRow = documentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tài liệu cần xóa!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn xóa tài liệu này?",
            "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            int docId = (int) tableModel.getValueAt(selectedRow, 0);
            if (documentDAO.deleteDocument(docId, currentUser.getId())) {
                JOptionPane.showMessageDialog(this, "Xóa tài liệu thành công!");
                loadDocuments();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa tài liệu!");
            }
        }
    }

    private void searchDocuments() {
        String searchText = searchField.getText();
        String searchType = (String) searchTypeCombo.getSelectedItem();

        List<Document> results;
        switch (searchType) {
            case "Từ khóa":
                results = documentDAO.searchDocuments(searchText, null, null, currentUser.getId());
                break;
            case "Tác giả":
                results = documentDAO.searchDocuments(null, searchText, null, currentUser.getId());
                break;
            case "Lĩnh vực":
                results = documentDAO.searchDocuments(null, null, searchText, currentUser.getId());
                break;
            default:
                results = documentDAO.getAllDocuments(currentUser.getId());
        }

        tableModel.setRowCount(0);
        for (Document doc : results) {
            tableModel.addRow(new Object[]{
                doc.getId(),
                doc.getTitle(),
                doc.getAuthor(),
                doc.getField(),
                doc.getNote(),
                doc.getKeywords(),
                doc.getContent(),
                doc.isOwnedBy(currentUser.getId()) ? "Của tôi" : "Chia sẻ"
            });
        }
    }

    private void exportToJSON() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu file JSON");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JSON files", "json"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                JSONArray jsonArray = new JSONArray();
                List<Document> documents = documentDAO.getAllDocuments(currentUser.getId());

                for (Document doc : documents) {
                    JSONObject jsonDoc = new JSONObject();
                    jsonDoc.put("id", doc.getId());
                    jsonDoc.put("title", doc.getTitle());
                    jsonDoc.put("author", doc.getAuthor());
                    jsonDoc.put("field", doc.getField());
                    jsonDoc.put("note", doc.getNote());
                    jsonDoc.put("keywords", doc.getKeywords());
                    jsonDoc.put("content", doc.getContent());
                    jsonArray.put(jsonDoc);
                }

                try (FileWriter file = new FileWriter(fileChooser.getSelectedFile())) {
                    file.write(jsonArray.toString(4));
                    JOptionPane.showMessageDialog(this, "Xuất dữ liệu thành công!");
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xuất dữ liệu: " + e.getMessage());
            }
        }
    }

    private void showTrackDialog() {
        JDialog dialog = new JDialog(this, "Theo dõi từ khóa", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(250, 252, 255));
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 200), 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Tạo model và list cho từ khóa
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> keywordList = new JList<>(listModel);
        keywordList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        keywordList.setVisibleRowCount(5);
        JScrollPane scrollPane = new JScrollPane(keywordList);

        // Panel chứa thanh tìm kiếm và các nút
        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBackground(new Color(250, 252, 255));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Thanh tìm kiếm
        JTextField keywordField = new JTextField();
        keywordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        controlPanel.add(keywordField, gbc);

        // Nút Thêm
        JButton addButton = new JButton("Thêm", getIcon("add", 20));
        addButton.setBackground(new Color(60, 130, 220));
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.setPreferredSize(new Dimension(100, 30));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        controlPanel.add(addButton, gbc);

        // Nút Xóa
        JButton removeButton = new JButton("Xóa", getIcon("delete", 20));
        removeButton.setBackground(new Color(60, 130, 220));
        removeButton.setForeground(Color.WHITE);
        removeButton.setFocusPainted(false);
        removeButton.setPreferredSize(new Dimension(100, 30));
        gbc.gridx = 1;
        gbc.gridy = 1;
        controlPanel.add(removeButton, gbc);

        // Thêm dữ liệu từ database
        List<String> keywords = trackedKeywordDAO.getTrackedKeywords(currentUser.getId());
        for (String keyword : keywords) {
            listModel.addElement(keyword);
        }

        // Xử lý sự kiện cho nút Thêm
        addButton.addActionListener(e -> {
            String keyword = keywordField.getText().trim();
            if (!keyword.isEmpty()) {
                if (trackedKeywordDAO.addKeyword(keyword, currentUser.getId())) {
                    listModel.addElement(keyword);
                    keywordField.setText("");
                } else {
                    JOptionPane.showMessageDialog(dialog, "Từ khóa đã tồn tại!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        // Xử lý sự kiện cho nút Xóa
        removeButton.addActionListener(e -> {
            String selected = keywordList.getSelectedValue();
            if (selected != null) {
                if (trackedKeywordDAO.removeKeyword(selected, currentUser.getId())) {
                    listModel.removeElement(selected);
                }
            }
        });

        // Thêm các thành phần vào panel chính
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    private void shareDocument() {
        int selectedRow = documentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tài liệu để chia sẻ!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int docId = (int) tableModel.getValueAt(selectedRow, 0);
        UserDAO userDAO = new UserDAO();
        List<User> users = userDAO.getAllUsers(currentUser.getId());
        if (users.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có người dùng nào khác để chia sẻ!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String[] usernames = users.stream().map(User::getUsername).toArray(String[]::new);
        JPanel panel = new JPanel();
        panel.setBackground(new java.awt.Color(250, 252, 255));
        panel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(180, 180, 200), 1));
        JComboBox<String> userCombo = new JComboBox<>(usernames);
        userCombo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        panel.add(new JLabel("Chọn người nhận:"));
        panel.add(userCombo);
        int result = JOptionPane.showConfirmDialog(this, panel, "Chia sẻ tài liệu", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, getIcon("share", 24));
        if (result == JOptionPane.OK_OPTION) {
            String selectedUser = (String) userCombo.getSelectedItem();
            int sharedToId = users.stream().filter(u -> u.getUsername().equals(selectedUser)).findFirst().get().getId();
            SharedDocumentDAO sharedDocumentDAO = new SharedDocumentDAO();
            boolean success = sharedDocumentDAO.shareDocument(docId, currentUser.getId(), sharedToId);
            if (success) {
                JOptionPane.showMessageDialog(this, "Chia sẻ tài liệu thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi chia sẻ tài liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showCommentDialog() {
        int selectedRow = documentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tài liệu để xem/chú thích!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int docId = (int) tableModel.getValueAt(selectedRow, 0);
        DocumentCommentDAO commentDAO = new DocumentCommentDAO();
        JDialog dialog = new JDialog(this, "Chú thích tài liệu", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new java.awt.Color(250, 252, 255));
        panel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(180, 180, 200), 2));
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> commentList = new JList<>(listModel);
        commentList.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(commentList);
        // Load comments
        List<DocumentCommentDAO.CommentInfo> comments = commentDAO.getComments(docId);
        for (DocumentCommentDAO.CommentInfo c : comments) {
            listModel.addElement("[" + c.username + "] " + c.comment + " (" + c.createdAt + ")");
        }
        // Add comment area
        JPanel addPanel = new JPanel(new BorderLayout());
        addPanel.setBackground(new java.awt.Color(250, 252, 255));
        JTextField commentField = new JTextField();
        commentField.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        JButton addCommentBtn = new JButton("Thêm chú thích", getIcon("comment", 24));
        addCommentBtn.setBackground(new java.awt.Color(60, 130, 220));
        addCommentBtn.setForeground(java.awt.Color.WHITE);
        addCommentBtn.setFocusPainted(false);
        addPanel.add(commentField, BorderLayout.CENTER);
        addPanel.add(addCommentBtn, BorderLayout.EAST);
        addCommentBtn.addActionListener(e -> {
            String text = commentField.getText().trim();
            if (!text.isEmpty()) {
                boolean ok = commentDAO.addComment(docId, currentUser.getId(), text);
                if (ok) {
                    listModel.addElement("[" + currentUser.getUsername() + "] " + text + " (now)");
                    commentField.setText("");
                } else {
                    JOptionPane.showMessageDialog(dialog, "Lỗi khi thêm chú thích!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(addPanel, BorderLayout.SOUTH);
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private javax.swing.Icon getIcon(String name) {
        // Có thể thay bằng đường dẫn icon thực tế nếu có file icon
        // Nếu không có, dùng icon mặc định Java
        switch (name) {
            case "add": return javax.swing.UIManager.getIcon("FileView.directoryIcon");
            case "edit": return javax.swing.UIManager.getIcon("FileView.fileIcon");
            case "delete": return javax.swing.UIManager.getIcon("OptionPane.errorIcon");
            case "export": return javax.swing.UIManager.getIcon("FileView.floppyDriveIcon");
            case "track": return javax.swing.UIManager.getIcon("FileChooser.detailsViewIcon");
            case "share": return javax.swing.UIManager.getIcon("FileView.computerIcon");
            case "comment": return javax.swing.UIManager.getIcon("OptionPane.informationIcon");
            case "view": return javax.swing.UIManager.getIcon("FileView.hardDriveIcon");
            default: return null;
        }
    }

    private javax.swing.Icon getIcon(String name, int size) {
        Icon base = getIcon(name);
        if (base instanceof ImageIcon) {
            Image img = ((ImageIcon) base).getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        }
        return base;
    }

    private void loadFields() {
        List<String> fields = documentDAO.getAllFields();
        fieldFilterCombo.removeAllItems();
        fieldFilterCombo.addItem("Tất cả");
        for (String field : fields) {
            fieldFilterCombo.addItem(field);
        }
    }

    private void showViewDialog() {
        int selectedRow = documentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tài liệu để xem chi tiết!");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Tiêu đề: ").append(tableModel.getValueAt(selectedRow, 1)).append("\n");
        sb.append("Tác giả: ").append(tableModel.getValueAt(selectedRow, 2)).append("\n");
        sb.append("Lĩnh vực: ").append(tableModel.getValueAt(selectedRow, 3)).append("\n");
        sb.append("Ghi chú: ").append(tableModel.getValueAt(selectedRow, 4)).append("\n");
        sb.append("Từ khóa: ").append(tableModel.getValueAt(selectedRow, 5)).append("\n");
        sb.append("Nội dung/Link: ").append(tableModel.getValueAt(selectedRow, 6)).append("\n");
        JOptionPane.showMessageDialog(this, sb.toString(), "Chi tiết tài liệu", JOptionPane.INFORMATION_MESSAGE);
    }
  private void moveToArchive() {
    int selectedRow = documentTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Vui lòng chọn tài liệu để lưu trữ!", "Thông báo", JOptionPane.WARNING_MESSAGE);
        return;
    }

    int docId = (int) tableModel.getValueAt(selectedRow, 0);
    String docTitle = (String) tableModel.getValueAt(selectedRow, 1);

    int confirm = JOptionPane.showConfirmDialog(this,
        "Bạn có chắc muốn lưu trữ tài liệu:\n" + docTitle + "?",
        "Xác nhận lưu trữ",
        JOptionPane.YES_NO_OPTION);

    if (confirm == JOptionPane.YES_OPTION) {
        try {
            if (documentDAO.moveToArchive(docId, currentUser.getId())) {
                JOptionPane.showMessageDialog(this,
                    "Đã lưu trữ tài liệu thành công: " + docTitle,
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
                loadDocuments();
                loadArchivedDocuments();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Không tìm thấy tài liệu để lưu trữ hoặc bạn không có quyền!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Lỗi hệ thống khi lưu trữ:\n" + ex.getMessage(),
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
    private void loadArchivedDocuments() {
    try {
        // Lấy bảng lưu trữ từ tab
        JTable archiveTable = getArchiveTable();
        if (archiveTable == null) {
            System.err.println("Không tìm thấy bảng lưu trữ");
            return;
        }

        // Lấy model của bảng
        DefaultTableModel archiveModel = (DefaultTableModel) archiveTable.getModel();
        archiveModel.setRowCount(0); // Xóa dữ liệu cũ

        // Lấy dữ liệu từ DAO
        List<Document> archivedDocs = documentDAO.getArchivedDocuments(currentUser.getId());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        // Thêm dữ liệu vào bảng
        for (Document doc : archivedDocs) {
            archiveModel.addRow(new Object[]{
                doc.getId(),
                doc.getTitle(),
                doc.getAuthor(),
                doc.getField(),
                doc.getArchivedDate() != null ? 
                    dateFormat.format(doc.getArchivedDate()) : "N/A"
            });
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this,
            "Lỗi khi tải tài liệu lưu trữ: " + ex.getMessage(),
            "Lỗi",
            JOptionPane.ERROR_MESSAGE);
    }
}
    private JTable getArchiveTable() {
    // Lấy tab lưu trữ (thường là tab index 1)
    Component archiveTab = tabbedPane.getComponentAt(1);
    
    if (archiveTab instanceof JPanel) {
        // Duyệt qua các component để tìm JScrollPane chứa JTable
        for (Component comp : ((JPanel) archiveTab).getComponents()) {
            if (comp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) comp;
                Component view = scrollPane.getViewport().getView();
                if (view instanceof JTable) {
                    return (JTable) view;
                }
            }
        }
    }
    return null;
}


}