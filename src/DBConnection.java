import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/booknest_db";
    private static final String USER = "root";

    public static Connection getConnection() throws SQLException {
        String password = System.getenv("BOOKNEST_DB_PASSWORD");
        if (password == null) password = "";
        return DriverManager.getConnection(URL, USER, password);
    }
}
