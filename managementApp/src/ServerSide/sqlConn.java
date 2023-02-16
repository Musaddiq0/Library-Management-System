package ServerSide;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class sqlConn {

    public static final String DB_URL = "jdbc:sqlite:Books.db";
    public static final String DB_USERNAME = "";
    public static final String DB_PASSWORD = "";

    public static Connection getConnected() throws SQLException{
        Connection connect = DriverManager.getConnection(DB_URL,DB_USERNAME,DB_PASSWORD);
        System.out.println("Connection to DB successful !!!");
        return connect;
    }
}
