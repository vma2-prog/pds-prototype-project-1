package br.com.bestand.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:bestand.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS usuarios (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nome TEXT UNIQUE NOT NULL,
                    senha TEXT NOT NULL,
                    permissoes TEXT NOT NULL
                );
            """);

            stmt.execute("""
                INSERT OR IGNORE INTO usuarios (id, nome, senha, permissoes)
                VALUES (1, 'admin', 'admin123', 'PRODUTOS,CATEGORIAS,FORNECEDORES,VENDAS,DEVOLUCOES,CARREGAMENTOS,PEDIDOS,RELATORIOS,USUARIOS');
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS categorias (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nome TEXT UNIQUE NOT NULL,
                    descricao TEXT
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS fornecedores (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nome TEXT NOT NULL,
                    contato TEXT,
                    telefone TEXT,
                    email TEXT,
                    endereco TEXT,
                    observacoes TEXT
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS produtos (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    codigo TEXT UNIQUE NOT NULL,
                    nome TEXT NOT NULL,
                    categoria_id INTEGER,
                    estoque INTEGER NOT NULL DEFAULT 0,
                    estoque_minimo INTEGER NOT NULL DEFAULT 0,
                    custo_atual REAL NOT NULL DEFAULT 0.0,
                    preco_venda REAL NOT NULL DEFAULT 0.0,
                    FOREIGN KEY (categoria_id) REFERENCES categorias(id) ON DELETE RESTRICT
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS vendas (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    data_hora TEXT NOT NULL,
                    valor_total REAL NOT NULL,
                    metodo_pagamento TEXT NOT NULL,
                    usuario_responsavel TEXT NOT NULL
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS itens_venda (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    venda_id INTEGER NOT NULL,
                    produto_id INTEGER NOT NULL,
                    quantidade INTEGER NOT NULL,
                    preco_unitario REAL NOT NULL,
                    custo_unitario_historico REAL NOT NULL,
                    FOREIGN KEY (venda_id) REFERENCES vendas(id) ON DELETE CASCADE,
                    FOREIGN KEY (produto_id) REFERENCES produtos(id)
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS devolucoes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    venda_id INTEGER NOT NULL,
                    data_hora TEXT NOT NULL,
                    motivo TEXT NOT NULL,
                    descricao_motivo TEXT,
                    valor_estornado REAL NOT NULL,
                    usuario_responsavel TEXT NOT NULL,
                    FOREIGN KEY (venda_id) REFERENCES vendas(id)
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS itens_devolucao (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    devolucao_id INTEGER NOT NULL,
                    produto_id INTEGER NOT NULL,
                    quantidade INTEGER NOT NULL,
                    FOREIGN KEY (devolucao_id) REFERENCES devolucoes(id) ON DELETE CASCADE,
                    FOREIGN KEY (produto_id) REFERENCES produtos(id)
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS carregamentos (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    data_hora TEXT NOT NULL,
                    fornecedor_fonte TEXT NOT NULL,
                    usuario_responsavel TEXT NOT NULL
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS itens_carregamento (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    carregamento_id INTEGER NOT NULL,
                    produto_id INTEGER NOT NULL,
                    quantidade INTEGER NOT NULL,
                    custo_unitario REAL NOT NULL,
                    FOREIGN KEY (carregamento_id) REFERENCES carregamentos(id) ON DELETE CASCADE,
                    FOREIGN KEY (produto_id) REFERENCES produtos(id)
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS pedidos_compra (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    fornecedor_id INTEGER NOT NULL,
                    data_pedido TEXT NOT NULL,
                    valor_total REAL NOT NULL,
                    situacao TEXT NOT NULL DEFAULT 'PENDENTE',
                    observacao TEXT,
                    FOREIGN KEY (fornecedor_id) REFERENCES fornecedores(id)
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS itens_pedido_compra (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    pedido_id INTEGER NOT NULL,
                    produto_id INTEGER NOT NULL,
                    quantidade INTEGER NOT NULL,
                    custo_unitario_previsto REAL NOT NULL,
                    FOREIGN KEY (pedido_id) REFERENCES pedidos_compra(id) ON DELETE CASCADE,
                    FOREIGN KEY (produto_id) REFERENCES produtos(id)
                );
            """);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
