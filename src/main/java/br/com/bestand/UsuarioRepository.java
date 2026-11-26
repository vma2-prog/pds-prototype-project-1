package br.com.bestand;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class UsuarioRepository {
    private final Database database;

    public UsuarioRepository(Database database) {
        this.database = database;
        criarTabelas();
    }

    private void criarTabelas() {
        try (Connection connection = database.abrirConexao();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS perfis (
                        nome TEXT PRIMARY KEY COLLATE NOCASE,
                        senha_hash TEXT NOT NULL,
                        salt TEXT NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS perfil_permissoes (
                        perfil_nome TEXT NOT NULL COLLATE NOCASE,
                        permissao TEXT NOT NULL,
                        PRIMARY KEY (perfil_nome, permissao),
                        FOREIGN KEY (perfil_nome) REFERENCES perfis(nome) ON DELETE CASCADE
                    )
                    """);
        } catch (SQLException e) {
            throw new IllegalStateException("Não foi possível preparar o banco de perfis.", e);
        }
    }

    public boolean existe(String nome) {
        try (Connection connection = database.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT 1 FROM perfis WHERE nome = ?")) {
            statement.setString(1, nome);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Não foi possível consultar os perfis.", e);
        }
    }

    public List<PerfilUsuario> listar() {
        List<PerfilUsuario> perfis = new ArrayList<>();
        try (Connection connection = database.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT nome FROM perfis ORDER BY nome COLLATE NOCASE");
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                String nome = result.getString("nome");
                perfis.add(new PerfilUsuario(nome, listarPermissoes(connection, nome)));
            }
            return perfis;
        } catch (SQLException e) {
            throw new IllegalStateException("Não foi possível carregar os perfis.", e);
        }
    }

    public Optional<Credencial> buscarCredencial(String nome) {
        try (Connection connection = database.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT nome, senha_hash, salt FROM perfis WHERE nome = ?")) {
            statement.setString(1, nome);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return Optional.empty();
                }
                String nomeEncontrado = result.getString("nome");
                return Optional.of(new Credencial(
                        new PerfilUsuario(nomeEncontrado, listarPermissoes(connection, nomeEncontrado)),
                        result.getString("senha_hash"),
                        result.getString("salt")
                ));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Não foi possível consultar o perfil.", e);
        }
    }

    public void inserir(String nome, String senhaHash, String salt, Set<Permissao> permissoes) {
        try (Connection connection = database.abrirConexao()) {
            connection.setAutoCommit(false);
            try (PreparedStatement perfil = connection.prepareStatement(
                    "INSERT INTO perfis (nome, senha_hash, salt) VALUES (?, ?, ?)");
                 PreparedStatement permissao = connection.prepareStatement(
                         "INSERT INTO perfil_permissoes (perfil_nome, permissao) VALUES (?, ?)")) {
                perfil.setString(1, nome);
                perfil.setString(2, senhaHash);
                perfil.setString(3, salt);
                perfil.executeUpdate();
                inserirPermissoes(permissao, nome, permissoes);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Não foi possível cadastrar o perfil.", e);
        }
    }

    public void atualizar(String nome, String senhaHash, String salt, Set<Permissao> permissoes) {
        try (Connection connection = database.abrirConexao()) {
            connection.setAutoCommit(false);
            try {
                if (senhaHash != null) {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "UPDATE perfis SET senha_hash = ?, salt = ? WHERE nome = ?")) {
                        statement.setString(1, senhaHash);
                        statement.setString(2, salt);
                        statement.setString(3, nome);
                        if (statement.executeUpdate() == 0) {
                            throw new SQLException("Perfil não encontrado.");
                        }
                    }
                }
                try (PreparedStatement remover = connection.prepareStatement(
                        "DELETE FROM perfil_permissoes WHERE perfil_nome = ?");
                     PreparedStatement inserir = connection.prepareStatement(
                             "INSERT INTO perfil_permissoes (perfil_nome, permissao) VALUES (?, ?)")) {
                    remover.setString(1, nome);
                    remover.executeUpdate();
                    inserirPermissoes(inserir, nome, permissoes);
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Não foi possível atualizar o perfil.", e);
        }
    }

    public void remover(String nome) {
        try (Connection connection = database.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM perfis WHERE nome = ?")) {
            statement.setString(1, nome);
            if (statement.executeUpdate() == 0) {
                throw new IllegalArgumentException("O perfil não foi encontrado.");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Não foi possível remover o perfil.", e);
        }
    }

    private Set<Permissao> listarPermissoes(Connection connection, String nome) throws SQLException {
        EnumSet<Permissao> permissoes = EnumSet.noneOf(Permissao.class);
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT permissao FROM perfil_permissoes WHERE perfil_nome = ?")) {
            statement.setString(1, nome);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    permissoes.add(Permissao.valueOf(result.getString("permissao")));
                }
            }
        }
        return permissoes;
    }

    private void inserirPermissoes(PreparedStatement statement, String nome, Set<Permissao> permissoes)
            throws SQLException {
        for (Permissao permissao : permissoes) {
            statement.setString(1, nome);
            statement.setString(2, permissao.name());
            statement.addBatch();
        }
        statement.executeBatch();
    }

    public record Credencial(PerfilUsuario perfil, String senhaHash, String salt) {
    }
}
