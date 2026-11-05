package br.com.bestand.ui;

import br.com.bestand.database.DatabaseManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.sql.*;

public class ProfitabilityReportView extends VBox {

    private TableView<LucroProduto> table = new TableView<>();
    private ObservableList<LucroProduto> list = FXCollections.observableArrayList();

    public ProfitabilityReportView() {
        this.setSpacing(16);

        Label title = new Label("Lucratividade por Produto (Lucro Bruto Estimado)");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");

        TableColumn<LucroProduto, String> colNome = new TableColumn<>("Produto");
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));

        TableColumn<LucroProduto, Integer> colQtd = new TableColumn<>("Qtd Vendida");
        colQtd.setCellValueFactory(new PropertyValueFactory<>("qtdVendida"));

        TableColumn<LucroProduto, Double> colReceita = new TableColumn<>("Receita (R$)");
        colReceita.setCellValueFactory(new PropertyValueFactory<>("receita"));

        TableColumn<LucroProduto, Double> colCusto = new TableColumn<>("Custo total (R$)");
        colCusto.setCellValueFactory(new PropertyValueFactory<>("custoTotal"));

        TableColumn<LucroProduto, Double> colLucro = new TableColumn<>("Lucro Bruto (R$)");
        colLucro.setCellValueFactory(new PropertyValueFactory<>("lucroBruto"));

        table.getColumns().addAll(colNome, colQtd, colReceita, colCusto, colLucro);
        table.setItems(list);

        carregarDados();

        this.getChildren().addAll(title, table);
    }

    private void carregarDados() {
        list.clear();
        String sql = """
            SELECT p.nome,
                   SUM(iv.quantidade) as qtd,
                   SUM(iv.quantidade * iv.preco_unitario) as receita,
                   SUM(iv.quantidade * iv.custo_unitario_historico) as custo
            FROM itens_venda iv
            JOIN produtos p ON p.id = iv.produto_id
            GROUP BY p.id, p.nome
            ORDER BY (receita - custo) DESC
        """;
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                double rec = rs.getDouble("receita");
                double cst = rs.getDouble("custo");
                list.add(new LucroProduto(
                    rs.getString("nome"),
                    rs.getInt("qtd"),
                    rec,
                    cst,
                    rec - cst
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static class LucroProduto {
        private final String nome;
        private final int qtdVendida;
        private final double receita;
        private final double custoTotal;
        private final double lucroBruto;

        public LucroProduto(String nome, int qtdVendida, double receita, double custoTotal, double lucroBruto) {
            this.nome = nome;
            this.qtdVendida = qtdVendida;
            this.receita = receita;
            this.custoTotal = custoTotal;
            this.lucroBruto = lucroBruto;
        }

        public String getNome() { return nome; }
        public int getQtdVendida() { return qtdVendida; }
        public double getReceita() { return receita; }
        public double getCustoTotal() { return custoTotal; }
        public double getLucroBruto() { return lucroBruto; }
    }
}
