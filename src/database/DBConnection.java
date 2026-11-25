package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnection {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/sportify?useSSL=false&serverTimezone=UTC";
    private static final String DB_USERNAME = "YOUR_MYSQL_USERNAME";
    private static final String DB_PASSWORD = "YOUR_MYSQL_PASSWORD";

    private DBConnection() { }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
    }
}
