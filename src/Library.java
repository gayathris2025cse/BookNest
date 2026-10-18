import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Library {
    private static int totalIssues;

    public Library() throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM issued_books");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) totalIssues = rs.getInt(1);
        }
    }

    public static int getTotalIssues() { return totalIssues; }

    public void addBook(String id, String title, String author, int copies) throws SQLException {
        id = id.trim(); title = title.trim(); author = author.trim();
        if (id.isEmpty() || title.isEmpty() || author.isEmpty() || copies <= 0)
            throw new IllegalArgumentException("Enter all fields and a positive copy count.");
        if (id.length() > 20 || title.length() > 100 || author.length() > 100)
            throw new IllegalArgumentException("One of the fields is too long.");
        String sql = "INSERT INTO books VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id); ps.setString(2, title); ps.setString(3, author);
            ps.setInt(4, copies); ps.setInt(5, copies);
            ps.executeUpdate();
        } catch (SQLException e) {
            if ("23000".equals(e.getSQLState())) throw new IllegalArgumentException("Book ID already exists!");
            throw e;
        }
    }

    public void registerMember(String id, String name) throws SQLException {
        id = id.trim(); name = name.trim();
        if (id.isEmpty() || name.isEmpty()) throw new IllegalArgumentException("Enter Member ID and name.");
        if (id.length() > 20 || name.length() > 100)
            throw new IllegalArgumentException("One of the fields is too long.");
        String sql = "INSERT INTO members (member_id, name) VALUES (?, ?)";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id); ps.setString(2, name); ps.executeUpdate();
        } catch (SQLException e) {
            if ("23000".equals(e.getSQLState())) throw new IllegalArgumentException("Member ID already exists!");
            throw e;
        }
    }

    public void issueBook(String memberId, String bookId) throws SQLException {
        memberId = memberId.trim(); bookId = bookId.trim();
        if (memberId.isEmpty() || bookId.isEmpty()) throw new IllegalArgumentException("Enter both IDs.");
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                checkMember(con, memberId);
                int available = lockBook(con, bookId);
                if (available == 0) throw new IllegalArgumentException("No copies available!");
                if (borrowedCount(con, memberId) >= 3)
                    throw new IllegalArgumentException("Maximum 3 books allowed!");
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO issued_books (member_id, book_id, issue_date, status) VALUES (?, ?, CURRENT_DATE, 'ISSUED')")) {
                    ps.setString(1, memberId); ps.setString(2, bookId); ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE books SET available_copies = available_copies - 1 WHERE book_id = ?")) {
                    ps.setString(1, bookId); ps.executeUpdate();
                }
                con.commit();
                totalIssues++;
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    public int returnBook(String memberId, String bookId, int days) throws SQLException {
        memberId = memberId.trim(); bookId = bookId.trim();
        if (memberId.isEmpty() || bookId.isEmpty()) throw new IllegalArgumentException("Enter both IDs.");
        if (days < 0) throw new IllegalArgumentException("Days kept cannot be negative.");
        int fine = calculateFine(days);
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                checkMember(con, memberId);
                lockBook(con, bookId);
                int issueId = -1;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT issue_id FROM issued_books WHERE member_id = ? AND book_id = ? AND status = 'ISSUED' ORDER BY issue_id LIMIT 1 FOR UPDATE")) {
                    ps.setString(1, memberId); ps.setString(2, bookId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) issueId = rs.getInt(1);
                    }
                }
                if (issueId == -1) throw new IllegalArgumentException("This book was not issued to this member!");
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE issued_books SET return_date = CURRENT_DATE, days_kept = ?, fine = ?, status = 'RETURNED' WHERE issue_id = ?")) {
                    ps.setInt(1, days); ps.setInt(2, fine); ps.setInt(3, issueId); ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE books SET available_copies = available_copies + 1 WHERE book_id = ?")) {
                    ps.setString(1, bookId); ps.executeUpdate();
                }
                con.commit();
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
        return fine;
    }

    public static int calculateFine(int days) {
        return Math.max(0, days - 7) * 2;
    }

    private void checkMember(Connection con, String id) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT member_id FROM members WHERE member_id = ? FOR UPDATE")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("Member not found!");
            }
        }
    }

    private int lockBook(Connection con, String id) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT available_copies FROM books WHERE book_id = ? FOR UPDATE")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("Book not found!");
                return rs.getInt(1);
            }
        }
    }

    private int borrowedCount(Connection con, String id) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT COUNT(*) FROM issued_books WHERE member_id = ? AND status = 'ISSUED'")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next(); return rs.getInt(1);
            }
        }
    }

    private Book readBook(ResultSet rs) throws SQLException {
        return new Book(rs.getString("book_id"), rs.getString("title"), rs.getString("author"),
                rs.getInt("total_copies"), rs.getInt("available_copies"));
    }

    public Book searchBook(String bookId) throws SQLException {
        String sql = "SELECT * FROM books WHERE book_id = ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, bookId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return readBook(rs);
            }
        }
        return null;
    }

    // Overloaded search method: finds titles containing the keyword.
    public ArrayList<Book> searchBook(String keyword, int limit) throws SQLException {
        ArrayList<Book> books = new ArrayList<Book>();
        String sql = "SELECT * FROM books WHERE title LIKE ? ORDER BY book_id LIMIT ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword.trim() + "%"); ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) books.add(readBook(rs));
            }
        }
        return books;
    }

    public ArrayList<Book> getAllBooks() throws SQLException {
        ArrayList<Book> books = new ArrayList<Book>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM books ORDER BY book_id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) books.add(readBook(rs));
        }
        return books;
    }

    public ArrayList<Member> getAllMembers() throws SQLException {
        ArrayList<Member> members = new ArrayList<Member>();
        String sql = "SELECT m.member_id, m.name, COUNT(i.issue_id) AS borrowed " +
                "FROM members m LEFT JOIN issued_books i ON m.member_id = i.member_id AND i.status = 'ISSUED' " +
                "GROUP BY m.member_id, m.name ORDER BY m.member_id";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) members.add(new Member(rs.getString("member_id"), rs.getString("name"), rs.getInt("borrowed")));
        }
        return members;
    }

    public int[] getSummary() throws SQLException {
        int[] numbers = new int[4];
        String sql = "SELECT (SELECT COUNT(*) FROM books), (SELECT COUNT(*) FROM members), " +
                "(SELECT COUNT(*) FROM issued_books WHERE status = 'ISSUED'), (SELECT COUNT(*) FROM issued_books)";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                for (int i = 0; i < 4; i++) numbers[i] = rs.getInt(i + 1);
            }
        }
        totalIssues = numbers[3];
        return numbers;
    }
}
