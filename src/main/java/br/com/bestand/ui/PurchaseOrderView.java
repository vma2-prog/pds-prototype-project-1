package br.com.bestand.ui;

import br.com.bestand.database.DatabaseManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.sql.*;

public class PurchaseOrderView extends VBox {

    private TableView<Pedido> table = new TableView<>();
    private ObservableList<Pedido> list = FXCollections.observableArrayList();

    public PurchaseOrderView() {
        this.getStyleClass().add("page-panel");
        this.setSpacing(16);
        this.setPadding(new Insets(8, 0, 0, 0));

        HBox header = new HBox(15);
        header.getStyleClass().add("section-bar");
        Label title = new Label("Pedidos de Compra");
        title.getStyleClass().add("page-title");
        title.setStyle("-fx-font-size: 20px;");

        header.getChildren().addAll(title);

        TableColumn<Pedido, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Pedido, String> colForn = new TableColumn<>("Fornecedor");
        colForn.setCellValueFactory(new PropertyValueFactory<>("fornecedor"));

        TableColumn<Pedido, String> colData = new TableColumn<>("Data");
        colData.setCellValueFactory(new PropertyValueFactory<>("data"));

        TableColumn<Pedido, Double> colValor = new TableColumn<>("Valor Total (R$)");
        colValor.setCellValueFactory(new PropertyValueFactory<>("valorTotal"));

        TableColumn<Pedido, String> colStatus = new TableColumn<>("Situação");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("situacao"));

        TableColumn<Pedido, Void> colAcoes = new TableColumn<>("Ações");
        colAcoes.setCellFactory(param -> new TableCell<>() {
            private final Button btnReceber = new Button("Receber Integral");
            {
                btnReceber.getStyleClass().add("btn-primary");
                btnReceber.setOnAction(e -> {
                    Pedido p = getTableView().getItems().get(getIndex());
                    receberPedidoIntegral(p);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || !"PENDENTE".equals(getTableView().getItems().get(getIndex()).getSituacao())) {
                    setGraphic(null);
                } else {
                    setGraphic(btnReceber);
                }
            }
        });

        table.getColumns().addAll(colId, colForn, colData, colValor, colStatus, colAcoes);
        table.setItems(list);
        table.getStyleClass().add("modern-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        carregarPedidos();
        this.getChildren().addAll(header, table);
    }

    private void carregarPedidos() {
        list.clear();
        String sql = """
            SELECT p.id, f.nome as fornecedor, p.data_pedido, p.valor_total, p.situacao
            FROM pedidos_compra p
            JOIN fornecedores f ON f.id = p.fornecedor_id
        """;
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Pedido(
                    rs.getInt("id"),
                    rs.getString("fornecedor"),
                    rs.getString("data_pedido"),
                    rs.getDouble("valor_total"),
                    rs.getString("situacao")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void receberPedidoIntegral(Pedido p) {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement("UPDATE pedidos_compra SET situacao = 'RECEBIDO' WHERE id = ?")) {
                pstmt.setInt(1, p.getId());
                pstmt.executeUpdate();
            }

            conn.commit();
            carregarPedidos();
            Alert a = new Alert(Alert.AlertType.INFORMATION, "Pedido #" + p.getId() + " recebido com sucesso. Estoque atualizado!");
            a.show();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static class Pedido {
        private final int id;
        private final String fornecedor;
        private final String data;
        private final double valorTotal;
        private final String situacao;

        public Pedido(int id, String fornecedor, String data, double valorTotal, String situacao) {
            this.id = id;
            this.fornecedor = fornecedor;
            this.data = data;
            this.valorTotal = valorTotal;
            this.situacao = situacao;
        }

        public int getId() { return id; }
        public String getFornecedor() { return fornecedor; }
        public String getData() { return data; }
        public double getValorTotal() { return valorTotal; }
        public String getSituacao() { return situacao; }
    }
}
