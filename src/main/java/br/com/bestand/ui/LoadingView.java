package br.com.bestand.ui;

import br.com.bestand.database.DatabaseManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.sql.*;
import java.time.LocalDateTime;

public class LoadingView extends VBox {

    private TableView<Carregamento> table = new TableView<>();
    private ObservableList<Carregamento> list = FXCollections.observableArrayList();

    public LoadingView() {
        this.setSpacing(16);

        HBox header = new HBox(15);
        Label title = new Label("Histórico de Carregamentos");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnNovo = new Button("+ Registrar Carregamento");
        btnNovo.getStyleClass().add("btn-primary");
        btnNovo.setOnAction(e -> showModalEntrada());

        header.getChildren().addAll(title, spacer, btnNovo);

        TableColumn<Carregamento, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Carregamento, String> colData = new TableColumn<>("Data / Hora");
        colData.setCellValueFactory(new PropertyValueFactory<>("dataHora"));

        TableColumn<Carregamento, String> colFonte = new TableColumn<>("Fonte / Fornecedor");
        colFonte.setCellValueFactory(new PropertyValueFactory<>("fonte"));

        TableColumn<Carregamento, String> colUser = new TableColumn<>("Usuário");
        colUser.setCellValueFactory(new PropertyValueFactory<>("usuario"));

        table.getColumns().addAll(colId, colData, colFonte, colUser);
        table.setItems(list);

        carregarHistorico();
        this.getChildren().addAll(header, table);
    }

    private void carregarHistorico() {
        list.clear();
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM carregamentos ORDER BY id DESC")) {
            while (rs.next()) {
                list.add(new Carregamento(
                    rs.getInt("id"),
                    rs.getString("data_hora"),
                    rs.getString("fornecedor_fonte"),
                    rs.getString("usuario_responsavel")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showModalEntrada() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Registrar Carregamento de Estoque");

        VBox form = new VBox(10);
        form.setPadding(new Insets(20));

        ComboBox<ProductView.Product> comboProd = new ComboBox<>();
        carregarProdutosCombo(comboProd);

        TextField txtQtd = new TextField();
        TextField txtCusto = new TextField();
        TextField txtFonte = new TextField();

        form.getChildren().addAll(
            new Label("Selecione o Produto:"), comboProd,
            new Label("Quantidade Recebida:"), txtQtd,
            new Label("Custo Unitário de Aquisição (R$):"), txtCusto,
            new Label("Fornecedor / Fonte:"), txtFonte
        );

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(type -> {
            if (type == ButtonType.OK) {
                try {
                    ProductView.Product p = comboProd.getValue();
                    int qtd = Integer.parseInt(txtQtd.getText());
                    double custo = Double.parseDouble(txtCusto.getText());
                    String fonte = txtFonte.getText().trim();

                    if (p == null || qtd <= 0 || fonte.isEmpty()) {
                        showError("Preencha todas as informações obrigatórias com valores válidos.");
                        return;
                    }

                    try (Connection conn = DatabaseManager.getConnection()) {
                        conn.setAutoCommit(false);

                        String sqlC = "INSERT INTO carregamentos(data_hora, fornecedor_fonte, usuario_responsavel) VALUES(?, ?, ?)";
                        int cId;
                        try (PreparedStatement pstmt = conn.prepareStatement(sqlC, Statement.RETURN_GENERATED_KEYS)) {
                            pstmt.setString(1, LocalDateTime.now().toString());
                            pstmt.setString(2, fonte);
                            pstmt.setString(3, "admin");
                            pstmt.executeUpdate();
                            ResultSet rs = pstmt.getGeneratedKeys();
                            rs.next();
                            cId = rs.getInt(1);
                        }

                        String sqlI = "INSERT INTO itens_carregamento(carregamento_id, produto_id, quantidade, custo_unitario) VALUES(?, ?, ?, ?)";
                        try (PreparedStatement pstmt = conn.prepareStatement(sqlI)) {
                            pstmt.setInt(1, cId);
                            pstmt.setInt(2, p.getId());
                            pstmt.setInt(3, qtd);
                            pstmt.setDouble(4, custo);
                            pstmt.executeUpdate();
                        }

                        String sqlP = "UPDATE produtos SET estoque = estoque + ?, custo_atual = ? WHERE id = ?";
                        try (PreparedStatement pstmt = conn.prepareStatement(sqlP)) {
                            pstmt.setInt(1, qtd);
                            pstmt.setDouble(2, custo);
                            pstmt.setInt(3, p.getId());
                            pstmt.executeUpdate();
                        }

                        conn.commit();
                        carregarHistorico();
                    }
                } catch (Exception ex) {
                    showError("Erro ao registrar carregamento: " + ex.getMessage());
                }
            }
        });
    }

    private void carregarProdutosCombo(ComboBox<ProductView.Product> combo) {
        ObservableList<ProductView.Product> items = FXCollections.observableArrayList();
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM produtos")) {
            while (rs.next()) {
                items.add(new ProductView.Product(
                    rs.getInt("id"), rs.getString("codigo"), rs.getString("nome"),
                    rs.getInt("estoque"), rs.getInt("estoque_minimo"),
                    rs.getDouble("custo_atual"), rs.getDouble("preco_venda")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        combo.setItems(items);
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg);
        a.show();
    }

    public static class Carregamento {
        private final int id;
        private final String dataHora;
        private final String fonte;
        private final String usuario;

        public Carregamento(int id, String dataHora, String fonte, String usuario) {
            this.id = id;
            this.dataHora = dataHora;
            this.fonte = fonte;
            this.usuario = usuario;
        }

        public int getId() { return id; }
        public String getDataHora() { return dataHora; }
        public String getFonte() { return fonte; }
        public String getUsuario() { return usuario; }
    }
}
