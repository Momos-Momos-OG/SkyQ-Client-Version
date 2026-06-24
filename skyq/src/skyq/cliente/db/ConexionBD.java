package skyq.cliente.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Conexion JDBC standalone para la App Cliente SkyQ. Sin dependencias del Manager. Reload. */
public final class ConexionBD {

    private static final String URL =
            "jdbc:sqlserver://localhost:1433;" +
            "databaseName=skyq_db;" +
            "encrypt=true;" +
            "trustServerCertificate=true";

    private static final String USER     = "SA";
    private static final String PASSWORD = "Momos@123";

    private ConexionBD() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}