import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class LibraryApp extends JFrame {
    private Library library;
    private JPanel middle;

    public LibraryApp() throws SQLException {
        library = new Library();
        setTitle("BookNest Library Management System");
        setSize(760, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel heading = new JLabel("BookNest Library", JLabel.CENTER);
        heading.setFont(new Font("Arial", Font.BOLD, 22));
        add(heading, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new GridLayout(5, 2, 6, 6));
        addButton(buttons, "Add Book", () -> addBook());
        addButton(buttons, "Register Member", () -> registerMember());
        addButton(buttons, "Issue Book", () -> issueBook());
        addButton(buttons, "Return Book", () -> returnBook());
        addButton(buttons, "Search by Book ID", () -> searchById());
        addButton(buttons, "Search by Title", () -> searchByTitle());
        addButton(buttons, "Show All Books", () -> showBooks());
        addButton(buttons, "Show Members", () -> showMembers());
        addButton(buttons, "Library Summary", () -> showSummary());
        addButton(buttons, "Exit", () -> dispose());
        add(buttons, BorderLayout.WEST);

        middle = new JPanel(new BorderLayout());
        middle.add(new JLabel("Choose an option from the left.", JLabel.CENTER), BorderLayout.NORTH);
        middle.add(new JLabel(new ImageIcon("images/welcome-books.png"), JLabel.CENTER), BorderLayout.CENTER);
        add(middle, BorderLayout.CENTER);
        setVisible(true);
    }

    private void addButton(JPanel panel, String text, Runnable action) {
        JButton button = new JButton(text);
        button.addActionListener(e -> action.run());
        panel.add(button);
    }

    private String[] getInput(String title, String[] labels) {
        JPanel panel = new JPanel(new GridLayout(labels.length, 2, 5, 5));
        JTextField[] fields = new JTextField[labels.length];
        for (int i = 0; i < labels.length; i++) {
            panel.add(new JLabel(labels[i]));
            fields[i] = new JTextField(15);
            panel.add(fields[i]);
        }
        int choice = JOptionPane.showConfirmDialog(this, panel, title, JOptionPane.OK_CANCEL_OPTION);
        if (choice != JOptionPane.OK_OPTION) return null;
        String[] values = new String[labels.length];
        for (int i = 0; i < labels.length; i++) values[i] = fields[i].getText().trim();
        return values;
    }

    private void message(String text) {
        JOptionPane.showMessageDialog(this, text);
    }

    private void error(Exception e) {
        if (e instanceof SQLException) message("Database connection or query failed!");
        else if (e instanceof NumberFormatException) message("Enter a valid number!");
        else message(e.getMessage());
    }

    private void addBook() {
        String[] data = getInput("Add Book", new String[]{"Book ID", "Title", "Author", "Number of copies"});
        if (data == null) return;
        try {
            int copies = Integer.parseInt(data[3]);
            library.addBook(data[0], data[1], data[2], copies);
            message("Book added successfully!");
            showBooks();
        } catch (SQLException | IllegalArgumentException e) { error(e); }
    }

    private void registerMember() {
        String[] data = getInput("Register Member", new String[]{"Member ID", "Member name"});
        if (data == null) return;
        try {
            library.registerMember(data[0], data[1]);
            message("Member registered successfully!");
            showMembers();
        } catch (SQLException | IllegalArgumentException e) { error(e); }
    }

    private void issueBook() {
        String[] data = getInput("Issue Book", new String[]{"Member ID", "Book ID"});
        if (data == null) return;
        try {
            library.issueBook(data[0], data[1]);
            message("Book issued successfully!");
            showBooks();
        } catch (SQLException | IllegalArgumentException e) { error(e); }
    }

    private void returnBook() {
        String[] data = getInput("Return Book", new String[]{"Member ID", "Book ID", "Days kept"});
        if (data == null) return;
        try {
            int days = Integer.parseInt(data[2]);
            int fine = library.returnBook(data[0], data[1], days);
            message("Book returned successfully! Fine: Rs. " + fine);
            showBooks();
        } catch (SQLException | IllegalArgumentException e) { error(e); }
    }

    private void searchById() {
        String[] data = getInput("Search Book", new String[]{"Book ID"});
        if (data == null) return;
        if (data[0].isEmpty()) { message("Enter a Book ID."); return; }
        try {
            Book book = library.searchBook(data[0]);
            ArrayList<Book> books = new ArrayList<Book>();
            if (book != null) books.add(book);
            showBookTable(books);
            if (book == null) message("Book not found!");
        } catch (SQLException e) { error(e); }
    }

    private void searchByTitle() {
        String[] data = getInput("Search Book", new String[]{"Title keyword"});
        if (data == null) return;
        if (data[0].isEmpty()) { message("Enter a title keyword."); return; }
        try {
            ArrayList<Book> books = library.searchBook(data[0], Integer.MAX_VALUE);
            showBookTable(books);
            if (books.isEmpty()) message("Book not found!");
        } catch (SQLException e) { error(e); }
    }

    private void showBooks() {
        try { showBookTable(library.getAllBooks()); }
        catch (SQLException e) { error(e); }
    }

    private void showBookTable(ArrayList<Book> books) {
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Book ID", "Title", "Author", "Total", "Available"}, 0);
        for (Book book : books) {
            model.addRow(new Object[]{book.getBookId(), book.getTitle(), book.getAuthor(),
                    book.getTotalCopies(), book.getAvailableCopies()});
        }
        showTable(model);
    }

    private void showMembers() {
        try {
            DefaultTableModel model = new DefaultTableModel(
                    new String[]{"Member ID", "Name", "Books borrowed"}, 0);
            for (Member member : library.getAllMembers()) {
                model.addRow(new Object[]{member.getMemberId(), member.getName(), member.getBorrowedCount()});
            }
            showTable(model);
        } catch (SQLException e) { error(e); }
    }

    private void showTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setEnabled(false);
        middle.removeAll();
        middle.add(new JScrollPane(table), BorderLayout.CENTER);
        middle.revalidate();
        middle.repaint();
    }

    private void showSummary() {
        try {
            int[] n = library.getSummary();
            JPanel panel = new JPanel(new GridLayout(4, 1, 5, 12));
            panel.add(new JLabel("Total books registered: " + n[0]));
            panel.add(new JLabel("Total members registered: " + n[1]));
            panel.add(new JLabel("Books currently issued: " + n[2]));
            panel.add(new JLabel("Successful issues since records began: " + n[3]));
            middle.removeAll();
            middle.add(panel, BorderLayout.NORTH);
            middle.revalidate();
            middle.repaint();
        } catch (SQLException e) { error(e); }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { new LibraryApp(); }
            catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Database connection failed! Check MySQL and DBConnection.java.");
            }
        });
    }
}
