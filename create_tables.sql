CREATE TABLE IF NOT EXISTS documents (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(100),
    field VARCHAR(100),
    note TEXT,
    keywords TEXT,
    user_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS tracked_keywords (
    id INT AUTO_INCREMENT PRIMARY KEY,
    keyword VARCHAR(100) NOT NULL,
    user_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE KEY unique_keyword_user (keyword, user_id)
); 
CREATE TABLE IF NOT EXISTS document_archive (
    id INT AUTO_INCREMENT PRIMARY KEY,
    original_doc_id VARCHAR(50),
    title VARCHAR(255),
    author VARCHAR(255),
    field VARCHAR(100),
    note TEXT,
    keywords TEXT,
    content TEXT,
    user_id INT,
    archived_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
