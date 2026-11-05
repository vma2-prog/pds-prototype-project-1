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

public class ReturnView extends VBox {

    private TableView<Devolucao> table = new TableView<>();
    private ObservableList<Devolucao> list = FXCollections.observableArrayList();

    public ReturnView() {
        this.setSpacing(16);

        HBox header = new HBox(15);
        Label title = new Label("Histórico e Registro de Devoluções");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnNova = new Button("+ Registrar Devolução");
        btnNova.getStyleClass().add("btn-primary");
        btnNova.setOnAction(e -> showWizardDevolucao());

        header.getChildren().addAll(title, spacer, btnNova);

        TableColumn<Devolucao, Integer> colId = new TableColumn<>("ID Devolução");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Devolucao, Integer> colVenda = new TableColumn<>("Venda Original");
        colVenda.setCellValueFactory(new PropertyValueFactory<>("vendaId"));

        TableColumn<Devolucao, String> colData = new TableColumn<>("Data");
        colData.setCellValueFactory(new PropertyValueFactory<>("dataHora"));

        TableColumn<Devolucao, String> colMotivo = new TableColumn<>("Motivo");
        colMotivo.setCellValueFactory(new PropertyValueFactory<>("motivo"));

        TableColumn<Devolucao, Double> colValor = new TableColumn<>("Valor Estornado (R$)");
        colValor.setCellValueFactory(new PropertyValueFactory<>("valorEstornado"));

        table.getColumns().addAll(colId, colVenda, colData, colMotivo, colValor);
        table.setItems(list);

        carregarDevolucoes();
        this.getChildren().addAll(header, table);
    }

    private void carregarDevolucoes() {
        list.clear();
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM devolucoes ORDER BY id DESC")) {
            while (rs.next()) {
                list.add(new Devolucao(
                    rs.getInt("id"),
                    rs.getInt("venda_id"),
                    rs.getString("data_hora"),
                    rs.getString("motivo"),
                    rs.getDouble("valor_estornado")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showWizardDevolucao() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Registrar Devolução de Venda");

        VBox form = new VBox(10);
        form.setPadding(new Insets(20));

        TextField txtVendaId = new TextField();
        txtVendaId.setPromptText("Informe o ID da Venda (ex: 1)");

        ComboBox<String> comboMotivo = new ComboBox<>();
        comboMotivo.getItems().addAll("Nenhum motivo especificado", "Produto com defeito", "Tamanho incorreto", "Outro");
        comboMotivo.setValue("Nenhum motivo especificado");

        TextArea txtOutro = new TextArea();
        txtOutro.setPromptText("Descreva o motivo (Obrigatório se selecionado 'Outro')");
        txtOutro.setPrefRowCount(3);
        txtOutro.setDisable(true);

        comboMotivo.valueProperty().addListener((obs, oldVal, newVal) -> txtOutro.setDisable(!"Outro".equals(newVal)));

        form.getChildren().addAll(
            new Label("ID da Venda Original:"), txtVendaId,
            new Label("Motivo da Devolução:"), comboMotivo,
            new Label("Descrição adicional:"), txtOutro
        );

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(type -> {
            if (type == ButtonType.OK) {
                try {
                    int vendaId = Integer.parseInt(txtVendaId.getText().trim());
                    String motivo = comboMotivo.getValue();
                    String desc = txtOutro.getText().trim();

                    if ("Outro".equals(motivo) && desc.isEmpty()) {
                        Alert a = new Alert(Alert.AlertType.ERROR, "É necessário informar a descrição para o motivo 'Outro'.");
                        a.show();
                        return;
                    }

                    try (Connection conn = DatabaseManager.getConnection()) {
                        conn.setAutoCommit(false);
                        try {
                            double valVenda = 0;
                            try (PreparedStatement pstmt = conn.prepareStatement("SELECT valor_total FROM vendas WHERE id = ?")) {
                                pstmt.setInt(1, vendaId);
                                ResultSet rs = pstmt.executeQuery();
                                if (!rs.next()) {
                                    Alert a = new Alert(Alert.AlertType.ERROR, "Venda não encontrada!");
                                    a.show();
                                    return;
                                }
                                valVenda = rs.getDouble(1);
                            }

                            String sqlDev = "INSERT INTO devolucoes(venda_id, data_hora, motivo, descricao_motivo, valor_estornado, usuario_responsavel) VALUES(?, ?, ?, ?, ?, ?)";
                            try (PreparedStatement pstmt = conn.prepareStatement(sqlDev)) {
                                pstmt.setInt(1, vendaId);
                                pstmt.setString(2, LocalDateTime.now().toString());
                                pstmt.setString(3, motivo);
                                pstmt.setString(4, desc);
                                pstmt.setDouble(5, valVenda);
                                pstmt.setString(6, "admin");
                                pstmt.executeUpdate();
                            }

                            String sqlItens = "SELECT produto_id, quantidade FROM itens_venda WHERE venda_id = ?";
                            String sqlRestock = "UPDATE produtos SET estoque = estoque + ? WHERE id = ?";
                            try (PreparedStatement pstmtItens = conn.prepareStatement(sqlItens);
                                 PreparedStatement pstmtRestock = conn.prepareStatement(sqlRestock)) {
                                pstmtItens.setInt(1, vendaId);
                                ResultSet rs = pstmtItens.executeQuery();
                                while (rs.next()) {
                                    pstmtRestock.setInt(1, rs.getInt("quantidade"));
                                    pstmtRestock.setInt(2, rs.getInt("produto_id"));
                                    pstmtRestock.executeUpdate();
                                }
                            }

                            conn.commit();
                            carregarDevolucoes();
                            Alert a = new Alert(Alert.AlertType.INFORMATION, "Devolução registrada e estoque restaurado com sucesso!");
                            a.show();
                        } catch (SQLException ex) {
                            conn.rollback();
                            throw ex;
                        }
                    }
                } catch (SQLException ex) {
                    Alert a = new Alert(Alert.AlertType.ERROR, "Informação inválida: " + ex.getMessage());
                    a.show();
                }
            }
        });
    }

    public static class Devolucao {
        private final int id;
        private final int vendaId;
        private final String dataHora;
        private final String motivo;
        private final double valorEstornado;

        public Devolucao(int id, int vendaId, String dataHora, String motivo, double valorEstornado) {
            this.id = id;
            this.vendaId = vendaId;
            this.dataHora = dataHora;
            this.motivo = motivo;
            this.valorEstornado = valorEstornado;
        }

        public int getId() { return id; }
        public int getVendaId() { return vendaId; }
        public String getDataHora() { return dataHora; }
        public String getMotivo() { return motivo; }
        public double getValorEstornado() { return valorEstornado; }
    }
}
