package br.com.bestand;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {
    private final String jdbcUrl;

    public Database(Path databaseFile) {
        try {
            Path parent = databaseFile.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível criar a pasta do banco de dados.", e);
        }
        jdbcUrl = "jdbc:sqlite:" + databaseFile.toAbsolutePath();
    }

    public Connection abrirConexao() throws SQLException {
        Connection connection = DriverManager.getConnection(jdbcUrl);
        try (var statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException e) {
            connection.close();
            throw e;
        }
        return connection;
    }
}
