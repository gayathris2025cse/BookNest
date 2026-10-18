import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

public class ConsoleLibraryApp {
    private static Library library;
    private static Scanner input = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            library = new Library();
        } catch (SQLException e) {
            System.out.println("Database connection failed! Check MySQL and your password.");
            return;
        }

        int choice;
        do {
            showMenu();
            try {
                choice = Integer.parseInt(ask("Enter choice: "));
            } catch (NumberFormatException e) {
                choice = -1;
            }

            try {
                switch (choice) {
                    case 1: addBook(); break;
                    case 2: registerMember(); break;
                    case 3: issueBook(); break;
                    case 4: returnBook(); break;
                    case 5: searchById(); break;
                    case 6: searchByTitle(); break;
                    case 7: showBooks(); break;
                    case 8: showMembers(); break;
                    case 9: showSummary(); break;
                    case 0: System.out.println("Goodbye!"); break;
                    default: System.out.println("Enter a choice from 0 to 9.");
                }
            } catch (SQLException e) {
                System.out.println("Database connection or query failed!");
            } catch (NumberFormatException e) {
                System.out.println("Enter a valid number!");
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        } while (choice != 0);
    }

    private static void showMenu() {
        System.out.println("\n===== BookNest Library =====");
        System.out.println("1. Add Book         2. Register Member");
        System.out.println("3. Issue Book       4. Return Book");
        System.out.println("5. Search by ID     6. Search by Title");
        System.out.println("7. Show All Books   8. Show Members");
        System.out.println("9. Library Summary  0. Exit");
    }

    private static String ask(String prompt) {
        System.out.print(prompt);
        return input.nextLine().trim();
    }

    private static void addBook() throws SQLException {
        String id = ask("Book ID: ");
        String title = ask("Title: ");
        String author = ask("Author: ");
        int copies = Integer.parseInt(ask("Number of copies: "));
        library.addBook(id, title, author, copies);
        System.out.println("Book added successfully!");
    }

    private static void registerMember() throws SQLException {
        String id = ask("Member ID: ");
        String name = ask("Member name: ");
        library.registerMember(id, name);
        System.out.println("Member registered successfully!");
    }

    private static void issueBook() throws SQLException {
        String memberId = ask("Member ID: ");
        String bookId = ask("Book ID: ");
        library.issueBook(memberId, bookId);
        System.out.println("Book issued successfully!");
    }

    private static void returnBook() throws SQLException {
        String memberId = ask("Member ID: ");
        String bookId = ask("Book ID: ");
        int days = Integer.parseInt(ask("Days kept: "));
        int fine = library.returnBook(memberId, bookId, days);
        System.out.println("Book returned successfully! Fine: Rs. " + fine);
    }

    private static void searchById() throws SQLException {
        String id = ask("Book ID: ");
        if (id.isEmpty()) {
            System.out.println("Enter a Book ID.");
            return;
        }
        Book book = library.searchBook(id);
        if (book == null) System.out.println("Book not found!");
        else printBook(book);
    }

    private static void searchByTitle() throws SQLException {
        String keyword = ask("Title keyword: ");
        if (keyword.isEmpty()) {
            System.out.println("Enter a title keyword.");
            return;
        }
        ArrayList<Book> books = library.searchBook(keyword, Integer.MAX_VALUE);
        if (books.isEmpty()) System.out.println("Book not found!");
        for (Book book : books) printBook(book);
    }

    private static void showBooks() throws SQLException {
        ArrayList<Book> books = library.getAllBooks();
        if (books.isEmpty()) System.out.println("No books registered.");
        for (Book book : books) printBook(book);
    }

    private static void printBook(Book book) {
        System.out.println(book.getBookId() + " | " + book.getTitle() + " | " + book.getAuthor()
                + " | Total: " + book.getTotalCopies() + " | Available: " + book.getAvailableCopies());
    }

    private static void showMembers() throws SQLException {
        ArrayList<Member> members = library.getAllMembers();
        if (members.isEmpty()) System.out.println("No members registered.");
        for (Member member : members) {
            System.out.println(member.getMemberId() + " | " + member.getName()
                    + " | Books borrowed: " + member.getBorrowedCount());
        }
    }

    private static void showSummary() throws SQLException {
        int[] n = library.getSummary();
        System.out.println("Total books registered: " + n[0]);
        System.out.println("Total members registered: " + n[1]);
        System.out.println("Books currently issued: " + n[2]);
        System.out.println("Successful issues since records began: " + n[3]);
    }
}
