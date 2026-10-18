CREATE DATABASE IF NOT EXISTS booknest_db;
USE booknest_db;

CREATE TABLE IF NOT EXISTS books (
    book_id VARCHAR(20) PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    author VARCHAR(100) NOT NULL,
    total_copies INT NOT NULL,
    available_copies INT NOT NULL
);

CREATE TABLE IF NOT EXISTS members (
    member_id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS issued_books (
    issue_id INT AUTO_INCREMENT PRIMARY KEY,
    member_id VARCHAR(20) NOT NULL,
    book_id VARCHAR(20) NOT NULL,
    issue_date DATE NOT NULL,
    return_date DATE,
    days_kept INT,
    fine INT,
    status VARCHAR(10) NOT NULL,
    FOREIGN KEY (member_id) REFERENCES members(member_id),
    FOREIGN KEY (book_id) REFERENCES books(book_id)
);

INSERT IGNORE INTO books VALUES
('B101', 'Java Basics', 'R. Sharma', 3, 3),
('B102', 'Database Systems', 'A. Kumar', 2, 2),
('B103', 'Computer Networks', 'S. Rao', 1, 1);

INSERT IGNORE INTO members VALUES
('M101', 'Asha'),
('M102', 'Rahul'),
('M103', 'Priya');
